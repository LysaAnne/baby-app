package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class HeadSideStatisticsTest {
    private val zone = ZoneId.of("Europe/Copenhagen")
    private val day = LocalDate.of(2026, 10, 25) // DST end: a 25-hour local day.
    private fun at(date: LocalDate, hour: Int = 0) = date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    private fun event(date: LocalDate, side: HeadSide) = headSideObservation("child", side, at(date))

    @Test fun observationsDoNotStartTimers() {
        val record = event(day, HeadSide.Left)
        assertEquals(record.startedAt, record.endedAt)
        assertNull(record.runningSince)
        assertEquals(0L, record.elapsedSeconds())
        assertTrue(record.timerSegments.isEmpty())
    }

    @Test fun periodAndCorrectionUseCalendarMidnightAndExcludeOtherData() {
        val previous = event(day.minusDays(1), HeadSide.Left)
        val current = event(day, HeadSide.Right)
        val late = headSideObservation("child", HeadSide.Right, at(day, 23))
        val records = listOf(previous, current, late, event(day.plusDays(1), HeadSide.Left), current.copy(childId = "other"), current.copy(isDraft = true), current.copy(deletedAt = 1), current.copy(endedAt = null))
        val result = headSideStatistics(records, "child", StatisticsRange(day.minusDays(1), day), day, zone)
        assertEquals(HeadSideTotals(1, 2), result.total)
        assertEquals(HeadSideTotals(1, 0), result.before)
        assertEquals(HeadSideTotals(0, 2), result.after)
        assertEquals(HeadSideTotals(1, 2), result.days.last().cumulative)
        assertEquals(2, result.days.size)
        assertEquals(100.0 / 3, result.total.leftPercent!!, .0001)
    }

    @Test fun emptyAndUnknownSidesNeverProduceInventedPercentages() {
        assertNull(headSideTotals(emptyList()).leftPercent)
        val result = headSideStatistics(listOf(event(day, HeadSide.Left).copy(headSide = null)), "child", StatisticsRange(day, day), day.minusDays(1), zone)
        assertEquals(1, result.total.unknown)
        assertNull(result.total.rightPercent)
        assertEquals(HeadSideTotals(0, 0), result.before)
        assertEquals(HeadSideTotals(0, 0, 1), result.after)
    }

    @Test fun deletedAndEditedObservationsChangeTotalsWithoutCountingSleep() {
        val left = event(day, HeadSide.Left)
        val sleep = CareEventEntity(childId = "child", type = CareEventType.Sleep, startedAt = at(day), endedAt = at(day, 1), leftSeconds = 3600)
        assertEquals(HeadSideTotals(0, 1), headSideTotals(listOf(left.copy(deletedAt = 1), left.copy(headSide = HeadSide.Right), sleep)))
    }

    @Test fun shortcutsAndCustomRangesAreInclusiveAndCalendarBased() {
        val today = LocalDate.of(2026, 3, 31)
        assertEquals(today, statisticsRange(StatisticsPeriod.Day, today, day, day).from)
        assertEquals(today.minusDays(6), statisticsRange(StatisticsPeriod.Week, today, day, day).from)
        assertEquals(LocalDate.of(2026, 3, 1), statisticsRange(StatisticsPeriod.Month, today, day, day).from)
        assertEquals(today.minusMonths(3).plusDays(1), statisticsRange(StatisticsPeriod.ThreeMonths, today, day, day).from)
        assertEquals(today.minusMonths(6).plusDays(1), statisticsRange(StatisticsPeriod.SixMonths, today, day, day).from)
        assertEquals(today.minusYears(1).plusDays(1), statisticsRange(StatisticsPeriod.Year, today, day, day).from)
        assertEquals(StatisticsRange(day, day.plusDays(2)), statisticsRange(StatisticsPeriod.Custom, today, day, day.plusDays(2)))
        assertTrue(runCatching { headSideStatistics(emptyList(), "child", StatisticsRange(day, day.minusDays(1))) }.isFailure)
    }
}
