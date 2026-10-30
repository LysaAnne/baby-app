package dk.babyapp.ui.tracking

import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.CalendarToday
import dk.babyapp.domain.accrueUntil
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
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.filled.Lightbulb
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
import dk.babyapp.data.tracking.HealthRecordStatus
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
    child: dk.babyapp.data.profile.ChildProfile? = null,
    events: List<CareEventEntity>,
    contentPadding: PaddingValues,
    preferences: AppPreferences,
    overdueDueDate: LocalDate? = null,
    careProviders: List<CareProvider> = emptyList(),
    onOpenFamily: () -> Unit = {},
    onStartBreastfeeding: (String, BreastSide) -> Unit,
    onStartPumping: (String, String) -> Unit,
    onStartSleep: (String, SleepType?, (Boolean) -> Unit) -> Unit,
    onStartActivity: (String, ActivityType, (Boolean) -> Unit) -> Unit,
    onToggleTimer: (CareEventEntity) -> Unit,
    onSwitchSide: (CareEventEntity) -> Unit,
    onStopTimer: (CareEventEntity, Int?, (CareEventEntity) -> Unit) -> Unit,
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String, String) -> Unit,
    onAddDiaper: (String, Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String, (CareEventEntity) -> Unit) -> Unit,
    onOpenTimeline: () -> Unit,
    onSaveHealthRecord: (CareEventEntity) -> Unit,
    onAddManualTimer: (String, CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit,
    onAddSleep: (String, Long, Long, SleepType?, String, String, Int?, SleepQuality?, String, (Boolean) -> Unit) -> Unit,
    onAddMeasurement: (String, Long, Boolean, MeasurementType, Double, String, String) -> Unit,
    onAddActivity: (String, Long, Long, ActivityType, String, String) -> Unit,
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
    onUpdateHealthQuestions: (List<dk.babyapp.data.preferences.HealthQuestion>) -> Unit = {},
    onUpdateBoardNotes: (List<dk.babyapp.data.preferences.BoardNote>) -> Unit = {},
    onUpdateInsightPeriod: (Int) -> Unit = {},
    onUpdateInsightRange: (String, String) -> Unit = { _, _ -> },
    onUpdateSimpleQuickRegistration: (Boolean) -> Unit = {},
    onUpdateShowBulletinBoard: (Boolean) -> Unit = {},
    onUpdateShowDevelopmentStrip: (Boolean) -> Unit = {},
    onUpdateCollapsedCards: (Set<String>) -> Unit = {},
    onUpdateDevelopmentChecks: (List<dk.babyapp.data.preferences.DevelopmentCheck>) -> Unit = {},
    onUpdateChild: (dk.babyapp.data.profile.ChildProfile) -> Unit = {},
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
    var summaryOpen by remember { mutableStateOf(false) }
    var cardSettingsFor by remember { mutableStateOf<QuickActionCategory?>(null) }
    var questionsOpen by remember { mutableStateOf(false) }
    var teethOpen by remember(childId) { mutableStateOf(false) }
    var headSideOpen by remember(childId) { mutableStateOf(false) }
    var chartsOpen by remember { mutableStateOf(false) }
    var manualActivityType by remember { mutableStateOf(ActivityType.Other) }
    var manualMenuOpen by remember { mutableStateOf(false) }
    var sleepError by remember { mutableStateOf(false) }
    var medicineCardOpen by remember { mutableStateOf(false) }
    // A graph opened from a card is shown over I dag, so back returns here.
    var insightOpen by remember { mutableStateOf<dk.babyapp.data.preferences.InsightCard?>(null) }
    var hundredFoodsOpen by remember { mutableStateOf(false) }
    var headSidePick by remember { mutableStateOf(false) }
    var developmentOpen by remember { mutableStateOf(false) }
    fun collapse(card: String, collapsed: Boolean) = onUpdateCollapsedCards(if (collapsed) preferences.collapsedCards + card else preferences.collapsedCards - card)
    var growthPick by remember { mutableStateOf<List<QuickAction>>(emptyList()) }
    // [events] only covers recent days; the whole history is loaded while an older day or a chart is shown.
    val historyEvents = if (recentFrom != null && (overviewDate <= recentFrom || headSideOpen || chartsOpen || insightOpen != null || hundredFoodsOpen || dialog == EditorKind.SolidFood)) history() else null
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
                onOpenSummary = { summaryOpen = true },
                collapsed = "today" in preferences.collapsedCards, onCollapse = { collapse("today", it) },
            )
        }
        if (childId != null && preferences.showBulletinBoard) item {
            BulletinBoard(child, childId, historyEvents ?: events, preferences.boardNotes, onUpdateBoardNotes, onSaveHealthRecord, "events" in preferences.collapsedCards) { collapse("events", it) }
        }
        if (child != null && preferences.showDevelopmentStrip) item {
            dk.babyapp.ui.more.DevelopmentStripCard(child, "development" in preferences.collapsedCards, { collapse("development", it) }) { developmentOpen = true }
        }
        item {
            BabySectionHeader("Hurtig registrering", actionIcon = Icons.Outlined.Tune, actionDescription = "Tilpas hurtig registrering", onAction = { customizeOpen = true })
        }
        orderedQuickActionCategories(preferences.quickActionCategoryOrder).forEach { category ->
            val actions = QuickAction.entries.filter { visible(it) && it.category == category }
            if (actions.isEmpty()) return@forEach
            item {
                val insightLinks = category.insightCards.map { (label, card) -> label to { insightOpen = card } }
                QuickCategory(preferences.simpleQuickRegistration, category.careCategory, category.label, insightLinks, onSettings = { cardSettingsFor = category }) {
                    fun canStart(type: CareEventType) = childId != null && dk.babyapp.domain.canStartTimer(childEvents, childId, type)
                    // Weight, height and head size share one Vækst button with a small window, like Amning and Liggeside.
                    val growth = listOf(QuickAction.Weight, QuickAction.Height, QuickAction.HeadCircumference).filter { it in actions }
                    val buttons = actions.flatMap { action ->
                        when (action) {
                            // One Amning button; the side is picked in a small window.
                            QuickAction.Breastfeeding -> listOf(QuickButtonSpec("Amning", canStart(CareEventType.Breastfeeding)) { dialog = EditorKind.StartBreast })
                            // Nap and night sleep are one Søvn button.
                            QuickAction.Night -> if (QuickAction.Nap in actions) emptyList() else listOf(QuickButtonSpec("Søvn", canStart(CareEventType.Sleep)) { childId?.let { id -> onStartSleep(id, null) { sleepError = !it } } })
                            QuickAction.Nap -> listOf(QuickButtonSpec("Søvn", canStart(CareEventType.Sleep)) { childId?.let { id -> onStartSleep(id, null) { sleepError = !it } } })
                            QuickAction.HeadSide -> listOf(QuickButtonSpec("Liggeside", childId != null) { headSidePick = true })
                            QuickAction.Weight, QuickAction.Height, QuickAction.HeadCircumference ->
                                if (growth.size > 1) { if (action == growth.first()) listOf(QuickButtonSpec("Vækst", childId != null) { growthPick = growth }) else emptyList() }
                                else listOf(QuickButtonSpec(action.label, childId != null) { dialog = EditorKind.valueOf(action.name) })
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
                                        QuickAction.Nap -> onStartSleep(id, null) { sleepError = !it }
                                        QuickAction.Night -> onStartSleep(id, null) { sleepError = !it }
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
                    // Teeth and questions sit in the same grid as the other health buttons; status lines follow below.
                    val healthExtras = if (category == QuickActionCategory.Health) {
                        val toothCount = teeth.count { it.childId == childId && it.lost == null }
                        val questionCount = preferences.healthQuestions.count { it.childId == childId }
                        listOf(
                            QuickButtonSpec(if (toothCount == 0) "Tænder" else "Tænder · $toothCount", childId != null) { teethOpen = true },
                            QuickButtonSpec(if (questionCount == 0) "Spørgsmål" else "Spørgsmål · $questionCount", childId != null) { questionsOpen = true },
                        )
                    } else emptyList()
                    // Health in a fixed order; the old Helbredsnotat button is gone (existing notes stay in the journal).
                    val shown = if (category == QuickActionCategory.Health) {
                        val all = (buttons + healthExtras).filterNot { it.label == QuickAction.HealthNote.label }
                        val order = listOf(QuickAction.HealthVisit.label, healthExtras[1].label, QuickAction.Medicine.label, QuickAction.Vaccination.label, healthExtras[0].label)
                        order.mapNotNull { label -> all.firstOrNull { it.label == label } } + all.filter { it.label !in order }
                    } else buttons
                    QuickButtonRows(shown, columns = 2)
                }
            }
        }

        }
        if (childId != null) FloatingActionButton(onClick = { manualMenuOpen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp)) { Icon(Icons.Outlined.Add, "Manuel registrering") }
    }

    if (childId != null) InsightDetailFor(
        insightOpen, childId, child, historyEvents ?: events,
        InsightPeriodState(preferences.insightPeriodDays, preferences.insightFrom, preferences.insightThrough), onUpdateInsightPeriod,
        { from, through -> onUpdateInsightRange(from, through); onUpdateInsightPeriod(-1) },
        preferences.measurementChartSettings, onUpdateMeasurementChartSettings, preferences.headSideStatistics, onUpdateHeadSideStatistics,
    ) { insightOpen = null }
    if (headSideOpen && childId != null) HeadSideStatisticsDialog(childId, historyEvents ?: events, preferences.headSideStatistics, onUpdateHeadSideStatistics) { headSideOpen = false }
    if (chartsOpen && childId != null) MeasurementChartsDialog(historyEvents ?: events, childId, preferences.measurementChartSettings, onUpdateMeasurementChartSettings, child) { chartsOpen = false }
    if (manualMenuOpen) ManualRegistrationMenu(
        listOf("Amning" to EditorKind.ManualBreastfeeding, "Flaske" to EditorKind.Bottle, "Pumpning" to EditorKind.ManualPumping,
            "Ble" to EditorKind.Diaper, "Søvn" to EditorKind.Sleep, "Vægt" to EditorKind.Weight, "Højde" to EditorKind.Height, "Hovedomkreds" to EditorKind.HeadCircumference, "Temperatur" to EditorKind.Temperature, "Fast føde" to EditorKind.SolidFood,
            "Medicin" to EditorKind.Medicine, "Sundhedsbesøg" to EditorKind.HealthVisit, "Vaccination" to EditorKind.Vaccination,
            "Mavetid" to EditorKind.Activity, "Bad" to EditorKind.Activity, "Leg" to EditorKind.Activity, "Udetid" to EditorKind.Activity, "Andet" to EditorKind.Activity).map { (label, kind) -> label to { manualActivityType = manualActivityTypeFor(label); manualMenuOpen = false; dialog = kind } },
        { manualMenuOpen = false })
    when (dialog) {
        EditorKind.StartBreast -> QuickChoiceDialog("Start amning", dk.babyapp.ui.theme.CareCategory.Feeding,
            BreastSide.entries.map { side -> QuickChoice(sideLabel(side).replaceFirstChar { it.uppercase() }) { childId?.let { onStartBreastfeeding(it, side) } } }) { if (dialog == EditorKind.StartBreast) dialog = null }
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
        EditorKind.HealthVisit -> childId?.let { id -> HealthRecordDialog(id, false, careProviders, onDismiss = { dialog = null }, openQuestions = preferences.healthQuestions,
            onQuestionsAttached = { used -> onUpdateHealthQuestions(preferences.healthQuestions - used.toSet()) }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.Vaccination -> childId?.let { id -> HealthRecordDialog(id, true, careProviders, onDismiss = { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.Weight -> childId?.let { id -> MeasurementDialog(MeasurementType.Weight, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.Height -> childId?.let { id -> MeasurementDialog(MeasurementType.Height, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.HeadCircumference -> childId?.let { id -> MeasurementDialog(MeasurementType.HeadCircumference, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.Temperature -> childId?.let { id -> MeasurementDialog(MeasurementType.Temperature, { dialog = null }) { time, specified, type, value, unit, notes -> onAddMeasurement(id, time, specified, type, value, unit, notes); dialog = null } }
        EditorKind.Medicine -> childId?.let { id -> MedicineDialog(id, null, { dialog = null }, medicines, onOpenMedicineCard = { medicineCardOpen = true }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.HealthNote -> childId?.let { id -> HealthNoteDialog(id, { dialog = null }) { onSaveHealthRecord(it); dialog = null } }
        EditorKind.StartActivity -> StartActivityDialog({ dialog = null }) { type -> childId?.let { onStartActivity(it, type) { success -> sleepError = !success } }; dialog = null }
        EditorKind.Activity -> childId?.let { id -> ActivityDialog(manualActivityType, { dialog = null }) { start, end, type, notes, name -> onAddActivity(id, start, end, type, notes, name); dialog = null } }
        EditorKind.SolidFood -> childId?.let { id -> SolidFoodDialog(id, null, { dialog = null }, historyEvents ?: events, onOpenList = { hundredFoodsOpen = true }) { onSaveHealthRecord(it); dialog = null } }
        null -> Unit
    }
    if (developmentOpen && child != null) dk.babyapp.ui.more.DevelopmentEntry(child, preferences.developmentChecks, onUpdateDevelopmentChecks, onUpdateChild) { developmentOpen = false }
    if (headSidePick && childId != null) HeadSidePickDialog(childEvents, childId, onSaveHealthRecord) { headSidePick = false }
    if (growthPick.isNotEmpty()) QuickChoiceDialog("Vækst", dk.babyapp.ui.theme.CareCategory.Measurement, growthPick.map { action -> QuickChoice(action.label) { dialog = EditorKind.valueOf(action.name) } }) { growthPick = emptyList() }
    // The whole 100 før 1 list opens from the Fast føde form, on top of it.
    if (hundredFoodsOpen && childId != null) HundredFoodsDialog(childId, child, historyEvents ?: events, onSaveHealthRecord) { hundredFoodsOpen = false }
    if (teethOpen && childId != null) TeethDialog(childId, teeth, onSaveTooth, child) { teethOpen = false }
    if (medicineCardOpen && childId != null) MedicineCardDialog(childId, medicines, onUpdateMedicines, { medicineCardOpen = false }, childEvents)
    deleteTarget?.let { event -> AlertDialog(
        onDismissRequest = { deleteTarget = null },
        title = { Text("Slet registrering?") },
        text = { Text("Registreringen fjernes fra oversigten, men bevares sikkert internt.") },
        confirmButton = { Button(onClick = { onDelete(event); deleteTarget = null }) { Text("Slet") } },
        dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Annuller") } },
    ) }
    if (customizeOpen) AllQuickActionsDialog(QuickAction.entries.filterNot(::visible).map { it.name }.toSet(), preferences.quickActionCategoryOrder, { customizeOpen = false }, preferences.simpleQuickRegistration, onUpdateSimpleQuickRegistration) { hidden, order -> onUpdateQuickActionCategoryOrder(order); onUpdateQuickActions(true, true, true, true); onUpdateHiddenQuickActions(hidden); customizeOpen = false }
    if (questionsOpen && childId != null) HealthQuestionsDialog(childId, preferences.healthQuestions, historyEvents ?: events, onUpdateHealthQuestions) { questionsOpen = false }
    cardSettingsFor?.let { category ->
        CardButtonsDialog(category, QuickAction.entries.filterNot(::visible).map { it.name }.toSet(), { cardSettingsFor = null }) { hidden -> onUpdateHiddenQuickActions(hidden); cardSettingsFor = null }
    }
    if (summaryOpen) DaySummaryDialog(allChildEvents, overviewDate) { summaryOpen = false }
    if (dashboardCustomizeOpen) DashboardMetricDialog(preferences.dashboardMetrics, { dashboardCustomizeOpen = false }, preferences.showBulletinBoard, onUpdateShowBulletinBoard, preferences.showDevelopmentStrip, onUpdateShowDevelopmentStrip) { metrics -> onUpdateDashboardMetrics(metrics); dashboardCustomizeOpen = false }
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
            // One value per line, so the box always grows to fit its text.
            else Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                iconValues.forEach { (icon, description, total) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.ui.res.painterResource(icon), description, Modifier.height(18.dp), tint = accent)
                        Text(total, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Text(if (count == 1) "1 registrering" else "$count registreringer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Sidst ${detail ?: "-"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    DashboardMetric.HeadSide -> "Liggeside"
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

/** Questions to bring to the next health visit. They are attached to the visit when it is registered. */
@Composable
private fun HealthQuestionsDialog(childId: String, all: List<dk.babyapp.data.preferences.HealthQuestion>, events: List<CareEventEntity>, onUpdate: (List<dk.babyapp.data.preferences.HealthQuestion>) -> Unit, onDismiss: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val mine = all.filter { it.childId == childId }
    val nextVisit = events.filter { it.childId == childId && it.deletedAt == null && it.type == CareEventType.HealthVisit && it.healthStatus == HealthRecordStatus.Scheduled && it.startedAt >= System.currentTimeMillis() - 86_400_000 }.minByOrNull { it.startedAt }
    FormDialog(onDismissRequest = onDismiss, category = dk.babyapp.ui.theme.CareCategory.Health, title = { Text("Spørgsmål til næste besøg") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(nextVisit?.let { "Næste planlagte besøg: ${it.healthTitle.ifBlank { it.healthVisitType?.displayLabel() ?: "Sundhedsbesøg" }} ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it.startedAt))}." }
                ?: "Skriv spørgsmål ned, når du kommer i tanke om dem. Når du registrerer besøget, kan du tage dem med.", style = MaterialTheme.typography.bodyMedium)
            mine.forEach { question ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• ${question.text}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { onUpdate(all - question) }) { Icon(Icons.Outlined.Delete, "Fjern ${question.text}", tint = MaterialTheme.colorScheme.error) }
                }
            }
            RegistrationTextField(draft, { draft = it }, label = { Text("Nyt spørgsmål") })
            FilledTonalButton(enabled = draft.isNotBlank(), onClick = { onUpdate(all + dk.babyapp.data.preferences.HealthQuestion(childId = childId, text = draft.trim())); draft = "" }) { Icon(Icons.Outlined.Add, null); Text(" Tilføj") }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Luk") } })
}

/** Each active medicine with its latest dose and how long ago. Information only; no dose advice. */
@Composable
internal fun MedicineStatus(plans: List<MedicinePlan>, events: List<CareEventEntity>) {
    if (plans.isEmpty()) return
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(60_000); now = System.currentTimeMillis() } }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        plans.forEach { plan ->
            val dose = dk.babyapp.domain.lastDose(plan, events)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(dose?.let { "Sidste dosis kl. ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it.startedAt))} · for ${dk.babyapp.domain.timeSinceLabel(it.startedAt, now)} siden" } ?: "Ingen dosis registreret",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun DashboardMetricDialog(current: List<DashboardMetric>, onDismiss: () -> Unit, board: Boolean = true, onBoardChange: (Boolean) -> Unit = {}, strip: Boolean = true, onStripChange: (Boolean) -> Unit = {}, onSave: (List<DashboardMetric>) -> Unit) {
    // Shown fields first in their order, then the hidden ones.
    var showBoard by remember { mutableStateOf(board) }
    var showStrip by remember { mutableStateOf(strip) }
    var order by remember { mutableStateOf(current + DashboardMetric.entries.filterNot { it in current }) }
    var shown by remember { mutableStateOf(current.toSet()) }
    FormDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tilpas overblikket") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Vælg felterne, og flyt dem op eller ned.", style = MaterialTheme.typography.bodyMedium)
                // The cards under I dag can only be shown or hidden.
                CustomizeRow("Begivenheder", showBoard, { showBoard = it })
                CustomizeRow("Udvikling", showStrip, { showStrip = it })
                order.forEachIndexed { index, metric ->
                    val (up, down) = order.moveCallbacks(index) { order = it }
                    CustomizeRow(metric.displayLabel(), metric in shown, { on -> shown = if (on) shown + metric else shown - metric }, up, down)
                }
            }
        },
        confirmButton = { Button(enabled = shown.isNotEmpty(), onClick = { if (showBoard != board) onBoardChange(showBoard); if (showStrip != strip) onStripChange(showStrip); onSave(order.filter { it in shown }) }) { Text("Gem") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun QuickActionCard(icon: ImageVector, title: String, iconTint: androidx.compose.ui.graphics.Color, category: dk.babyapp.ui.theme.CareCategory? = null, insights: List<Pair<String, () -> Unit>> = emptyList(), onSettings: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
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
                InsightLinkButton(title, insights)
                if (onSettings != null) IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "Tilpas $title") }
            }
            // The buttons take the category's colours.
            CategoryFormTheme(category) { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { content() } }
        }
    }
}

/** The graph icon on a quick card: straight to its Indsigt page, or a small menu when there are several. */
@Composable
private fun InsightLinkButton(title: String, insights: List<Pair<String, () -> Unit>>) {
    if (insights.isEmpty()) return
    var menu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { if (insights.size == 1) insights.single().second() else menu = true }) { Icon(Icons.Outlined.ShowChart, "Indsigt for $title") }
        androidx.compose.material3.DropdownMenu(menu, { menu = false }) {
            insights.forEach { (label, open) -> androidx.compose.material3.DropdownMenuItem(text = { Text(label) }, onClick = { menu = false; open() }) }
        }
    }
}

/** A quick-registration category either as a card with a heading, or in the simple view as an icon and its buttons. */
@Composable
private fun QuickCategory(simple: Boolean, category: dk.babyapp.ui.theme.CareCategory, title: String, insights: List<Pair<String, () -> Unit>>, onSettings: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.babyColors.category(category)
    if (!simple) { QuickActionCard(category.icon, title, colors.accent, category, insights, onSettings, content); return }
    // Simple view: like the manual menu, the coloured icon (tap for Indsigt) beside the buttons, no headings.
    androidx.compose.material3.Surface(shape = MaterialTheme.shapes.large, color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            var menu by remember { mutableStateOf(false) }
            Box {
                androidx.compose.material3.Surface(onClick = { if (insights.size == 1) insights.single().second() else if (insights.isNotEmpty()) menu = true }, enabled = insights.isNotEmpty(), shape = androidx.compose.foundation.shape.CircleShape, color = colors.container, modifier = Modifier.padding(top = 2.dp).size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(category.icon, contentDescription = title, tint = colors.accent, modifier = Modifier.size(20.dp)) }
                }
                androidx.compose.material3.DropdownMenu(menu, { menu = false }) {
                    insights.forEach { (label, open) -> androidx.compose.material3.DropdownMenuItem(text = { Text(label) }, onClick = { menu = false; open() }) }
                }
            }
            CategoryFormTheme(category) { Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) { content() } }
        }
    }
}

/** The graphs that belong to each quick-registration card, named as in its menu when there are several. */
private val QuickActionCategory.insightCards: List<Pair<String, dk.babyapp.data.preferences.InsightCard>> get() {
    fun page(card: dk.babyapp.data.preferences.InsightCard, label: String = card.label) = label to card
    return when (this) {
        QuickActionCategory.Feeding, QuickActionCategory.SolidFood -> listOf(page(dk.babyapp.data.preferences.InsightCard.Feeding))
        QuickActionCategory.Sleep -> listOf(page(dk.babyapp.data.preferences.InsightCard.Sleep), page(dk.babyapp.data.preferences.InsightCard.Rhythm), page(dk.babyapp.data.preferences.InsightCard.HeadSide))
        QuickActionCategory.Diaper -> listOf(page(dk.babyapp.data.preferences.InsightCard.Diapers))
        // The temperature graph is the Sundhed page.
        QuickActionCategory.Measurements -> listOf(page(dk.babyapp.data.preferences.InsightCard.Growth), page(dk.babyapp.data.preferences.InsightCard.Health, "Temperatur"))
        QuickActionCategory.Health -> listOf(page(dk.babyapp.data.preferences.InsightCard.Health, "Temperatur"))
        QuickActionCategory.Misc -> listOf(page(dk.babyapp.data.preferences.InsightCard.Misc))
    }
}

private class QuickButtonSpec(val label: String, val enabled: Boolean, val onClick: () -> Unit)

@Composable
private fun QuickButtonRows(buttons: List<QuickButtonSpec>, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        buttons.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // A button alone on its row takes the whole width.
                row.forEach { button -> QuickButton(button.label, Modifier.weight(1f), button.enabled, button.onClick) }
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
internal fun ActiveTimerCard(event: CareEventEntity, onToggle: (CareEventEntity) -> Unit, onSwitch: (CareEventEntity) -> Unit, onStop: (CareEventEntity) -> Unit, onCancel: (CareEventEntity) -> Unit, onEdit: (CareEventEntity) -> Unit, onShield: (CareEventEntity, Boolean) -> Unit, keepAwake: Boolean, onKeepAwake: (Boolean) -> Unit, modifier: Modifier = Modifier, minimized: Boolean = true, onMinimizedChange: (Boolean) -> Unit = {}, childLabel: String? = null, onPlace: ((CareEventEntity, String) -> Unit)? = null) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.runningSince) { while (event.runningSince != null) { now = System.currentTimeMillis(); delay(1_000) } }
    val elapsed = event.elapsedSeconds(now)
    val title = listOfNotNull(childLabel, when (event.type) { CareEventType.Breastfeeding -> "Amning – ${sideLabel(event.activeSide)}"; CareEventType.Sleep -> "Søvn"; CareEventType.Activity -> event.activityName.takeIf { event.activityType == ActivityType.Other && it.isNotBlank() } ?: event.activityType?.displayLabel() ?: "Aktivitet"; else -> "Pumpning" }).joinToString(" · ")
    val running = event.runningSince != null
    val category = MaterialTheme.babyColors.category(event.careCategory)
    val toggle: @Composable () -> Unit = {
        androidx.compose.material3.FilledTonalIconButton(onClick = { onToggle(event) }) { Icon(if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, if (running) "Pause" else "Fortsæt") }
    }
    val expandButton: @Composable () -> Unit = {
        IconButton(onClick = { onMinimizedChange(!minimized) }) { Icon(if (minimized) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (minimized) "Udvid tæller" else "Minimer tæller") }
    }
    // The timer takes its category's colours, like the rest of that category.
    CategoryFormTheme(event.careCategory) {
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = formBody(event.careCategory)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        if (minimized) {
            // Compact: the full title on top, the time and the buttons below, so nothing gets cut off.
            Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 2.dp, top = 4.dp, bottom = 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(24.dp).background(category.container, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                        Icon(event.careCategory.icon, contentDescription = null, tint = category.accent, modifier = Modifier.size(14.dp))
                    }
                    Text(title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    expandButton()
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(formatDuration(elapsed) + if (running) "" else " · pause", style = MaterialTheme.typography.titleMedium, maxLines = 1, modifier = Modifier.weight(1f).padding(start = 30.dp))
                    // Edit the intervals straight from the compact bar.
                    IconButton(onClick = { onEdit(event) }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.Edit, "Ret intervaller") }
                    if (event.type == CareEventType.Breastfeeding) IconButton(onClick = { onSwitch(event) }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.SwapHoriz, "Skift side") }
                    // Screen on/off as an icon only; the expanded timer spells it out.
                    androidx.compose.material3.IconToggleButton(keepAwake, onKeepAwake, modifier = Modifier.size(40.dp)) {
                        Icon(if (keepAwake) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb, if (keepAwake) "Skærmen holdes tændt" else "Hold skærmen tændt", tint = if (keepAwake) androidx.compose.ui.graphics.Color(0xFFE0A100) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    toggle()
                    androidx.compose.material3.FilledIconButton(onClick = { onStop(event) }, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.Stop, "Stop") }
                    androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                }
            }
        } else Column(Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Expanded: the same heading as the compact bar, then time, sides, settings and actions.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(32.dp).background(category.container, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                    Icon(event.careCategory.icon, contentDescription = null, tint = category.accent, modifier = Modifier.size(18.dp))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                expandButton()
            }
            Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(formatDuration(elapsed), style = MaterialTheme.typography.displaySmall)
                    Text(if (running) "kører" else "pause", style = MaterialTheme.typography.labelLarge, color = if (running) category.accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                }
                if (event.type == CareEventType.Breastfeeding) {
                    val sides = event.accrueUntil(now)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BreastSide.entries.forEach { side ->
                            val active = side == (event.activeSide ?: BreastSide.Left)
                            Column(
                                Modifier.weight(1f).background(if (active) category.container else MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium).padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(sideLabel(side).replaceFirstChar { it.uppercase() } + if (active) " · nu" else "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatDuration(if (side == BreastSide.Left) sides.leftSeconds else sides.rightSeconds), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
                // Sleep: where the child sleeps can be picked while the timer runs.
                if (event.type == CareEventType.Sleep && onPlace != null) SleepPlaceField(event.sleepLocation) { onPlace(event, it) }
                // Nursing always lists its intervals; other timers once they have been paused. Editing sits right below.
                if (event.type == CareEventType.Breastfeeding || event.segmentIntervals().size > 1) NursingIntervalOverview(event, now)
                TextButton(onClick = { onEdit(event) }, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp)); Text(" Ret intervaller")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.FilledTonalButton(onClick = { onToggle(event) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Icon(if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null, Modifier.size(18.dp)); Text(if (running) " Pause" else " Fortsæt")
                    }
                    if (event.type == CareEventType.Breastfeeding) androidx.compose.material3.FilledTonalButton(onClick = { onSwitch(event) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Icon(Icons.Outlined.SwapHoriz, null, Modifier.size(18.dp)); Text(" Skift side")
                    }
                    Button(onClick = { onStop(event) }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Icon(Icons.Outlined.Stop, null, Modifier.size(18.dp)); Text(" Stop")
                    }
                }
                // Same text whether on or off, so the bulb never moves; it lights up yellow like in the compact bar.
                val awakeColors = if (keepAwake) androidx.compose.material3.ButtonDefaults.filledTonalButtonColors() else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                androidx.compose.material3.FilledTonalButton(onClick = { onKeepAwake(!keepAwake) }, colors = awakeColors, modifier = Modifier.fillMaxWidth().semantics { contentDescription = if (keepAwake) "Skærmen holdes tændt" else "Hold skærmen tændt" },
                    border = if (keepAwake) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Icon(if (keepAwake) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb, null, Modifier.size(18.dp), tint = if (keepAwake) androidx.compose.ui.graphics.Color(0xFFE0A100) else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" Hold skærmen tændt")
                }
            }
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
            OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldText), shape = MaterialTheme.shapes.small, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), onClick = {
                DatePickerDialog(context, { _, year, month, day -> calendar.set(year, month, day); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
            }) { Icon(Icons.Outlined.CalendarToday, null, Modifier.size(18.dp)); Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(value)), Modifier.weight(1f).padding(start = 10.dp)) }
        }
        Column(Modifier.weight(1f)) {
            Text("Tid *", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldText), shape = MaterialTheme.shapes.small, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), onClick = {
                TimePickerDialog(context, { _, hour, minute -> calendar.set(java.util.Calendar.HOUR_OF_DAY, hour); calendar.set(java.util.Calendar.MINUTE, minute); calendar.set(java.util.Calendar.SECOND, 0); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
            }) { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)); Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value)), Modifier.weight(1f).padding(start = 10.dp)) }
        }
    }
}

