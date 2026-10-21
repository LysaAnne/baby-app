package dk.babyapp.domain

import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.SleepType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerIntervalsTest {
    private val minute = 60_000L
    private val t = 1_000_000_000_000L

    @Test fun movingAStartEarlierMovesThePreviousEndAlong() {
        val intervals = listOf(NursingInterval(t, t + 60 * minute, BreastSide.Left), NursingInterval(t + 60 * minute, t + 90 * minute, BreastSide.Right))
        val moved = intervals.withStart(1, t + 55 * minute)
        assertEquals(t + 55 * minute, moved[0].end)
        assertEquals(t + 55 * minute, moved[1].start)
        // A start before the previous interval even began is left for validation to reject.
        assertEquals(t + 60 * minute, intervals.withStart(1, t - minute)[0].end)
    }

    @Test fun movingAnEndLaterMovesTheNextStartAlong() {
        val intervals = listOf(NursingInterval(t, t + 30 * minute, null), NursingInterval(t + 40 * minute, null, null))
        val moved = intervals.withEnd(0, t + 45 * minute)
        assertEquals(t + 45 * minute, moved[1].start)
        assertEquals(t + 40 * minute, intervals.withEnd(0, t + 35 * minute)[1].start)
    }

    @Test fun pausedSleepKeepsItsIntervalsAndOnlyCountsSleepingTime() {
        val now = t + 120 * minute
        val sleep = CareEventEntity(childId = "c", type = CareEventType.Sleep, sleepType = SleepType.Nap, startedAt = t, endedAt = t + 90 * minute,
            leftSeconds = 80 * 60, timerSegments = "$t-${t + 50 * minute};${t + 60 * minute}-${t + 90 * minute}")
        assertEquals(2, sleep.timerIntervals().size)
        val edited = sleep.editTimerIntervals(sleep.timerIntervals().withStart(1, t + 45 * minute), now)
        assertEquals("$t-${t + 45 * minute};${t + 45 * minute}-${t + 90 * minute}", edited.timerSegments)
        assertEquals(90L * 60, edited.leftSeconds)
        assertEquals(t + 90 * minute, edited.endedAt)
        assertNull(edited.runningSince)
    }

    @Test fun runningSleepKeepsItsOpenInterval() {
        val now = t + 30 * minute
        val running = CareEventEntity(childId = "c", type = CareEventType.Sleep, startedAt = t, runningSince = t + 20 * minute, leftSeconds = 10 * 60, timerSegments = "$t-${t + 10 * minute};${t + 20 * minute}-")
        val edited = running.editTimerIntervals(running.timerIntervals().withStart(0, t - 5 * minute), now)
        assertEquals(t - 5 * minute, edited.startedAt)
        assertEquals(t + 20 * minute, edited.runningSince)
        assertEquals(15L * 60, edited.leftSeconds)
        assertNull(edited.endedAt)
    }
}
