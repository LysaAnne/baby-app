package dk.babyapp.ui.tracking

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.profile.CareProvider
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

private enum class TimelineRange(val days: Long?) { Today(1), Week(7), All(null) }
private enum class AddKind { Breastfeeding, Bottle, Pumping, Diaper, Sleep }

@Composable
fun TimelineScreen(
    activeChildId: String?,
    events: List<CareEventEntity>,
    careProviders: List<CareProvider>,
    contentPadding: PaddingValues,
    onAddSleep: (String, Long, Long, SleepType, String, String, Int?, SleepQuality?, String, (Boolean) -> Unit) -> Unit,
    onAddBottle: (String, Long, BottleContent, Int?, Int?, String) -> Unit,
    onAddDiaper: (String, Long, DiaperType, String, String, (CareEventEntity) -> Unit) -> Unit,
    onAddManualTimer: (String, CareEventType, Long, Long, BreastSide?, Int?, String) -> Unit,
    onUpdate: (CareEventEntity, (Boolean) -> Unit) -> Unit,
    onDelete: (CareEventEntity) -> Unit,
) {
    var typeFilter by remember { mutableStateOf<CareEventType?>(null) }
    var range by remember { mutableStateOf(TimelineRange.Week) }
    var addMenu by remember { mutableStateOf(false) }
    var addKind by remember { mutableStateOf<AddKind?>(null) }
    var editing by remember { mutableStateOf<CareEventEntity?>(null) }
    var deleting by remember { mutableStateOf<CareEventEntity?>(null) }
    var overlapError by remember { mutableStateOf(false) }
    var selectChildError by remember { mutableStateOf(false) }
    var expandedDays by remember { mutableStateOf(setOf(LocalDate.now())) }
    val zone = ZoneId.systemDefault()
    val firstDate = range.days?.let { LocalDate.now().minusDays(it - 1) }
    val filtered = events.asSequence()
        .filter { it.childId == activeChildId }
        .filter { typeFilter == null || it.type == typeFilter }
        .filter { firstDate == null || Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() >= firstDate }
        .toList()
    val groups = filtered.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() }.toSortedMap(compareByDescending { it })

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { if (activeChildId != null) addMenu = true else selectChildError = true }) {
                Icon(Icons.Outlined.Add, "Tilføj søvn")
            }
        },
    ) { inner ->
        LazyColumn(
            contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + inner.calculateTopPadding() + 12.dp, bottom = contentPadding.calculateBottomPadding() + 88.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Text("Tidslinje", style = MaterialTheme.typography.headlineSmall) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(typeFilter == null, { typeFilter = null }, { Text("Alle typer") })
                    CareEventType.entries.forEach { type -> FilterChip(typeFilter == type, { typeFilter = type }, { Text(typeLabel(type)) }) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimelineRange.entries.forEach { option -> FilterChip(range == option, { range = option }, { Text(when (option) { TimelineRange.Today -> "I dag"; TimelineRange.Week -> "7 dage"; TimelineRange.All -> "Alle" }) }) }
                }
            }
            if (groups.isEmpty()) item { Text("Ingen registreringer matcher filtrene.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            groups.forEach { (date, dayEvents) ->
                item(key = "day-$date") {
                    TextButton(onClick = { expandedDays = if (date in expandedDays) expandedDays - date else expandedDays + date }) {
                        Text("${if (date in expandedDays) "▾" else "▸"} ${date.format(DateTimeFormatter.ofPattern("EEEE d. MMMM"))}", style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (date in expandedDays) items(dayEvents, key = { it.id }) { event ->
                    EventCard(event, onEdit = { editing = event }, onDelete = { deleting = event })
                }
            }
        }
    }
    if (addMenu) AlertDialog(
        onDismissRequest = { addMenu = false }, title = { Text("Tilføj registrering") },
        text = {
            Column {
                AddKind.entries.forEach { kind ->
                    TextButton(onClick = { addMenu = false; addKind = kind }) {
                        Text(when (kind) {
                            AddKind.Breastfeeding -> "Amning"
                            AddKind.Bottle -> "Flaske"
                            AddKind.Pumping -> "Pumpning"
                            AddKind.Diaper -> "Ble"
                            AddKind.Sleep -> "Søvn"
                        })
                    }
                }
            }
        },
        confirmButton = {}, dismissButton = { TextButton(onClick = { addMenu = false }) { Text("Annuller") } },
    )
    if (addKind == AddKind.Sleep) SleepDialog(onDismiss = { addKind = null }) { start, end, type, location, settling, awakenings, quality, notes ->
        val childId = activeChildId
        if (childId != null) onAddSleep(childId, start, end, type, location, settling, awakenings, quality, notes) { success -> overlapError = !success }
        addKind = null
    }
    if (addKind == AddKind.Bottle) BottleDialog({ addKind = null }) { time, content, offered, consumed, notes -> activeChildId?.let { onAddBottle(it, time, content, offered, consumed, notes) }; addKind = null }
    if (addKind == AddKind.Diaper) DiaperDialog({ addKind = null }) { time, type, observation, notes -> activeChildId?.let { onAddDiaper(it, time, type, observation, notes) {} }; addKind = null }
    if (addKind == AddKind.Breastfeeding) ManualTimerDialog(CareEventType.Breastfeeding, { addKind = null }) { type, start, end, side, amount, notes -> activeChildId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; addKind = null }
    if (addKind == AddKind.Pumping) ManualTimerDialog(CareEventType.Pumping, { addKind = null }) { type, start, end, side, amount, notes -> activeChildId?.let { onAddManualTimer(it, type, start, end, side, amount, notes) }; addKind = null }
    editing?.let { event ->
        if (event.type == CareEventType.HealthVisit || event.type == CareEventType.Vaccination) {
            HealthRecordDialog(event.childId, event.type == CareEventType.Vaccination, careProviders, event, { editing = null }) { updated ->
                onUpdate(updated) { success -> overlapError = !success; if (success) editing = null }
            }
        } else EditEventDialog(event, { editing = null }) { updated ->
            onUpdate(updated) { success -> overlapError = !success; if (success) editing = null }
        }
    }
    deleting?.let { event -> AlertDialog(
        onDismissRequest = { deleting = null }, title = { Text("Slet registrering?") },
        text = { Text("Registreringen fjernes fra tidslinjen, men bevares internt.") },
        confirmButton = { Button(onClick = { onDelete(event); deleting = null }) { Text("Slet") } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text("Annuller") } },
    ) }
    if (overlapError) AlertDialog(
        onDismissRequest = { overlapError = false }, title = { Text("Søvnen overlapper") },
        text = { Text("Der findes allerede søvn i dette tidsrum.") },
        confirmButton = { Button(onClick = { overlapError = false }) { Text("OK") } },
    )
    if (selectChildError) AlertDialog(
        onDismissRequest = { selectChildError = false }, title = { Text("Vælg et barn") },
        text = { Text("Vælg først det barn, registreringen skal tilføjes til.") },
        confirmButton = { Button(onClick = { selectChildError = false }) { Text("OK") } },
    )
}

private fun typeLabel(type: CareEventType) = when (type) {
    CareEventType.Breastfeeding -> "Amning"
    CareEventType.Bottle -> "Flaske"
    CareEventType.Pumping -> "Pumpning"
    CareEventType.Diaper -> "Ble"
    CareEventType.Sleep -> "Søvn"
    CareEventType.HealthVisit -> "Sundhedsbesøg"
    CareEventType.Vaccination -> "Vaccination"
}
