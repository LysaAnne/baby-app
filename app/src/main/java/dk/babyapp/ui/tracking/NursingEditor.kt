package dk.babyapp.ui.tracking

import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*
import dk.babyapp.ui.theme.careCategory

@Composable
internal fun NursingIntervalOverview(event: CareEventEntity, now: Long) {
    val intervals = event.timerIntervals()
    val nursing = event.type == CareEventType.Breastfeeding
    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    fun time(at: Long) = java.time.Instant.ofEpochMilli(at).atZone(java.time.ZoneId.systemDefault()).format(formatter)
    // One line per interval: number (and side for nursing), the clock times, and its length.
    Column(Modifier.fillMaxWidth().heightIn(max = 160.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (intervals.isEmpty()) Text("Ingen intervaller endnu", style = MaterialTheme.typography.bodySmall)
        intervals.forEachIndexed { index, interval ->
            val side = when (interval.side) { BreastSide.Left -> "Venstre"; BreastSide.Right -> "Højre"; null -> "Side ikke registreret" }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Interval ${index + 1}" + if (nursing) " · $side" else "", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("${time(interval.start)} – ${interval.end?.let { time(it) } ?: if (event.runningSince != null) "kører" else "pause"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("  " + formatDuration(((interval.end ?: now) - interval.start).coerceAtLeast(0) / 1000), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Edits the intervals of a nursing or sleep timer. Moving a start or end pushes the neighbouring interval along. */
@Composable
internal fun NursingEditor(event: CareEventEntity, onDismiss: () -> Unit, onSave: (CareEventEntity) -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.id) { while (event.endedAt == null) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val nursing = event.type == CareEventType.Breastfeeding
    var intervals by remember(event.id) { mutableStateOf(event.timerIntervals()) }
    var notes by remember(event.id) { mutableStateOf(event.notes) }
    var issue by remember(event.id) { mutableStateOf(event.breastfeedingIssue) }
    var sleepType by remember(event.id) { mutableStateOf(event.sleepType ?: SleepType.Nap) }
    var location by remember(event.id) { mutableStateOf(event.sleepLocation) }
    var settling by remember(event.id) { mutableStateOf(event.settlingMethod) }
    var awakenings by remember(event.id) { mutableStateOf(event.awakenings?.toString().orEmpty()) }
    var quality by remember(event.id) { mutableStateOf(event.sleepQuality) }
    var activityType by remember(event.id) { mutableStateOf(event.activityType ?: ActivityType.Other) }
    var pumpingMethod by remember(event.id) { mutableStateOf(event.pumpingMethod) }
    var pumped by remember(event.id) { mutableStateOf(event.pumpedAmountMl?.toString().orEmpty()) }
    val sleep = event.type == CareEventType.Sleep
    var error by remember { mutableStateOf<String?>(null) }
    FormScreen(when (event.type) { CareEventType.Breastfeeding -> "Rediger amning"; CareEventType.Sleep -> "Rediger søvn"; CareEventType.Pumping -> "Rediger pumpning"; else -> "Rediger ${activityType.displayLabel().lowercase()}" }, event.careCategory, onDismiss, footer = {
                    TextButton(onClick = onDismiss) { Text("Annuller") }
                    Button(onClick = {
                        runCatching {
                            val edited = event.editTimerIntervals(intervals, System.currentTimeMillis()).copy(notes = notes)
                            when (event.type) {
                                CareEventType.Breastfeeding -> edited.copy(breastfeedingIssue = issue)
                                CareEventType.Activity -> edited.copy(activityType = activityType)
                                CareEventType.Pumping -> edited.copy(pumpingMethod = pumpingMethod, pumpedAmountMl = pumped.toIntOrNull())
                                else -> edited.copy(sleepType = sleepType, sleepLocation = location, settlingMethod = settling, awakenings = awakenings.toIntOrNull(), sleepQuality = quality)
                            }
                        }.onSuccess(onSave).onFailure { error = it.message ?: "Kontrollér intervallerne." }
                    }) { Text("Gem") }
    }) {
                    if (sleep) ChoiceChips(SleepType.entries.map { it to if (it == SleepType.Nap) "Lur" else "Nattesøvn" }, sleepType) { it?.let { sleepType = it } }
                    if (event.type == CareEventType.Activity) ChoiceChips(ActivityType.entries.filter { it != ActivityType.Medicine }.map { it to it.displayLabel() }, activityType) { it?.let { activityType = it } }
                    if (event.type == CareEventType.Pumping) AmountField(pumped, { pumped = it }, "Pumpet (ml)")
                    Text("Pauser tæller ikke med. Et igangværende interval fortsætter, mens du redigerer.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    intervals.forEachIndexed { index, interval ->
                        androidx.compose.material3.Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Interval ${index + 1} · ${((interval.end ?: now) - interval.start).coerceAtLeast(0) / 60000} min${if (interval.end == null) " · kører" else ""}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                if (nursing || intervals.size > 1) IconButton(onClick = { intervals = intervals.filterIndexed { i, _ -> i != index }; error = null }) {
                                    Icon(androidx.compose.material.icons.Icons.Outlined.Delete, "Slet interval ${index + 1}", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Text("Start", style = MaterialTheme.typography.labelMedium)
                            DateTimeFields(interval.start) { time -> intervals = intervals.withStart(index, time); error = null }
                            if (interval.end != null) { Text("Slut", style = MaterialTheme.typography.labelMedium); DateTimeFields(interval.end) { time -> intervals = intervals.withEnd(index, time); error = null } }
                            if (nursing) ChoiceChips(BreastSide.entries.map { it to if (it == BreastSide.Left) "Venstre" else "Højre" }, interval.side) { side -> side?.let { intervals = intervals.toMutableList().apply { this[index] = interval.copy(side = it) }; error = null } }
                        } }
                    }
                    if (intervals.isEmpty()) Text("Alle intervaller er fjernet. Gem giver 0 minutters amning. Du kan annullere for at beholde dem.")
                    MoreDetails(when (event.type) { CareEventType.Breastfeeding -> "gener, noter"; CareEventType.Sleep -> "sted, puttemetode, opvågninger, kvalitet, noter"; CareEventType.Pumping -> "pumpetype, noter"; else -> "noter" },
                        notes.isNotBlank() || issue != null || listOf(location, settling, awakenings).any(String::isNotBlank) || quality != null || pumpingMethod.isNotBlank()) {
                        when (event.type) {
                            CareEventType.Breastfeeding -> ChoiceChips(BreastfeedingIssue.entries.map { it to it.displayLabel() }, issue, allowNone = true, label = "Gener") { issue = it }
                            CareEventType.Pumping -> PumpingMethodChips(pumpingMethod) { pumpingMethod = it }
                            CareEventType.Sleep -> {
                                SleepPlaceField(location) { location = it }
                                RegistrationTextField(settling, { settling = it }, label = { Text("Puttemetode") })
                                RegistrationTextField(awakenings, { awakenings = it.filter(Char::isDigit) }, label = { Text("Opvågninger") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                                ChoiceChips(SleepQuality.entries.map { it to it.displayLabel() }, quality, allowNone = true, label = "Søvnkvalitet") { quality = it }
                            }
                            else -> Unit
                        }
                        RegistrationTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Noter") })
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    }
}
