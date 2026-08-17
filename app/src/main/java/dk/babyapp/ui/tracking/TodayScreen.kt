package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.segmentIntervals
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.profile.CareProvider
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.delay

@Composable
fun TodayScreen(
    childId: String?,
    events: List<CareEventEntity>,
    contentPadding: PaddingValues,
    preferences: AppPreferences,
    overdueDueDate: LocalDate? = null,
    careProviders: List<CareProvider> = emptyList(),
    onOpenFamily: () -> Unit = {},
    onStartBreastfeeding: (String, BreastSide) -> Unit,
    onStartPumping: (String) -> Unit,
    onStartSleep: (String, SleepType, (Boolean) -> Unit) -> Unit,
    onToggleTimer: (CareEventEntity) -> Unit,
    onSwitchSide: (CareEventEntity) -> Unit,
    onStopTimer: (CareEventEntity, Int?, (CareEventEntity) -> Unit) -> Unit,
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String) -> Unit,
    onAddDiaper: (String, Long, DiaperType, String, String, (CareEventEntity) -> Unit) -> Unit,
    onOpenTimeline: () -> Unit,
    onSaveHealthRecord: (CareEventEntity) -> Unit,
    onAddManualTimer: (String, CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit,
    onAddSleep: (String, Long, Long, SleepType, String, String, Int?, SleepQuality?, String, (Boolean) -> Unit) -> Unit,
    onUpdate: (CareEventEntity, (Boolean) -> Unit) -> Unit,
    onDelete: (CareEventEntity) -> Unit,
    onUpdateQuickActions: (Boolean, Boolean, Boolean, Boolean) -> Unit,
) {
    val childEvents = childId?.let { id -> events.filter { it.childId == id } }.orEmpty()
    val active = childEvents.firstOrNull { it.endedAt == null && it.type in listOf(CareEventType.Breastfeeding, CareEventType.Pumping, CareEventType.Sleep) }
    val today = LocalDate.now()
    val todayEvents = childEvents.filter { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate() == today }
    var dialog by remember { mutableStateOf<EditorKind?>(null) }
    var editing by remember { mutableStateOf<CareEventEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CareEventEntity?>(null) }
    var customizeOpen by remember { mutableStateOf(false) }
    var manualMenuOpen by remember { mutableStateOf(false) }
    var sleepError by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (overdueDueDate != null) item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Outlined.NotificationsActive, null)
                        Text("Er barnet blevet født?", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Terminen var $overdueDueDate. Opdater profilen og indtast fødselsdatoen, hvis barnet er født.")
                    Button(onClick = onOpenFamily) { Text("Opdater barnets profil") }
                }
            }
        }
        item {
            val breastMinutes = todayEvents.filter { it.type == CareEventType.Breastfeeding }.sumOf { it.elapsedSeconds() } / 60
            val bottleMl = todayEvents.filter { it.type == CareEventType.Bottle }.sumOf { it.amountConsumedMl ?: 0 }
            val diapers = todayEvents.count { it.type == CareEventType.Diaper }
            val sleepMinutes = todayEvents.filter { it.type == CareEventType.Sleep }.sumOf { it.elapsedSeconds() } / 60
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Dagens overblik", style = MaterialTheme.typography.titleLarge)
                    Text("Amning: $breastMinutes min.  •  Flaske: $bottleMl ml  •  Bleer: $diapers  •  Søvn: $sleepMinutes min.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (active != null) item {
            ActiveTimerCard(active, onToggleTimer, onSwitchSide) { event ->
                if (event.type == CareEventType.Pumping) dialog = EditorKind.StopPump else onStopTimer(event, null) { editing = it }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hurtig registrering", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = { customizeOpen = true }) { Icon(Icons.Outlined.Tune, "Tilpas hurtig registrering") }
            }
        }
        if (preferences.showBreastfeedingQuickAction || preferences.showBottleQuickAction || preferences.showPumpingQuickAction) item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocalDrink, null, tint = MaterialTheme.colorScheme.primary)
                Text("Madning", style = MaterialTheme.typography.titleLarge)
            }
            if (preferences.showBreastfeedingQuickAction) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickButton("Venstre bryst", Modifier.weight(1f), enabled = childId != null) { childId?.let { onStartBreastfeeding(it, BreastSide.Left) } }; QuickButton("Højre bryst", Modifier.weight(1f), enabled = childId != null) { childId?.let { onStartBreastfeeding(it, BreastSide.Right) } }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (preferences.showBottleQuickAction) QuickButton("Flaske", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Bottle }
                if (preferences.showPumpingQuickAction) QuickButton("Pumpning", Modifier.weight(1f), enabled = childId != null) { childId?.let(onStartPumping) }
            }
            childEvents.firstOrNull { it.type == CareEventType.Breastfeeding && it.endedAt != null }?.activeSide?.let { Text("Sidst brugte side: ${sideLabel(it)}", style = MaterialTheme.typography.bodySmall) }
        }
        if (preferences.showDiaperQuickAction) item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Outlined.BabyChangingStation, null, tint = MaterialTheme.colorScheme.primary)
                Text("Ble", style = MaterialTheme.typography.titleLarge)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DiaperType.entries.forEach { type ->
                    OutlinedButton(modifier = Modifier.weight(1f), enabled = childId != null, contentPadding = PaddingValues(horizontal = 2.dp), onClick = { childId?.let { onAddDiaper(it, System.currentTimeMillis(), type, "", "") { created -> editing = created } } }) {
                        Text(diaperLabel(type), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Outlined.Bedtime, null, tint = MaterialTheme.colorScheme.primary)
                Text("Søvn", style = MaterialTheme.typography.titleLarge)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickButton("Start lur", Modifier.weight(1f), enabled = childId != null && active == null) {
                    childId?.let { onStartSleep(it, SleepType.Nap) { success -> sleepError = !success } }
                }
                QuickButton("Start nattesøvn", Modifier.weight(1f), enabled = childId != null && active == null) {
                    childId?.let { onStartSleep(it, SleepType.Night) { success -> sleepError = !success } }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.EditNote, null, Modifier.padding(end = 12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Manuel registrering", style = MaterialTheme.typography.titleLarge)
                        Text("Registrér hændelser med dato, detaljer og egne noter", style = MaterialTheme.typography.bodyMedium)
                    }
                    Box {
                        IconButton(enabled = childId != null, onClick = { manualMenuOpen = true }) { Icon(Icons.Outlined.Add, "Vælg manuel registrering") }
                        DropdownMenu(expanded = manualMenuOpen, onDismissRequest = { manualMenuOpen = false }) {
                            DropdownMenuItem(text = { Text("Amning") }, onClick = { manualMenuOpen = false; dialog = EditorKind.ManualBreastfeeding })
                            DropdownMenuItem(text = { Text("Flaske") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Bottle })
                            DropdownMenuItem(text = { Text("Pumpning") }, onClick = { manualMenuOpen = false; dialog = EditorKind.ManualPumping })
                            DropdownMenuItem(text = { Text("Ble") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Diaper })
                            DropdownMenuItem(text = { Text("Søvn") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Sleep })
                            DropdownMenuItem(text = { Text("Sundhedsbesøg") }, onClick = { manualMenuOpen = false; dialog = EditorKind.HealthVisit })
                            DropdownMenuItem(text = { Text("Vaccination") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Vaccination })
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Seneste", style = MaterialTheme.typography.headlineSmall)
            }
        }
        if (childEvents.isEmpty()) item { Text("Ingen registreringer endnu.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(childEvents.take(5), key = { it.id }) { event ->
            EventCard(event, onEdit = { editing = event }, onDelete = { deleteTarget = event })
        }
        if (childEvents.size > 5) item { TextButton(onClick = onOpenTimeline) { Text("Vis flere") } }
    }

    when (dialog) {
        EditorKind.Bottle -> BottleDialog(onDismiss = { dialog = null }) { time, content, offered, consumed, notes -> childId?.let { onAddBottle(it, time, content, offered, consumed, notes) }; dialog = null }
        EditorKind.Diaper -> DiaperDialog(onDismiss = { dialog = null }) { time, type, observation, notes -> childId?.let { onAddDiaper(it, time, type, observation, notes) { created -> editing = created } }; dialog = null }
        EditorKind.ManualBreastfeeding -> ManualTimerDialog(CareEventType.Breastfeeding, onDismiss = { dialog = null }) { type, start, end, side, amount, notes -> childId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; dialog = null }
        EditorKind.ManualPumping -> ManualTimerDialog(CareEventType.Pumping, onDismiss = { dialog = null }) { type, start, end, side, amount, notes -> childId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; dialog = null }
        EditorKind.StopPump -> AmountDialog("Stop pumpning", "Pumpet mængde i ml") { amount -> active?.let { onStopTimer(it, amount) { completed -> editing = completed } }; dialog = null }
        EditorKind.Sleep -> SleepDialog(onDismiss = { dialog = null }) { start, end, type, location, settling, awakenings, quality, notes ->
            childId?.let { onAddSleep(it, start, end, type, location, settling, awakenings, quality, notes) { success -> sleepError = !success } }
            dialog = null
        }
        EditorKind.HealthVisit -> childId?.let { id -> HealthRecordDialog(id, false, careProviders, onDismiss = { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.Vaccination -> childId?.let { id -> HealthRecordDialog(id, true, careProviders, onDismiss = { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        null -> Unit
    }
    editing?.let { event -> EditEventDialog(event, onDismiss = { editing = null }) { updated ->
        onUpdate(updated) { success -> sleepError = !success; if (success) editing = null }
    } }
    deleteTarget?.let { event -> AlertDialog(
        onDismissRequest = { deleteTarget = null },
        title = { Text("Slet registrering?") },
        text = { Text("Registreringen fjernes fra oversigten, men bevares sikkert internt.") },
        confirmButton = { Button(onClick = { onDelete(event); deleteTarget = null }) { Text("Slet") } },
        dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Annuller") } },
    ) }
    if (customizeOpen) QuickActionDialog(preferences, onDismiss = { customizeOpen = false }) { breast, bottle, pumping, diaper -> onUpdateQuickActions(breast, bottle, pumping, diaper); customizeOpen = false }
    if (sleepError) AlertDialog(
        onDismissRequest = { sleepError = false },
        title = { Text("Søvnen overlapper") },
        text = { Text("Der findes allerede en aktiv registrering eller en søvnregistrering i dette tidsrum. Ret tiderne, før du gemmer.") },
        confirmButton = { Button(onClick = { sleepError = false }) { Text("OK") } },
    )
}

private enum class EditorKind { Bottle, Diaper, ManualBreastfeeding, ManualPumping, StopPump, Sleep, HealthVisit, Vaccination }

@Composable private fun QuickButton(text: String, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) = FilledTonalButton(modifier = modifier, enabled = enabled, onClick = onClick) { Text(text) }

@Composable private fun QuickActionDialog(preferences: AppPreferences, onDismiss: () -> Unit, onSave: (Boolean, Boolean, Boolean, Boolean) -> Unit) {
    var breast by remember { mutableStateOf(preferences.showBreastfeedingQuickAction) }; var bottle by remember { mutableStateOf(preferences.showBottleQuickAction) }; var pumping by remember { mutableStateOf(preferences.showPumpingQuickAction) }; var diaper by remember { mutableStateOf(preferences.showDiaperQuickAction) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilpas hurtig registrering") }, text = { Column {
        Row { Checkbox(breast, { breast = it }); Text("Amning", Modifier.padding(top = 12.dp)) }
        Row { Checkbox(bottle, { bottle = it }); Text("Flaske", Modifier.padding(top = 12.dp)) }
        Row { Checkbox(pumping, { pumping = it }); Text("Pumpning", Modifier.padding(top = 12.dp)) }
        Row { Checkbox(diaper, { diaper = it }); Text("Ble", Modifier.padding(top = 12.dp)) }
    } }, confirmButton = { Button(onClick = { onSave(breast, bottle, pumping, diaper) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
private fun ActiveTimerCard(event: CareEventEntity, onToggle: (CareEventEntity) -> Unit, onSwitch: (CareEventEntity) -> Unit, onStop: (CareEventEntity) -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.runningSince) { while (event.runningSince != null) { now = System.currentTimeMillis(); delay(1_000) } }
    val elapsed = event.elapsedSeconds(now)
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(when (event.type) { CareEventType.Breastfeeding -> "Amning – ${sideLabel(event.activeSide)}"; CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"; else -> "Pumpning" }, style = MaterialTheme.typography.titleLarge)
        Text(formatDuration(elapsed), style = MaterialTheme.typography.headlineMedium)
        if (event.type == CareEventType.Breastfeeding) Text("Venstre ${formatDuration(event.leftSeconds)}  •  Højre ${formatDuration(event.rightSeconds)}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onToggle(event) }) { Icon(if (event.runningSince == null) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, null); Text(if (event.runningSince == null) "Fortsæt" else "Pause") }
            if (event.type == CareEventType.Breastfeeding) OutlinedButton(onClick = { onSwitch(event) }) { Text("Skift side") }
            Button(onClick = { onStop(event) }) { Icon(Icons.Outlined.Stop, null); Text("Stop") }
        }
    } }
}

@Composable
internal fun EventCard(event: CareEventEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember(event.id) { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(eventTitle(event), style = MaterialTheme.typography.titleSmall)
            val recordedAt = if (event.type == CareEventType.HealthVisit || event.type == CareEventType.Vaccination) {
                DateFormat.getDateInstance(DateFormat.SHORT).format(Date(event.startedAt))
            } else {
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(event.startedAt))
            }
            Text(recordedAt)
            if (event.isRunning) Text("Kører nu", color = MaterialTheme.colorScheme.primary)
            if (expanded) {
                Text(eventDetails(event), color = MaterialTheme.colorScheme.onSurfaceVariant)
                val intervals = event.segmentIntervals()
                if (intervals.isNotEmpty()) {
                    Text("Tidsintervaller", style = MaterialTheme.typography.labelLarge)
                    intervals.forEach { (start, end) ->
                        Text("${formatClock(start)} – ${end?.let(::formatClock) ?: "kører"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (expanded) "Fold sammen" else "Vis detaljer") }
        IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Rediger") }
        IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Slet") }
    } }
}

private fun formatClock(value: Long) = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value))

internal fun eventTitle(event: CareEventEntity) = when (event.type) { CareEventType.Breastfeeding -> "Amning"; CareEventType.Bottle -> "Flaske"; CareEventType.Pumping -> "Pumpning"; CareEventType.Diaper -> "Ble – ${diaperLabel(event.diaperType)}"; CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"; CareEventType.HealthVisit -> event.healthTitle.ifBlank { "Sundhedsbesøg" }; CareEventType.Vaccination -> event.vaccineName.ifBlank { "Vaccination" } }
private fun eventDetails(event: CareEventEntity) = when (event.type) {
    CareEventType.Breastfeeding -> "${formatDuration(event.elapsedSeconds())} · V ${formatDuration(event.leftSeconds)} · H ${formatDuration(event.rightSeconds)}"
    CareEventType.Bottle -> "${event.amountConsumedMl ?: 0} af ${event.amountOfferedMl ?: 0} ml · ${bottleLabel(event.bottleContent)}"
    CareEventType.Pumping -> "${formatDuration(event.elapsedSeconds())}${event.pumpedAmountMl?.let { " · $it ml" } ?: ""}"
    CareEventType.Diaper -> listOf(event.observation, event.notes).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "Registreret" }
    CareEventType.Sleep -> listOf(formatDuration(event.elapsedSeconds()), event.sleepLocation, event.sleepQuality?.let(::sleepQualityLabel), event.notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.HealthVisit -> listOf(event.healthStatus?.let(::statusLabel), event.providerDisplayName, event.healthReason, event.healthObservations, event.healthAdvice, event.followUp, event.notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Vaccination -> listOf(event.healthStatus?.let(::statusLabel), event.vaccineDose, event.vaccineBatchNumber, event.injectionSite, event.reactionNotes, event.notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
}
private fun formatDuration(seconds: Long) = "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)
private fun sideLabel(side: BreastSide?) = if (side == BreastSide.Right) "højre" else "venstre"
private fun diaperLabel(type: DiaperType?) = when (type) { DiaperType.Wet -> "Våd"; DiaperType.Dirty -> "Afføring"; DiaperType.Both -> "Begge"; DiaperType.Dry -> "Tør"; null -> "Ble" }
private fun bottleLabel(content: BottleContent?) = when (content) { BottleContent.BreastMilk -> "Modermælk"; BottleContent.Formula -> "Modermælkserstatning"; BottleContent.Water -> "Vand"; BottleContent.Other -> "Andet"; null -> "Ikke angivet" }
internal fun sleepQualityLabel(quality: SleepQuality) = when (quality) { SleepQuality.Restful -> "Rolig"; SleepQuality.Mixed -> "Blandet"; SleepQuality.Restless -> "Urolig" }

@Composable
internal fun DateTimeButton(value: Long, onChange: (Long) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = value }
        DatePickerDialog(context, { _, year, month, day ->
            TimePickerDialog(context, { _, hour, minute -> calendar.set(year, month, day, hour, minute, 0); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
    }) { Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(value))) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun BottleDialog(onDismiss: () -> Unit, onSave: (Long, BottleContent, Int?, Int?, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var offered by remember { mutableStateOf("") }; var consumed by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }; var content by remember { mutableStateOf(BottleContent.BreastMilk) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj flaske") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeButton(time) { time = it }
        Text("Indhold"); Row { BottleContent.entries.forEach { TextButton(onClick = { content = it }) { Text(if (content == it) "✓ ${bottleLabel(it)}" else bottleLabel(it)) } } }
        OutlinedTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") })
        OutlinedTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter (valgfrit)") })
    } }, confirmButton = { Button(onClick = { onSave(time, content, offered.toIntOrNull(), consumed.toIntOrNull(), notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun DiaperDialog(onDismiss: () -> Unit, onSave: (Long, DiaperType, String, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var type by remember { mutableStateOf(DiaperType.Wet) }; var observation by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj ble") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeButton(time) { time = it }; Row { DiaperType.entries.forEach { TextButton(onClick = { type = it }) { Text(if (type == it) "✓ ${diaperLabel(it)}" else diaperLabel(it)) } } }
        OutlinedTextField(observation, { observation = it }, label = { Text("Observation (valgfrit)") }); OutlinedTextField(notes, { notes = it }, label = { Text("Noter (valgfrit)") })
    } }, confirmButton = { Button(onClick = { onSave(time, type, observation, notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun ManualTimerDialog(type: CareEventType, onDismiss: () -> Unit, onSave: (CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit) {
    var start by remember { mutableLongStateOf(System.currentTimeMillis() - 600_000) }; var end by remember { mutableLongStateOf(System.currentTimeMillis()) }; var side by remember { mutableStateOf(BreastSide.Left) }; var amount by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (type == CareEventType.Breastfeeding) "Manuel amning" else "Manuel pumpning") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Start"); DateTimeButton(start) { start = it }; Text("Slut"); DateTimeButton(end) { end = it }
        if (type == CareEventType.Breastfeeding) Row { BreastSide.entries.forEach { TextButton(onClick = { side = it }) { Text(if (side == it) "✓ ${sideLabel(it)}" else sideLabel(it)) } } }
        if (type == CareEventType.Pumping) OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Mængde (ml)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter (valgfrit)") })
    } }, confirmButton = { Button(enabled = end >= start, onClick = { onSave(type, start, end, if (type == CareEventType.Breastfeeding) side else null, amount.toIntOrNull(), notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable private fun AmountDialog(title: String, label: String, onSave: (Int?) -> Unit) { var amount by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = { onSave(null) }, title = { Text(title) }, text = { OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text(label) }) }, confirmButton = { Button(onClick = { onSave(amount.toIntOrNull()) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = { onSave(null) }) { Text("Spring over") } }) }

@Composable
internal fun SleepDialog(
    existing: CareEventEntity? = null,
    onDismiss: () -> Unit,
    onSave: (Long, Long, SleepType, String, String, Int?, SleepQuality?, String) -> Unit,
) {
    var start by remember { mutableLongStateOf(existing?.startedAt ?: System.currentTimeMillis() - 3_600_000) }
    var end by remember { mutableLongStateOf(existing?.endedAt ?: System.currentTimeMillis()) }
    var type by remember { mutableStateOf(existing?.sleepType ?: SleepType.Nap) }
    var location by remember { mutableStateOf(existing?.sleepLocation.orEmpty()) }
    var settling by remember { mutableStateOf(existing?.settlingMethod.orEmpty()) }
    var awakenings by remember { mutableStateOf(existing?.awakenings?.toString().orEmpty()) }
    var quality by remember { mutableStateOf(existing?.sleepQuality) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tilføj søvn" else "Rediger søvn") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row { SleepType.entries.forEach { option -> TextButton(onClick = { type = option }) { Text(if (type == option) "✓ ${if (option == SleepType.Nap) "Lur" else "Nattesøvn"}" else if (option == SleepType.Nap) "Lur" else "Nattesøvn") } } }
            Text("Start"); DateTimeButton(start) { start = it }; Text("Slut"); DateTimeButton(end) { end = it }
            OutlinedTextField(location, { location = it }, label = { Text("Sovested (valgfrit)") })
            OutlinedTextField(settling, { settling = it }, label = { Text("Puttemetode (valgfrit)") })
            OutlinedTextField(awakenings, { awakenings = it.filter(Char::isDigit) }, label = { Text("Opvågninger (valgfrit)") })
            Text("Søvnkvalitet (valgfrit)")
            Row { SleepQuality.entries.forEach { option -> TextButton(onClick = { quality = if (quality == option) null else option }) { Text(if (quality == option) "✓ ${sleepQualityLabel(option)}" else sleepQualityLabel(option)) } } }
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter (valgfrit)") })
        } },
        confirmButton = { Button(enabled = end > start, onClick = { onSave(start, end, type, location, settling, awakenings.toIntOrNull(), quality, notes) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable internal fun EditEventDialog(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    if (event.type == CareEventType.Sleep) {
        SleepDialog(event, onDismiss) { start, end, type, location, settling, awakenings, quality, notes ->
            onSave(event.copy(startedAt = start, endedAt = end, runningSince = null, leftSeconds = (end - start) / 1_000, sleepType = type, sleepLocation = location, settlingMethod = settling, awakenings = awakenings, sleepQuality = quality, notes = notes))
        }
        return
    }
    var time by remember { mutableLongStateOf(event.startedAt) }; var notes by remember { mutableStateOf(event.notes) }; var consumed by remember { mutableStateOf(event.amountConsumedMl?.toString().orEmpty()) }; var observation by remember { mutableStateOf(event.observation) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rediger registrering") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeButton(time) { time = it }
        if (event.type == CareEventType.Bottle) OutlinedTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") })
        if (event.type == CareEventType.Diaper) OutlinedTextField(observation, { observation = it }, label = { Text("Observation") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(event.copy(startedAt = time, amountConsumedMl = consumed.toIntOrNull() ?: event.amountConsumedMl, observation = observation, notes = notes)) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}
