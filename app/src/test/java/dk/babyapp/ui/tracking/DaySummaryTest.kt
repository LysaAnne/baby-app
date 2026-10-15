package dk.babyapp.ui.tracking

import dk.babyapp.data.tracking.*
import dk.babyapp.domain.eventsForDay
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DaySummaryTest {
    @Test fun headSidesHaveParentTotalAndTwoCountRows() {
        val left = dk.babyapp.domain.headSideObservation("child", HeadSide.Left, 1000)
        assertEquals(listOf(SummaryRow("Favoritside", 3, "–"), SummaryRow("Venstre", 2, "–", true), SummaryRow("Højre", 1, "–", true)), daySummaryRows(listOf(left, left.copy(id = "second"), left.copy(headSide = HeadSide.Right))))
    }

    @Test fun nursingSidesCountRegistrationsAndClipAtMidnight() {
        val day = LocalDate.of(2026, 10, 11)
        val midnight = day.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val event = CareEventEntity(childId = "child", type = CareEventType.Breastfeeding,
            startedAt = midnight - 60000, endedAt = midnight + 180000,
            timerSegments = "${midnight - 60000}-${midnight + 60000};${midnight + 60000}-${midnight + 120000};${midnight + 120000}-${midnight + 180000}", timerSegmentSides = "Left;Right;Left")
        val rows = daySummaryRows(eventsForDay(listOf(event), day, ZoneOffset.UTC))
        assertEquals(listOf(SummaryRow("Amning", 1, "3 min"), SummaryRow("Venstre", 1, "2 min", true), SummaryRow("Højre", 1, "1 min", true)), rows)
    }

    @Test fun diaperSubtypesPartitionTheTotalWithoutCountingBothTwice() {
        val events = listOf(DiaperType.Wet, DiaperType.Both, DiaperType.Dirty, DiaperType.Dry, null).map { CareEventEntity(childId = "child", type = CareEventType.Diaper, startedAt = 0, diaperType = it) }
        val rows = daySummaryRows(events)
        assertEquals(SummaryRow("Bleer", 5, "–"), rows.first())
        assertEquals(5, rows.drop(1).sumOf { it.count })
        assertEquals(1, rows.single { it.label == "Begge" }.count)
    }

    @Test fun measurementsKeepUnitsSeparateUnderOneParent() {
        val weight = CareEventEntity(childId = "child", type = CareEventType.Measurement, startedAt = 0, measurementType = MeasurementType.Weight, measurementValue = 5.0, measurementUnit = "kg")
        val height = weight.copy(measurementType = MeasurementType.Height, measurementValue = 60.0, measurementUnit = "cm")
        assertEquals(listOf(SummaryRow("Mål", 2, "–"), SummaryRow("Vægt", 1, "5.0 kg", true), SummaryRow("Højde/længde", 1, "60.0 cm", true)), daySummaryRows(listOf(weight, height)))
    }
}
