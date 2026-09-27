package dk.babyapp.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.MainActivity
import dk.babyapp.R
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class DailyReminderScheduler @Inject constructor(@param:ApplicationContext private val context: Context) {
    fun update(enabled: Boolean, hour: Int, minute: Int) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(context, 7301, Intent(context, DailyReminderReceiver::class.java).putExtra("hour", hour).putExtra("minute", minute), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarm.cancel(pending)
        if (!enabled) return
        val now = LocalDateTime.now(); var next = now.toLocalDate().atTime(hour, minute)
        if (!next.isAfter(now)) next = next.plusDays(1)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pending)
    }
}

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Daglig påmindelse", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(7301, NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("BabyLog").setContentText("Husk dagens registreringer, hvis der er noget, du vil gemme.").setAutoCancel(true).setContentIntent(open).build())
        DailyReminderScheduler(context.applicationContext).update(true, intent?.getIntExtra("hour", 20) ?: 20, intent?.getIntExtra("minute", 0) ?: 0)
    }
    private companion object { const val CHANNEL = "daily_reminder" }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val preferences = DataStoreAppPreferencesRepository(context.applicationContext).preferences.first()
                DailyReminderScheduler(context.applicationContext).update(preferences.dailyReminderEnabled, preferences.dailyReminderHour, preferences.dailyReminderMinute)
            }
            pending.finish()
        }
    }
}
