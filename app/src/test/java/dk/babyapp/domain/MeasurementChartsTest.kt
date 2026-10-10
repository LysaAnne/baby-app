package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class MeasurementChartsTest {
    private val day = LocalDate.of(2026, 10, 10)
    private val start = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    private val weight = CareEventEntity(childId = "a", type = CareEventType.Measurement, startedAt = start, measurementType = MeasurementType.Weight, measurementValue = 4500.0, measurementUnit = "g")
    @Test fun measurementsWithoutTimeKeepDateOnly() {
        val point = measurementPoints(listOf(weight.copy(timeSpecified = false)), "a", MeasurementType.Weight, day, day, ZoneOffset.UTC).single()
        assertFalse(point.timeSpecified)
        assertEquals(day.atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli(), point.at)
    }
    @Test fun filtersDatesChildrenDraftsAndDeletionAndConvertsUnits() {
        val entries = listOf(weight, weight.copy(childId = "b"), weight.copy(isDraft = true), weight.copy(deletedAt = start), weight.copy(startedAt = start - 1), weight.copy(startedAt = start + 86400000))
        assertEquals(listOf(MeasurementPoint(start, 4.5)), measurementPoints(entries, "a", MeasurementType.Weight, day, day, ZoneOffset.UTC))
    }
    @Test fun pointsAreChronologicalAndTemperatureIsCelsius() {
        val entries = listOf(weight.copy(startedAt = start + 1000, measurementType = MeasurementType.Temperature, measurementValue = 100.4, measurementUnit = "°F"), weight.copy(measurementType = MeasurementType.Temperature, measurementValue = 37.0, measurementUnit = "°C"))
        val points = measurementPoints(entries, "a", MeasurementType.Temperature, null, day, ZoneOffset.UTC)
        assertEquals(start, points.first().at)
        assertEquals(38.0, points.last().value, 0.00001)
    }
    @Test fun unsupportedAndNonFiniteValuesAreExcluded() {
        assertTrue(measurementPoints(listOf(weight.copy(measurementUnit = "unknown"), weight.copy(measurementValue = Double.NaN)), "a", MeasurementType.Weight, null, day, ZoneOffset.UTC).isEmpty())
    }
}
