package dk.babyapp.reminders

import android.app.*
import android.content.*
import android.net.Uri
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dk.babyapp.MainActivity
import dk.babyapp.R
import dk.babyapp.data.book.*
import dk.babyapp.data.profile.*
import dk.babyapp.domain.*
import java.time.*
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

const val BOOK_REMINDER_CHANNEL = "baby_book_reminders"

data class BookNotificationTarget(val childId: String, val pageId: String) {
    companion object {
        fun from(intent: Intent?): BookNotificationTarget? {
            val uri = intent?.data ?: return null
            if (uri.scheme != "babyapp" || uri.host != "book" || uri.pathSegments.size != 2) return null
            val page = uri.pathSegments[1]
            if (babyBookTemplates.none { it.id == page }) return null
            return BookNotificationTarget(uri.pathSegments[0], page)
        }
    }
}

class BookReminderScheduler(private val context: Context) {
    fun update(children: List<ChildProfile>, enabled: Boolean = true) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(BOOK_REMINDER_CHANNEL, "Barnets bog", NotificationManager.IMPORTANCE_DEFAULT))
        val alarm = context.getSystemService(AlarmManager::class.java)
        val next = if (enabled) nextBookReminderAt(children, ZonedDateTime.now()) else null
        val pending = PendingIntent.getBroadcast(context, 7400, Intent(context, BookReminderReceiver::class.java).putExtra("scheduledDate", next?.toLocalDate()?.toString()), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarm.cancel(pending)
        if (!enabled) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.activeNotifications.filter { it.notification.channelId == BOOK_REMINDER_CHANNEL }.forEach { manager.cancel(it.id) }
        }
        if (next == null) return
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), pending)
    }
}

@AndroidEntryPoint
class BookReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var children: ChildProfileRepository
    @Inject lateinit var book: BabyBookRepository
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profiles = children.profiles.first()
                // Schedule before reading pages: a malformed/removed page must not stop future reminders.
                val enabled = dk.babyapp.data.preferences.DataStoreAppPreferencesRepository(context.applicationContext).preferences.first().bookRemindersEnabled
                BookReminderScheduler(context).update(profiles, enabled)
                if (!enabled) return@launch
                if (intent?.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED)) return@launch
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(NotificationChannel(BOOK_REMINDER_CHANNEL, "Barnets bog", NotificationManager.IMPORTANCE_DEFAULT))
                if (!manager.areNotificationsEnabled() || manager.getNotificationChannel(BOOK_REMINDER_CHANNEL).importance == NotificationManager.IMPORTANCE_NONE) return@launch
                val pages = book.pages.first()
                val today = LocalDate.now()
                val scheduledDate = runCatching { LocalDate.parse(intent?.getStringExtra("scheduledDate")) }.getOrDefault(today)
                val history = context.getSharedPreferences("book_reminder_history", Context.MODE_PRIVATE)
                val sent = history.getStringSet("milestones", emptySet()).orEmpty().toMutableSet()
                val available = profiles.flatMap { child -> babyBookTemplates.filter { template ->
                    bookPageAvailable(template, child, today) && !bookPageHasContent(pages.firstOrNull { it.childId == child.id && it.pageId == template.id })
                }.map { child to it } }
                val milestones = available.filter { (child, template) ->
                    val month = template.id.removePrefix("month-").toLongOrNull()
                    val due = if (template.id.startsWith("month-") && month != null) child.birthDate?.plusMonths(month) else null
                    due != null && due == scheduledDate && today >= due && "${child.id}:${template.id}:$due" !in sent
                }
                milestones.forEach { (child, template) ->
                    notifyPage(context, manager, child, template, 7401 + child.id.hashCode().and(0xffff))
                    sent += "${child.id}:${template.id}:${child.birthDate?.plusMonths(template.id.removePrefix("month-").toLong())}"
                }
                history.edit().putStringSet("milestones", sent).apply()
                if (scheduledDate.dayOfWeek in bookReminderWeekdays && today >= scheduledDate && history.getString("weeklyDate", null) != scheduledDate.toString()) {
                    // One general prompt across all children, avoiding an immediate repeat where possible.
                    val last = history.getString("lastPage", null)
                    val candidates = available.filter { pair -> pair !in milestones }
                    val choice = candidates.filter { "${it.first.id}:${it.second.id}" != last }.ifEmpty { candidates }.randomOrNull()
                    if (choice != null) {
                        notifyPage(context, manager, choice.first, choice.second, 7400)
                        history.edit().putString("weeklyDate", scheduledDate.toString()).putString("lastPage", "${choice.first.id}:${choice.second.id}").apply()
                    }
                }
            } catch (error: Exception) {
                android.util.Log.w("BookReminder", "Could not deliver book reminder", error)
            } finally { pending.finish() }
        }
    }

    private fun notifyPage(context: Context, manager: NotificationManager, child: ChildProfile, page: BookPageTemplate, id: Int) {
        val uri = Uri.Builder().scheme("babyapp").authority("book").appendPath(child.id).appendPath(page.id).build()
        val intent = Intent(context, MainActivity::class.java).setData(uri).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val open = PendingIntent.getActivity(context, 7400, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(id, NotificationCompat.Builder(context, BOOK_REMINDER_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("${child.name} · Barnets bog")
            .setContentText("Gem et minde på siden ‘${page.title}’.")
            .setAutoCancel(true).setContentIntent(open).build())
    }
}
