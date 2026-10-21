package dk.babyapp.ui.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import dk.babyapp.data.tracking.segmentIntervals
import dk.babyapp.ui.theme.careCategory
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.domain.changeNippleShield
import dk.babyapp.ui.theme.BabySpacing

/**
 * Running and paused timers, shown on every screen just above the menu bar while they exist.
 * Compact by default; each can be expanded for intervals and keep-screen-on. A stopped timer is saved at once
 * and a short window offers optional details; a draft left by an older version opens the same way.
 */
@Composable
internal fun ActiveTimers(
    timers: List<CareEventEntity>,
    recentEvents: List<CareEventEntity>,
    activeChildId: String?,
    childName: (String) -> String?,
    careProviders: List<CareProvider>,
    medicines: List<MedicinePlan>,
    keepAwake: Boolean,
    onKeepAwake: (Boolean) -> Unit,
    onToggle: (CareEventEntity) -> Unit,
    onSwitchSide: (CareEventEntity) -> Unit,
    onStop: (CareEventEntity, Int?, (CareEventEntity) -> Unit) -> Unit,
    onUpdate: (CareEventEntity, (Boolean) -> Unit) -> Unit,
    onDelete: (CareEventEntity) -> Unit,
    onUpdateDefaultNippleShield: (Boolean) -> Unit,
) {
    var editing by remember { mutableStateOf<CareEventEntity?>(null) }
    var done by remember { mutableStateOf<CareEventEntity?>(null) }
    var saveFailed by remember { mutableStateOf(false) }
    LaunchedEffect(activeChildId, recentEvents) {
        if (editing == null && done == null) done = recentEvents.firstOrNull { it.childId == activeChildId && it.isDraft && it.deletedAt == null }
    }
    if (timers.isNotEmpty()) {
        // At most about 60 % of the screen, so content above stays reachable even with an expanded timer.
        val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.62f).dp
        Column(
            // An opaque strip, so the screen above never shows through between the timers.
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).heightIn(max = maxHeight).verticalScroll(rememberScrollState())
                .padding(horizontal = BabySpacing.m, vertical = BabySpacing.xs),
            verticalArrangement = Arrangement.spacedBy(BabySpacing.xs),
        ) {
            timers.forEach { event ->
                key(event.id) {
                    var minimized by rememberSaveable(event.id) { mutableStateOf(true) }
                    ActiveTimerCard(
                        event = event,
                        onToggle = onToggle,
                        onSwitch = onSwitchSide,
                        onStop = { running -> onStop(running, null) { done = it } },
                        onCancel = onDelete,
                        onEdit = { editing = it },
                        onShield = { current, enabled ->
                            onUpdate(current.changeNippleShield(enabled, System.currentTimeMillis())) { success -> saveFailed = !success; if (success) onUpdateDefaultNippleShield(enabled) }
                        },
                        keepAwake = keepAwake,
                        onKeepAwake = onKeepAwake,
                        minimized = minimized,
                        onMinimizedChange = { minimized = it },
                        childLabel = childName(event.childId).takeIf { event.childId != activeChildId },
                    )
                }
            }
        }
    }
    editing?.let { event ->
        val dismiss = { if (event.isDraft) onDelete(event); editing = null }
        val save: (CareEventEntity) -> Unit = { updated -> onUpdate(updated.copy(isDraft = false)) { success -> saveFailed = !success; if (success) editing = null } }
        val providers = careProviders.filter { it.childId == event.childId }
        if (event.type in setOf(CareEventType.HealthVisit, CareEventType.Vaccination)) HealthRecordDialog(event.childId, event.type == CareEventType.Vaccination, providers, event, dismiss, onSave = save)
        else if (event.activityType == ActivityType.Medicine) MedicineDialog(event.childId, event, dismiss, medicines, onSave = save)
        else EditEventDialog(event, dismiss, save)
    }
    done?.let { event ->
        TimerDoneDialog(event,
            onDone = { updated -> onUpdate(updated.copy(isDraft = false)) { success -> saveFailed = !success; if (success) done = null } },
            onDelete = { onDelete(event); done = null },
            onMoreDetails = { updated -> onUpdate(updated.copy(isDraft = false)) { success -> if (success) { done = null; editing = updated.copy(isDraft = false) } else saveFailed = true } })
    }
    if (saveFailed) AlertDialog(
        onDismissRequest = { saveFailed = false },
        title = { Text("Registreringen kan ikke gemmes") },
        text = { Text("Kontrollér at slut er efter start, og prøv igen.") },
        confirmButton = { Button(onClick = { saveFailed = false }) { Text("OK") } },
    )
}

