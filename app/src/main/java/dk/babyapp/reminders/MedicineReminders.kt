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
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import java.time.ZonedDateTime
import java.time.LocalTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class MedicineReminderScheduler(private val context: Context) {
    private val alarm = context.getSystemService(AlarmManager::class.java)
    private val index = context.getSharedPreferences("medicine_alarm_index", Context.MODE_PRIVATE)

    fun sync(plans: List<MedicinePlan>) {
        val active = plans.filter { it.active && it.reminders && !it.asNeeded }
        val uris = active.flatMap { plan -> plan.validTimes().map { time -> "babyapp://medicine/${plan.id}/$time" } }.toSet()
        index.getStringSet("scheduled", emptySet()).orEmpty().forEach { alarm.cancel(pending(it)) }
        index.edit().putStringSet("scheduled", uris).apply()
        active.forEach { plan -> plan.validTimes().forEach { time -> scheduleNext(plan, time) } }
    }

    fun scheduleNext(plan: MedicinePlan, time: LocalTime) {
            val now = ZonedDateTime.now()
            var next = now.toLocalDate().atTime(time).atZone(now.zone)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val intent = pending("babyapp://medicine/${plan.id}/$time")
            if (Build.VERSION.SDK_INT < 31 || alarm.canScheduleExactAlarms()) {
                try { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), intent) }
                catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), intent) }
            } else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), intent)
    }

    private fun pending(uri: String): PendingIntent = PendingIntent.getBroadcast(context, 0,
        Intent(context, MedicineReminderReceiver::class.java).setData(Uri.parse(uri)), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}

class MedicineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plans = DataStoreAppPreferencesRepository(context.applicationContext).preferences.first().medicines
                if (intent?.action !in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) {
                    val id = intent?.data?.pathSegments?.firstOrNull()
                    val plan = plans.firstOrNull { it.id == id && it.active && it.reminders && !it.asNeeded }
                    val time = intent?.data?.pathSegments?.getOrNull(1)?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                    if (plan != null && time in plan.validTimes() && NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        val manager = context.getSystemService(NotificationManager::class.java)
                        manager.createNotificationChannel(NotificationChannel("medicine_reminders", "Medicinpåmindelser", NotificationManager.IMPORTANCE_HIGH))
                        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        manager.notify(plan.id, 7302, NotificationCompat.Builder(context, "medicine_reminders")
                            .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle("Tid til medicin")
                            .setContentText(listOf(plan.name, plan.dose).filter(String::isNotBlank).joinToString(" · ")).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            .setContentIntent(open).setAutoCancel(true).build())
                    }
                    if (plan != null && time != null && time in plan.validTimes()) MedicineReminderScheduler(context).scheduleNext(plan, time)
                } else MedicineReminderScheduler(context).sync(plans)
            } finally { result.finish() }
        }
    }
}
