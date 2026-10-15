package dk.babyapp.reminders

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Retained to cancel alarms created by older versions, including queued broadcasts. */
@Singleton
class DailyReminderScheduler @Inject constructor(@param:ApplicationContext private val context: Context) {
    @Suppress("UNUSED_PARAMETER")
    fun update(enabled: Boolean, hour: Int, minute: Int) {
        val pending = PendingIntent.getBroadcast(context, 7301, Intent(context, DailyReminderReceiver::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(7301)
        manager.deleteNotificationChannel("daily_reminder")
    }
}

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { DailyReminderScheduler(context).update(false, 20, 0) }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { DailyReminderScheduler(context).update(false, 20, 0) }
}