@Composable
internal fun DateAndOptionalTimeFields(value: Long, timeSpecified: Boolean, onChange: (Long) -> Unit, onTimeSpecifiedChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = value }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Dato *", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldText), shape = MaterialTheme.shapes.small, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), onClick = {
            DatePickerDialog(context, { _, year, month, day -> calendar.set(year, month, day); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }) { Icon(Icons.Outlined.CalendarToday, null, Modifier.size(18.dp)); Text(DateFormat.getDateInstance(DateFormat.SHORT).format(Date(value)), Modifier.weight(1f).padding(start = 10.dp)) }
        if (timeSpecified) {
            Text("Tid", style = MaterialTheme.typography.labelMedium)
            OutlinedButton(colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.babyColors.fieldContainer, contentColor = MaterialTheme.babyColors.fieldText), shape = MaterialTheme.shapes.small, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), onClick = {
                TimePickerDialog(context, { _, hour, minute -> calendar.set(java.util.Calendar.HOUR_OF_DAY, hour); calendar.set(java.util.Calendar.MINUTE, minute); calendar.set(java.util.Calendar.SECOND, 0); onChange(calendar.timeInMillis) }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
            }) { Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp)); Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value)), Modifier.weight(1f).padding(start = 10.dp)) }
        }
        // Under the time box, so it reads as adding or removing that box.
        TimeToggle(if (timeSpecified) "Fjern tidspunkt" else "Tilføj tidspunkt", timeSpecified) { onTimeSpecifiedChange(!timeSpecified) }
    }
}

