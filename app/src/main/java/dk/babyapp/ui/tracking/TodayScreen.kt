package dk.babyapp.ui.tracking

import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.material.icons.outlined.SwapHoriz
import dk.babyapp.ui.theme.babyColors
import dk.babyapp.ui.theme.careCategory
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import dk.babyapp.domain.changeNippleShield
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
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
import dk.babyapp.data.preferences.QuickActionCategory
import dk.babyapp.data.medicine.MedicinePlan
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ShowChart
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
    onStartPumping: (String, String) -> Unit,
    onStartSleep: (String, SleepType, (Boolean) -> Unit) -> Unit,
    onStartActivity: (String, ActivityType, (Boolean) -> Unit) -> Unit,
    onToggleTimer: (CareEventEntity) -> Unit,
    onSwitchSide: (CareEventEntity) -> Unit,
    onStopTimer: (CareEventEntity, Int?, (CareEventEntity) -> Unit) -> Unit,
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String, String) -> Unit,
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
    scrollToTopRequest: Int = 0,
    onUpdateQuickActionCategoryOrder: (List<dk.babyapp.data.preferences.QuickActionCategory>) -> Unit = {},
    onUpdateDefaultNippleShield: (Boolean) -> Unit = {},
    onUpdateHeadSideStatistics: (dk.babyapp.data.preferences.HeadSideStatisticsSettings) -> Unit = {},
    onUpdateMeasurementChartSettings: (dk.babyapp.data.preferences.MeasurementChartSettings) -> Unit = {},
    onUpdateHiddenQuickActions: (Set<String>) -> Unit = {},
    onSaveTooth: (dk.babyapp.data.teeth.ToothRecord) -> Unit = {},
    onUpdateMedicines: (List<MedicinePlan>) -> Unit = {},
    medicines: List<MedicinePlan> = emptyList(),
    teeth: List<dk.babyapp.data.teeth.ToothRecord> = emptyList(),
    recentFrom: LocalDate? = null,
    history: @Composable () -> List<CareEventEntity> = { events },
) {
    val childEvents = childId?.let { id -> events.filter { it.childId == id && !it.isDraft && it.deletedAt == null } }.orEmpty()
    fun visible(action: QuickAction): Boolean = action.name !in preferences.hiddenQuickActions && when (action) {
        QuickAction.Breastfeeding -> preferences.showBreastfeedingQuickAction
        QuickAction.Bottle -> preferences.showBottleQuickAction
        QuickAction.Pumping -> preferences.showPumpingQuickAction
        QuickAction.Diaper -> preferences.showDiaperQuickAction
        else -> true
    }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(scrollToTopRequest) { if (scrollToTopRequest > 0) listState.scrollToItem(0) }
    val today = LocalDate.now()
    var overviewDate by remember(childId) { mutableStateOf(today) }
    var dialog by remember { mutableStateOf<EditorKind?>(null) }
    var selectedDiaperType by remember { mutableStateOf(DiaperType.Wet) }
    var deleteTarget by remember { mutableStateOf<CareEventEntity?>(null) }
    var customizeOpen by remember { mutableStateOf(false) }
    var dashboardCustomizeOpen by remember { mutableStateOf(false) }
    var teethOpen by remember(childId) { mutableStateOf(false) }
    var headSideOpen by remember(childId) { mutableStateOf(false) }
    var chartsOpen by remember { mutableStateOf(false) }
    var manualActivityType by remember { mutableStateOf(ActivityType.Other) }
    var manualMenuOpen by remember { mutableStateOf(false) }
    var sleepError by remember { mutableStateOf(false) }
    var medicineCardOpen by remember { mutableStateOf(false) }
    // [events] only covers recent days; the whole history is loaded while an older day or a chart is shown.
    val historyEvents = if (recentFrom != null && (overviewDate <= recentFrom || headSideOpen || chartsOpen)) history() else null
    val allChildEvents = historyEvents?.let { all -> all.filter { it.childId == childId && !it.isDraft && it.deletedAt == null } } ?: childEvents

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 8.dp, bottom = contentPadding.calculateBottomPadding() + 88.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
        if (overdueDueDate != null) item {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
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
            DailyOverview(
                dk.babyapp.domain.eventsForDay(allChildEvents, overviewDate), preferences.dashboardMetrics, overviewDate, onCustomize = { dashboardCustomizeOpen = true },
                onPrevious = { overviewDate = overviewDate.minusDays(1) },
                onNext = { overviewDate = overviewDate.plusDays(1) }.takeIf { overviewDate < today },
                onBackToToday = { overviewDate = today }.takeIf { overviewDate != today },
            )
        }
        item {
            BabySectionHeader("Hurtig registrering", actionIcon = Icons.Outlined.Tune, actionDescription = "Tilpas hurtig registrering", onAction = { customizeOpen = true })
        }
        orderedQuickActionCategories(preferences.quickActionCategoryOrder).forEach { category ->
            val actions = QuickAction.entries.filter { visible(it) && it.category == category }
            if (actions.isEmpty()) return@forEach
            item {
                QuickActionCard(category.careCategory.icon, category.label, MaterialTheme.babyColors.category(category.careCategory).accent, onChart = when (category) { QuickActionCategory.Measurements -> ({ chartsOpen = true }); QuickActionCategory.Sleep -> ({ headSideOpen = true }); else -> null }) {
                    fun canStart(type: CareEventType) = childId != null && dk.babyapp.domain.canStartTimer(childEvents, childId, type)
                    val buttons = actions.filterNot { it == QuickAction.HeadSide }.flatMap { action ->
                        when (action) {
                            QuickAction.Breastfeeding -> BreastSide.entries.map { side ->
                                QuickButtonSpec(sideLabel(side).replaceFirstChar { it.uppercase() }, canStart(CareEventType.Breastfeeding)) { childId?.let { onStartBreastfeeding(it, side) } }
                            }
                            QuickAction.Diaper -> DiaperType.entries.map { type ->
                                QuickButtonSpec(type.displayLabel(), childId != null) { selectedDiaperType = type; dialog = EditorKind.Diaper }
                            }
                            else -> {
                                val timerType = when (action) {
                                    QuickAction.Nap, QuickAction.Night -> CareEventType.Sleep
                                    QuickAction.TummyTime, QuickAction.Bath, QuickAction.Play, QuickAction.OutdoorTime, QuickAction.Activity -> CareEventType.Activity
                                    else -> null
                                }
                                listOf(QuickButtonSpec(action.label, if (timerType == null) childId != null else canStart(timerType)) {
                                    val id = childId ?: return@QuickButtonSpec
                                    when (action) {
                                        QuickAction.Pumping -> dialog = EditorKind.ManualPumping
                                        QuickAction.Nap -> onStartSleep(id, SleepType.Nap) { sleepError = !it }
                                        QuickAction.Night -> onStartSleep(id, SleepType.Night) { sleepError = !it }
                                        QuickAction.TummyTime -> onStartActivity(id, ActivityType.TummyTime) { sleepError = !it }
                                        QuickAction.Bath -> onStartActivity(id, ActivityType.Bath) { sleepError = !it }
                                        QuickAction.Play -> onStartActivity(id, ActivityType.Play) { sleepError = !it }
                                        QuickAction.OutdoorTime -> onStartActivity(id, ActivityType.OutdoorTime) { sleepError = !it }
                                        QuickAction.Activity -> onStartActivity(id, ActivityType.Other) { sleepError = !it }
                                        else -> dialog = EditorKind.valueOf(action.name)
                                    }
                                })
                            }
                        }
                    }
                    if (category == QuickActionCategory.Sleep && QuickAction.HeadSide in actions) {
                        // Favourite side and sleep side by side, each with its small heading.
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.weight(1f)) { HeadSideQuickButtons(childEvents, childId, onSave = onSaveHealthRecord) }
                            if (buttons.isNotEmpty()) Column(Modifier.weight(1f)) {
                                Text("Søvn", style = MaterialTheme.typography.labelMedium)
                                QuickButtonRows(buttons, columns = 2)
                            }
                        }
                    } else {
                        if (category == QuickActionCategory.Sleep) Text("Søvn", style = MaterialTheme.typography.labelMedium)
                        // Four to a row when the labels are short, otherwise two, so long words are never cut.
                        QuickButtonRows(buttons, columns = if (buttons.all { it.label.length <= 10 }) 4 else 2)
                    }
                    if (category == QuickActionCategory.Health) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { teethOpen = true }, enabled = childId != null) { Text("Tænder") }
                        TextButton(onClick = { medicineCardOpen = true }, enabled = childId != null) { Text("Åbn medicinkort") }
                    }
                }
            }
        }

        }
        if (childId != null) FloatingActionButton(onClick = { manualMenuOpen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp)) { Icon(Icons.Outlined.Add, "Manuel registrering") }
    }

    if (headSideOpen && childId != null) HeadSideStatisticsDialog(childId, historyEvents ?: events, preferences.headSideStatistics, onUpdateHeadSideStatistics) { headSideOpen = false }
    if (chartsOpen && childId != null) MeasurementChartsDialog(historyEvents ?: events, childId, preferences.measurementChartSettings, onUpdateMeasurementChartSettings) { chartsOpen = false }
    if (manualMenuOpen) ManualRegistrationMenu(
        listOf("Amning" to EditorKind.ManualBreastfeeding, "Flaske" to EditorKind.Bottle, "Pumpning" to EditorKind.ManualPumping,
            "Ble" to EditorKind.Diaper, "Søvn" to EditorKind.Sleep, "Vægt" to EditorKind.Weight, "Højde" to EditorKind.Height, "Hovedomkreds" to EditorKind.HeadCircumference, "Temperatur" to EditorKind.Temperature, "Fast føde" to EditorKind.SolidFood,
            "Medicin" to EditorKind.Medicine, "Sundhedsbesøg" to EditorKind.HealthVisit, "Vaccination" to EditorKind.Vaccination,
            "Helbredsnotat" to EditorKind.HealthNote, "Mavetid" to EditorKind.Activity, "Bad" to EditorKind.Activity, "Leg" to EditorKind.Activity, "Udetid" to EditorKind.Activity, "Andet" to EditorKind.Activity).map { (label, kind) -> label to { manualActivityType = manualActivityTypeFor(label); manualMenuOpen = false; dialog = kind } },
        { manualMenuOpen = false })
    when (dialog) {
        EditorKind.StartBreast -> AlertDialog(onDismissRequest = { dialog = null }, title = { Text("Start amning") },
            text = { Row { BreastSide.entries.forEach { side -> TextButton(onClick = { childId?.let { onStartBreastfeeding(it, side) }; dialog = null }) { Text(sideLabel(side).replaceFirstChar { it.uppercase() }) } } } },
            confirmButton = {}, dismissButton = { TextButton(onClick = { dialog = null }) { Text("Annuller") } })
        EditorKind.Bottle -> BottleDialog(onDismiss = { dialog = null }) { time, content, offered, consumed, notes, ratio -> childId?.let { onAddBottle(it, time, content, offered, consumed, notes, ratio) }; dialog = null }
        EditorKind.Diaper -> DiaperDialog(initialType = selectedDiaperType, onDismiss = { dialog = null; selectedDiaperType = DiaperType.Wet }) { time, type, color, consistency, observation, notes -> childId?.let { onAddDiaper(it, time, type, color, consistency, observation, notes) {} }; dialog = null; selectedDiaperType = DiaperType.Wet }
        EditorKind.ManualBreastfeeding -> childId?.let { id ->
            val now = remember { System.currentTimeMillis() }
            EditEventDialog(remember { CareEventEntity(childId = id, type = CareEventType.Breastfeeding, startedAt = now - 600_000, endedAt = now, leftSeconds = 600, activeSide = BreastSide.Left, timerSegments = "${now - 600_000}-$now", timerSegmentSides = "Left") }, { dialog = null }) { onSaveHealthRecord(it); dialog = null }
        }
        EditorKind.ManualPumping -> childId?.let { id -> PumpingDialog(id, preferences.lastPumpingMethod, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
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
        EditorKind.Medicine -> childId?.let { id -> MedicineDialog(id, null, { dialog = null }, medicines) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.HealthNote -> childId?.let { id -> HealthNoteDialog(id, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.StartActivity -> StartActivityDialog({ dialog = null }) { type -> childId?.let { onStartActivity(it, type) { success -> sleepError = !success } }; dialog = null }
        EditorKind.Activity -> childId?.let { id -> ActivityDialog(manualActivityType, { dialog = null }) { start, end, type, notes -> onAddActivity(id, start, end, type, notes); dialog = null } }
        EditorKind.SolidFood -> childId?.let { id -> SolidFoodDialog(id, null, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        null -> Unit
    }
    if (teethOpen && childId != null) TeethDialog(childId, teeth, onSaveTooth) { teethOpen = false }
    if (medicineCardOpen && childId != null) MedicineCardDialog(childId, medicines, onUpdateMedicines, { medicineCardOpen = false })
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
        text = { Text("Der kan højst være to tællere og én af hver type. Der kan også være overlappende søvn eller en amning, som er ændret imens. Kontrollér tiderne, eller luk redigeringen og åbn den igen.") },
        confirmButton = { Button(onClick = { sleepError = false }) { Text("OK") } },
    )
}

private enum class EditorKind { StartBreast, SolidFood, Bottle, Diaper, ManualBreastfeeding, ManualPumping, Sleep, HealthVisit, Vaccination, HealthNote, Weight, Height, HeadCircumference, Temperature, Medicine, StartActivity, Activity }

private data class OverviewItem(val label: String, val value: String, val event: CareEventEntity?)

@Composable
private fun SummaryMetric(label: String, value: String, detail: String? = null, modifier: Modifier = Modifier, count: Int = 0, iconValues: List<Triple<Int, String, String>>? = null, category: dk.babyapp.ui.theme.CareCategory? = null) {
    val accent = category?.let { MaterialTheme.babyColors.category(it).accent } ?: MaterialTheme.colorScheme.primary
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                if (category != null) Icon(category.icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (iconValues == null) Text(value, style = MaterialTheme.typography.titleMedium)
            else androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                iconValues.forEach { (icon, description, total) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.ui.res.painterResource(icon), description, Modifier.height(18.dp), tint = accent)
                        Text(total, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Text(
                (if (count == 1) "1 registrering" else "$count registreringer") + " · sidst ${detail ?: "-"}",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
            )
        }
    }
}

private val DashboardMetric.careCategory get() = when (this) {
    DashboardMetric.Feeding, DashboardMetric.Pumping -> dk.babyapp.ui.theme.CareCategory.Feeding
    DashboardMetric.Diapers -> dk.babyapp.ui.theme.CareCategory.Diaper
    DashboardMetric.Sleep, DashboardMetric.HeadSide -> dk.babyapp.ui.theme.CareCategory.Sleep
    DashboardMetric.Weight, DashboardMetric.Height, DashboardMetric.HeadCircumference -> dk.babyapp.ui.theme.CareCategory.Measurement
    DashboardMetric.Temperature, DashboardMetric.Medicine, DashboardMetric.HealthVisits, DashboardMetric.Vaccinations -> dk.babyapp.ui.theme.CareCategory.Health
    DashboardMetric.SolidFood -> dk.babyapp.ui.theme.CareCategory.SolidFood
    DashboardMetric.TummyTime, DashboardMetric.Activities -> dk.babyapp.ui.theme.CareCategory.Misc
}

private fun DashboardMetric.displayLabel() = when (this) {
    DashboardMetric.HeadSide -> "Favoritside"
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
    DashboardMetric.HeadSide -> event.type == CareEventType.HeadSide
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
internal fun DashboardMetricDialog(current: List<DashboardMetric>, onDismiss: () -> Unit, onSave: (List<DashboardMetric>) -> Unit) {
    var selected by remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tilpas Dagens overblik") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vælg felter, og flyt dem op eller ned for at ændre rækkefølgen.")
            selected.forEachIndexed { index, metric ->
                androidx.compose.material3.OutlinedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectionDropdown("Felt ${index + 1}", metric.displayLabel(), DashboardMetric.entries.map { it to it.displayLabel() }) { replacement ->
                    selected = selected.toMutableList().also { updated ->
                        val otherIndex = updated.indexOf(replacement)
                        if (otherIndex >= 0) updated[otherIndex] = metric
                        updated[index] = replacement
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomizationIconButton("Flyt felt ${index + 1} op", Icons.Outlined.ArrowUpward, index > 0) { selected = selected.toMutableList().apply { add(index - 1, removeAt(index)) } }
                    CustomizationIconButton("Flyt felt ${index + 1} ned", Icons.Outlined.ArrowDownward, index < selected.lastIndex) { selected = selected.toMutableList().apply { add(index + 1, removeAt(index)) } }
                    CustomizationIconButton("Fjern felt ${index + 1}", Icons.Outlined.Close, selected.size > 1) { selected = selected.filterIndexed { position, _ -> position != index } }
                }
                } }
            }
            if (selected.size < DashboardMetric.entries.size) FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = { selected = selected + DashboardMetric.entries.first { it !in selected } }) { Icon(Icons.Outlined.Add, null); Text("Tilføj felt") }
        } },
        confirmButton = { Button(onClick = { onSave(selected) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, iconTint: androidx.compose.ui.graphics.Color, onChart: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (onChart != null) IconButton(onClick = onChart) { Icon(Icons.Outlined.ShowChart, if (title == "Søvn") "Åbn favoritside" else "Vis målegrafer") }
            }
            content()
        }
    }
}

private class QuickButtonSpec(val label: String, val enabled: Boolean, val onClick: () -> Unit)

@Composable
private fun QuickButtonRows(buttons: List<QuickButtonSpec>, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        buttons.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { button -> QuickButton(button.label, Modifier.weight(1f), button.enabled, button.onClick) }
                repeat(columns - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable internal fun QuickButton(text: String, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) =
    FilledTonalButton(modifier = modifier, enabled = enabled, onClick = onClick, contentPadding = PaddingValues(horizontal = 6.dp)) {
        androidx.compose.foundation.text.BasicText(
            text, maxLines = 1,
            style = MaterialTheme.typography.labelLarge.copy(color = androidx.compose.material3.LocalContentColor.current, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = MaterialTheme.typography.labelLarge.fontSize),
        )
    }

@Composable
internal fun ActiveTimerCard(event: CareEventEntity, onToggle: (CareEventEntity) -> Unit, onSwitch: (CareEventEntity) -> Unit, onStop: (CareEventEntity) -> Unit, onCancel: (CareEventEntity) -> Unit, onEdit: (CareEventEntity) -> Unit, onShield: (CareEventEntity, Boolean) -> Unit, keepAwake: Boolean, onKeepAwake: (Boolean) -> Unit, modifier: Modifier = Modifier, minimized: Boolean = true, onMinimizedChange: (Boolean) -> Unit = {}, childLabel: String? = null) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.runningSince) { while (event.runningSince != null) { now = System.currentTimeMillis(); delay(1_000) } }
    val elapsed = event.elapsedSeconds(now)
    val title = listOfNotNull(childLabel, when (event.type) { CareEventType.Breastfeeding -> "Amning – ${sideLabel(event.activeSide)}"; CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"; CareEventType.Activity -> event.activityType?.displayLabel() ?: "Aktivitet"; else -> "Pumpning" }).joinToString(" · ")
    val running = event.runningSince != null
    val category = MaterialTheme.babyColors.category(event.careCategory)
    val toggle: @Composable () -> Unit = {
        androidx.compose.material3.FilledTonalIconButton(onClick = { onToggle(event) }) { Icon(if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, if (running) "Pause" else "Fortsæt") }
    }
    val expandButton: @Composable () -> Unit = {
        IconButton(onClick = { onMinimizedChange(!minimized) }) { Icon(if (minimized) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (minimized) "Udvid tæller" else "Minimer tæller") }
    }
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        if (minimized) {
            // Compact: one line with the essentials, so the timer never covers much of the screen.
            Row(Modifier.fillMaxWidth().padding(start = 10.dp, end = 2.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(32.dp).background(category.container, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                    Icon(event.careCategory.icon, contentDescription = null, tint = category.accent, modifier = Modifier.size(18.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    Text(formatDuration(elapsed) + if (running) "" else " · pause", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                }
                if (event.type == CareEventType.Breastfeeding) IconButton(onClick = { onSwitch(event) }) { Icon(Icons.Outlined.SwapHoriz, "Skift side") }
                toggle()
                androidx.compose.material3.FilledIconButton(onClick = { onStop(event) }) { Icon(Icons.Outlined.Stop, "Stop") }
                expandButton()
            }
        } else Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                expandButton()
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatDuration(elapsed), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                Checkbox(keepAwake, onKeepAwake, modifier = Modifier.semantics { contentDescription = "Hold skærmen tændt" })
                Text("Hold skærmen\ntændt", style = MaterialTheme.typography.bodySmall)
            }
            if (event.type != CareEventType.Breastfeeding) TextButton(onClick = { onCancel(event) }) { Text("Annuller") }
            if (event.type == CareEventType.Breastfeeding) {
                NursingIntervalOverview(event, now)
                OutlinedButton(onClick = { onEdit(event) }) { Text("Ret intervaller") }
                Text("Venstre ${formatDuration(event.leftSeconds)}  •  Højre ${formatDuration(event.rightSeconds)}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                toggle()
                if (event.type == CareEventType.Breastfeeding) OutlinedButton(onClick = { onSwitch(event) }) { Text("Skift side") }
                Button(onClick = { onStop(event) }) { Icon(Icons.Outlined.Stop, null); Text("Stop") }
            }
        }
    }
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
            OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldIcon), modifier = Modifier.fillMaxWidth(), onClick = {
                DatePickerDialog(context, { _, year, month, day -> calendar.set(year, month, day); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
            }) { Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(value))) }
        }
        Column(Modifier.weight(1f)) {
            Text("Tid *", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldIcon), modifier = Modifier.fillMaxWidth(), onClick = {
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
        OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldIcon), modifier = Modifier.fillMaxWidth(), onClick = {
            DatePickerDialog(context, { _, year, month, day -> calendar.set(year, month, day); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }) { Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(value))) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(timeSpecified, onCheckedChange = onTimeSpecifiedChange)
            Text("Tilføj tidspunkt")
        }
        if (timeSpecified) {
            Text("Tid", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldIcon), modifier = Modifier.fillMaxWidth(), onClick = {
                TimePickerDialog(context, { _, hour, minute -> calendar.set(java.util.Calendar.HOUR_OF_DAY, hour); calendar.set(java.util.Calendar.MINUTE, minute); calendar.set(java.util.Calendar.SECOND, 0); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
            }) { Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value))) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun BottleDialog(onDismiss: () -> Unit, onSave: (Long, BottleContent, Int?, Int?, String, String) -> Unit) {
    var ratio by remember { mutableStateOf("") }
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var offered by remember { mutableStateOf("") }; var consumed by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }; var content by remember { mutableStateOf(BottleContent.BreastMilk) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj flaske") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeFields(time) { time = it }
        SelectionDropdown("Indhold *", content.displayLabel(), BottleContent.entries.map { it to it.displayLabel() }) { content = it }
        if (content == BottleContent.Mixed) BottleRatioField(ratio) { ratio = it }
        RegistrationTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        RegistrationTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(time, content, listOfNotNull(offered.toIntOrNull(), consumed.toIntOrNull()).maxOrNull(), consumed.toIntOrNull(), notes, if (content == BottleContent.Mixed) ratio else "") }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun DiaperDialog(onDismiss: () -> Unit, initialType: DiaperType = DiaperType.Wet, onSave: (Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var type by remember { mutableStateOf(initialType) }; var color by remember { mutableStateOf<DiaperColor?>(null) }; var consistency by remember { mutableStateOf<DiaperConsistency?>(null) }; var observation by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tilføj ble") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DateTimeFields(time) { time = it }; SelectionDropdown("Type *", type.displayLabel(), DiaperType.entries.map { it to it.displayLabel() }) { type = it }
        SelectionDropdown("Farve", color?.displayLabel() ?: "Ikke angivet", listOf<DiaperColor?>(null).map { it to "Ikke angivet" } + DiaperColor.entries.map { it as DiaperColor? to it.displayLabel() }) { color = it }
        SelectionDropdown("Konsistens", consistency?.displayLabel() ?: "Ikke angivet", listOf<DiaperConsistency?>(null).map { it to "Ikke angivet" } + DiaperConsistency.entries.map { it as DiaperConsistency? to it.displayLabel() }) { consistency = it }
        DiaperObservationField(observation) { observation = it }; RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
    } }, confirmButton = { Button(onClick = { onSave(time, type, color, consistency, observation, notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun ManualTimerDialog(type: CareEventType, onDismiss: () -> Unit, onSave: (CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit) {
    var start by remember { mutableLongStateOf(System.currentTimeMillis() - 600_000) }; var end by remember { mutableLongStateOf(System.currentTimeMillis()) }; var side by remember { mutableStateOf(BreastSide.Left) }; var amount by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (type == CareEventType.Breastfeeding) "Manuel amning" else "Manuel pumpning") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Start *"); DateTimeFields(start) { start = it }; Text("Slut *"); DateTimeFields(end) { end = it }
        if (type == CareEventType.Breastfeeding) Row { BreastSide.entries.forEach { TextButton(onClick = { side = it }) { Text(if (side == it) "✓ ${sideLabel(it)}" else sideLabel(it)) } } }
        if (type == CareEventType.Pumping) RegistrationTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Mængde (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
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
            RegistrationTextField(location, { location = it }, label = { Text("Sovested") })
            RegistrationTextField(settling, { settling = it }, label = { Text("Puttemetode") })
            RegistrationTextField(awakenings, { awakenings = it.filter(Char::isDigit) }, label = { Text("Opvågninger") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            Text("Søvnkvalitet")
            Row { SleepQuality.entries.forEach { option -> TextButton(onClick = { quality = if (quality == option) null else option }) { Text(if (quality == option) "✓ ${option.displayLabel()}" else option.displayLabel()) } } }
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
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
    var unit by remember { mutableStateOf(if (initialType == MeasurementType.Weight) "g" else initialType.defaultUnit()) }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrér mål") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            SelectionDropdown("Måling *", type.displayLabel(), MeasurementType.entries.map { it to it.displayLabel() }) { if (type != it) { type = it; unit = if (it == MeasurementType.Weight) "g" else it.defaultUnit(); value = "" } }
            if (type == MeasurementType.Weight) WeightUnitSelector(unit, value) { newUnit, newValue -> unit = newUnit; value = newValue }
            RegistrationTextField(value, { value = it.replace(',', '.').filter { char -> char.isDigit() || char == '.' } }, label = { Text("Værdi ($unit) *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = value.toDoubleOrNull() != null, onClick = { onSave(time, timeSpecified, type, value.toDouble(), unit, notes) }) { Text("Gem") } },
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
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
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
    val plans = medicines.filter { it.childId == childId && it.active }
    var selectedPlan by remember { mutableStateOf(plans.firstOrNull { it.id == existing?.medicineId }) }
    var dose by remember { mutableStateOf(existing?.medicationDose.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var time by remember { mutableLongStateOf(existing?.startedAt ?: System.currentTimeMillis()) }
    var timeSpecified by remember { mutableStateOf(existing?.timeSpecified ?: false) }
    val name = selectedPlan?.name ?: existing?.medicationName.orEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Registrér medicin" else "Rediger medicin") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Registrér kun medicin, som barnet faktisk har fået.")
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            if (plans.isEmpty()) Text("Tilføj eller aktivér medicin på medicinkortet før registrering.")
            if (existing != null && selectedPlan == null) Text("Tidligere registrering: ${existing.medicationName}")
            if (plans.isNotEmpty()) SelectionDropdown("Fra medicinkort *", selectedPlan?.name ?: "Vælg medicin", plans.map { it to "${it.name} · ${it.dose}" }) { plan -> selectedPlan = plan; dose = plan.dose }
            RegistrationTextField(dose, { dose = it }, label = { Text("Dosis") })
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = name.isNotBlank() && (selectedPlan != null || existing != null), onClick = {
            onSave((existing ?: CareEventEntity(childId = childId, type = CareEventType.Activity, startedAt = time)).copy(
                startedAt = time, endedAt = time, runningSince = null, activityType = ActivityType.Medicine,
                activityDurationSeconds = null, medicineId = selectedPlan?.id ?: existing?.medicineId.orEmpty(), timeSpecified = timeSpecified, medicationName = name, medicationDose = dose, notes = notes,
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
            RegistrationTextField(title, { title = it }, label = { Text("Titel *") })
            RegistrationTextField(notes, { notes = it }, label = { Text("Observationer") })
        } },
        confirmButton = { Button(enabled = title.isNotBlank(), onClick = {
            onSave(CareEventEntity(childId = childId, type = CareEventType.HealthVisit, startedAt = time, endedAt = time, timeSpecified = timeSpecified, healthVisitType = dk.babyapp.data.tracking.HealthVisitType.Other, healthStatus = dk.babyapp.data.tracking.HealthRecordStatus.Completed, healthTitle = title.trim(), healthObservations = notes))
        }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable internal fun EditEventDialog(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    if (event.type == CareEventType.HeadSide) { HeadSideEditor(event, onDismiss, onSave); return }
    if (event.type == CareEventType.Breastfeeding) { NursingEditor(event, onDismiss, onSave); return }
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
    var unit by remember { mutableStateOf(event.measurementUnit.ifBlank { (event.measurementType ?: MeasurementType.Weight).defaultUnit() }) }
    val nursingHistory = event.type == CareEventType.Breastfeeding && event.timerSegments.isNotBlank()
    val timed = event.type in setOf(CareEventType.Breastfeeding, CareEventType.Activity) ||
        (event.type == CareEventType.Pumping && (event.endedAt ?: event.startedAt) > event.startedAt)
    val valid = (!timed || end >= start) && (event.type != CareEventType.Measurement || (value.toDoubleOrNull()?.let { it.isFinite() && it > 0 } == true)) &&
        (offered.isBlank() || offered.toIntOrNull() != null) && (consumed.isBlank() || consumed.toIntOrNull() != null) && (pumped.isBlank() || pumped.toIntOrNull() != null)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rediger registrering") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (event.type == CareEventType.Measurement) DateAndOptionalTimeFields(start, draft.timeSpecified, { start = it }, { draft = draft.copy(timeSpecified = it) })
            else if (nursingHistory) {
                Text("Tidsintervaller og sideskift bevares. Brug Fortsæt amning i Journal for at tilføje mere amning.")
            } else {
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
                    if (draft.bottleContent == BottleContent.Mixed) BottleRatioField(draft.bottleMixRatio) { draft = draft.copy(bottleMixRatio = it) }
                    RegistrationTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    RegistrationTextField(consumed, { consumed = it.filter(Char::isDigit) }, label = { Text("Spist (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
                CareEventType.Pumping -> {
                    SelectionDropdown("Pumpetype", draft.pumpingMethod.ifBlank { "Ikke angivet" }, listOf("", "Hånd", "Maskine", "Manuel pumpe").map { it to it.ifBlank { "Ikke angivet" } }) { draft = draft.copy(pumpingMethod = it) }
                    RegistrationTextField(pumped, { pumped = it.filter(Char::isDigit) }, label = { Text("Pumpet (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
                CareEventType.Diaper -> {
                    SelectionDropdown("Type *", draft.diaperType.displayLabel(), DiaperType.entries.map { it to it.displayLabel() }) { draft = draft.copy(diaperType = it) }
                    SelectionDropdown("Farve", draft.diaperColor?.displayLabel() ?: "Ikke angivet", listOf<DiaperColor?>(null).map { it to "Ikke angivet" } + DiaperColor.entries.map { it as DiaperColor? to it.displayLabel() }) { draft = draft.copy(diaperColor = it) }
                    SelectionDropdown("Konsistens", draft.diaperConsistency?.displayLabel() ?: "Ikke angivet", listOf<DiaperConsistency?>(null).map { it to "Ikke angivet" } + DiaperConsistency.entries.map { it as DiaperConsistency? to it.displayLabel() }) { draft = draft.copy(diaperConsistency = it) }
                    DiaperObservationField(draft.observation) { draft = draft.copy(observation = it) }
                }
                CareEventType.Measurement -> {
                    val type = draft.measurementType ?: MeasurementType.Weight
                    SelectionDropdown("Måling *", type.displayLabel(), MeasurementType.entries.map { it to it.displayLabel() }) { if (type != it) { unit = if (it == MeasurementType.Weight) "g" else it.defaultUnit(); draft = draft.copy(measurementType = it); value = "" } }
                    if (type == MeasurementType.Weight) WeightUnitSelector(unit, value) { newUnit, newValue -> unit = newUnit; value = newValue }
                    RegistrationTextField(value, { value = it.replace(',', '.') }, label = { Text("Værdi ($unit) *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
                }
                CareEventType.Activity -> SelectionDropdown("Type *", draft.activityType?.displayLabel() ?: "Andet", ActivityType.entries.filter { it != ActivityType.Medicine }.map { it to it.displayLabel() }) { draft = draft.copy(activityType = it) }
                else -> Unit
            }
            RegistrationTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Noter") })
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
            timerSegmentSides = if (timed && changed) draft.activeSide?.name.orEmpty() else event.timerSegmentSides,
            bottleMixRatio = if (draft.bottleContent == BottleContent.Mixed) draft.bottleMixRatio.trim() else "",
            amountOfferedMl = listOfNotNull(offered.toIntOrNull(), consumed.toIntOrNull()).maxOrNull(), amountConsumedMl = consumed.toIntOrNull(), pumpedAmountMl = pumped.toIntOrNull(),
            measurementValue = value.toDoubleOrNull(), measurementUnit = if (event.type == CareEventType.Measurement) unit else event.measurementUnit, activityDurationSeconds = if (event.type == CareEventType.Activity) duration else event.activityDurationSeconds))
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun DailyOverview(todayEvents: List<CareEventEntity>, metrics: List<DashboardMetric>, date: LocalDate, onCustomize: (() -> Unit)? = null, onPrevious: (() -> Unit)? = null, onNext: (() -> Unit)? = null, onBackToToday: (() -> Unit)? = null) {
            val breastMinutes = todayEvents.filter { it.type == CareEventType.Breastfeeding }.sumOf { it.elapsedSeconds() } / 60
            val lastNursing = dk.babyapp.domain.lastNursingInterval(todayEvents)
            val lastSide = lastNursing?.second
            val breastSummary = "$breastMinutes min"
            val bottleMl = todayEvents.filter { it.type == CareEventType.Bottle }.sumOf { it.amountConsumedMl ?: 0 }
            val diapers = todayEvents.filter { it.type == CareEventType.Diaper }
            val wetDiapers = diapers.count { it.diaperType == DiaperType.Wet || it.diaperType == DiaperType.Both }
            val dirtyDiapers = diapers.count { it.diaperType == DiaperType.Dirty || it.diaperType == DiaperType.Both }
            val sleepMinutes = todayEvents.filter { it.type == CareEventType.Sleep }.sumOf { it.elapsedSeconds() } / 60
            val tummyEvents = todayEvents.filter { it.type == CareEventType.Activity && it.activityType == ActivityType.TummyTime }
            val tummyMinutes = tummyEvents.sumOf { it.activityDurationSeconds ?: 0 } / 60
            val lastFeeding = todayEvents.filter { it.type == CareEventType.Breastfeeding || it.type == CareEventType.Bottle }.maxByOrNull { it.startedAt }
            val lastDiaper = todayEvents.filter { it.type == CareEventType.Diaper }.maxByOrNull { it.startedAt }
            val lastSleep = todayEvents.filter { it.type == CareEventType.Sleep }.maxByOrNull { it.startedAt }
            val overviewItems = metrics.map { metric ->
                when (metric) {
                    DashboardMetric.HeadSide -> {
                        val counts = dk.babyapp.domain.headSideTotals(todayEvents)
                        OverviewItem(metric.displayLabel(), "Venstre ${counts.left} · Højre ${counts.right}", todayEvents.filter { it.type == CareEventType.HeadSide }.maxByOrNull { it.startedAt })
                    }
                    DashboardMetric.Feeding -> OverviewItem(metric.displayLabel(), if (lastFeeding == null) "-" else listOfNotNull("$breastMinutes min".takeIf { todayEvents.any { it.type == CareEventType.Breastfeeding } }, "$bottleMl ml".takeIf { todayEvents.any { it.type == CareEventType.Bottle } }).joinToString(" · "), lastFeeding)
                    DashboardMetric.Diapers -> OverviewItem(metric.displayLabel(), "", lastDiaper)
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
                Column(Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Day browsing sits in the heading row so the overview starts right away.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (onPrevious != null) IconButton(onClick = onPrevious) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Forrige dags overblik") }
                        Column(Modifier.weight(1f).padding(start = if (onPrevious == null) 10.dp else 0.dp)) {
                            Text(if (date == LocalDate.now()) "Dagens overblik" else date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE d. MMM", java.util.Locale.forLanguageTag("da"))).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                            if (onBackToToday != null) TextButton(onClick = onBackToToday, contentPadding = PaddingValues(horizontal = 0.dp)) { Text("Tilbage til i dag", style = MaterialTheme.typography.labelMedium) }
                        }
                        if (onPrevious != null) IconButton(onClick = { onNext?.invoke() }, enabled = onNext != null) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Næste dags overblik") }
                        if (onCustomize != null) IconButton(onClick = onCustomize) { Icon(Icons.Outlined.Tune, "Tilpas Dagens overblik") }
                    }
                    Column(Modifier.padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    overviewItems.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEachIndexed { columnIndex, item ->
                                val metric = metrics[rowIndex * 2 + columnIndex]
                                val count = todayEvents.count { event -> when (metric) {
                                    DashboardMetric.Feeding -> event.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle)
                                    DashboardMetric.Diapers -> event.type == CareEventType.Diaper
                                    DashboardMetric.Sleep -> event.type == CareEventType.Sleep
                                    DashboardMetric.TummyTime -> event.type == CareEventType.Activity && event.activityType == ActivityType.TummyTime
                                    else -> metric.matches(event)
                                } }
                                val lastTime = if (metric == DashboardMetric.Feeding && lastNursing != null) lastNursing.first else item.event?.startedAt
                                val lastDetail = lastTime?.let { DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it)) }?.let { time ->
                                    time + if (metric == DashboardMetric.Feeding) when (lastSide) { BreastSide.Left -> " - V"; BreastSide.Right -> " - H"; null -> "" } else ""
                                }
                                SummaryMetric(item.label, item.value, lastDetail, Modifier.weight(1f).fillMaxHeight(), count, category = metric.careCategory, iconValues = when (metric) {
                                    DashboardMetric.Diapers -> listOf(Triple(dk.babyapp.R.drawable.ic_diaper_wet, "Våd", wetDiapers.toString()), Triple(dk.babyapp.R.drawable.ic_diaper_stool, "Afføring", dirtyDiapers.toString()))
                                    DashboardMetric.Feeding -> listOf(Triple(dk.babyapp.R.drawable.ic_feeding_bottle, "Flaske", "$bottleMl ml"), Triple(dk.babyapp.R.drawable.ic_feeding_breast, "Amning", breastSummary))
                                    else -> null
                                })
                            }
                            if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                        }
                    }
                    }
                }
            }
}
