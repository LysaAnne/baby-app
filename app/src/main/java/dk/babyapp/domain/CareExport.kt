package dk.babyapp.domain

import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun createCareCsv(child: ChildProfile, events: List<CareEventEntity>, from: LocalDate, through: LocalDate, units: String, zone: ZoneId = ZoneId.systemDefault()): String {
    val rows = events.filter { event ->
        event.childId == child.id && event.deletedAt == null && !event.isDraft && Instant.ofEpochMilli(event.startedAt).atZone(zone).toLocalDate() in from..through
    }.sortedBy { it.startedAt }
    return buildString {
        appendLine("# Barn,${csv(child.name)}")
        appendLine("# Periode,$from,$through")
        appendLine("# Enheder,${csv(units)}")
        appendLine("# Tidszone,${csv(zone.id)}")
        appendLine("id,type,dato,tid,start_iso,slut_iso,varighed_sekunder,vaerdi,enhed,noter")
        rows.forEach { event ->
            val local = Instant.ofEpochMilli(event.startedAt).atZone(zone)
            val value = event.measurementValue?.toString() ?: event.amountConsumedMl?.toString() ?: event.pumpedAmountMl?.toString().orEmpty()
            appendLine(listOf(event.id, event.type.name, local.toLocalDate().toString(), if (event.timeSpecified) local.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) else "", Instant.ofEpochMilli(event.startedAt).toString(), event.endedAt?.let { Instant.ofEpochMilli(it).toString() }.orEmpty(), event.elapsedSeconds().toString(), value, event.measurementUnit, event.notes).joinToString(",", transform = ::csv))
        }
    }
}

private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
