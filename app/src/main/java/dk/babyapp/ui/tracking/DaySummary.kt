package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.*

@Composable
internal fun DaySummary(events: List<CareEventEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("Type", Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium)
            Text("Antal", Modifier.width(42.dp), style = MaterialTheme.typography.labelMedium)
            Text("I alt / detaljer", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
        }
        HorizontalDivider()
        events.sortedWith(compareBy<CareEventEntity> { it.type.ordinal }.thenBy { it.activityType?.ordinal }.thenBy { it.measurementType?.ordinal }).groupBy { event ->
            when (event.type) {
                CareEventType.Activity -> event.activityType?.displayLabel() ?: "Aktivitet"
                CareEventType.Measurement -> event.measurementType?.displayLabel() ?: "Mål"
                else -> event.type.displayLabel()
            }
        }.forEach { (label, entries) ->
            val details = when (entries.first().type) {
                CareEventType.Breastfeeding -> "${entries.sumOf { it.elapsedSeconds() } / 60} min i alt\nVenstre ${entries.sumOf { dk.babyapp.domain.nursingSideSeconds(it, BreastSide.Left) } / 60} min\nHøjre ${entries.sumOf { dk.babyapp.domain.nursingSideSeconds(it, BreastSide.Right) } / 60} min"
                CareEventType.Sleep -> "${entries.sumOf { it.elapsedSeconds() } / 60} min"
                CareEventType.Pumping -> "${entries.sumOf { it.pumpedAmountMl ?: 0 }} ml · ${entries.sumOf { it.elapsedSeconds() } / 60} min"
                CareEventType.Bottle -> "${entries.sumOf { it.amountConsumedMl ?: 0 }} ml spist"
                CareEventType.Diaper -> "${entries.count { it.diaperType in setOf(DiaperType.Wet, DiaperType.Both) }} våd · ${entries.count { it.diaperType in setOf(DiaperType.Dirty, DiaperType.Both) }} afføring"
                CareEventType.Measurement -> entries.joinToString(" · ") { "${it.measurementValue ?: "–"} ${it.measurementUnit}" }
                CareEventType.Activity -> if (entries.first().activityType == ActivityType.Medicine) entries.map { it.medicationName }.filter { it.isNotBlank() }.distinct().joinToString(", ") else "${entries.sumOf { it.activityDurationSeconds ?: it.elapsedSeconds() } / 60} min"
                CareEventType.SolidFood -> entries.map { it.foodName }.filter { it.isNotBlank() }.distinct().joinToString(", ")
                else -> "–"
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, Modifier.weight(1.3f), style = MaterialTheme.typography.bodySmall)
                Text(entries.size.toString(), Modifier.width(38.dp), style = MaterialTheme.typography.bodySmall)
                Text(details, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            }
            HorizontalDivider()
        }
        Text("${events.size} registreringer i alt", style = MaterialTheme.typography.labelMedium)
    }
}
