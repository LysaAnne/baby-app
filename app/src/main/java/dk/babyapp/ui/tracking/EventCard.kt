package dk.babyapp.ui.tracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.segmentIntervals
import java.text.DateFormat
import java.util.Date

@Composable
internal fun EventCard(event: CareEventEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember(event.id) { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(event.icon(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(end = 10.dp))
                Column(Modifier.weight(1f)) {
                    Text(eventTitle(event), style = MaterialTheme.typography.titleSmall)
                    Text("${event.recordedAt()}  ·  ${event.summary()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (event.isRunning) Text("Kører nu", color = MaterialTheme.colorScheme.primary)
                }
                Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (expanded) "Fold sammen" else "Vis detaljer")
            }
            if (expanded) {
                Text(event.details(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                val intervals = event.segmentIntervals()
                if (intervals.isNotEmpty()) {
                    Text("Tidsintervaller", style = MaterialTheme.typography.labelLarge)
                    intervals.forEach { (start, end) -> Text("${formatClock(start)} – ${end?.let(::formatClock) ?: "kører"}", style = MaterialTheme.typography.bodySmall) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null); Text("Rediger") }
                    TextButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null); Text("Slet", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

internal fun eventTitle(event: CareEventEntity) = when (event.type) {
    CareEventType.Diaper -> "Ble – ${event.diaperType.displayLabel()}"
    CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"
    CareEventType.HealthVisit -> event.healthTitle.ifBlank { event.type.displayLabel() }
    CareEventType.Vaccination -> event.vaccineName.ifBlank { event.type.displayLabel() }
    else -> event.type.displayLabel()
}

private fun CareEventEntity.details() = when (type) {
    CareEventType.Breastfeeding -> "${formatDuration(elapsedSeconds())} · V ${formatDuration(leftSeconds)} · H ${formatDuration(rightSeconds)}"
    CareEventType.Bottle -> "${amountConsumedMl ?: 0} af ${amountOfferedMl ?: 0} ml · ${bottleContent.displayLabel()}"
    CareEventType.Pumping -> "${formatDuration(elapsedSeconds())}${pumpedAmountMl?.let { " · $it ml" } ?: ""}"
    CareEventType.Diaper -> listOf(observation, notes).filter(String::isNotBlank).joinToString(" · ").ifBlank { "Registreret" }
    CareEventType.Sleep -> listOf(formatDuration(elapsedSeconds()), sleepLocation, sleepQuality?.displayLabel(), notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.HealthVisit -> listOf(healthStatus?.displayLabel(), providerDisplayName, healthReason, healthObservations, healthAdvice, followUp, notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Vaccination -> listOf(healthStatus?.displayLabel(), vaccineDose, vaccineBatchNumber, injectionSite, reactionNotes, notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
}

private fun CareEventEntity.summary() = when (type) {
    CareEventType.Breastfeeding, CareEventType.Pumping, CareEventType.Sleep -> formatDuration(elapsedSeconds())
    CareEventType.Bottle -> amountConsumedMl?.let { "$it ml" } ?: bottleContent.displayLabel()
    CareEventType.Diaper -> diaperType.displayLabel()
    CareEventType.HealthVisit -> providerDisplayName.ifBlank { "Besøg" }
    CareEventType.Vaccination -> vaccineDose.ifBlank { "Vaccine" }
}

private fun CareEventEntity.recordedAt(): String = if (type == CareEventType.HealthVisit || type == CareEventType.Vaccination) {
    DateFormat.getDateInstance(DateFormat.SHORT).format(Date(startedAt))
} else {
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(startedAt))
}

private fun CareEventEntity.icon() = when (type) {
    CareEventType.Breastfeeding -> "🤱"
    CareEventType.Bottle -> "🍼"
    CareEventType.Pumping -> "🥛"
    CareEventType.Diaper -> "🧷"
    CareEventType.Sleep -> "🌙"
    CareEventType.HealthVisit -> "🩺"
    CareEventType.Vaccination -> "💉"
}

internal fun formatDuration(seconds: Long) = "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)
private fun formatClock(value: Long) = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value))
