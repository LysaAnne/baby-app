package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.nursingSideSeconds

internal data class SummaryRow(val label: String, val count: Int, val details: String, val subordinate: Boolean = false)

/** Input is already scoped to the active child and sliced to the selected day. */
internal fun daySummaryRows(events: List<CareEventEntity>): List<SummaryRow> = buildList {
    events.groupBy { it.type }.toSortedMap(compareBy { it.ordinal }).forEach { (type, entries) ->
        add(SummaryRow(if (type == CareEventType.Diaper) "Bleer" else type.displayLabel(), entries.size, summaryDetails(type, entries)))
        if (type == CareEventType.HeadSide) {
            HeadSide.entries.forEach { side -> add(SummaryRow(side.displayLabel(), entries.count { it.headSide == side }, "–", true)) }
            if (entries.any { it.headSide == null }) add(SummaryRow("Side ikke angivet", entries.count { it.headSide == null }, "–", true))
        } else if (type == CareEventType.Breastfeeding) {
            BreastSide.entries.forEach { side ->
                val seconds = entries.map { nursingSideSeconds(it, side) }
                add(SummaryRow(if (side == BreastSide.Left) "Venstre" else "Højre", seconds.count { it > 0 }, "${seconds.sum() / 60} min", true))
            }
            val unknown = entries.map { (it.elapsedSeconds() - nursingSideSeconds(it, BreastSide.Left) - nursingSideSeconds(it, BreastSide.Right)).coerceAtLeast(0) }
            if (unknown.any { it > 0 }) add(SummaryRow("Side ikke angivet", unknown.count { it > 0 }, "${unknown.sum() / 60} min", true))
        } else {
            val subgroups = when (type) {
                CareEventType.Diaper -> entries.groupBy { it.diaperType?.displayLabel() ?: "Ikke angivet" }
                CareEventType.Bottle -> entries.groupBy { it.bottleContent.displayLabel() }
                CareEventType.Sleep -> entries.groupBy { when (it.sleepType) { SleepType.Nap -> "Lur"; SleepType.Night -> "Nattesøvn"; null -> "Ikke angivet" } }
                CareEventType.Pumping -> entries.groupBy { it.pumpingMethod.ifBlank { "Ikke angivet" } }
                CareEventType.Measurement -> entries.groupBy { it.measurementType?.displayLabel() ?: "Ikke angivet" }
                CareEventType.Activity -> entries.groupBy { it.activityType?.displayLabel() ?: "Ikke angivet" }
                CareEventType.HealthVisit -> entries.groupBy { it.healthVisitType?.displayLabel() ?: "Ikke angivet" }
                else -> emptyMap()
            }
            subgroups.forEach { (label, children) ->
                add(SummaryRow(label, children.size, summaryDetails(type, children, subordinate = true), true))
            }
        }
    }
}

private fun summaryDetails(type: CareEventType, entries: List<CareEventEntity>, subordinate: Boolean = false): String = when (type) {
    CareEventType.Breastfeeding, CareEventType.Sleep -> "${entries.sumOf { it.elapsedSeconds() } / 60} min"
    CareEventType.Pumping -> "${entries.sumOf { it.pumpedAmountMl ?: 0 }} ml · ${entries.sumOf { it.elapsedSeconds() } / 60} min"
    CareEventType.Bottle -> "${entries.sumOf { it.amountConsumedMl ?: 0 }} ml spist"
    CareEventType.Measurement -> if (subordinate) entries.joinToString(" · ") { "${it.measurementValue ?: "–"} ${it.measurementUnit}" } else "–"
    CareEventType.Activity -> if (!subordinate) "–" else if (entries.first().activityType == ActivityType.Medicine) entries.map { it.medicationName }.filter { it.isNotBlank() }.distinct().joinToString(", ") else "${entries.sumOf { it.activityDurationSeconds ?: it.elapsedSeconds() } / 60} min"
    CareEventType.SolidFood -> entries.map { it.foodName }.filter { it.isNotBlank() }.distinct().joinToString(", ")
    else -> "–"
}

@Composable
internal fun DaySummary(events: List<CareEventEntity>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Type", Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium)
            Text("Antal", Modifier.width(38.dp), style = MaterialTheme.typography.labelMedium)
            Text("I alt / detaljer", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
        }
        daySummaryRows(events).forEach { row ->
            if (!row.subordinate) HorizontalDivider()
            val weight = if (row.subordinate) FontWeight.Normal else FontWeight.Bold
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(row.label, Modifier.weight(1.3f).padding(start = if (row.subordinate) 12.dp else 0.dp), style = MaterialTheme.typography.bodySmall, fontWeight = weight)
                Text(row.count.toString(), Modifier.width(38.dp), style = MaterialTheme.typography.bodySmall, fontWeight = weight)
                Text(row.details, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = weight)
            }
        }
        HorizontalDivider()
        Text("${events.size} registreringer i alt", style = MaterialTheme.typography.labelMedium)
        if (events.any { it.type == CareEventType.Breastfeeding }) Text("Antal pr. side er amninger med tid på siden. En amning med begge sider tæller én gang under hver side.", style = MaterialTheme.typography.bodySmall)
    }
}
