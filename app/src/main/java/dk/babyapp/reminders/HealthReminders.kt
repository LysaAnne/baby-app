package dk.babyapp.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dk.babyapp.MainActivity
import dk.babyapp.R
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.domain.programmeRemindersFor
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val HEALTH_REMINDER_CHANNEL = "health_programme_reminders"

/** A daily check at 10:00; the receiver decides whether anything in the Danish programme is due. */
object HealthReminderScheduler {
    fun ensureScheduled(context: Context) {
        val pending = PendingIntent.getBroadcast(context, 7500, Intent(context, HealthReminderReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val now = ZonedDateTime.now()
        val next = now.with(LocalTime.of(10, 0)).let { if (it <= now) it.plusDays(1) else it }
        context.getSystemService(AlarmManager::class.java).setInexactRepeating(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), AlarmManager.INTERVAL_DAY, pending)
    }
}

@AndroidEntryPoint
class HealthReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var children: ChildProfileRepository
    @Inject lateinit var events: CareEventRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) { HealthReminderScheduler.ensureScheduled(context); return }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!DataStoreAppPreferencesRepository(context.applicationContext).preferences.first().healthRemindersEnabled) return@launch
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(NotificationChannel(HEALTH_REMINDER_CHANNEL, "Vaccinationer og børneundersøgelser", NotificationManager.IMPORTANCE_DEFAULT))
                if (!manager.areNotificationsEnabled()) return@launch
                val today = LocalDate.now()
                children.profiles.first().forEach { child ->
                    val due = programmeRemindersFor(child, events.eventsForChild(child.id).first(), today)
                    if (due.isEmpty()) return@forEach
                    val text = due.joinToString(" og ") { it.template.title.substringAfter(" · ").lowercase().replaceFirstChar { c -> c.uppercase() } } +
                        if (due.first().due == today) " er i dag." else " er om en uge."
                    val open = PendingIntent.getActivity(context, 7500, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    manager.notify(7500 + (child.id.hashCode() and 0xfff), NotificationCompat.Builder(context, HEALTH_REMINDER_CHANNEL)
                        .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle("${child.name} · det danske børneprogram")
                        .setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText("$text Book en tid hos lægen, hvis I ikke har gjort det."))
                        .setContentIntent(open).setAutoCancel(true).build())
                }
            } catch (error: Exception) {
                android.util.Log.w("HealthReminder", "Could not check the health programme", error)
            } finally { pending.finish() }
        }
    }
}
