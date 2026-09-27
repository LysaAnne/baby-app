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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import dk.babyapp.data.preferences.orderedQuickActionCategories
import dk.babyapp.data.preferences.QuickAction
import dk.babyapp.data.medicine.MedicinePlan
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
    keepTimerAwake: Boolean = false,
    onKeepTimerAwake: (Boolean) -> Unit = {},
    onUpdateQuickActionCategoryOrder: (List<String>) -> Unit = {},
    onUpdateHiddenQuickActions: (Set<String>) -> Unit = {},
    onUpdateMedicines: (List<MedicinePlan>) -> Unit = {},
) {
    val childEvents = childId?.let { id -> events.filter { it.childId == id && !it.isDraft && it.deletedAt == null } }.orEmpty()
    val active = childEvents.firstOrNull { it.endedAt == null && it.type in listOf(CareEventType.Breastfeeding, CareEventType.Pumping, CareEventType.Sleep, CareEventType.Activity) }
    fun visible(action: QuickAction): Boolean = action.name !in preferences.hiddenQuickActions && when (action) {
        QuickAction.Breastfeeding -> preferences.showBreastfeedingQuickAction
        QuickAction.Bottle -> preferences.showBottleQuickAction
        QuickAction.Pumping -> preferences.showPumpingQuickAction
        QuickAction.Diaper -> preferences.showDiaperQuickAction
        else -> true
    }
    val today = LocalDate.now()
    val todayEvents = childEvents.filter { Instant.ofEpochMilli(it.startedAt).atZone(ZoneId.systemDefault()).toLocalDate() == today }
    var dialog by remember { mutableStateOf<EditorKind?>(null) }
    var editing by remember { mutableStateOf<CareEventEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CareEventEntity?>(null) }
    var customizeOpen by remember { mutableStateOf(false) }
    var dashboardCustomizeOpen by remember { mutableStateOf(false) }
    var manualMenuOpen by remember { mutableStateOf(false) }
    var sleepError by remember { mutableStateOf(false) }
    var pendingPumpMethod by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(active?.id, pendingPumpMethod) { if (active?.type == CareEventType.Pumping && pendingPumpMethod != null) { onSaveHealthRecord(active.copy(pumpingMethod = pendingPumpMethod!!)); pendingPumpMethod = null } }
    var medicineCardOpen by remember { mutableStateOf(false) }
    LaunchedEffect(childId, events) {
        if (editing == null) editing = events.firstOrNull { it.childId == childId && it.isDraft && it.deletedAt == null }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + if (active == null) 100.dp else 300.dp, start = 16.dp, end = 16.dp),
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
                    DashboardMetric.Feeding -> OverviewItem(metric.displayLabel(), if (lastFeeding == null) "-" else listOfNotNull("$breastMinutes min".takeIf { todayEvents.any { it.type == CareEventType.Breastfeeding } }, "$bottleMl ml".takeIf { todayEvents.any { it.type == CareEventType.Bottle } }).joinToString(" · "), lastFeeding)
                    DashboardMetric.Diapers -> OverviewItem(metric.displayLabel(), if (lastDiaper == null) "-" else diapers.toString(), lastDiaper)
                    DashboardMetric.Sleep -> OverviewItem(metric.displayLabel(), if (lastSleep == null) "-" else formatMinutes(sleepMinutes), lastSleep)
                    DashboardMetric.TummyTime -> OverviewItem(metric.displayLabel(), if (tummyEvents.isEmpty()) "-" else formatMinutes(tummyMinutes), tummyEvents.maxByOrNull { it.startedAt })
                    DashboardMetric.Pumping, DashboardMetric.SolidFood, DashboardMetric.Medicine, DashboardMetric.HealthVisits, DashboardMetric.Vaccinations, DashboardMetric.Activities -> {
                        val records = todayEvents.filter { metric.matches(it) }
                        val value = when {
                            records.isEmpty() -> "-"
                            metric == DashboardMetric.Pumping -> "${records.sumOf { it.pumpedAmountMl ?: 0 }} ml"
                            metric == DashboardMetric.Activities -> formatMinutes(records.sumOf { it.activityDurationSeconds ?: 0 } / 60)
                            else -> records.size.toString()
                        }
                        OverviewItem(metric.displayLabel(), value, records.maxByOrNull { it.startedAt })
                    }
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
                    overviewItems.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { columnIndex, item ->
                                val metric = preferences.dashboardMetrics[rowIndex * 2 + columnIndex]
                                val count = todayEvents.count { event -> when (metric) {
                                    DashboardMetric.Feeding -> event.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle)
                                    DashboardMetric.Diapers -> event.type == CareEventType.Diaper
                                    DashboardMetric.Sleep -> event.type == CareEventType.Sleep
                                    DashboardMetric.TummyTime -> event.type == CareEventType.Activity && event.activityType == ActivityType.TummyTime
                                    else -> metric.matches(event)
                                } }
                                SummaryMetric(item.label, item.value, item.event?.let { timeAgo(it.startedAt) }, Modifier.weight(1f), count)
                            }
                            if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item {
            BabySectionHeader("Hurtig registrering", actionIcon = Icons.Outlined.Tune, actionDescription = "Tilpas hurtig registrering", onAction = { customizeOpen = true })
        }
        orderedQuickActionCategories(preferences.quickActionCategoryOrder).forEach { category ->
            val actions = QuickAction.entries.filter { visible(it) && it.category == category }
            if (actions.isEmpty()) return@forEach
            item {
                QuickActionCard(when (category) {
                    "Søvn" -> Icons.Outlined.Bedtime
                    "Ble" -> Icons.Outlined.BabyChangingStation
                    "Mål" -> Icons.Outlined.Straighten
                    "Sundhed" -> Icons.Outlined.HealthAndSafety
                    "Diverse" -> Icons.Outlined.ChildCare
                    else -> Icons.Outlined.Restaurant
                }, category) {
                    if (QuickAction.Breastfeeding in actions) {
                        Text("Amning", style = MaterialTheme.typography.titleSmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BreastSide.entries.forEach { side ->
                                QuickButton(sideLabel(side), Modifier.weight(1f), childId != null && active == null) {
                                    childId?.let { onStartBreastfeeding(it, side) }
                                }
                            }
                        }
                    }
                    if (QuickAction.Diaper in actions) {
                        DiaperType.entries.chunked(2).forEach { types ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                types.forEach { type ->
                                    QuickButton(type.displayLabel(), Modifier.weight(1f), childId != null) {
                                        childId?.let { onAddDiaper(it, System.currentTimeMillis(), type, null, null, "", "") {} }
                                    }
                                }
                            }
                        }
                    }
                    actions.filterNot { it == QuickAction.Breastfeeding || it == QuickAction.Diaper }.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { action ->
                                val timer = action in setOf(QuickAction.Breastfeeding, QuickAction.Pumping, QuickAction.Nap, QuickAction.Night, QuickAction.TummyTime, QuickAction.Activity)
                                QuickButton(action.label, Modifier.weight(1f), childId != null && (!timer || active == null)) {
                                    val id = childId ?: return@QuickButton
                                    when (action) {
                                        QuickAction.Breastfeeding -> dialog = EditorKind.StartBreast
                                        QuickAction.Pumping -> dialog = EditorKind.StartPump
                                        QuickAction.Nap -> onStartSleep(id, SleepType.Nap) { sleepError = !it }
                                        QuickAction.Night -> onStartSleep(id, SleepType.Night) { sleepError = !it }
                                        QuickAction.TummyTime -> onStartActivity(id, ActivityType.TummyTime) { sleepError = !it }
                                        QuickAction.Activity -> dialog = EditorKind.StartActivity
                                        else -> dialog = EditorKind.valueOf(action.name)
                                    }
                                }
                            }
                        }
                    }
                    if (category == "Sundhed") TextButton(onClick = { medicineCardOpen = true }, enabled = childId != null) { Text("Åbn medicinkort") }
                }
            }
        }
        if (QuickAction.entries.none { visible(it) && it.category == "Sundhed" }) item {
            TextButton(onClick = { medicineCardOpen = true }, enabled = childId != null) { Text("Medicinkort") }
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
        if (childId != null) FloatingActionButton(onClick = { manualMenuOpen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp)) { Icon(Icons.Outlined.Add, "Manuel registrering") }
        active?.let { event ->
            ActiveTimerCard(
                event = event,
                onToggle = onToggleTimer,
                onSwitch = onSwitchSide,
                onStop = { running ->
                    onStopTimer(running, null) { editing = it }
                },
                onCancel = onDelete,
                keepAwake = keepTimerAwake, onKeepAwake = onKeepTimerAwake,
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = contentPadding.calculateBottomPadding() + 82.dp),
            )
        }
    }

    if (manualMenuOpen) AlertDialog(
        onDismissRequest = { manualMenuOpen = false }, title = { Text("Manuel registrering") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) {
            listOf("Amning" to EditorKind.ManualBreastfeeding, "Flaske" to EditorKind.Bottle, "Pumpning" to EditorKind.ManualPumping,
                "Ble" to EditorKind.Diaper, "Søvn" to EditorKind.Sleep, "Mål" to EditorKind.Weight, "Fast føde" to EditorKind.SolidFood,
                "Medicin" to EditorKind.Medicine, "Sundhedsbesøg" to EditorKind.HealthVisit, "Vaccination" to EditorKind.Vaccination,
                "Helbredsnotat" to EditorKind.HealthNote, "Diverse" to EditorKind.Activity).forEach { (label, kind) ->
                TextButton(onClick = { manualMenuOpen = false; dialog = kind }) { Text(label) }
            }
        } }, confirmButton = {}, dismissButton = { TextButton(onClick = { manualMenuOpen = false }) { Text("Luk") } },
    )
    when (dialog) {
        EditorKind.StartBreast -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Start amning") },
            text = { Row { BreastSide.entries.forEach { side -> TextButton(onClick = { childId?.let { onStartBreastfeeding(it, side) }; dialog = null }) { Text(sideLabel(side)) } } } },
            confirmButton = {}, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Annuller") } })
        EditorKind.Bottle -> BottleDialog(onDismiss = { dialog = null }) { time, content, offered, consumed, notes -> childId?.let { onAddBottle(it, time, content, offered, consumed, notes) }; dialog = null }
        EditorKind.Diaper -> DiaperDialog(onDismiss = { dialog = null }) { time, type, color, consistency, observation, notes -> childId?.let { onAddDiaper(it, time, type, color, consistency, observation, notes) {} }; dialog = null }
        EditorKind.ManualBreastfeeding -> ManualTimerDialog(CareEventType.Breastfeeding, onDismiss = { dialog = null }) { type, start, end, side, amount, notes -> childId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; dialog = null }
        EditorKind.ManualPumping -> childId?.let { id -> val now = remember { System.currentTimeMillis() }; EditEventDialog(remember { CareEventEntity(childId = id, type = CareEventType.Pumping, startedAt = now - 600_000, endedAt = now, leftSeconds = 600) }, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.StartPump -> SelectionStartPumpDialog({ dialog = null }) { method -> childId?.let { id -> onStartPumping(id); pendingPumpMethod = method }; dialog = null }
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
        EditorKind.Medicine -> childId?.let { id -> MedicineDialog(id, null, { dialog = null }, preferences.medicines) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.HealthNote -> childId?.let { id -> HealthNoteDialog(id, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.StartActivity -> StartActivityDialog({ dialog = null }) { type -> childId?.let { onStartActivity(it, type) { success -> sleepError = !success } }; dialog = null }
        EditorKind.Activity -> childId?.let { id -> ActivityDialog(null, { dialog = null }) { start, end, type, notes -> onAddActivity(id, start, end, type, notes); dialog = null } }
        EditorKind.SolidFood -> childId?.let { id -> SolidFoodDialog(id, null, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        null -> Unit
    }
    if (medicineCardOpen && childId != null) MedicineCardDialog(childId, preferences.medicines, onUpdateMedicines, { medicineCardOpen = false })
    editing?.let { event ->
        val dismiss = { if (event.isDraft) onDelete(event); editing = null }
        val save: (CareEventEntity) -> Unit = { updated -> onUpdate(updated.copy(isDraft = false)) { success -> sleepError = !success; if (success) editing = null } }
        if (event.type in setOf(CareEventType.HealthVisit, CareEventType.Vaccination)) HealthRecordDialog(event.childId, event.type == CareEventType.Vaccination, careProviders, event, dismiss, save)
        else if (event.activityType == ActivityType.Medicine) MedicineDialog(event.childId, event, dismiss, preferences.medicines, save)
        else EditEventDialog(event, dismiss, save)
    }
    deleteTarget?.let { event -> AlertDialog(
        onDismissRequest = { deleteTarget = null },
        title = { Text("Slet registrering?") },
        text = { Text("Registreringen fjernes fra oversigten, men bevares sikkert internt.") },
        confirmButton = { Button(onClick = { onDelete(event); deleteTarget = null }) { Text("Slet") } },
        dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Annuller") } },
    ) }
    if (customizeOpen) AllQuickActionsDialog(QuickAction.entries.filterNot(::visible).map { it.name }.toSet(), preferences.quickActionCategoryOrder, { customizeOpen = false }) { hidden, order -> onUpdateQuickActionCategoryOrder(order); onUpdateQuickActions(true, true, true, true); onUpdateHiddenQuickActions(hidden); customizeOpen = false }
    if (dashboardCustomizeOpen) DashboardMetricDialog(preferences.dashboardMetrics, { dashboardCustomizeOpen = false }) { metrics -> onUpdateDashboardMetrics(metrics); dashboardCustomizeOpen = false }
    if (sleepError) AlertDialog(
        onDismissRequest = { sleepError = false },
        title = { Text("Registreringen kan ikke startes") },
        text = { Text("Der findes allerede en aktiv tæller eller en overlappende søvnregistrering. Stop den aktive tæller eller ret tidsrummet først.") },
        confirmButton = { Button(onClick = { sleepError = false }) { Text("OK") } },
    )
}

private enum class EditorKind { StartBreast, SolidFood, Bottle, Diaper, ManualBreastfeeding, ManualPumping, StartPump, Sleep, HealthVisit, Vaccination, HealthNote, Weight, Height, HeadCircumference, Temperature, Medicine, StartActivity, Activity }

private data class OverviewItem(val label: String, val value: String, val event: CareEventEntity?)

@Composable
private fun SummaryMetric(label: String, value: String, detail: String? = null, modifier: Modifier = Modifier, count: Int = 0) {
    Card(
        modifier.heightIn(min = 108.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(if (count == 1) "1 registrering" else "$count registreringer", style = MaterialTheme.typography.labelSmall)
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
    DashboardMetric.Pumping -> "Pumpning"
    DashboardMetric.SolidFood -> "Fast føde"
    DashboardMetric.Medicine -> "Medicin"
    DashboardMetric.HealthVisits -> "Sundhedsbesøg"
    DashboardMetric.Vaccinations -> "Vaccinationer"
    DashboardMetric.Activities -> "Aktiviteter"
}

private fun DashboardMetric.matches(event: CareEventEntity): Boolean = when (this) {
    DashboardMetric.Pumping -> event.type == CareEventType.Pumping
    DashboardMetric.SolidFood -> event.type == CareEventType.SolidFood
    DashboardMetric.Medicine -> event.type == CareEventType.Activity && event.activityType == ActivityType.Medicine
    DashboardMetric.HealthVisits -> event.type == CareEventType.HealthVisit
    DashboardMetric.Vaccinations -> event.type == CareEventType.Vaccination
    DashboardMetric.Activities -> event.type == CareEventType.Activity && event.activityType !in setOf(ActivityType.Medicine, ActivityType.TummyTime)
    else -> event.type == CareEventType.Measurement && event.measurementType == measurementType()
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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vælg felter. Du kan tilføje flere eller fjerne dem igen.")
            selected.forEachIndexed { index, metric ->
                SelectionDropdown("Felt ${index + 1}", metric.displayLabel(), DashboardMetric.entries.map { it to it.displayLabel() }) { replacement ->
                    selected = selected.toMutableList().also { updated ->
                        val otherIndex = updated.indexOf(replacement)
                        if (otherIndex >= 0) updated[otherIndex] = metric
                        updated[index] = replacement
                    }
                }
                if (selected.size > 1) TextButton(onClick = { selected = selected.filterIndexed { position, _ -> position != index } }) { Text("Fjern felt ${index + 1}") }
            }
            if (selected.size < DashboardMetric.entries.size) TextButton(onClick = { selected = selected + DashboardMetric.entries.first { it !in selected } }) { Text("Tilføj felt") }
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BabySectionHeader(title, icon)
            content()
        }
    }
}

@Composable private fun QuickButton(text: String, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) = FilledTonalButton(modifier = modifier, enabled = enabled, onClick = onClick) { Text(text) }

@Composable
private fun ActiveTimerCard(event: CareEventEntity, onToggle: (CareEventEntity) -> Unit, onSwitch: (CareEventEntity) -> Unit, onStop: (CareEventEntity) -> Unit, onCancel: (CareEventEntity) -> Unit, keepAwake: Boolean, onKeepAwake: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.runningSince) { while (event.runningSince != null) { now = System.currentTimeMillis(); delay(1_000) } }
    val elapsed = event.elapsedSeconds(now)
    Card(modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(when (event.type) { CareEventType.Breastfeeding -> "Amning – ${sideLabel(event.activeSide)}"; CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"; CareEventType.Activity -> event.activityType?.displayLabel() ?: "Aktivitet"; else -> "Pumpning" }, style = MaterialTheme.typography.titleLarge)
        Text(formatDuration(elapsed), style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(keepAwake, onKeepAwake); Text("Hold skærmen tændt", style = MaterialTheme.typography.bodySmall); TextButton(onClick = { onCancel(event) }) { Text("Annuller") } }
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj flaske") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeFields(time) { time = it }
        SelectionDropdown("Indhold *", content.displayLabel(), BottleContent.entries.map { it to it.displayLabel() }) { content = it }
        OutlinedTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") })
        OutlinedTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(time, content, offered.toIntOrNull(), consumed.toIntOrNull(), notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun DiaperDialog(onDismiss: () -> Unit, onSave: (Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var type by remember { mutableStateOf(DiaperType.Wet) }; var color by remember { mutableStateOf<DiaperColor?>(null) }; var consistency by remember { mutableStateOf<DiaperConsistency?>(null) }; var observation by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj ble") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeFields(time) { time = it }; SelectionDropdown("Type *", type.displayLabel(), DiaperType.entries.map { it to it.displayLabel() }) { type = it }
        SelectionDropdown("Farve", color?.displayLabel() ?: "Ikke angivet", listOf<DiaperColor?>(null).map { it to "Ikke angivet" } + DiaperColor.entries.map { it as DiaperColor? to it.displayLabel() }) { color = it }
        SelectionDropdown("Konsistens", consistency?.displayLabel() ?: "Ikke angivet", listOf<DiaperConsistency?>(null).map { it to "Ikke angivet" } + DiaperConsistency.entries.map { it as DiaperConsistency? to it.displayLabel() }) { consistency = it }
        DiaperObservationField(observation) { observation = it }; OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(time, type, color, consistency, observation, notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun ManualTimerDialog(type: CareEventType, onDismiss: () -> Unit, onSave: (CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit) {
    var start by remember { mutableLongStateOf(System.currentTimeMillis() - 600_000) }; var end by remember { mutableLongStateOf(System.currentTimeMillis()) }; var side by remember { mutableStateOf(BreastSide.Left) }; var amount by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (type == CareEventType.Breastfeeding) "Manuel amning" else "Manuel pumpning") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Start *"); DateTimeFields(start) { start = it }; Text("Slut *"); DateTimeFields(end) { end = it }
        if (type == CareEventType.Breastfeeding) Row { BreastSide.entries.forEach { TextButton(onClick = { side = it }) { Text(if (side == it) "✓ ${sideLabel(it)}" else sideLabel(it)) } } }
        if (type == CareEventType.Pumping) OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Mængde (ml)") })
        OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(enabled = end >= start, onClick = { onSave(type, start, end, if (type == CareEventType.Breastfeeding) side else null, amount.toIntOrNull(), notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vælg aktiviteten. Tælleren starter, når du trykker Start.")
            SelectionDropdown("Aktivitet *", type.displayLabel(), choices.map { it to it.displayLabel() }) { type = it }
        } },
        confirmButton = { Button(onClick = { onStart(type) }) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
internal fun MedicineDialog(childId: String, existing: CareEventEntity?, onDismiss: () -> Unit, medicines: List<MedicinePlan> = emptyList(), onSave: (CareEventEntity) -> Unit) {
    val ownChoice = "Egen medicin"
    val plans = medicines.filter { it.childId == childId && it.active }
    var selectedPlan by remember { mutableStateOf(plans.firstOrNull { it.id == existing?.medicineId }) }
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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Registrér kun medicin, som barnet faktisk har fået.")
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            if (plans.isNotEmpty()) SelectionDropdown("Fra medicinkort", selectedPlan?.name ?: "Vælg medicin", plans.map { it to "${it.name} · ${it.dose}" }) { plan -> selectedPlan = plan; selection = ownChoice; customName = plan.name; dose = plan.dose }
            SelectionDropdown("Medicin *", selection, common.map { it to it }) { selection = it; selectedPlan = null }
            if (selection == ownChoice) OutlinedTextField(customName, { customName = it; if (it != selectedPlan?.name) selectedPlan = null }, label = { Text("Navn på medicin *") })
            OutlinedTextField(dose, { dose = it }, label = { Text("Dosis") })
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = name.isNotBlank(), onClick = {
            onSave((existing ?: CareEventEntity(childId = childId, type = CareEventType.Activity, startedAt = time)).copy(
                startedAt = time, endedAt = time, runningSince = null, activityType = ActivityType.Medicine,
                activityDurationSeconds = null, medicineId = selectedPlan?.id.orEmpty(), timeSpecified = timeSpecified, medicationName = name, medicationDose = dose, notes = notes,
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
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    if (event.type == CareEventType.SolidFood) { SolidFoodDialog(event.childId, event, onDismiss, onSave); return }
    if (event.type == CareEventType.Activity && event.activityType == ActivityType.Medicine) {
        MedicineDialog(event.childId, event, onDismiss, onSave = onSave); return
    }
    if (event.type == CareEventType.Sleep) {
        SleepDialog(event, onDismiss) { start, end, type, location, settling, awakenings, quality, notes ->
            val changed = start != event.startedAt || end != event.endedAt
            onSave(event.copy(startedAt = start, endedAt = end, runningSince = null,
                leftSeconds = if (changed) (end - start) / 1_000 else event.leftSeconds,
                rightSeconds = if (changed) 0 else event.rightSeconds,
                timerSegments = if (changed) "$start-$end" else event.timerSegments,
                sleepType = type, sleepLocation = location, settlingMethod = settling, awakenings = awakenings, sleepQuality = quality, notes = notes))
        }
        return
    }
    var draft by remember(event.id) { mutableStateOf(event) }
    var start by remember { mutableLongStateOf(event.startedAt) }
    var end by remember { mutableLongStateOf(event.endedAt ?: event.startedAt) }
    var offered by remember { mutableStateOf(event.amountOfferedMl?.toString().orEmpty()) }
    var consumed by remember { mutableStateOf(event.amountConsumedMl?.toString().orEmpty()) }
    var pumped by remember { mutableStateOf(event.pumpedAmountMl?.toString().orEmpty()) }
    var value by remember { mutableStateOf(event.measurementValue?.toString().orEmpty()) }
    val timed = event.type in setOf(CareEventType.Breastfeeding, CareEventType.Pumping, CareEventType.Activity)
    val valid = (!timed || end >= start) && (event.type != CareEventType.Measurement || (value.toDoubleOrNull()?.let { it.isFinite() && it > 0 } == true)) &&
        (offered.isBlank() || offered.toIntOrNull() != null) && (consumed.isBlank() || consumed.toIntOrNull() != null) && (pumped.isBlank() || pumped.toIntOrNull() != null)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rediger registrering") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (event.type == CareEventType.Measurement) DateAndOptionalTimeFields(start, draft.timeSpecified, { start = it }, { draft = draft.copy(timeSpecified = it) })
            else {
                if (timed) Text("Start")
                DateTimeFields(start) { start = it }
                if (timed) { Text("Slut"); DateTimeFields(end) { end = it } }
            }
            when (event.type) {
                CareEventType.Breastfeeding -> {
                    SelectionDropdown("Sidst brugte side", sideLabel(draft.activeSide), BreastSide.entries.map { it to sideLabel(it) }) { draft = draft.copy(activeSide = it) }
                    SelectionDropdown("Gener", draft.breastfeedingIssue?.displayLabel() ?: "Ingen angivet",
                        listOf<BreastfeedingIssue?>(null).map { it to "Ingen angivet" } + BreastfeedingIssue.entries.map { it as BreastfeedingIssue? to it.displayLabel() }) { draft = draft.copy(breastfeedingIssue = it) }
                }
                CareEventType.Bottle -> {
                    SelectionDropdown("Indhold *", draft.bottleContent.displayLabel(), BottleContent.entries.map { it to it.displayLabel() }) { draft = draft.copy(bottleContent = it) }
                    OutlinedTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") })
                    OutlinedTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") })
                }
                CareEventType.Pumping -> {
                    SelectionDropdown("Metode (valgfrit)", draft.pumpingMethod.ifBlank { "Ikke angivet" }, listOf("", "Hånd", "Maskine", "Manuel pumpe").map { it to it.ifBlank { "Ikke angivet" } }) { draft = draft.copy(pumpingMethod = it) }
                    OutlinedTextField(pumped, { pumped = it.filter(Char::isDigit) }, label = { Text("Pumpet (ml)") })
                }
                CareEventType.Diaper -> {
                    SelectionDropdown("Type *", draft.diaperType.displayLabel(), DiaperType.entries.map { it to it.displayLabel() }) { draft = draft.copy(diaperType = it) }
                    SelectionDropdown("Farve", draft.diaperColor?.displayLabel() ?: "Ikke angivet", listOf<DiaperColor?>(null).map { it to "Ikke angivet" } + DiaperColor.entries.map { it as DiaperColor? to it.displayLabel() }) { draft = draft.copy(diaperColor = it) }
                    SelectionDropdown("Konsistens", draft.diaperConsistency?.displayLabel() ?: "Ikke angivet", listOf<DiaperConsistency?>(null).map { it to "Ikke angivet" } + DiaperConsistency.entries.map { it as DiaperConsistency? to it.displayLabel() }) { draft = draft.copy(diaperConsistency = it) }
                    DiaperObservationField(draft.observation) { draft = draft.copy(observation = it) }
                }
                CareEventType.Measurement -> {
                    val type = draft.measurementType ?: MeasurementType.Weight
                    SelectionDropdown("Måling *", type.displayLabel(), MeasurementType.entries.map { it to it.displayLabel() }) { draft = draft.copy(measurementType = it, measurementUnit = it.defaultUnit()) }
                    OutlinedTextField(value, { value = it.replace(',', '.') }, label = { Text("Værdi (${type.defaultUnit()}) *") })
                }
                CareEventType.Activity -> SelectionDropdown("Type *", draft.activityType?.displayLabel() ?: "Andet", ActivityType.entries.filter { it != ActivityType.Medicine }.map { it to it.displayLabel() }) { draft = draft.copy(activityType = it) }
                else -> Unit
            }
            OutlinedTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Noter") })
        }
    }, confirmButton = { Button(enabled = valid, onClick = {
        val changed = start != event.startedAt || end != event.endedAt
        val duration = if (changed) (end - start).coerceAtLeast(0) / 1_000 else event.elapsedSeconds()
        val oldDuration = event.elapsedSeconds()
        val left = if (event.type == CareEventType.Breastfeeding) {
            if (oldDuration > 0) duration * event.leftSeconds / oldDuration else if (draft.activeSide == BreastSide.Right) 0 else duration
        } else duration
        onSave(draft.copy(startedAt = start, endedAt = if (timed) end else start, runningSince = null,
            leftSeconds = if (timed) left else event.leftSeconds, rightSeconds = if (timed) duration - left else event.rightSeconds,
            timerSegments = if (timed && changed) "$start-$end" else event.timerSegments,
            amountOfferedMl = offered.toIntOrNull(), amountConsumedMl = consumed.toIntOrNull(), pumpedAmountMl = pumped.toIntOrNull(),
            measurementValue = value.toDoubleOrNull(), activityDurationSeconds = if (event.type == CareEventType.Activity) duration else event.activityDurationSeconds))
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}
