package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.SleepType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightsCalculatorTest {
    private val zone = ZoneId.of("Europe/Copenhagen")
    private val day = LocalDate.of(2026, 8, 18)
    private val noon = day.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    @Test fun `summaries only include selected child and date range`() {
        val events = listOf(
            CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = noon, endedAt = noon + 600_000, leftSeconds = 600),
            CareEventEntity(childId = "a", type = CareEventType.Bottle, startedAt = noon, endedAt = noon, amountConsumedMl = 90),
            CareEventEntity(childId = "a", type = CareEventType.Diaper, startedAt = noon, endedAt = noon, diaperType = DiaperType.Both, observation = "notat"),
            CareEventEntity(childId = "a", type = CareEventType.Sleep, startedAt = noon, endedAt = noon + 7_200_000, leftSeconds = 7_200, sleepType = SleepType.Nap),
            CareEventEntity(childId = "b", type = CareEventType.Bottle, startedAt = noon, endedAt = noon, amountConsumedMl = 500),
        )
        val result = calculateInsights(events, "a", day, day, zone)
        assertEquals(2, result.feedingCount)
        assertEquals(10, result.breastfeedingMinutes)
        assertEquals(90, result.bottleMl)
        assertEquals(1, result.diaperCount)
        assertEquals(1, result.wetDiapers)
        assertEquals(1, result.dirtyDiapers)
        assertEquals(120, result.sleepMinutes)
        assertEquals(1, result.napCount)
    }
}
