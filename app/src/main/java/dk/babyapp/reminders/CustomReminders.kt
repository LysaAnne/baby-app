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
import dagger.hilt.android.AndroidEntryPoint
import dk.babyapp.MainActivity
import dk.babyapp.R
import dk.babyapp.data.preferences.CustomReminder
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.domain.developmentStart
import dk.babyapp.domain.leaps
import dk.babyapp.domain.milestones
import dk.babyapp.domain.plusWeekFraction
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

const val CUSTOM_REMINDER_CHANNEL = "custom_reminders"
const val DEVELOPMENT_REMINDER_CHANNEL = "development_reminders"

/** Own reminders, e.g. mavetid on weekdays at 10: each enabled reminder has one alarm for its next time. */
object CustomReminderScheduler {
    private fun pending(context: Context, id: String) = PendingIntent.getBroadcast(context, 0,
        Intent(context, CustomReminderReceiver::class.java).setData(Uri.parse("babyapp://reminder/$id")), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    /** The next time on one of the chosen weekdays (1 = Monday), or null when no day is chosen. */
    fun nextTime(reminder: CustomReminder, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
        if (reminder.days.isEmpty()) return null
        var next = now.toLocalDate().atTime(LocalTime.of(reminder.hour, reminder.minute)).atZone(now.zone)
        if (!next.isAfter(now)) next = next.plusDays(1)
        repeat(7) { if (next.dayOfWeek.value !in reminder.days) next = next.plusDays(1) }
        return next
    }

    /** Cancels the [previous] alarms and sets one for each enabled reminder in [reminders]. */
    fun update(context: Context, previous: List<CustomReminder>, reminders: List<CustomReminder>) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        previous.forEach { alarm.cancel(pending(context, it.id)) }
        reminders.filter { it.enabled }.forEach { reminder ->
            val at = nextTime(reminder)?.toInstant()?.toEpochMilli() ?: return@forEach
            val intent = pending(context, reminder.id)
            if (Build.VERSION.SDK_INT < 31 || alarm.canScheduleExactAlarms()) {
                try { alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent) } catch (_: SecurityException) { alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent) }
            } else alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }
}

class CustomReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = DataStoreAppPreferencesRepository(context.applicationContext).preferences.first().customReminders
                if (intent?.action == Intent.ACTION_BOOT_COMPLETED) { CustomReminderScheduler.update(context, reminders, reminders); return@launch }
                val id = intent?.data?.lastPathSegment
                val reminder = reminders.firstOrNull { it.id == id && it.enabled } ?: return@launch
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(NotificationChannel(CUSTOM_REMINDER_CHANNEL, "Egne påmindelser", NotificationManager.IMPORTANCE_DEFAULT))
                val open = PendingIntent.getActivity(context, 7600, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                if (manager.areNotificationsEnabled()) manager.notify(7600 + (reminder.id.hashCode() and 0xfff), NotificationCompat.Builder(context, CUSTOM_REMINDER_CHANNEL)
                    .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle(reminder.title).setContentText("Påmindelse fra Baby Tracker")
                    .setContentIntent(open).setAutoCancel(true).build())
                // The next time for this reminder.
                CustomReminderScheduler.update(context, listOf(reminder), listOf(reminder))
            } catch (error: Exception) {
                android.util.Log.w("CustomReminder", "Could not show a reminder", error)
            } finally { pending.finish() }
        }
    }
}

/** A daily check at 9:00: tells when a development spurt begins or a milestone's usual window opens. */
object DevelopmentReminderScheduler {
    fun ensureScheduled(context: Context) {
        val pending = PendingIntent.getBroadcast(context, 7700, Intent(context, DevelopmentReminderReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val now = ZonedDateTime.now()
        val next = now.with(LocalTime.of(9, 0)).let { if (it <= now) it.plusDays(1) else it }
        context.getSystemService(AlarmManager::class.java).setInexactRepeating(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), AlarmManager.INTERVAL_DAY, pending)
    }

    /** What begins on [today] for a child counted from [start]: spurts on their first day, milestones when their window opens. */
    fun newsFor(start: LocalDate, today: LocalDate): List<String> =
        leaps.filter { start.plusWeekFraction(it.fussyFromWeek) == today }.map { "Spring ${it.number} begynder nu: ${it.title.lowercase()}." } +
            milestones.filter { start.plusDays(Math.round(it.fromMonths * 30.44)) == today }.map { "Nu begynder den typiske tid for: ${it.title.lowercase()}." } +
            dk.babyapp.domain.psychPhenomena.filter { it.fromMonths > 0 && start.plusDays(Math.round(it.fromMonths * 30.44)) == today }.map { "Nyt i psykologisk udvikling: ${it.title.lowercase()}." }
}

@AndroidEntryPoint
class DevelopmentReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var children: ChildProfileRepository

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) { DevelopmentReminderScheduler.ensureScheduled(context); return }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!DataStoreAppPreferencesRepository(context.applicationContext).preferences.first().developmentRemindersEnabled) return@launch
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(NotificationChannel(DEVELOPMENT_REMINDER_CHANNEL, "Udvikling", NotificationManager.IMPORTANCE_DEFAULT))
                if (!manager.areNotificationsEnabled()) return@launch
                children.profiles.first().forEach { child ->
                    val start = developmentStart(child) ?: return@forEach
                    val news = DevelopmentReminderScheduler.newsFor(start, LocalDate.now())
                    if (news.isEmpty()) return@forEach
                    val open = PendingIntent.getActivity(context, 7700, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    manager.notify(7700 + (child.id.hashCode() and 0xfff), NotificationCompat.Builder(context, DEVELOPMENT_REMINDER_CHANNEL)
                        .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle("${child.name} · udvikling").setContentText(news.first())
                        .setStyle(NotificationCompat.BigTextStyle().bigText(news.joinToString("\n"))).setContentIntent(open).setAutoCancel(true).build())
                }
            } catch (error: Exception) {
                android.util.Log.w("DevelopmentReminder", "Could not check development", error)
            } finally { pending.finish() }
        }
    }
}
