package dk.babyapp.ui.tracking

import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import dk.babyapp.ui.theme.babyColors
import dk.babyapp.ui.theme.careCategory
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.IconButton

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.shieldForInterval
import dk.babyapp.data.tracking.segmentIntervals
import java.text.DateFormat
import java.util.Date

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun EventCard(event: CareEventEntity, expandAll: Boolean? = null, onEdit: () -> Unit, onDelete: () -> Unit, onResume: (() -> Unit)? = null, onMeasurements: (() -> Unit)? = null) {
    var expanded by remember(event.id) { mutableStateOf(false) }
    var menuOpen by remember(event.id) { mutableStateOf(false) }
    LaunchedEffect(expandAll) { expandAll?.let { expanded = it } }
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    val colors = MaterialTheme.babyColors.category(event.careCategory)
    val canEdit = event.endedAt != null || event.type == CareEventType.Breastfeeding
    val canResume = onResume != null && event.type == CareEventType.Breastfeeding && event.endedAt != null
    androidx.compose.material3.Surface(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Box {
            // Tap shows details; holding a registration opens Rediger and Slet.
            Column(
                Modifier.fillMaxWidth()
                    .combinedClickable(onClickLabel = if (expanded) "Fold sammen" else "Vis detaljer", onLongClickLabel = "Flere valg", onClick = { expanded = !expanded }, onLongClick = {
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); menuOpen = true
                    })
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(36.dp).background(colors.container, CircleShape), contentAlignment = Alignment.Center) { Text(event.icon(), style = MaterialTheme.typography.titleMedium) }
                    Column(Modifier.weight(1f)) {
                        Text(eventTitle(event), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("${event.recordedAt()}  ·  ${event.summary()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        if (event.isRunning) Text("Kører nu", style = MaterialTheme.typography.labelMedium, color = colors.accent)
                    }
                    if (event.type == CareEventType.Measurement && onMeasurements != null) IconButton(onClick = onMeasurements) { Icon(Icons.Outlined.ShowChart, "Vis målegrafer") }
                    Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (expanded) "Fold sammen" else "Vis detaljer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (expanded) Column(Modifier.padding(start = 46.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    event.details().takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    val recordedIntervals = event.segmentIntervals()
                    val intervals = if (recordedIntervals.isEmpty() && event.type == CareEventType.Breastfeeding) listOf(event.startedAt to event.endedAt) else recordedIntervals
                    if (intervals.isNotEmpty()) {
                        Text("Tidsintervaller", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        intervals.forEachIndexed { index, (start, end) ->
                            val side = when ((if (recordedIntervals.isEmpty() && (event.leftSeconds == 0L || event.rightSeconds == 0L)) event.activeSide?.name else event.timerSegmentSides.split(';').getOrNull(index))) { "Left" -> "Venstre"; "Right" -> "Højre"; else -> "Side ikke registreret" }
                            Text(compactInterval(start, end, anchor = event.startedAt) + if (event.type == CareEventType.Breastfeeding) (" · $side") else "", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (canResume) TextButton(onClick = { onResume?.invoke() }) { Text("Fortsæt amning") }
                        if (canEdit) TextButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp)); Text(" Rediger") }
                        TextButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error); Text(" Slet", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
            androidx.compose.material3.DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                if (canEdit) androidx.compose.material3.DropdownMenuItem(text = { Text("Rediger") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menuOpen = false; onEdit() })
                if (canResume) androidx.compose.material3.DropdownMenuItem(text = { Text("Fortsæt amning") }, leadingIcon = { Icon(Icons.Outlined.PlayArrow, null) }, onClick = { menuOpen = false; onResume?.invoke() })
                androidx.compose.material3.DropdownMenuItem(text = { Text("Slet", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { menuOpen = false; onDelete() })
            }
        }
    }
}

internal fun eventTitle(event: CareEventEntity) = when (event.type) {
    CareEventType.HeadSide -> "Favoritside – ${event.headSide.displayLabel()}"
    CareEventType.SolidFood -> "Fast føde – ${event.foodName}"
    CareEventType.Diaper -> event.diaperType?.let { "Ble – ${it.displayLabel()}" } ?: "Ble"
    CareEventType.Sleep -> if (event.sleepType == SleepType.Night) "Nattesøvn" else "Lur"
    CareEventType.Measurement -> event.measurementType?.displayLabel() ?: event.type.displayLabel()
    CareEventType.Activity -> if (event.activityType == dk.babyapp.data.tracking.ActivityType.Medicine) event.medicationName.ifBlank { "Medicin" } else event.activityType?.displayLabel() ?: "Aktivitet"
    CareEventType.HealthVisit -> event.healthTitle.ifBlank { event.type.displayLabel() }
    CareEventType.Vaccination -> event.vaccineName.ifBlank { event.type.displayLabel() }
    else -> event.type.displayLabel()
}

internal fun CareEventEntity.details() = when (type) {
    CareEventType.HeadSide -> listOf(headSide.displayLabel(), notes).filter(String::isNotBlank).joinToString(" · ")
    CareEventType.SolidFood -> listOf(foodName, foodTexture, foodAmount, foodReaction, notes).filter(String::isNotBlank).joinToString(" · ")
    CareEventType.Breastfeeding -> listOf("${formatDuration(elapsedSeconds())} · V ${formatDuration(leftSeconds)} · H ${formatDuration(rightSeconds)}", breastfeedingIssue?.displayLabel(), notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Bottle -> listOf((if (amountConsumedMl == null && amountOfferedMl == null) "Mængde ikke angivet" else "${amountConsumedMl ?: 0} af ${amountOfferedMl ?: amountConsumedMl ?: 0} ml") + " · ${bottleContent?.displayLabel() ?: "indhold ikke angivet"}", bottleMixRatio.takeIf { it.isNotBlank() && bottleContent == dk.babyapp.data.tracking.BottleContent.Mixed }?.let { "Modermælk : erstatning $it" }.orEmpty(), notes).filter(String::isNotBlank).joinToString(" · ")
    CareEventType.Pumping -> listOf(elapsedSeconds().takeIf { it > 0 }?.let { formatDuration(it) }, pumpedAmountMl?.let { "$it ml" }, pumpingMethod, notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Diaper -> listOf(diaperColor?.displayLabel(), diaperConsistency?.displayLabel(), observation, notes).filter { !it.isNullOrBlank() }.joinToString(" · ").ifBlank { "Registreret" }
    CareEventType.Sleep -> listOf(formatDuration(elapsedSeconds()), sleepLocation, settlingMethod, awakenings?.let { "$it opvågninger" }, sleepQuality?.displayLabel(), notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Measurement -> listOf(measurementValue?.let { formatMeasurement(it, measurementUnit) }, notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Activity -> if (activityType == dk.babyapp.data.tracking.ActivityType.Medicine) listOf(medicationName, medicationDose, notes).filter { it.isNotBlank() }.joinToString(" · ") else listOf(activityDurationSeconds?.let(::formatDuration), notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.HealthVisit -> listOf(healthStatus?.displayLabel(), providerDisplayName, healthReason, healthObservations, healthAdvice, healthQuestions, followUp, notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
    CareEventType.Vaccination -> listOf(healthStatus?.displayLabel(), providerDisplayName, vaccineDose, vaccineBatchNumber, injectionSite, reactionNotes, notes).filter { !it.isNullOrBlank() }.joinToString(" · ")
}

internal fun CareEventEntity.summary() = when (type) {
    CareEventType.HeadSide -> headSide.displayLabel()
    CareEventType.SolidFood -> foodAmount.ifBlank { foodTexture.ifBlank { "Måltid" } }
    CareEventType.Pumping -> pumpedAmountMl?.let { "$it ml pumpet" } ?: "Mængde ikke angivet"
    CareEventType.Breastfeeding, CareEventType.Sleep -> formatDuration(elapsedSeconds())
    CareEventType.Bottle -> amountConsumedMl?.let { "$it ml" } ?: bottleContent?.displayLabel() ?: "Mængde ikke angivet"
    CareEventType.Diaper -> diaperType?.displayLabel() ?: "Type ikke angivet"
    CareEventType.Measurement -> measurementValue?.let { formatMeasurement(it, measurementUnit) } ?: "Måling"
    CareEventType.Activity -> if (activityType == dk.babyapp.data.tracking.ActivityType.Medicine) medicationDose.ifBlank { "Medicin" } else activityDurationSeconds?.let(::formatDuration) ?: "Type ikke angivet"
    CareEventType.HealthVisit -> providerDisplayName.ifBlank { "Besøg" }
    CareEventType.Vaccination -> vaccineDose.ifBlank { "Vaccine" }
}

private fun CareEventEntity.recordedAt(): String = if (!timeSpecified) {
    DateFormat.getDateInstance(DateFormat.SHORT).format(Date(startedAt))
} else {
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(startedAt))
}

private fun CareEventEntity.icon() = when (type) {
    CareEventType.HeadSide -> "↔"
    CareEventType.SolidFood -> "🥣"
    CareEventType.Breastfeeding -> "🤱"
    CareEventType.Bottle -> "🍼"
    CareEventType.Pumping -> "🥛"
    CareEventType.Diaper -> "🧷"
    CareEventType.Sleep -> "🌙"
    CareEventType.Measurement -> "📏"
    CareEventType.Activity -> "🧸"
    CareEventType.HealthVisit -> "🩺"
    CareEventType.Vaccination -> "💉"
}

internal fun formatDuration(seconds: Long) = "%02d:%02d:%02d".format(seconds / 3600, (seconds % 3600) / 60, seconds % 60)
private fun formatClock(value: Long) = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(value))

internal fun compactInterval(start: Long, end: Long?, now: Long = System.currentTimeMillis(), anchor: Long = start): String {
    val zone = java.time.ZoneId.systemDefault()
    fun date(at: Long) = java.time.Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
    val clock = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    fun time(at: Long) = java.time.Instant.ofEpochMilli(at).atZone(zone).format(clock)
    val endText = end?.let { if (date(it) == date(start)) time(it) else "${date(it).format(java.time.format.DateTimeFormatter.ofPattern("d/M"))} ${time(it)}" } ?: "nu"
    val minutes = ((end ?: now) - start).coerceAtLeast(0) / 60000.0
    val startText = if (date(start) == date(anchor)) time(start) else "${date(start).format(java.time.format.DateTimeFormatter.ofPattern("d/M"))} ${time(start)}"
    return "$startText–$endText · ${String.format(java.util.Locale.forLanguageTag("da"), "%.1f", minutes)} min"
}

/** A measurement the Danish way: decimal comma and no ".0", so 5200.0 g reads 5200 g and 5.2 kg reads 5,2 kg. */
internal fun formatMeasurement(value: Double, unit: String): String =
    "${java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString().replace('.', ',')} $unit".trim()
