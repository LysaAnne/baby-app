package dk.babyapp.tracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.MainActivity
import dk.babyapp.R
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.segmentIntervals
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.domain.switchNursingSide
import dk.babyapp.data.tracking.closeSegment
import dk.babyapp.data.tracking.startSegment
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface TimerNotificationController {
    fun show(eventId: String)
    fun hide()
}

class AndroidTimerNotificationController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : TimerNotificationController {
    override fun show(eventId: String) {
        ContextCompat.startForegroundService(context, TimerNotificationService.intent(context, TimerNotificationService.ACTION_SHOW, eventId))
    }
    // The database flow removes completed/cancelled timers and stops the service
    // when none remain, without hiding another child\'s active timer.
    override fun hide() = Unit
}

@AndroidEntryPoint
class TimerNotificationService : Service() {
    @Inject lateinit var repository: CareEventRepository
    @Inject lateinit var profiles: dk.babyapp.data.profile.ChildProfileRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val actionMutex = Mutex()
    private var updateJob: Job? = null
    private var eventId: String? = null
    private var secondaryIds = emptySet<Int>()

    override fun onCreate() {
        super.onCreate()
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            NotificationChannel(CHANNEL, "Aktive registreringer", NotificationManager.IMPORTANCE_LOW).apply { lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC },
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        eventId = intent?.getStringExtra(EXTRA_EVENT_ID) ?: eventId
        val id = eventId ?: return START_NOT_STICKY
        startForeground(NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_timer_notification).setContentTitle("Aktiv registrering").setOngoing(true).setOnlyAlertOnce(true).build())
        scope.launch { actionMutex.withLock {
            val event = repository.get(id) ?: return@launch stopSelf()
            if (event.deletedAt != null || event.endedAt != null) { stopSelf(); return@launch }
            when (intent?.action) {
                ACTION_PAUSE -> { val now = System.currentTimeMillis(); repository.save(accrue(event).closeSegment(now).copy(runningSince = null)) }
                ACTION_RESUME -> if (event.runningSince == null) { val now = System.currentTimeMillis(); repository.save(event.startSegment(now).copy(runningSince = now)) }
                ACTION_SWITCH -> repository.save(event.switchNursingSide(System.currentTimeMillis()))
                ACTION_STOP -> {
                    val now = System.currentTimeMillis()
                    val accrued = accrue(event).closeSegment(now)
                    repository.save(accrued.copy(isDraft = false, endedAt = now, runningSince = null, activityDurationSeconds = accrued.elapsedSeconds().takeIf { event.type == CareEventType.Activity } ?: event.activityDurationSeconds))
                    // Reconcile all active timers below, including other children.
                }
            }
            beginUpdates()
        } }
        return START_NOT_STICKY
    }

    private fun beginUpdates() {
        updateJob?.cancel()
        updateJob = scope.launch {
            repository.unfinishedEvents.combine(profiles.profiles) { active, children -> active to children }.collect { (active, children) ->
                names = children.associate { it.id to it.name }
                val manager = getSystemService(NotificationManager::class.java)
                val nextIds = active.drop(1).map { 0x40000000 or (it.id.hashCode() and 0x3fffffff) }.toSet()
                (secondaryIds - nextIds).forEach(manager::cancel)
                secondaryIds = nextIds
                if (active.isEmpty()) stopSelf() else {
                    startForeground(NOTIFICATION_ID, notification(active.first()))
                    active.drop(1).forEach { event -> manager.notify(0x40000000 or (event.id.hashCode() and 0x3fffffff), notification(event)) }
                }
            }
        }
    }

    private var names: Map<String, String> = emptyMap()

    private fun notification(event: CareEventEntity): android.app.Notification {
        val what = when (event.type) {
            CareEventType.Breastfeeding -> "Amning · ${if (event.activeSide == BreastSide.Right) "højre" else "venstre"} side"
            CareEventType.Sleep -> "Søvn"
            CareEventType.Activity -> event.activityType?.let { type -> when (type) {
                dk.babyapp.data.tracking.ActivityType.TummyTime -> "Mavetid"
                dk.babyapp.data.tracking.ActivityType.Bath -> "Bad"
                dk.babyapp.data.tracking.ActivityType.OutdoorTime -> "Udetid"
                dk.babyapp.data.tracking.ActivityType.Play -> "Leg"
                dk.babyapp.data.tracking.ActivityType.Medicine -> "Medicin"
                dk.babyapp.data.tracking.ActivityType.Other -> "Anden aktivitet"
            } } ?: "Aktivitet"
            else -> "Pumpning"
        }
        val title = listOfNotNull(names[event.childId], what).joinToString(" · ")
        val elapsed = event.elapsedSeconds()
        val text = "%02d:%02d:%02d".format(elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60)
        // What is running in plain words: when it started, pauses so far and, for nursing, the time per side.
        val clock = java.text.SimpleDateFormat("HH.mm", java.util.Locale.forLanguageTag("da")).format(java.util.Date(event.startedAt))
        val pauses = (event.segmentIntervals().size - 1).coerceAtLeast(0)
        val details = listOfNotNull(
            "startet kl. $clock",
            // Per-side totals are only exact while paused; a running side keeps counting in the clock.
            if (event.type == CareEventType.Breastfeeding && event.runningSince == null) "venstre ${event.leftSeconds / 60} min · højre ${event.rightSeconds / 60} min" else null,
            pauses.takeIf { it > 0 }?.let { if (it == 1) "1 pause" else "$it pauser" },
        ).joinToString(" · ")
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val toggleAction = if (event.runningSince == null) ACTION_RESUME else ACTION_PAUSE
        val toggleLabel = if (event.runningSince == null) "Fortsæt" else "Pause"
        val builder = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_timer_notification).setContentTitle(title).setContentText(if (event.runningSince == null) "På pause · $text i alt · $details" else details.replaceFirstChar { it.uppercase() })
            .setStyle(NotificationCompat.BigTextStyle().bigText(if (event.runningSince == null) "På pause · $text i alt\n${details.replaceFirstChar { it.uppercase() }}" else details.replaceFirstChar { it.uppercase() }))
            .setOngoing(true).setOnlyAlertOnce(true).setContentIntent(open)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setWhen(System.currentTimeMillis() - elapsed * 1_000).setUsesChronometer(event.runningSince != null)
            .setShowWhen(true)
            .addAction(0, toggleLabel, servicePendingIntent(toggleAction, event.id, 1))
        if (event.type == CareEventType.Breastfeeding) builder.addAction(0, "Skift side", servicePendingIntent(ACTION_SWITCH, event.id, 2))
        return builder.addAction(0, "Stop", servicePendingIntent(ACTION_STOP, event.id, 3)).build()
    }

    private fun servicePendingIntent(action: String, id: String, request: Int) = PendingIntent.getService(this, request, intent(this, action, id), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    private fun accrue(event: CareEventEntity): CareEventEntity {
        val now = System.currentTimeMillis(); val seconds = event.runningSince?.let { (now - it).coerceAtLeast(0) / 1_000 } ?: 0
        return when { event.type != CareEventType.Breastfeeding -> event.copy(leftSeconds = event.leftSeconds + seconds); event.activeSide == BreastSide.Right -> event.copy(rightSeconds = event.rightSeconds + seconds); else -> event.copy(leftSeconds = event.leftSeconds + seconds) }
    }
    override fun onDestroy() { updateJob?.cancel(); scope.cancel(); secondaryIds.forEach { getSystemService(NotificationManager::class.java).cancel(it) }; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SHOW = "dk.babyapp.timer.SHOW"; const val ACTION_PAUSE = "dk.babyapp.timer.PAUSE"; const val ACTION_RESUME = "dk.babyapp.timer.RESUME"; const val ACTION_SWITCH = "dk.babyapp.timer.SWITCH"; const val ACTION_STOP = "dk.babyapp.timer.STOP"
        private const val EXTRA_EVENT_ID = "event_id"; private const val CHANNEL = "active_timer"; private const val NOTIFICATION_ID = 4103
        fun intent(context: Context, action: String, eventId: String) = Intent(context, TimerNotificationService::class.java).setAction(action).setData(android.net.Uri.parse("babyapp://timer/$eventId/$action")).putExtra(EXTRA_EVENT_ID, eventId)
    }
}