/** Adds or removes an optional time box; shows a plus or minus to say which. */
@Composable
internal fun TimeToggle(label: String, on: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 4.dp)) {
        Icon(if (on) Icons.Outlined.Remove else Icons.Outlined.Add, null, Modifier.size(18.dp)); Text(" $label")
    }
}

/** The end of a manual registration, which can be left out when only the time it happened is known. */
@Composable
internal fun OptionalEndField(end: Long?, onChange: (Long?) -> Unit, defaultEnd: () -> Long) {
    if (end != null) { Text("Slut", style = MaterialTheme.typography.labelMedium); DateTimeFields(end) { onChange(it) } }
    TimeToggle(if (end == null) "Tilføj sluttidspunkt" else "Fjern sluttidspunkt", end != null) { onChange(if (end == null) defaultEnd() else null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun BottleDialog(onDismiss: () -> Unit, onSave: (Long, BottleContent, Int?, Int?, String, String) -> Unit) {
    var ratio by remember { mutableStateOf("") }
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var offered by remember { mutableStateOf("") }; var consumed by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }; var content by remember { mutableStateOf(BottleContent.BreastMilk) }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Feeding, onDismissRequest = onDismiss, title = { Text("Flaske") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceChips(BottleContent.entries.map { it to it.displayLabel() }, content) { it?.let { content = it } }
        if (content == BottleContent.Mixed) BottleRatioField(ratio) { ratio = it }
        AmountField(consumed, { consumed = it }, "Spist (ml)")
        MoreDetails("tidspunkt, tilbudt, noter") {
            DateTimeFields(time) { time = it }
            RegistrationTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
        }
    } }, confirmButton = { Button(onClick = { onSave(time, content, listOfNotNull(offered.toIntOrNull(), consumed.toIntOrNull()).maxOrNull(), consumed.toIntOrNull(), notes, if (content == BottleContent.Mixed) ratio else "") }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun DiaperDialog(onDismiss: () -> Unit, initialType: DiaperType = DiaperType.Wet, onSave: (Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String) -> Unit) {
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }; var type by remember { mutableStateOf(initialType) }; var color by remember { mutableStateOf<DiaperColor?>(null) }; var consistency by remember { mutableStateOf<DiaperConsistency?>(null) }; var observation by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Diaper, onDismissRequest = onDismiss, title = { Text("Ble") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceChips(DiaperType.entries.map { it to it.displayLabel() }, type) { it?.let { type = it } }
        MoreDetails("tidspunkt, farve, konsistens, observation, noter") {
            DateTimeFields(time) { time = it }
            ChoiceChips(DiaperColor.entries.map { it to it.displayLabel() }, color, allowNone = true, label = "Farve") { color = it }
            ChoiceChips(DiaperConsistency.entries.map { it to it.displayLabel() }, consistency, allowNone = true, label = "Konsistens") { consistency = it }
            DiaperObservationField(observation) { observation = it }; RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
        }
    } }, confirmButton = { Button(onClick = { onSave(time, type, color, consistency, observation, notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable internal fun ManualTimerDialog(type: CareEventType, onDismiss: () -> Unit, onSave: (CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit) {
    val now = remember { System.currentTimeMillis() }
    // Pumping is usually just "when"; nursing has a length. The end can be switched on or off either way.
    var start by remember { mutableLongStateOf(if (type == CareEventType.Pumping) now else now - 600_000) }
    var end by remember { mutableStateOf<Long?>(if (type == CareEventType.Pumping) null else now) }
    var side by remember { mutableStateOf(BreastSide.Left) }; var amount by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Feeding, onDismissRequest = onDismiss, title = { Text(if (type == CareEventType.Breastfeeding) "Amning" else "Pumpning") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (type == CareEventType.Breastfeeding) ChoiceChips(BreastSide.entries.map { it to sideLabel(it).replaceFirstChar { c -> c.uppercase() } }, side) { it?.let { side = it } }
        if (type == CareEventType.Pumping) AmountField(amount, { amount = it }, "Mængde (ml)")
        OptionalEndField(end, { end = it }) { maxOf(start + 600_000, now) }
        MoreDetails("starttidspunkt, noter", notes.isNotBlank()) {
            Text(if (end == null) "Tidspunkt" else "Start", style = MaterialTheme.typography.labelMedium); DateTimeFields(start) { start = it }
            RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
        }
    } }, confirmButton = { Button(enabled = (end ?: start) >= start, onClick = { onSave(type, start, end ?: start, if (type == CareEventType.Breastfeeding) side else null, amount.toIntOrNull(), notes) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
internal fun SleepDialog(
    existing: CareEventEntity? = null,
    onDismiss: () -> Unit,
    onSave: (Long, Long, SleepType?, String, String, Int?, SleepQuality?, String) -> Unit,
) {
    var start by remember { mutableLongStateOf(existing?.startedAt ?: System.currentTimeMillis() - 3_600_000) }
    var end by remember { mutableLongStateOf(existing?.endedAt ?: System.currentTimeMillis()) }
    // Søvn is one kind; an old nap/night value is kept untouched when editing.
    val type = existing?.sleepType
    var location by remember { mutableStateOf(existing?.sleepLocation.orEmpty()) }
    var settling by remember { mutableStateOf(existing?.settlingMethod.orEmpty()) }
    var awakenings by remember { mutableStateOf(existing?.awakenings?.toString().orEmpty()) }
    var quality by remember { mutableStateOf(existing?.sleepQuality) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Sleep,
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Søvn" else "Rediger søvn") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Slut", style = MaterialTheme.typography.labelMedium); DateTimeFields(end) { end = it }
            MoreDetails("start, sted, puttemetode, opvågninger, kvalitet, noter", existing != null || listOf(location, settling, awakenings, notes).any(String::isNotBlank) || quality != null) {
                Text("Start", style = MaterialTheme.typography.labelMedium); DateTimeFields(start) { start = it }
                SleepPlaceField(location) { location = it }
                RegistrationTextField(settling, { settling = it }, label = { Text("Puttemetode") })
                RegistrationTextField(awakenings, { awakenings = it.filter(Char::isDigit) }, label = { Text("Opvågninger") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                ChoiceChips(SleepQuality.entries.map { it to it.displayLabel() }, quality, allowNone = true, label = "Søvnkvalitet") { quality = it }
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        } },
        confirmButton = { Button(enabled = end > start, onClick = { onSave(start, end, type, location, settling, awakenings.toIntOrNull(), quality, notes) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

/** Common sleeping places as chips; anything else can still be written. */
@Composable
internal fun SleepPlaceField(value: String, onChange: (String) -> Unit) {
    val places = listOf("Seng", "Barnevogn", "Lift", "Kontaktlur", "Bil", "Bæresele")
    var other by remember { mutableStateOf(value.isNotBlank() && value !in places) }
    ChoiceChips(places.map { it to it } + ("" to "Andet"), if (other) "" else value.takeIf { it in places }, allowNone = true, label = "Sovested") { picked ->
        other = picked == ""
        onChange(if (picked == "") value.takeIf { it !in places }.orEmpty() else picked.orEmpty())
    }
    if (other) RegistrationTextField(value.takeIf { it !in places }.orEmpty(), onChange, label = { Text("Andet sted") })
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
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Measurement,
        onDismissRequest = onDismiss,
        title = { Text("Måling") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChips(MeasurementType.entries.map { it to it.displayLabel() }, type) { new -> if (new != null && type != new) { type = new; unit = if (new == MeasurementType.Weight) "g" else new.defaultUnit(); value = "" } }
            if (type == MeasurementType.Weight) WeightUnitSelector(unit, value) { newUnit, newValue -> unit = newUnit; value = newValue }
            RegistrationTextField(value, { value = it.replace(',', '.').filter { char -> char.isDigit() || char == '.' } }, label = { Text("Værdi ($unit) *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            MoreDetails("dato, tidspunkt, noter") {
                DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        } },
        confirmButton = { Button(enabled = value.toDoubleOrNull() != null, onClick = { onSave(time, timeSpecified, type, value.toDouble(), unit, notes) }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
internal fun ActivityDialog(
    initialType: ActivityType?,
    onDismiss: () -> Unit,
    onSave: (Long, Long, ActivityType, String, String) -> Unit,
) {
    var start by remember { mutableLongStateOf(System.currentTimeMillis() - 300_000) }
    var name by remember { mutableStateOf("") }
    var end by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var type by remember { mutableStateOf(initialType ?: ActivityType.TummyTime) }
    var notes by remember { mutableStateOf("") }
    val availableTypes = if (initialType == ActivityType.Medicine) listOf(ActivityType.Medicine) else ActivityType.entries.filter { it != ActivityType.Medicine }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Misc,
        onDismissRequest = onDismiss,
        title = { Text(if (availableTypes.size > 1) "Diverse" else type.displayLabel()) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (availableTypes.size > 1) ChoiceChips(availableTypes.map { it to it.displayLabel() }, type) { it?.let { type = it } }
            if (type == ActivityType.Other) ActivityNameField(name) { name = it }
            OptionalEndField(end, { end = it }) { maxOf(start + 300_000, System.currentTimeMillis()) }
            MoreDetails("starttidspunkt, noter") {
                Text(if (end == null) "Tidspunkt" else "Start", style = MaterialTheme.typography.labelMedium); DateTimeFields(start) { start = it }
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        } },
        confirmButton = { Button(enabled = (end ?: start) >= start, onClick = { onSave(start, end ?: start, type, notes, if (type == ActivityType.Other) name.trim() else "") }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
private fun StartActivityDialog(onDismiss: () -> Unit, onStart: (ActivityType) -> Unit) {
    var type by remember { mutableStateOf(ActivityType.Play) }
    val choices = listOf(ActivityType.Bath, ActivityType.OutdoorTime, ActivityType.Play, ActivityType.Other)
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Misc,
        onDismissRequest = onDismiss,
        title = { Text("Start aktivitet") },
        text = { ChoiceChips(choices.map { it to it.displayLabel() }, type) { it?.let { type = it } } },
        confirmButton = { Button(onClick = { onStart(type) }) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable
internal fun MedicineDialog(childId: String, existing: CareEventEntity?, onDismiss: () -> Unit, medicines: List<MedicinePlan> = emptyList(), onOpenMedicineCard: (() -> Unit)? = null, onSave: (CareEventEntity) -> Unit) {
    val plans = medicines.filter { it.childId == childId && it.active }
    var selectedPlan by remember { mutableStateOf(plans.firstOrNull { it.id == existing?.medicineId }) }
    var dose by remember { mutableStateOf(existing?.medicationDose.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var time by remember { mutableLongStateOf(existing?.startedAt ?: System.currentTimeMillis()) }
    var timeSpecified by remember { mutableStateOf(existing?.timeSpecified ?: false) }
    val name = selectedPlan?.name ?: existing?.medicationName.orEmpty()
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Health,
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Medicin" else "Rediger medicin") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Nothing to log until the medicine card has a medicine; only a pointer to it.
            if (plans.isEmpty() && existing == null) {
                Text("Opret først medicinen på medicinkortet. Så kan du vælge den her.", style = MaterialTheme.typography.bodyLarge)
                if (onOpenMedicineCard != null) Button(onClick = onOpenMedicineCard) { Icon(Icons.Outlined.Medication, null, Modifier.size(18.dp)); Text(" Åbn medicinkort") }
                return@Column
            }
            if (existing != null && selectedPlan == null) Text("Tidligere registrering: ${existing.medicationName}")
            // The medicine first, then the dose it fills in, which can be changed if something else was given.
            if (plans.isNotEmpty()) SelectionDropdown("Medicin *", selectedPlan?.let { listOf(it.name, it.dose).filter(String::isNotBlank).joinToString(" · ") } ?: "Vælg medicin", plans.map { it to listOf(it.name, it.dose).filter(String::isNotBlank).joinToString(" · ") }) { plan -> selectedPlan = plan; dose = plan.dose }
            RegistrationTextField(dose, { dose = it }, label = { Text("Dosis") })
            MoreDetails("dato, tidspunkt, noter", notes.isNotBlank()) {
                DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        } },
        confirmButton = { Button(enabled = name.isNotBlank() && (selectedPlan != null || existing != null), onClick = {
            onSave((existing ?: CareEventEntity(childId = childId, type = CareEventType.Activity, startedAt = time)).copy(
                startedAt = time, endedAt = time, runningSince = null, activityType = ActivityType.Medicine,
                activityDurationSeconds = null, medicineId = selectedPlan?.id ?: existing?.medicineId.orEmpty(), timeSpecified = timeSpecified, medicationName = name, medicationDose = dose, notes = notes,
            ))
        }) { Text("Gem") } },
        dismissButton = {
            // The medicine card sits in the footer, on the same line as Annuller and Gem.
            if (onOpenMedicineCard != null) TextButton(onClick = onOpenMedicineCard, contentPadding = PaddingValues(horizontal = 4.dp)) { Icon(Icons.Outlined.Medication, null, Modifier.size(18.dp)); Text(" Medicinkort") }
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("Annuller") }
        },
    )
}

@Composable
private fun HealthNoteDialog(childId: String, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpecified by remember { mutableStateOf(false) }
    FormDialog(category = dk.babyapp.ui.theme.CareCategory.Health,
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
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@Composable internal fun EditEventDialog(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    if (event.type == CareEventType.HeadSide) { HeadSideEditor(event, onDismiss, onSave); return }
    if (event.type == CareEventType.Breastfeeding) { NursingEditor(event, onDismiss, onSave, openDetails = event.endedAt == null); return }
    if (event.type == CareEventType.SolidFood) { SolidFoodDialog(event.childId, event, onDismiss, onSave = onSave); return }
    if (event.type == CareEventType.Activity && event.activityType == ActivityType.Medicine) {
        MedicineDialog(event.childId, event, onDismiss, onSave = onSave); return
    }
    // A timer that was paused, or is still running, is edited interval by interval like nursing.
    // A timer that was paused, or is still running, uses the timer window; "Ret intervaller" on a running timer opens its details.
    if (event.type in setOf(CareEventType.Sleep, CareEventType.Activity, CareEventType.Pumping) && (event.endedAt == null || event.segmentIntervals().size > 1)) { NursingEditor(event, onDismiss, onSave, openDetails = event.endedAt == null); return }
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
    FormDialog(category = event.careCategory, onDismissRequest = onDismiss, title = { Text("Rediger registrering") }, text = {
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
                    ChoiceChips(BottleContent.entries.map { it to it.displayLabel() }, draft.bottleContent) { it?.let { draft = draft.copy(bottleContent = it) } }
                    if (draft.bottleContent == BottleContent.Mixed) BottleRatioField(draft.bottleMixRatio) { draft = draft.copy(bottleMixRatio = it) }
                    AmountField(consumed, { consumed = it }, "Spist (ml)")
                    RegistrationTextField(offered, { offered = it.filter(Char::isDigit) }, label = { Text("Tilbudt (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                }
                CareEventType.Pumping -> {
                    AmountField(pumped, { pumped = it }, "Pumpet (ml)")
                    PumpingMethodChips(draft.pumpingMethod) { draft = draft.copy(pumpingMethod = it) }
                }
                CareEventType.Diaper -> {
                    ChoiceChips(DiaperType.entries.map { it to it.displayLabel() }, draft.diaperType) { it?.let { draft = draft.copy(diaperType = it) } }
                    MoreDetails("farve, konsistens, observation", draft.diaperColor != null || draft.diaperConsistency != null || draft.observation.isNotBlank()) {
                        ChoiceChips(DiaperColor.entries.map { it to it.displayLabel() }, draft.diaperColor, allowNone = true, label = "Farve") { draft = draft.copy(diaperColor = it) }
                        ChoiceChips(DiaperConsistency.entries.map { it to it.displayLabel() }, draft.diaperConsistency, allowNone = true, label = "Konsistens") { draft = draft.copy(diaperConsistency = it) }
                        DiaperObservationField(draft.observation) { draft = draft.copy(observation = it) }
                    }
                }
                CareEventType.Measurement -> {
                    val type = draft.measurementType ?: MeasurementType.Weight
                    SelectionDropdown("Måling *", type.displayLabel(), MeasurementType.entries.map { it to it.displayLabel() }) { if (type != it) { unit = if (it == MeasurementType.Weight) "g" else it.defaultUnit(); draft = draft.copy(measurementType = it); value = "" } }
                    if (type == MeasurementType.Weight) WeightUnitSelector(unit, value) { newUnit, newValue -> unit = newUnit; value = newValue }
                    RegistrationTextField(value, { value = it.replace(',', '.') }, label = { Text("Værdi ($unit) *") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
                }
                CareEventType.Activity -> {
                    ChoiceChips(ActivityType.entries.filter { it != ActivityType.Medicine }.map { it to it.displayLabel() }, draft.activityType ?: ActivityType.Other) { it?.let { draft = draft.copy(activityType = it) } }
                    if ((draft.activityType ?: ActivityType.Other) == ActivityType.Other) ActivityNameField(draft.activityName) { draft = draft.copy(activityName = it) }
                }
                else -> Unit
            }
            MoreDetails("noter", draft.notes.isNotBlank()) { RegistrationTextField(draft.notes, { draft = draft.copy(notes = it) }, label = { Text("Noter") }) }
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
internal fun DailyOverview(todayEvents: List<CareEventEntity>, metrics: List<DashboardMetric>, date: LocalDate, onCustomize: (() -> Unit)? = null, onPrevious: (() -> Unit)? = null, onNext: (() -> Unit)? = null, onOpenSummary: (() -> Unit)? = null, collapsed: Boolean = false, onCollapse: ((Boolean) -> Unit)? = null) {
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
                        OverviewItem(metric.displayLabel(), latest?.measurementValue?.let { formatMeasurement(it, latest.measurementUnit) } ?: "-", latest)
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
                        // The day's name opens the full summary of the day.
                        val title = if (date == LocalDate.now()) "I dag" else date.format(java.time.format.DateTimeFormatter.ofPattern("d. MMMM", java.util.Locale.forLanguageTag("da")))
                        Row(Modifier.weight(1f).padding(start = if (onPrevious == null) 10.dp else 0.dp).clip(MaterialTheme.shapes.small)
                            .then(if (onOpenSummary != null) Modifier.clickable(onClickLabel = "Vis dagens opsummering", onClick = onOpenSummary) else Modifier).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        }
                        if (onPrevious != null) IconButton(onClick = { onNext?.invoke() }, enabled = onNext != null) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Næste dags overblik") }
                        if (onCustomize != null) IconButton(onClick = onCustomize) { Icon(Icons.Outlined.Tune, "Tilpas overblikket") }
                        if (onCollapse != null) IconButton(onClick = { onCollapse(!collapsed) }) {
                            Icon(if (collapsed) Icons.Outlined.ExpandMore else Icons.Outlined.ExpandLess, if (collapsed) "Fold I dag ud" else "Fold I dag sammen")
                        }
                    }
                    if (!collapsed) Column(Modifier.padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

/** What "Andet" was, e.g. babysvømning; shown as the registration's title. */
@Composable
internal fun ActivityNameField(value: String, onChange: (String) -> Unit) =
    RegistrationTextField(value, onChange, label = { Text("Hvad?") }, placeholder = { Text("Fx babysvømning") }, singleLine = true)
