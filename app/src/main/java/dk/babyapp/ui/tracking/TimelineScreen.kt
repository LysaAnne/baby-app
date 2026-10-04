package dk.babyapp.ui.tracking

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.DiaperColor
import dk.babyapp.data.tracking.DiaperConsistency
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.ui.components.BabyEmptyState
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

private enum class AddKind { Breastfeeding, Bottle, Pumping, Diaper, Sleep, Measurement, Activity, SolidFood, Medicine, HealthVisit, Vaccination }

@Composable
fun TimelineScreen(
    activeChildId: String?,
    events: List<CareEventEntity>,
    careProviders: List<CareProvider>,
    contentPadding: PaddingValues,
    onAddSleep: (String, Long, Long, SleepType, String, String, Int?, SleepQuality?, String, (Boolean) -> Unit) -> Unit,
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String) -> Unit,
    onAddDiaper: (String, Long, DiaperType, DiaperColor?, DiaperConsistency?, String, String, (CareEventEntity) -> Unit) -> Unit,
    onAddManualTimer: (String, CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit,
    onAddMeasurement: (String, Long, Boolean, MeasurementType, Double, String, String) -> Unit,
    onAddActivity: (String, Long, Long, ActivityType, String) -> Unit,
    onUpdate: (CareEventEntity, (Boolean) -> Unit) -> Unit,
    onDelete: (CareEventEntity) -> Unit,
    onResumeBreastfeeding: (CareEventEntity, BreastSide, (Boolean) -> Unit) -> Unit = { _, _, result -> result(false) },
    dashboardMetrics: List<dk.babyapp.data.preferences.DashboardMetric> = dk.babyapp.data.preferences.DashboardMetric.defaults,
    quickFilters: List<String> = dk.babyapp.data.preferences.AppPreferences().journalQuickFilters,
    onUpdateQuickFilters: (List<String>) -> Unit = {},
    medicines: List<dk.babyapp.data.medicine.MedicinePlan> = emptyList(),
) {
    var typeFilters by remember { mutableStateOf(emptySet<String>()) }
    var editQuickFilters by remember { mutableStateOf(false) }
    var resumeEvent by remember { mutableStateOf<CareEventEntity?>(null) }
    var resumeError by remember { mutableStateOf(false) }
    var resuming by remember { mutableStateOf(false) }
    var overviewDate by remember { mutableStateOf<LocalDate?>(null) }
    var filtersOpen by remember { mutableStateOf(false) }
    var addMenu by remember { mutableStateOf(false) }
    var addKind by remember { mutableStateOf<AddKind?>(null) }
    var editing by remember { mutableStateOf<CareEventEntity?>(null) }
    var deleting by remember { mutableStateOf<CareEventEntity?>(null) }
    var overlapError by remember { mutableStateOf(false) }
    var selectChildError by remember { mutableStateOf(false) }
    var collapsedDays by remember(activeChildId) { mutableStateOf(emptySet<LocalDate>()) }
    var expandAllRecords by remember { mutableStateOf<Boolean?>(null) }
    val zone = ZoneId.systemDefault()
    val groups = remember(events, activeChildId, typeFilters, zone) {
        events.asSequence()
        .filter { it.childId == activeChildId && !it.isDraft && it.deletedAt == null }
        .filter { typeFilters.isEmpty() || journalFilters.any { filter -> filter.label in typeFilters && filter.matches(it) } }
        .sortedByDescending { it.startedAt }
        .groupBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() }
        .toSortedMap(compareByDescending { it })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { if (activeChildId != null) addMenu = true else selectChildError = true }) {
                Icon(Icons.Outlined.Add, "Tilføj registrering")
            }
        },
    ) { inner ->
        LazyColumn(
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + inner.calculateTopPadding() + 12.dp, bottom = contentPadding.calculateBottomPadding() + 88.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Journal", style = MaterialTheme.typography.headlineSmall)

                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Udfold alle", style = MaterialTheme.typography.labelMedium)
                    val allDaysExpanded = groups.isNotEmpty() && groups.keys.none { it in collapsedDays }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp), enabled = groups.isNotEmpty(), onClick = { collapsedDays = if (allDaysExpanded) groups.keys.toSet() else emptySet() }) {
                            Icon(if (allDaysExpanded) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore, if (allDaysExpanded) "Fold datoer sammen" else "Udfold datoer")
                            Text("Datoer")
                        }
                        OutlinedButton(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp), enabled = groups.isNotEmpty(), onClick = { expandAllRecords = expandAllRecords != true; if (expandAllRecords == true) collapsedDays = emptySet() }) {
                            Icon(if (expandAllRecords == true) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore, if (expandAllRecords == true) "Fold registreringer sammen" else "Udfold registreringer")
                            Text("Registreringer", maxLines = 1)
                        }
                    }
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickFilters.forEach { label ->
                            FilterChip(label in typeFilters, { typeFilters = if (label in typeFilters) typeFilters - label else typeFilters + label }, { Text(journalFilterLabel(label)) })
                        }
                        OutlinedButton(onClick = { filtersOpen = true }) { Text(if (typeFilters.isEmpty()) "Flere filtre" else "Flere filtre (${typeFilters.size})") }
                        if (typeFilters.isNotEmpty()) TextButton(onClick = { typeFilters = emptySet() }) { Text("Nulstil") }
                    }
                }
            }
            if (groups.isEmpty()) item { BabyEmptyState(Icons.Outlined.History, "Ingen registreringer fundet", "Prøv at nulstille filtrene, eller tilføj en ny registrering.") }
            groups.forEach { (date, dayEvents) ->
                item(key = "day-$date") {
                    TextButton(onClick = { collapsedDays = if (date in collapsedDays) collapsedDays - date else collapsedDays + date }) {
                        Text("${if (date !in collapsedDays) "▾" else "▸"} ${date.format(DateTimeFormatter.ofPattern("EEEE d. MMMM yyyy"))}", style = MaterialTheme.typography.titleMedium)
                    }
                }
                item(key = "overview-$date") { TextButton(onClick = { overviewDate = date }) { Text("Vis dagsoversigt") } }
                if (date !in collapsedDays) items(dayEvents, key = { it.id }) { event ->
                    EventCard(event, expandAll = expandAllRecords, onEdit = { editing = event }, onDelete = { deleting = event }, onResume = { resumeEvent = event })
                }
            }
        }
    }
    resumeEvent?.let { event -> AlertDialog(onDismissRequest = { if (!resuming) resumeEvent = null }, title = { Text("Fortsæt amning") }, text = {
        Column { Text("Et nyt interval starter nu på samme registrering. Pausen tæller ikke med.")
            Row { BreastSide.entries.forEach { side -> TextButton(enabled = !resuming, onClick = {
                resuming = true
                onResumeBreastfeeding(event, side) { success -> resuming = false; resumeEvent = null; resumeError = !success }
            }) { Text(if (side == BreastSide.Left) "Venstre" else "Højre") } } }
        }
    }, confirmButton = {}, dismissButton = { TextButton(enabled = !resuming, onClick = { resumeEvent = null }) { Text("Annuller") } }) }
    if (resumeError) AlertDialog(onDismissRequest = { resumeError = false }, title = { Text("Amningen kunne ikke fortsættes") }, text = { Text("Afslut først en eventuel aktiv timer for barnet, og prøv igen.") }, confirmButton = { TextButton(onClick = { resumeError = false }) { Text("OK") } })
    overviewDate?.let { date -> AlertDialog(onDismissRequest = { overviewDate = null }, title = { Text("Dagsoversigt") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DailyOverview(dk.babyapp.domain.eventsForDay(events.filter { it.childId == activeChildId }, date), dashboardMetrics, date)
        }
    }, confirmButton = { TextButton(onClick = { overviewDate = null }) { Text("Luk") } }) }
    if (filtersOpen) AlertDialog(
        onDismissRequest = { filtersOpen = false },
        title = { Text("Filtrér journalen") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { editQuickFilters = true; filtersOpen = false }) { Text("Rediger hurtigfiltre") }
            Text("Registreringstyper", style = MaterialTheme.typography.titleSmall)
            journalFilters.forEach { filter ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(filter.label in typeFilters, { selected -> typeFilters = if (selected) typeFilters + filter.label else typeFilters - filter.label })
                    Text(journalFilterLabel(filter.label))
                }
            }
            TextButton(onClick = { typeFilters = emptySet() }) { Text("Nulstil filtre") }
        } },
        confirmButton = { Button(onClick = { filtersOpen = false }) { Text("Vis resultater") } },
    )
    if (editQuickFilters) JournalQuickFiltersDialog(quickFilters, { editQuickFilters = false; filtersOpen = true }) {
        onUpdateQuickFilters(it); editQuickFilters = false; filtersOpen = true
    }
    if (addMenu) AlertDialog(
        onDismissRequest = { addMenu = false }, title = { Text("Tilføj registrering") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                AddKind.entries.forEach { kind ->
                    TextButton(onClick = { addMenu = false; addKind = kind }) {
                        Text(when (kind) {
                            AddKind.Breastfeeding -> "Amning"
                            AddKind.Bottle -> "Flaske"
                            AddKind.Pumping -> "Pumpning"
                            AddKind.Diaper -> "Ble"
                            AddKind.Sleep -> "Søvn"
                            AddKind.Measurement -> "Mål"
                            AddKind.Activity -> "Diverse"
                            AddKind.SolidFood -> "Fast føde"
                            AddKind.Medicine -> "Medicin"
                            AddKind.HealthVisit -> "Sundhedsbesøg"
                            AddKind.Vaccination -> "Vaccination"
                        })
                    }
                }
            }
        },
        confirmButton = {}, dismissButton = { TextButton(onClick = { addMenu = false }) { Text("Annuller") } },
    )
    fun saveAdded(event: CareEventEntity) { onUpdate(event) { success -> if (success) addKind = null else overlapError = true } }
    activeChildId?.let { id ->
        if (addKind == AddKind.SolidFood) SolidFoodDialog(id, null, { addKind = null }, ::saveAdded)
        if (addKind == AddKind.Medicine) MedicineDialog(id, null, { addKind = null }, medicines, ::saveAdded)
        if (addKind == AddKind.HealthVisit || addKind == AddKind.Vaccination) HealthRecordDialog(id, addKind == AddKind.Vaccination, careProviders, onDismiss = { addKind = null }, onSave = ::saveAdded)
    }
    if (addKind == AddKind.Sleep) SleepDialog(onDismiss = { addKind = null }) { start, end, type, location, settling, awakenings, quality, notes ->
        val childId = activeChildId
        if (childId != null) onAddSleep(childId, start, end, type, location, settling, awakenings, quality, notes) { success -> overlapError = !success }
        addKind = null
    }
    if (addKind == AddKind.Bottle) BottleDialog({ addKind = null }) { time, content, offered, consumed, notes -> activeChildId?.let { onAddBottle(it, time, content, offered, consumed, notes) }; addKind = null }
    if (addKind == AddKind.Diaper) DiaperDialog({ addKind = null }) { time, type, color, consistency, observation, notes -> activeChildId?.let { onAddDiaper(it, time, type, color, consistency, observation, notes) {} }; addKind = null }
    if (addKind == AddKind.Breastfeeding && activeChildId != null) {
        val now = remember { System.currentTimeMillis() }
        EditEventDialog(remember { CareEventEntity(childId = activeChildId, type = CareEventType.Breastfeeding, startedAt = now - 600_000, endedAt = now, leftSeconds = 600, activeSide = BreastSide.Left, timerSegments = "${now - 600_000}-$now", timerSegmentSides = "Left") }, { addKind = null }, ::saveAdded)
    }
    if (addKind == AddKind.Pumping && activeChildId != null) { val now = remember { System.currentTimeMillis() }; EditEventDialog(remember { CareEventEntity(childId = activeChildId, type = CareEventType.Pumping, startedAt = now - 600_000, endedAt = now, leftSeconds = 600, pumpingMethod = "Maskine") }, { addKind = null }, ::saveAdded) }
    if (addKind == AddKind.Measurement) MeasurementDialog(MeasurementType.Weight, { addKind = null }) { time, timeSpecified, type, value, unit, notes -> activeChildId?.let { onAddMeasurement(it, time, timeSpecified, type, value, unit, notes) }; addKind = null }
    if (addKind == AddKind.Activity) ActivityDialog(null, { addKind = null }) { start, end, type, notes -> activeChildId?.let { onAddActivity(it, start, end, type, notes) }; addKind = null }
    editing?.let { event ->
        if (event.type == CareEventType.HealthVisit || event.type == CareEventType.Vaccination) {
            HealthRecordDialog(event.childId, event.type == CareEventType.Vaccination, careProviders, event, { editing = null }) { updated ->
                onUpdate(updated) { success -> overlapError = !success; if (success) editing = null }
            }
        } else if (event.activityType == ActivityType.Medicine) MedicineDialog(event.childId, event, { editing = null }, medicines) { updated -> onUpdate(updated) { success -> if (success) editing = null } }
        else EditEventDialog(event, { editing = null }) { updated ->
            onUpdate(updated) { success -> overlapError = !success; if (success) editing = null }
        }
    }
    deleting?.let { event -> AlertDialog(
        onDismissRequest = { deleting = null }, title = { Text("Slet registrering?") },
        text = { Text("Registreringen fjernes fra journalen, men bevares internt.") },
        confirmButton = { Button(onClick = { onDelete(event); deleting = null }) { Text("Slet") } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text("Annuller") } },
    ) }
    if (overlapError) AlertDialog(
        onDismissRequest = { overlapError = false }, title = { Text("Registreringen kunne ikke gemmes") },
        text = { Text("Søvntider må ikke overlappe. Hvis ammetimeren er ændret imens, skal du lukke redigeringen og åbne den igen.") },
        confirmButton = { Button(onClick = { overlapError = false }) { Text("OK") } },
    )
    if (selectChildError) AlertDialog(
        onDismissRequest = { selectChildError = false }, title = { Text("Vælg et barn") },
        text = { Text("Vælg først det barn, registreringen skal tilføjes til.") },
        confirmButton = { Button(onClick = { selectChildError = false }) { Text("OK") } },
    )
}

private fun journalFilterLabel(label: String) = when (label) {
    "Madning · alle" -> "Madning"
    "Sundhed · alle" -> "Sundhed"
    "Ble" -> "Bleer"
    else -> label
}

@Composable
private fun JournalQuickFiltersDialog(current: List<String>, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rediger hurtigfiltre") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vælg de fire filtre, der vises i Journal.")
            selected.forEachIndexed { index, label ->
                SelectionDropdown("Filter ${index + 1}", journalFilterLabel(label), journalFilters.map { it.label to journalFilterLabel(it.label) }) { replacement ->
                    selected = selected.toMutableList().apply {
                        val other = indexOf(replacement)
                        if (other >= 0) this[other] = label
                        this[index] = replacement
                    }
                }
            }
        }
    }, confirmButton = { Button(onClick = { onSave(selected) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}
