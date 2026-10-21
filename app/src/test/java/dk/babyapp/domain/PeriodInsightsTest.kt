package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.HeadSide
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PeriodInsightsTest {
    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 10, 21)
    private val now = today.atTime(12, 0).toInstant(zone).toEpochMilli()
    private fun at(date: LocalDate, hour: Int, minute: Int = 0) = date.atTime(hour, minute).toInstant(zone).toEpochMilli()
    private fun event(type: CareEventType, start: Long, end: Long = start) = CareEventEntity(childId = "child", type = type, startedAt = start, endedAt = end)

    @Test fun nightSleepIsSplitAtMidnightAndShownAsBlocks() {
        val yesterday = today.minusDays(1)
        val night = event(CareEventType.Sleep, at(yesterday.minusDays(1), 20), at(yesterday, 6)).copy(sleepType = SleepType.Night)
        val insights = periodInsights(listOf(night), "child", 3, today, zone, now)
        val (first, second) = insights.days.take(2)
        assertEquals(240, first.nightMinutes)
        assertEquals(360, second.nightMinutes)
        assertEquals(20 / 24f, first.sleepBlocks.single().start, 0.001f)
        assertEquals(1f, first.sleepBlocks.single().endInclusive, 0.001f)
        assertEquals(6 / 24f, second.sleepBlocks.single().endInclusive, 0.001f)
    }

    @Test fun averagesUseFinishedDaysSinceTheFirstRegistration() {
        val yesterday = today.minusDays(1)
        val events = listOf(
            event(CareEventType.Bottle, at(yesterday.minusDays(1), 8)).copy(amountConsumedMl = 100),
            event(CareEventType.Bottle, at(yesterday, 8)).copy(amountConsumedMl = 120),
            event(CareEventType.Bottle, at(yesterday, 14)).copy(amountConsumedMl = 80),
            event(CareEventType.Bottle, at(today, 8)).copy(amountConsumedMl = 500),
            event(CareEventType.Diaper, at(yesterday, 9)).copy(diaperType = DiaperType.Both),
        )
        val insights = periodInsights(events, "child", 7, today, zone, now)
        assertEquals(7, insights.days.size)
        assertEquals(2, insights.averagedDays)
        assertEquals(1.5, insights.avgFeedings, 0.001)
        assertEquals(150, insights.avgBottleMl)
        assertEquals(0.5, insights.avgDiapers, 0.001)
        assertEquals(0.5, insights.avgWet, 0.001)
        assertEquals(0.5, insights.avgDirty, 0.001)
    }

    @Test fun bedtimeIsTheMiddleOfEveningStartsAndNeedsThreeNights() {
        val times = listOf(LocalTime.of(19, 30), LocalTime.of(20, 0), LocalTime.of(20, 15), LocalTime.of(23, 50))
        val nights = times.mapIndexed { i, t -> event(CareEventType.Sleep, at(today.minusDays(i + 1L), t.hour, t.minute), at(today.minusDays(i.toLong()), 5)).copy(sleepType = SleepType.Night, awakenings = i) }
        val insights = periodInsights(nights, "child", 7, today, zone, now)
        assertEquals(LocalTime.of(20, 0)..LocalTime.of(23, 50), insights.bedtime)
        assertEquals(1.5, insights.avgAwakenings!!, 0.001)
        assertNull(periodInsights(nights.take(2), "child", 7, today, zone, now).bedtime)
    }

    @Test fun growthShowsLatestAndWeightChangeInPeriodAndHeadSidesAreCounted() {
        fun weight(daysAgo: Long, kg: Double) = event(CareEventType.Measurement, at(today.minusDays(daysAgo), 10)).copy(measurementType = MeasurementType.Weight, measurementValue = kg, measurementUnit = "kg")
        val events = listOf(weight(20, 6.5), weight(10, 6.68), weight(2, 6.8),
            event(CareEventType.HeadSide, at(today, 9)).copy(headSide = HeadSide.Left), event(CareEventType.HeadSide, at(today, 10)).copy(headSide = HeadSide.Right),
            event(CareEventType.HeadSide, at(today.minusDays(9), 10)).copy(headSide = HeadSide.Left))
        val insights = periodInsights(events, "child", 7, today, zone, now)
        assertEquals(6.8, insights.latest.getValue(MeasurementType.Weight).value, 0.001)
        assertEquals(0.12, insights.weightChange!!, 0.001)
        assertEquals(1, insights.headLeft)
        assertEquals(1, insights.headRight)
        assertNull(periodInsights(listOf(weight(2, 6.8)), "child", 7, today, zone, now).weightChange)
    }

    @Test fun otherChildrenDraftsAndDeletedAreIgnored() {
        val diaper = event(CareEventType.Diaper, at(today.minusDays(1), 9)).copy(diaperType = DiaperType.Wet)
        val insights = periodInsights(listOf(diaper.copy(childId = "other"), diaper.copy(isDraft = true), diaper.copy(deletedAt = 1)), "child", 7, today, zone, now)
        assertEquals(0, insights.days.sumOf { it.diapers })
    }
}
