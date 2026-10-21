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
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormHeader(when (event.type) { CareEventType.Breastfeeding -> "Rediger amning"; CareEventType.Sleep -> "Rediger søvn"; CareEventType.Pumping -> "Rediger pumpning"; else -> "Rediger ${activityType.displayLabel().lowercase()}" }, event.careCategory)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (nursing) "Ret tid og brystside for hvert interval. Pauser tæller ikke med. Et igangværende interval fortsætter, mens du redigerer."
                        else "Ret tiden for hvert interval. Pauser tæller ikke med. Et igangværende interval fortsætter, mens du redigerer.")
                    if (sleep) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SleepType.entries.forEach { option -> FilterChip(selected = sleepType == option, onClick = { sleepType = option }, label = { Text(if (option == SleepType.Nap) "Lur" else "Nattesøvn") }) }
                    }
                    intervals.forEachIndexed { index, interval ->
                        androidx.compose.material3.OutlinedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Interval ${index + 1} · ${((interval.end ?: now) - interval.start).coerceAtLeast(0) / 60000} min${if (interval.end == null) " · kører" else ""}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                if (nursing || intervals.size > 1) IconButton(onClick = { intervals = intervals.filterIndexed { i, _ -> i != index }; error = null }) {
                                    Icon(androidx.compose.material.icons.Icons.Outlined.Delete, "Slet interval ${index + 1}", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Text("Start")
                            DateTimeFields(interval.start) { time -> intervals = intervals.withStart(index, time); error = null }
                            if (interval.end != null) { Text("Slut"); DateTimeFields(interval.end) { time -> intervals = intervals.withEnd(index, time); error = null } }
                            if (nursing) SelectionDropdown("Brystside", interval.side?.let { if (it == BreastSide.Left) "Venstre" else "Højre" } ?: "Side ikke registreret", BreastSide.entries.map { it to if (it == BreastSide.Left) "Venstre" else "Højre" }) { side -> intervals = intervals.toMutableList().apply { this[index] = interval.copy(side = side) }; error = null }
                        } }
                    }
                    if (intervals.isEmpty()) Text("Alle intervaller er fjernet. Gem giver 0 minutters amning. Du kan annullere for at beholde dem.")
                    if (nursing) SelectionDropdown("Gener", issue?.displayLabel() ?: "Ingen angivet", listOf<BreastfeedingIssue?>(null).map { it to "Ingen angivet" } + BreastfeedingIssue.entries.map { it as BreastfeedingIssue? to it.displayLabel() }) { issue = it }
                    else if (event.type == CareEventType.Activity) SelectionDropdown("Type *", activityType.displayLabel(), ActivityType.entries.filter { it != ActivityType.Medicine }.map { it to it.displayLabel() }) { activityType = it }
                    else if (event.type == CareEventType.Pumping) {
                        SelectionDropdown("Pumpetype", pumpingMethod.ifBlank { "Ikke angivet" }, listOf("", "Hånd", "Maskine", "Manuel pumpe").map { it to it.ifBlank { "Ikke angivet" } }) { pumpingMethod = it }
                        RegistrationTextField(pumped, { pumped = it.filter(Char::isDigit) }, label = { Text("Pumpet (ml)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    } else {
                        RegistrationTextField(location, { location = it }, label = { Text("Sovested") })
                        RegistrationTextField(settling, { settling = it }, label = { Text("Puttemetode") })
                        RegistrationTextField(awakenings, { awakenings = it.filter(Char::isDigit) }, label = { Text("Opvågninger") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                        Text("Søvnkvalitet")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SleepQuality.entries.forEach { option -> FilterChip(selected = quality == option, onClick = { quality = if (quality == option) null else option }, label = { Text(option.displayLabel()) }) } }
                    }
                    RegistrationTextField(notes, { notes = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Noter") })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End)) {
                    androidx.compose.material3.OutlinedButton(onClick = onDismiss) { Text("Annuller") }
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
                }
            }
        }
    }
}
