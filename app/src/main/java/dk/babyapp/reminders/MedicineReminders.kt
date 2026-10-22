package dk.babyapp.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dk.babyapp.MainActivity
import dk.babyapp.R
import dk.babyapp.data.medicine.MedicinePlan
import java.time.ZonedDateTime
import java.time.LocalTime
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MedicineReminderScheduler(private val context: Context) {
    private val alarm = context.getSystemService(AlarmManager::class.java)
    private val index = context.getSharedPreferences("medicine_alarm_index", Context.MODE_PRIVATE)

    fun sync(plans: List<MedicinePlan>) {
        val active = plans.filter { it.active && it.reminders && !it.asNeeded }
        val uris = active.flatMap { plan -> plan.validTimes().map { time -> "babyapp://medicine/${plan.id}/$time" } }.toSet()
        index.getStringSet("scheduled", emptySet()).orEmpty().forEach { uri ->
            alarm.cancel(pending(uri))
            val planId = Uri.parse(uri).pathSegments.firstOrNull()
            if (planId != null && active.none { it.id == planId }) context.getSystemService(NotificationManager::class.java).cancel(planId, 7302)
        }
        index.edit().putStringSet("scheduled", uris).apply()
        active.forEach { plan -> plan.validTimes().forEach { time -> scheduleNext(plan, time) } }
    }

    fun scheduleNext(plan: MedicinePlan, time: LocalTime) {
        val now = ZonedDateTime.now()
        var next = now.toLocalDate().atTime(time).atZone(now.zone)
        if (!next.isAfter(now)) next = next.plusDays(1)
        // Every other day, weekly or monthly: move on to the next day the medicine is due.
        repeat(400) { if (!plan.isDueOn(next.toLocalDate())) next = next.plusDays(1) }
        scheduleAt(next.toInstant().toEpochMilli(), pending("babyapp://medicine/${plan.id}/$time"))
    }

    private fun scheduleAt(at: Long, intent: PendingIntent) {
        if (Build.VERSION.SDK_INT < 31 || alarm.canScheduleExactAlarms()) {
            try { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent) }
            catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent) }
        } else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
    }

    fun snooze(plan: MedicinePlan, time: LocalTime, occurrence: Long) {
        val uri = "babyapp://medicine/${plan.id}/$time/snooze/$occurrence"
        val scheduled = index.getStringSet("scheduled", emptySet()).orEmpty() + uri
        index.edit().putStringSet("scheduled", scheduled).apply()
        scheduleAt(System.currentTimeMillis() + 10 * 60_000, pending(uri))
    }

    private fun pending(uri: String): PendingIntent = PendingIntent.getBroadcast(context, 0,
        Intent(context, MedicineReminderReceiver::class.java).setData(Uri.parse(uri)), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}

@dagger.hilt.android.AndroidEntryPoint
class MedicineReminderReceiver : BroadcastReceiver() {
    companion object { private val actionLock = Mutex() }
    @javax.inject.Inject lateinit var events: dk.babyapp.data.tracking.CareEventRepository
    @javax.inject.Inject lateinit var medicines: dk.babyapp.data.medicine.MedicineRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plans = medicines.all()
                val action = intent?.action
                if (action in setOf("medicine.taken", "medicine.snooze", "medicine.skip")) {
                    actionLock.withLock {
                        val plan = plans.firstOrNull { it.id == intent?.data?.pathSegments?.firstOrNull() }
                        val occurrence = intent?.getLongExtra("occurrence", 0L) ?: 0L
                        val time = intent?.data?.pathSegments?.getOrNull(1)?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                        if (plan != null && occurrence > 0 && time != null) {
                            val handled = context.getSharedPreferences("medicine_actions", Context.MODE_PRIVATE)
                            val key = "${plan.id}/$time/$occurrence"
                            if (!handled.getBoolean(key, false)) {
                                if (action == "medicine.taken" && plan.active) {
                                    val now = System.currentTimeMillis()
                                    val eventId = "medicine-$key"
                                    if (events.get(eventId) == null) events.save(dk.babyapp.data.tracking.CareEventEntity(
                                        id = eventId, childId = plan.childId, type = dk.babyapp.data.tracking.CareEventType.Activity,
                                        startedAt = now, endedAt = now, timeSpecified = true,
                                        activityType = dk.babyapp.data.tracking.ActivityType.Medicine,
                                        medicineId = plan.id, medicationName = plan.name, medicationDose = plan.dose,
                                    ))
                                } else if (action == "medicine.snooze" && plan.active && plan.reminders && !plan.asNeeded) {
                                    MedicineReminderScheduler(context).snooze(plan, time, System.currentTimeMillis())
                                }
                                handled.edit().putBoolean(key, true).commit()
                            }
                            context.getSystemService(NotificationManager::class.java).cancel(plan.id, 7302)
                        }
                    }
                    return@launch
                }
                if (intent?.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) {
                    val id = intent?.data?.pathSegments?.firstOrNull()
                    val plan = plans.firstOrNull { it.id == id && it.active && it.reminders && !it.asNeeded }
                    val time = intent?.data?.pathSegments?.getOrNull(1)?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                    if (plan != null && time in plan.validTimes() && NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        val manager = context.getSystemService(NotificationManager::class.java)
                        manager.createNotificationChannel(NotificationChannel("medicine_reminders", "Medicinpåmindelser", NotificationManager.IMPORTANCE_HIGH))
                        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        val occurrence = System.currentTimeMillis()
                        fun actionIntent(action: String): PendingIntent = PendingIntent.getBroadcast(context, 0,
                            Intent(context, MedicineReminderReceiver::class.java).setAction(action)
                                .setData(Uri.parse("babyapp://medicine/${plan.id}/$time/$occurrence"))
                                .putExtra("occurrence", occurrence), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        manager.notify(plan.id, 7302, NotificationCompat.Builder(context, "medicine_reminders")
                            .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle("Tid til medicin")
                            .setContentText(listOf(plan.name, plan.dose).filter(String::isNotBlank).joinToString(" · ")).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setContentIntent(open)
                            .addAction(0, "Taget", actionIntent("medicine.taken"))
                            .addAction(0, "Snooze 10 min", actionIntent("medicine.snooze"))
                            .addAction(0, "Skip", actionIntent("medicine.skip"))
                            .setAutoCancel(true).build())
                    }
                    if (plan != null && time != null && time in plan.validTimes()) MedicineReminderScheduler(context).scheduleNext(plan, time)
                } else MedicineReminderScheduler(context).sync(plans)
            } finally { result.finish() }
        }
    }
}
