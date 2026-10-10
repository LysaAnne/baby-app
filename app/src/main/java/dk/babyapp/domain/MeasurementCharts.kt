package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import java.time.*

data class MeasurementPoint(val at: Long, val value: Double, val timeSpecified: Boolean = true)

fun measurementPoints(events: List<CareEventEntity>, childId: String, type: MeasurementType, from: LocalDate?, through: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<MeasurementPoint> {
    val start = from?.atStartOfDay(zone)?.toInstant()?.toEpochMilli() ?: Long.MIN_VALUE
    val end = through.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return events.filter { it.childId == childId && !it.isDraft && it.deletedAt == null && it.type == CareEventType.Measurement && it.measurementType == type && it.startedAt in start until end }
        .mapNotNull { event ->
            val raw = event.measurementValue?.takeIf { it.isFinite() } ?: return@mapNotNull null
            val unit = event.measurementUnit.trim().lowercase()
            val value = when (type) {
                MeasurementType.Weight -> when (unit) { "", "kg" -> raw; "g" -> raw / 1000; "lb", "lbs" -> raw * 0.45359237; else -> return@mapNotNull null }
                MeasurementType.Height, MeasurementType.HeadCircumference -> when (unit) { "", "cm" -> raw; "mm" -> raw / 10; "in", "inch" -> raw * 2.54; else -> return@mapNotNull null }
                MeasurementType.Temperature -> when (unit) { "", "°c", "c" -> raw; "°f", "f" -> (raw - 32) * 5 / 9; else -> return@mapNotNull null }
            }
            val at = if (event.timeSpecified) event.startedAt else Instant.ofEpochMilli(event.startedAt).atZone(zone).toLocalDate().atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
            MeasurementPoint(at, value, event.timeSpecified)
        }.sortedBy { it.at }
}
