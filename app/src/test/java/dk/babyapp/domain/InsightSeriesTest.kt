package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightSeriesTest {
    @Test fun `series contains empty days and daily values`() {
        val zone = ZoneId.of("Europe/Copenhagen"); val start = LocalDate.of(2026, 8, 1); val time = start.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val result = dailyInsightSeries(listOf(CareEventEntity(childId = "a", type = CareEventType.Bottle, startedAt = time, endedAt = time, amountConsumedMl = 80)), "a", start, start.plusDays(2), zone)
        assertEquals(3, result.size); assertEquals(80f, result.first().bottleMl); assertEquals(0f, result.last().bottleMl)
    }
}
