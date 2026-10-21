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
 * Compact by default; each can be expanded for intervals, keep-screen-on and cancel. Stopping a
 * timer opens it for saving, and a timer that was stopped earlier but not yet saved opens again.
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
    var saveFailed by remember { mutableStateOf(false) }
    LaunchedEffect(activeChildId, recentEvents) {
        if (editing == null) editing = recentEvents.firstOrNull { it.childId == activeChildId && it.isDraft && it.deletedAt == null }
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
                        onStop = { running -> onStop(running, null) { editing = it } },
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
        if (event.type in setOf(CareEventType.HealthVisit, CareEventType.Vaccination)) HealthRecordDialog(event.childId, event.type == CareEventType.Vaccination, providers, event, dismiss, save)
        else if (event.activityType == ActivityType.Medicine) MedicineDialog(event.childId, event, dismiss, medicines, save)
        else EditEventDialog(event, dismiss, save)
    }
    if (saveFailed) AlertDialog(
        onDismissRequest = { saveFailed = false },
        title = { Text("Registreringen kan ikke gemmes") },
        text = { Text("Der kan være overlappende søvn, eller amningen er ændret imens. Kontrollér tiderne, eller luk redigeringen og åbn den igen.") },
        confirmButton = { Button(onClick = { saveFailed = false }) { Text("OK") } },
    )
}
