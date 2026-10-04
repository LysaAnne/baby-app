package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class DailyOverviewTest {
    @Test fun nursingAcrossMidnightExcludesTheGapAndSplitsDays() {
        val zone = ZoneId.of("Europe/Copenhagen")
        val day = LocalDate.of(2026, 10, 2)
        val midnight = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val event = CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = midnight - 600000, endedAt = midnight + 1200000,
            timerSegments = "${midnight - 600000}-${midnight + 300000};${midnight + 900000}-${midnight + 1200000}")
        assertEquals(600L, eventsForDay(listOf(event), day, zone).single().elapsedSeconds())
        assertEquals(600L, eventsForDay(listOf(event), day.plusDays(1), zone).single().elapsedSeconds())
        assertTrue(eventsForDay(listOf(event.copy(isDraft = true)), day, zone).isEmpty())
        assertTrue(eventsForDay(listOf(event.copy(deletedAt = midnight)), day, zone).isEmpty())
    }
    @Test fun clockChangeUsesActualDayBoundaries() {
        val zone = ZoneId.of("Europe/Copenhagen")
        val date = LocalDate.of(2026, 10, 25)
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val sleep = CareEventEntity(childId = "a", type = CareEventType.Sleep, startedAt = start, endedAt = end, timerSegments = "$start-$end")
        assertEquals(25 * 3600L, eventsForDay(listOf(sleep), date, zone).single().elapsedSeconds())
    }
    @Test fun latestSideUsesLastIntervalWithinTheSelectedDay() {
        val zone = ZoneId.of("Europe/Copenhagen")
        val date = LocalDate.of(2026, 10, 2)
        val midnight = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val event = CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = midnight - 60000, endedAt = midnight + 60000,
            activeSide = BreastSide.Left, timerSegments = "${midnight - 60000}-$midnight;$midnight-${midnight + 60000}", timerSegmentSides = "Left;Right")
        assertEquals(BreastSide.Left, lastNursingSide(eventsForDay(listOf(event), date, zone)))
        assertEquals(BreastSide.Right, lastNursingSide(eventsForDay(listOf(event), date.plusDays(1), zone)))
    }

}
