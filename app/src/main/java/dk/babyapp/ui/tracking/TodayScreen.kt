package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.HealthAndSafety
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.BreastfeedingIssue
import dk.babyapp.data.tracking.DiaperColor
import dk.babyapp.data.tracking.DiaperConsistency
import dk.babyapp.data.tracking.segmentIntervals
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.DashboardMetric
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.ui.components.BabyEmptyState
import dk.babyapp.ui.components.BabySectionHeader
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
    onStartActivity: (String, ActivityType, (Boolean) -> Unit) -> Unit,
    onToggleTimer: (CareEventEntity) -> Unit,
    onSwitchSide: (CareEventEntity) -> Unit,
    onStopTimer: (CareEventEntity, Int?, (CareEventEntity) -> Unit) -> Unit,
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String) -> Unit,
    onAddDiaper: (String, Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String, (CareEventEntity) -> Unit) -> Unit,
    onOpenTimeline: () -> Unit,
    onSaveHealthRecord: (CareEventEntity) -> Unit,
    onAddManualTimer: (String, CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit,
    onAddSleep: (String, Long, Long, SleepType, String, String, Int?, SleepQuality?, String, (Boolean) -> Unit) -> Unit,
    onAddMeasurement: (String, Long, Boolean, MeasurementType, Double, String, String) -> Unit,
    onAddActivity: (String, Long, Long, ActivityType, String) -> Unit,
    onUpdate: (CareEventEntity, (Boolean) -> Unit) -> Unit,
    onDelete: (CareEventEntity) -> Unit,
    onUpdateQuickActions: (Boolean, Boolean, Boolean, Boolean) -> Unit,
    onUpdateDashboardMetrics: (List<DashboardMetric>) -> Unit,
) {
    val childEvents = childId?.let { id -> events.filter { it.childId == id } }.orEmpty()
    val active = childEvents.firstOrNull { it.endedAt == null && it.type in listOf(CareEventType.Breastfeeding, CareEventType.Pumping, CareEventType.Sleep, CareEventType.Activity) }
    val today = LocalDate.now()
    val todayEvents = childEvents.filter { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate() == today }
    var dialog by remember { mutableStateOf<EditorKind?>(null) }
    var editing by remember { mutableStateOf<CareEventEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CareEventEntity?>(null) }
    var customizeOpen by remember { mutableStateOf(false) }
    var dashboardCustomizeOpen by remember { mutableStateOf(false) }
    var manualMenuOpen by remember { mutableStateOf(false) }
    var sleepError by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + if (active == null) 24.dp else 170.dp, start = 16.dp, end = 16.dp),
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
            val tummyEvents = todayEvents.filter { it.type == CareEventType.Activity && it.activityType == ActivityType.TummyTime }
            val tummyMinutes = tummyEvents.sumOf { it.activityDurationSeconds ?: 0 } / 60
            val lastFeeding = todayEvents.filter { it.type == CareEventType.Breastfeeding || it.type == CareEventType.Bottle }.maxByOrNull { it.startedAt }
            val lastDiaper = todayEvents.filter { it.type == CareEventType.Diaper }.maxByOrNull { it.startedAt }
            val lastSleep = todayEvents.filter { it.type == CareEventType.Sleep }.maxByOrNull { it.startedAt }
            val overviewItems = preferences.dashboardMetrics.map { metric ->
                when (metric) {
                    DashboardMetric.Feeding -> OverviewItem(metric.displayLabel(), if (lastFeeding == null) "-" else if (bottleMl > 0) "$bottleMl ml" else "$breastMinutes min", lastFeeding)
                    DashboardMetric.Diapers -> OverviewItem(metric.displayLabel(), if (lastDiaper == null) "-" else diapers.toString(), lastDiaper)
                    DashboardMetric.Sleep -> OverviewItem(metric.displayLabel(), if (lastSleep == null) "-" else formatMinutes(sleepMinutes), lastSleep)
                    DashboardMetric.TummyTime -> OverviewItem(metric.displayLabel(), if (tummyEvents.isEmpty()) "-" else formatMinutes(tummyMinutes), tummyEvents.maxByOrNull { it.startedAt })
                    DashboardMetric.Weight, DashboardMetric.Height, DashboardMetric.HeadCircumference, DashboardMetric.Temperature -> {
                        val type = metric.measurementType()
                        val latest = todayEvents.filter { it.type == CareEventType.Measurement && it.measurementType == type }.maxByOrNull { it.startedAt }
                        OverviewItem(metric.displayLabel(), latest?.measurementValue?.let { "$it ${latest.measurementUnit}" } ?: "-", latest)
                    }
                }
            }
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Dagens overblik", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { dashboardCustomizeOpen = true }) { Icon(Icons.Outlined.Tune, "Tilpas Dagens overblik") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        overviewItems.take(2).forEach { item -> SummaryMetric(item.label, item.value, item.event?.let { timeAgo(it.startedAt) }, Modifier.weight(1f)) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        overviewItems.drop(2).take(2).forEach { item -> SummaryMetric(item.label, item.value, item.event?.let { timeAgo(it.startedAt) }, Modifier.weight(1f)) }
                    }
                }
            }
        }
        item {
            BabySectionHeader("Hurtig registrering", actionIcon = Icons.Outlined.Tune, actionDescription = "Tilpas hurtig registrering", onAction = { customizeOpen = true })
        }
        if (preferences.showBreastfeedingQuickAction || preferences.showBottleQuickAction || preferences.showPumpingQuickAction) item {
            QuickActionCard(Icons.Outlined.Restaurant, "Madning") {
                if (preferences.showBreastfeedingQuickAction) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Venstre bryst", Modifier.weight(1f), enabled = childId != null) { childId?.let { onStartBreastfeeding(it, BreastSide.Left) } }; QuickButton("Højre bryst", Modifier.weight(1f), enabled = childId != null) { childId?.let { onStartBreastfeeding(it, BreastSide.Right) } }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (preferences.showBottleQuickAction) QuickButton("Flaske", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Bottle }
                    if (preferences.showPumpingQuickAction) QuickButton("Pumpning", Modifier.weight(1f), enabled = childId != null) { childId?.let(onStartPumping) }
                }
                childEvents.firstOrNull { it.type == CareEventType.Breastfeeding && it.endedAt != null }?.activeSide?.let { Text("Sidst brugte side: ${sideLabel(it)}", style = MaterialTheme.typography.bodySmall) }
            }
        }
        if (preferences.showDiaperQuickAction) item {
            QuickActionCard(Icons.Outlined.BabyChangingStation, "Ble") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DiaperType.entries.forEach { type ->
                        OutlinedButton(modifier = Modifier.weight(1f), enabled = childId != null, contentPadding = PaddingValues(horizontal = 2.dp), onClick = { childId?.let { onAddDiaper(it, System.currentTimeMillis(), type, null, null, "", "") { created -> editing = created } } }) {
                            Text(type.displayLabel(), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        item {
            QuickActionCard(Icons.Outlined.Bedtime, "Søvn") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Start lur", Modifier.weight(1f), enabled = childId != null && active == null) {
                        childId?.let { onStartSleep(it, SleepType.Nap) { success -> sleepError = !success } }
                    }
                    QuickButton("Start nattesøvn", Modifier.weight(1f), enabled = childId != null && active == null) {
                        childId?.let { onStartSleep(it, SleepType.Night) { success -> sleepError = !success } }
                    }
                }
            }
        }
        item {
            QuickActionCard(Icons.Outlined.Straighten, "Mål") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Vægt", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Weight }
                    QuickButton("Højde", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Height }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Hovedomkreds", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.HeadCircumference }
                    QuickButton("Temperatur", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Temperature }
                }
            }
        }
        item {
            QuickActionCard(Icons.Outlined.HealthAndSafety, "Sundhed") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Sundhedsbesøg", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.HealthVisit }
                    QuickButton("Vaccination", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Vaccination }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Medicin", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.Medicine }
                    QuickButton("Helbredsnotat", Modifier.weight(1f), enabled = childId != null) { dialog = EditorKind.HealthNote }
                }
            }
        }
        item {
            QuickActionCard(Icons.Outlined.ChildCare, "Diverse") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickButton("Mavetid", Modifier.weight(1f), enabled = childId != null && active == null) {
                        childId?.let { onStartActivity(it, ActivityType.TummyTime) { success -> sleepError = !success } }
                    }
                    QuickButton("Anden aktivitet", Modifier.weight(1f), enabled = childId != null && active == null) { dialog = EditorKind.StartActivity }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(enabled = childId != null) { manualMenuOpen = true },
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
                            DropdownMenuItem(text = { Text("Mål") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Weight })
                            DropdownMenuItem(text = { Text("Diverse") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Activity })
                            DropdownMenuItem(text = { Text("Sundhedsbesøg") }, onClick = { manualMenuOpen = false; dialog = EditorKind.HealthVisit })
                            DropdownMenuItem(text = { Text("Vaccination") }, onClick = { manualMenuOpen = false; dialog = EditorKind.Vaccination })
                        }
                    }
                }
            }
        }
        item {
            BabySectionHeader("Seneste", icon = Icons.Outlined.History)
        }
        if (childEvents.isEmpty()) item { BabyEmptyState(Icons.Outlined.History, "Ingen registreringer endnu", "Start en hurtig registrering ovenfor, eller tilføj en tidligere hændelse manuelt.") }
        items(childEvents.take(5), key = { it.id }) { event ->
            EventCard(event, onEdit = { editing = event }, onDelete = { deleteTarget = event })
        }
        if (childEvents.size > 5) item { TextButton(onClick = onOpenTimeline) { Text("Vis flere") } }
        }
        active?.let { event ->
            ActiveTimerCard(
                event = event,
                onToggle = onToggleTimer,
                onSwitch = onSwitchSide,
                onStop = { running ->
                    if (running.type == CareEventType.Pumping) dialog = EditorKind.StopPump else onStopTimer(running, null) { editing = it }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = contentPadding.calculateBottomPadding() + 10.dp),
            )
        }
    }

    when (dialog) {
        EditorKind.Bottle -> BottleDialog(onDismiss = { dialog = null }) { time, content, offered, consumed, notes -> childId?.let { onAddBottle(it, time, content, offered, consumed, notes) }; dialog = null }
        EditorKind.Diaper -> DiaperDialog(onDismiss = { dialog = null }) { time, type, color, consistency, observation, notes -> childId?.let { onAddDiaper(it, time, type, color, consistency, observation, notes) { created -> editing = created } }; dialog = null }
        EditorKind.ManualBreastfeeding -> ManualTimerDialog(CareEventType.Breastfeeding, onDismiss = { dialog = null }) { type, start, end, side, amount, notes -> childId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; dialog = null }
        EditorKind.ManualPumping -> ManualTimerDialog(CareEventType.Pumping, onDismiss = { dialog = null }) { type, start, end, side, amount, notes -> childId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; dialog = null }
        EditorKind.StopPump -> AmountDialog("Stop pumpning", "Pumpet mængde i ml") { amount -> active?.let { onStopTimer(it, amount) { completed -> editing = completed } }; dialog = null }
        EditorKind.Sleep -> SleepDialog(onDismiss = { dialog = null }) { start, end, type, location, settling, awakenings, quality, notes ->
            childId?.let { onAddSleep(it, start, end, type, location, settling, awakenings, quality, notes) { success -> sleepError = !success } }
            dialog = null
        }
        EditorKind.HealthVisit -> childId?.let { id -> HealthRecordDialog(id, false, careProviders, onDismiss = { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.Vaccination -> childId?.let { id -> HealthRecordDialog(id, true, careProviders, onDismiss = { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.Weight -> childId?.let { id -> MeasurementDialog(MeasurementType.Weight, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.Height -> childId?.let { id -> MeasurementDialog(MeasurementType.Height, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.HeadCircumference -> childId?.let { id -> MeasurementDialog(MeasurementType.HeadCircumference, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.Temperature -> childId?.let { id -> MeasurementDialog(MeasurementType.Temperature, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.Medicine -> childId?.let { id -> MedicineDialog(id, null, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.HealthNote -> childId?.let { id -> HealthNoteDialog(id, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.StartActivity -> StartActivityDialog({ dialog = null }) { type -> childId?.let { onStartActivity(it, type) { success -> sleepError = !success } }; dialog = null }
        EditorKind.Activity -> childId?.let { id -> ActivityDialog(null, { dialog = null }) { start, end, type, notes -> onAddActivity(id, start, end, type, notes); dialog = null } }
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
    if (dashboardCustomizeOpen) DashboardMetricDialog(preferences.dashboardMetrics, { dashboardCustomizeOpen = false }) { metrics -> onUpdateDashboardMetrics(metrics); dashboardCustomizeOpen = false }
    if (sleepError) AlertDialog(
        onDismissRequest = { sleepError = false },
        title = { Text("Registreringen kan ikke startes") },
        text = { Text("Der findes allerede en aktiv tæller eller en overlappende søvnregistrering. Stop den aktive tæller eller ret tidsrummet først.") },
        confirmButton = { Button(onClick = { sleepError = false }) { Text("OK") } },
    )
}

private enum class EditorKind { Bottle, Diaper, ManualBreastfeeding, ManualPumping, StopPump, Sleep, HealthVisit, Vaccination, HealthNote, Weight, Height, HeadCircumference, Temperature, Medicine, StartActivity, Activity }

private data class OverviewItem(val label: String, val value: String, val event: CareEventEntity?)

@Composable
private fun SummaryMetric(label: String, value: String, detail: String? = null, modifier: Modifier = Modifier) {
    Card(
        modifier.height(88.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text("Sidst: ${detail?.let { "$it siden" } ?: "-"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun DashboardMetric.displayLabel() = when (this) {
    DashboardMetric.Feeding -> "Madning"
    DashboardMetric.Diapers -> "Bleer"
    DashboardMetric.Sleep -> "Søvn"
    DashboardMetric.TummyTime -> "Mavetid"
    DashboardMetric.Weight -> "Vægt"
    DashboardMetric.Height -> "Højde"
    DashboardMetric.HeadCircumference -> "Hovedomkreds"
    DashboardMetric.Temperature -> "Temperatur"
}

private fun DashboardMetric.measurementType() = when (this) {
    DashboardMetric.Weight -> MeasurementType.Weight
    DashboardMetric.Height -> MeasurementType.Height
    DashboardMetric.HeadCircumference -> MeasurementType.HeadCircumference
    DashboardMetric.Temperature -> MeasurementType.Temperature
    else -> null
}

@Composable
private fun DashboardMetricDialog(current: List<DashboardMetric>, onDismiss: () -> Unit, onSave: (List<DashboardMetric>) -> Unit) {
    var selected by remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tilpas Dagens overblik") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vælg indholdet i hver af de fire felter.")
            selected.forEachIndexed { index, metric ->
                SelectionDropdown("Felt ${index + 1}", metric.displayLabel(), DashboardMetric.entries.map { it to it.displayLabel() }) { replacement ->
                    selected = selected.toMutableList().also { updated ->
                        val otherIndex = updated.indexOf(replacement)
                        if (otherIndex >= 0) updated[otherIndex] = metric
                        updated[index] = replacement
                    }
                }
            }
        } },
        confirmButton = { Button(onClick = { onSave(selected) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BabySectionHeader(title, icon)
            content()
        }
    }
}

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
private fun ActiveTimerCard(event: CareEventEntity, onToggle: (CareEventEntity) -> Unit, onSwitch: (CareEventEntity) -> Unit, onStop: (CareEventEntity) -> Unit, modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.runningSince) { while (event.runningSince != null) { now = System.currentTimeMillis(); delay(1_000) } }
    val elapsed = event.elapsedSeconds(now)
    Card(modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(when (event.type) { CareEventType.Breastfeeding -> "Amning – ${sideLabel(event.activeSide)}"; CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"; CareEventType.Activity -> event.activityType?.displayLabel() ?: "Aktivitet"; else -> "Pumpning" }, style = MaterialTheme.typography.titleLarge)
        Text(formatDuration(elapsed), style = MaterialTheme.typography.headlineMedium)
        if (event.type == CareEventType.Breastfeeding) Text("Venstre ${formatDuration(event.leftSeconds)}  •  Højre ${formatDuration(event.rightSeconds)}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onToggle(event) }) { Icon(if (event.runningSince == null) Icons.Outlined.PlayArrow else Icons.Outlined.Pause, null); Text(if (event.runningSince == null) "Fortsæt" else "Pause") }
            if (event.type == CareEventType.Breastfeeding) OutlinedButton(onClick = { onSwitch(event) }) { Text("Skift side") }
            Button(onClick = { onStop(event) }) { Icon(Icons.Outlined.Stop, null); Text("Stop") }
        }
    } }
}

private fun formatMinutes(minutes: Long) = if (minutes >= 60) "${minutes / 60} t ${minutes % 60} min" else "$minutes min"
private fun timeAgo(value: Long): String {
    val minutes = ((System.currentTimeMillis() - value).coerceAtLeast(0) / 60_000)
    return when { minutes < 1 -> "Nu"; minutes < 60 -> "$minutes min"; minutes < 1_440 -> "${minutes / 60} t"; else -> "${minutes / 1_440} d" }
}
private fun sideLabel(side: BreastSide?) = if (side == BreastSide.Right) "højre" else "venstre"

@Composable
internal fun DateTimeFields(value: Long, onChange: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = value }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) {
            Text("Dato *", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                DatePickerDialog(context, { _, year, month, day -> calendar.set(year, month, day); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
            }) { Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(value))) }
        }
        Column(Modifier.weight(1f)) {
            Text("Tid *", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                TimePickerDialog(context, { _, hour, minute -> calendar.set(java.util.Calendar.HOUR_OF_DAY, hour); calendar.set(java.util.Calendar.MINUTE, minute); calendar.set(java.util.Calendar.SECOND, 0); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
            }) { Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value))) }
        }
    }
}

@Composable
internal fun DateAndOptionalTimeFields(value: Long, timeSpecified: Boolean, onChange: (Long) -> Unit, onTimeSpecifiedChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = value }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Dato *", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
            DatePickerDialog(context, { _, year, month, day -> calendar.set(year, month, day); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }) { Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(value))) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(timeSpecified, onCheckedChange = onTimeSpecifiedChange)
            Text("Tilføj tidspunkt")
        }
        if (timeSpecified) {
            Text("Tid", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                TimePickerDialog(context, { _, hour, minute -> calendar.set(java.util.Calendar.HOUR_OF_DAY, hour); calendar.set(java.util.Calendar.MINUTE, minute); calendar.set(java.util.Calendar.SECOND, 0); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
            }) { Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value))) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun BottleDialog(onDismiss: () -> Unit, onSave: (Long, BottleContent, Int?, Int?, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var offered by remember { mutableStateOf("") }; var consumed by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }; var content by remember { mutableStateOf(BottleContent.BreastMilk) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj flaske") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeFields(time) { time = it }
        Text("Indhold *"); Row { BottleContent.entries.forEach { TextButton(onClick = { content = it }) { Text(if (content == it) "✓ ${it.displayLabel()}" else it.displayLabel()) } } }
        OutlinedTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") })
        OutlinedTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(time, content, offered.toIntOrNull(), consumed.toIntOrNull(), notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun DiaperDialog(onDismiss: () -> Unit, onSave: (Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var type by remember { mutableStateOf(DiaperType.Wet) }; var color by remember { mutableStateOf<DiaperColor?>(null) }; var consistency by remember { mutableStateOf<DiaperConsistency?>(null) }; var observation by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj ble") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeFields(time) { time = it }; Text("Type *"); Row { DiaperType.entries.forEach { TextButton(onClick = { type = it }) { Text(if (type == it) "✓ ${it.displayLabel()}" else it.displayLabel()) } } }
        SelectionDropdown("Farve", color?.displayLabel() ?: "Ikke angivet", listOf<DiaperColor?>(null).map { it to "Ikke angivet" } + DiaperColor.entries.map { it as DiaperColor? to it.displayLabel() }) { color = it }
        SelectionDropdown("Konsistens", consistency?.displayLabel() ?: "Ikke angivet", listOf<DiaperConsistency?>(null).map { it to "Ikke angivet" } + DiaperConsistency.entries.map { it as DiaperConsistency? to it.displayLabel() }) { consistency = it }
        OutlinedTextField(observation, { observation = it }, label = { Text("Observation") }); OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(time, type, color, consistency, observation, notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun ManualTimerDialog(type: CareEventType, onDismiss: () -> Unit, onSave: (CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit) {
    var start by remember { mutableLongStateOf(System.currentTimeMillis() - 600_000) }; var end by remember { mutableLongStateOf(System.currentTimeMillis()) }; var side by remember { mutableStateOf(BreastSide.Left) }; var amount by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (type == CareEventType.Breastfeeding) "Manuel amning" else "Manuel pumpning") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Start *"); DateTimeFields(start) { start = it }; Text("Slut *"); DateTimeFields(end) { end = it }
        if (type == CareEventType.Breastfeeding) Row { BreastSide.entries.forEach { TextButton(onClick = { side = it }) { Text(if (side == it) "✓ ${sideLabel(it)}" else sideLabel(it)) } } }
        if (type == CareEventType.Pumping) OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Mængde (ml)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
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
            Text("Start *"); DateTimeFields(start) { start = it }; Text("Slut *"); DateTimeFields(end) { end = it }
            OutlinedTextField(location, { location = it }, label = { Text("Sovested") })
            OutlinedTextField(settling, { settling = it }, label = { Text("Puttemetode") })
            OutlinedTextField(awakenings, { awakenings = it.filter(Char::isDigit) }, label = { Text("Opvågninger") })
            Text("Søvnkvalitet")
            Row { SleepQuality.entries.forEach { option -> TextButton(onClick = { quality = if (quality == option) null else option }) { Text(if (quality == option) "✓ ${option.displayLabel()}" else option.displayLabel()) } } }
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = end > start, onClick = { onSave(start, end, type, location, settling, awakenings.toIntOrNull(), quality, notes) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
internal fun MeasurementDialog(
    initialType: MeasurementType,
    onDismiss: () -> Unit,
    onSave: (Long, Boolean, MeasurementType, Double, String, String) -> Unit,
) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpecified by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(initialType) }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrér mål") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            SelectionDropdown("Måling *", type.displayLabel(), MeasurementType.entries.map { it to it.displayLabel() }) { type = it }
            OutlinedTextField(value, { value = it.replace(',', '.').filter { char -> char.isDigit() || char == '.' } }, label = { Text("Værdi (${type.defaultUnit()}) *") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = value.toDoubleOrNull() != null, onClick = { onSave(time, timeSpecified, type, value.toDouble(), type.defaultUnit(), notes) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
internal fun ActivityDialog(
    initialType: ActivityType?,
    onDismiss: () -> Unit,
    onSave: (Long, Long, ActivityType, String) -> Unit,
) {
    var start by remember { mutableLongStateOf(System.currentTimeMillis() - 300_000) }
    var end by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var type by remember { mutableStateOf(initialType ?: ActivityType.TummyTime) }
    var notes by remember { mutableStateOf("") }
    val availableTypes = if (initialType == ActivityType.Medicine) listOf(ActivityType.Medicine) else ActivityType.entries.filter { it != ActivityType.Medicine }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrér diverse") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (availableTypes.size > 1) SelectionDropdown("Type *", type.displayLabel(), availableTypes.map { it to it.displayLabel() }) { type = it }
            else Text("Type: ${type.displayLabel()}", style = MaterialTheme.typography.labelLarge)
            Text("Start *"); DateTimeFields(start) { start = it }
            Text("Slut *"); DateTimeFields(end) { end = it }
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = end > start, onClick = { onSave(start, end, type, notes) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun StartActivityDialog(onDismiss: () -> Unit, onStart: (ActivityType) -> Unit) {
    var type by remember { mutableStateOf(ActivityType.Play) }
    val choices = listOf(ActivityType.Bath, ActivityType.OutdoorTime, ActivityType.Play, ActivityType.Other)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start aktivitet") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vælg aktiviteten. Tælleren starter, når du trykker Start.")
            SelectionDropdown("Aktivitet *", type.displayLabel(), choices.map { it to it.displayLabel() }) { type = it }
        } },
        confirmButton = { Button(onClick = { onStart(type) }) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun MedicineDialog(childId: String, existing: CareEventEntity?, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    val ownChoice = "Egen medicin"
    val common = listOf("D-vitamin", "Smertestillende/febernedsættende", "Antibiotika", "Allergimedicin", "Inhalationsmedicin", ownChoice)
    val existingName = existing?.medicationName.orEmpty()
    var selection by remember { mutableStateOf(existingName.takeIf { it in common } ?: ownChoice) }
    var customName by remember { mutableStateOf(existingName.takeIf { it !in common }.orEmpty()) }
    var dose by remember { mutableStateOf(existing?.medicationDose.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var time by remember { mutableLongStateOf(existing?.startedAt ?: System.currentTimeMillis()) }
    var timeSpecified by remember { mutableStateOf(existing?.timeSpecified ?: false) }
    val name = if (selection == ownChoice) customName.trim() else selection
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Registrér medicin" else "Rediger medicin") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Registrér kun medicin, som barnet faktisk har fået.")
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            SelectionDropdown("Medicin *", selection, common.map { it to it }) { selection = it }
            if (selection == ownChoice) OutlinedTextField(customName, { customName = it }, label = { Text("Navn på medicin *") })
            OutlinedTextField(dose, { dose = it }, label = { Text("Dosis") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = name.isNotBlank(), onClick = {
            onSave((existing ?: CareEventEntity(childId = childId, type = CareEventType.Activity, startedAt = time)).copy(
                startedAt = time, endedAt = time, runningSince = null, activityType = ActivityType.Medicine,
                activityDurationSeconds = null, timeSpecified = timeSpecified, medicationName = name, medicationDose = dose, notes = notes,
            ))
        }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun HealthNoteDialog(childId: String, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpecified by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nyt helbredsnotat") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            OutlinedTextField(title, { title = it }, label = { Text("Titel *") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Observationer") })
        } },
        confirmButton = { Button(enabled = title.isNotBlank(), onClick = {
            onSave(CareEventEntity(childId = childId, type = CareEventType.HealthVisit, startedAt = time, endedAt = time, timeSpecified = timeSpecified, healthVisitType = dk.babyapp.data.tracking.HealthVisitType.Other, healthStatus = dk.babyapp.data.tracking.HealthRecordStatus.Completed, healthTitle = title.trim(), healthObservations = notes))
        }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable internal fun EditEventDialog(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    if (event.type == CareEventType.Activity && event.activityType == ActivityType.Medicine) {
        MedicineDialog(event.childId, event, onDismiss, onSave)
        return
    }
    if (event.type == CareEventType.Sleep) {
        SleepDialog(event, onDismiss) { start, end, type, location, settling, awakenings, quality, notes ->
            onSave(event.copy(startedAt = start, endedAt = end, runningSince = null, leftSeconds = (end - start) / 1_000, sleepType = type, sleepLocation = location, settlingMethod = settling, awakenings = awakenings, sleepQuality = quality, notes = notes))
        }
        return
    }
    var time by remember { mutableLongStateOf(event.startedAt) }
    var end by remember { mutableLongStateOf(event.endedAt ?: event.startedAt) }
    var notes by remember { mutableStateOf(event.notes) }
    var consumed by remember { mutableStateOf(event.amountConsumedMl?.toString().orEmpty()) }
    var observation by remember { mutableStateOf(event.observation) }
    var breastfeedingIssue by remember { mutableStateOf(event.breastfeedingIssue) }
    var diaperColor by remember { mutableStateOf(event.diaperColor) }
    var diaperConsistency by remember { mutableStateOf(event.diaperConsistency) }
    var measurementType by remember { mutableStateOf(event.measurementType ?: MeasurementType.Weight) }
    var measurementValue by remember { mutableStateOf(event.measurementValue?.toString().orEmpty()) }
    var activityType by remember { mutableStateOf(event.activityType ?: ActivityType.TummyTime) }
    var timeSpecified by remember { mutableStateOf(event.timeSpecified) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rediger registrering") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (event.type == CareEventType.Measurement) {
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
        } else if (event.type == CareEventType.Breastfeeding || event.type == CareEventType.Pumping || event.type == CareEventType.Activity) {
            Text("Start"); DateTimeFields(time) { time = it }
            Text("Slut"); DateTimeFields(end) { end = it }
        } else DateTimeFields(time) { time = it }
        if (event.type == CareEventType.Breastfeeding) SelectionDropdown(
            "Gener",
            breastfeedingIssue?.displayLabel() ?: "Ingen angivet",
            listOf<BreastfeedingIssue?>(null).map { it to "Ingen angivet" } + BreastfeedingIssue.entries.map { it as BreastfeedingIssue? to it.displayLabel() },
        ) { breastfeedingIssue = it }
        if (event.type == CareEventType.Bottle) OutlinedTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") })
        if (event.type == CareEventType.Diaper) {
            SelectionDropdown("Farve", diaperColor?.displayLabel() ?: "Ikke angivet", listOf<DiaperColor?>(null).map { it to "Ikke angivet" } + DiaperColor.entries.map { it as DiaperColor? to it.displayLabel() }) { diaperColor = it }
            SelectionDropdown("Konsistens", diaperConsistency?.displayLabel() ?: "Ikke angivet", listOf<DiaperConsistency?>(null).map { it to "Ikke angivet" } + DiaperConsistency.entries.map { it as DiaperConsistency? to it.displayLabel() }) { diaperConsistency = it }
            OutlinedTextField(observation, { observation = it }, label = { Text("Observation") })
        }
        if (event.type == CareEventType.Measurement) {
            SelectionDropdown("Måling *", measurementType.displayLabel(), MeasurementType.entries.map { it to it.displayLabel() }) { measurementType = it }
            OutlinedTextField(measurementValue, { measurementValue = it.replace(',', '.') }, label = { Text("Værdi (${measurementType.defaultUnit()}) *") })
        }
        if (event.type == CareEventType.Activity) SelectionDropdown("Type *", activityType.displayLabel(), ActivityType.entries.filter { it != ActivityType.Medicine }.map { it to it.displayLabel() }) { activityType = it }
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(enabled = end >= time && (event.type != CareEventType.Measurement || measurementValue.toDoubleOrNull() != null), onClick = {
        val duration = ((end - time).coerceAtLeast(0) / 1_000)
        val oldDuration = (event.leftSeconds + event.rightSeconds).coerceAtLeast(1)
        val left = if (event.type == CareEventType.Breastfeeding) duration * event.leftSeconds / oldDuration else event.leftSeconds
        val right = if (event.type == CareEventType.Breastfeeding) duration - left else event.rightSeconds
        onSave(event.copy(startedAt = time, endedAt = if (event.type in setOf(CareEventType.Breastfeeding, CareEventType.Pumping, CareEventType.Activity)) end else event.endedAt, timeSpecified = timeSpecified, leftSeconds = left, rightSeconds = right, breastfeedingIssue = breastfeedingIssue, amountConsumedMl = consumed.toIntOrNull() ?: event.amountConsumedMl, diaperColor = diaperColor, diaperConsistency = diaperConsistency, observation = observation, measurementType = measurementType.takeIf { event.type == CareEventType.Measurement } ?: event.measurementType, measurementValue = measurementValue.toDoubleOrNull() ?: event.measurementValue, measurementUnit = if (event.type == CareEventType.Measurement) measurementType.defaultUnit() else event.measurementUnit, activityType = activityType.takeIf { event.type == CareEventType.Activity } ?: event.activityType, activityDurationSeconds = duration.takeIf { event.type == CareEventType.Activity } ?: event.activityDurationSeconds, notes = notes))
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}