/** After a timer: already saved, so only quick optional choices, Færdig, or the bin to throw it away. */
@Composable
private fun TimerDoneDialog(event: CareEventEntity, onDone: (CareEventEntity) -> Unit, onDelete: () -> Unit, onMoreDetails: (CareEventEntity) -> Unit) {
    var draft by remember(event.id) { mutableStateOf(event) }
    var pumped by remember(event.id) { mutableStateOf(event.pumpedAmountMl?.toString().orEmpty()) }
    val clock = java.time.format.DateTimeFormatter.ofPattern("HH.mm")
    fun time(at: Long) = java.time.Instant.ofEpochMilli(at).atZone(java.time.ZoneId.systemDefault()).format(clock)
    val pauses = (event.segmentIntervals().size - 1).coerceAtLeast(0)
    val what = when (event.type) {
        CareEventType.Breastfeeding -> "Amning"
        CareEventType.Sleep -> if (event.sleepType == dk.babyapp.data.tracking.SleepType.Night) "Nattesøvn" else "Lur"
        CareEventType.Pumping -> "Pumpning"
        else -> event.activityType?.displayLabel() ?: "Aktivitet"
    }
    val subtitle = listOfNotNull("${time(event.startedAt)}–${time(event.endedAt ?: event.startedAt)}", formatDuration(event.elapsedSeconds()),
        pauses.takeIf { it > 0 }?.let { if (it == 1) "1 pause" else "$it pauser" }).joinToString(" · ")
    fun current() = draft.copy(pumpedAmountMl = if (event.type == CareEventType.Pumping) pumped.toIntOrNull() else draft.pumpedAmountMl)
    FormDialog(onDismissRequest = { onDone(current()) }, category = event.careCategory, title = { Text("$what gemt") }, subtitle = subtitle, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (event.type) {
                CareEventType.Breastfeeding -> {
                    Text("Venstre ${event.leftSeconds / 60} min · højre ${event.rightSeconds / 60} min", style = MaterialTheme.typography.bodyMedium)
                    ChoiceChips(dk.babyapp.data.tracking.BreastfeedingIssue.entries.map { it to it.displayLabel() }, draft.breastfeedingIssue, allowNone = true, label = "Gener (valgfrit)") { draft = draft.copy(breastfeedingIssue = it) }
                }
                CareEventType.Sleep -> {
                    ChoiceChips(dk.babyapp.data.tracking.SleepQuality.entries.map { it to it.displayLabel() }, draft.sleepQuality, allowNone = true, label = "Hvordan gik det? (valgfrit)") { draft = draft.copy(sleepQuality = it) }
                    SleepPlaceField(draft.sleepLocation) { draft = draft.copy(sleepLocation = it) }
                }
                CareEventType.Pumping -> {
                    AmountField(pumped, { pumped = it }, "Pumpet (ml)")
                    PumpingMethodChips(draft.pumpingMethod) { draft = draft.copy(pumpingMethod = it) }
                }
                else -> Text("Tiden er gemt.", style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { onMoreDetails(current()) }, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Icon(Icons.Outlined.ExpandMore, null); Text(" Flere detaljer · intervaller, noter")
            }
        }
    }, dismissButton = {
        IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Kassér registreringen", tint = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.weight(1f))
    }, confirmButton = { Button(onClick = { onDone(current()) }) { Text("Færdig") } })
}
