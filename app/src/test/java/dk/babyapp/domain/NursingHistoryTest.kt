package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import org.junit.Assert.*
import org.junit.Test

class NursingHistoryTest {
    @Test fun switchingSidesCreatesSeparateIntervalsAndPreservesPause() {
        val first = CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = 1000, runningSince = 1000, activeSide = BreastSide.Left).startSegment(1000)
        val switched = first.switchNursingSide(6000)
        val paused = switched.accrueUntil(9000).closeSegment(9000).copy(runningSince = null)
        val resumed = paused.copy(runningSince = 15000).startSegment(15000)
        val stopped = resumed.accrueUntil(17000).closeSegment(17000).copy(endedAt = 17000, runningSince = null)
        assertEquals(listOf(1000L to 6000L, 6000L to 9000L, 15000L to 17000L), stopped.segmentIntervals())
        assertEquals("Left;Right;Right", stopped.timerSegmentSides)
        assertEquals(5L, stopped.leftSeconds)
        assertEquals(5L, stopped.rightSeconds)
        assertEquals(10L, stopped.elapsedSeconds())
    }
    @Test fun resumingSavedNursingKeepsIdentityAndDoesNotCountGap() {
        val event = CareEventEntity(id = "saved", childId = "a", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 6000, leftSeconds = 5, timerSegments = "1000-6000", timerSegmentSides = "Left", notes = "Bevar")
        val resumed = event.resumeNursing(BreastSide.Right, 30000)
        assertEquals("saved", resumed.id)
        assertEquals("Bevar", resumed.notes)
        assertEquals(7L, resumed.elapsedSeconds(32000))
        assertEquals("Left;Right", resumed.timerSegmentSides)
        assertNull(resumed.endedAt)
        assertTrue(resumed.nursingContinued)
        assertFalse(resumed.isDraft)
    }
    @Test fun oldHistoryDoesNotInventSides() {
        val event = CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 6000, timerSegments = "1000-2000;4000-6000")
        assertEquals(";;Left", event.resumeNursing(BreastSide.Left, 9000).timerSegmentSides)
    }
    @Test fun sideSwitchDuringPauseDoesNotStartAnInterval() {
        val event = CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = 1000, activeSide = BreastSide.Left, timerSegments = "1000-2000", timerSegmentSides = "Left")
        val switched = event.switchNursingSide(5000)
        assertNull(switched.runningSince)
        assertEquals(event.timerSegments, switched.timerSegments)
        assertEquals(BreastSide.Right, switched.activeSide)
    }
}
