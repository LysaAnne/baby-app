package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import org.junit.Assert.*
import org.junit.Test

class NursingIntervalEditingTest {
    private val event = CareEventEntity(childId = "a", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 21000, leftSeconds = 10, rightSeconds = 5, timerSegments = "1000-11000;16000-21000", timerSegmentSides = "Left;Right")
    @Test fun editingAndDeletingRecalculatesBothSidesWithoutPause() {
        val edited = event.editNursingIntervals(listOf(NursingInterval(1000, 6000, BreastSide.Right), NursingInterval(16000, 20000, BreastSide.Left)), 30000)
        assertEquals(4L, edited.leftSeconds); assertEquals(5L, edited.rightSeconds)
        val removed = edited.editNursingIntervals(edited.nursingIntervals().drop(1), 30000)
        assertEquals(16000L, removed.startedAt); assertEquals(4L, removed.elapsedSeconds()); assertEquals(0L, removed.rightSeconds)
        val empty = removed.editNursingIntervals(emptyList(), 30000)
        assertEquals(0L, empty.elapsedSeconds()); assertTrue(empty.nursingIntervals().isEmpty())
    }
    @Test fun activeStartCanMoveBackWithoutDoubleCounting() {
        val running = event.copy(endedAt = null, runningSince = 16000, rightSeconds = 0, timerSegments = "1000-11000;16000-")
        val edited = running.editNursingIntervals(listOf(NursingInterval(1000, 11000, BreastSide.Left), NursingInterval(12000, null, BreastSide.Right)), 21000)
        assertEquals(12000L, edited.runningSince); assertEquals(19L, edited.elapsedSeconds(21000)); assertEquals(0L, edited.rightSeconds)
        val paused = edited.editNursingIntervals(edited.nursingIntervals().dropLast(1), 21000)
        assertNull(paused.runningSince); assertNull(paused.endedAt)
    }
    @Test fun invalidOverlapsAndFutureTimesAreRejected() {
        val bad = listOf(
            listOf(NursingInterval(1000, 17000, BreastSide.Left), NursingInterval(16000, 21000, BreastSide.Right)),
            listOf(NursingInterval(1000, 31000, BreastSide.Left)),
            listOf(NursingInterval(1000, 500, BreastSide.Left)),
        )
        bad.forEach { assertTrue(runCatching { event.editNursingIntervals(it, 30000) }.isFailure) }
    }
    @Test fun oldUnknownSideTotalsArePreservedOnMetadataOnlySave() {
        val legacy = event.copy(timerSegmentSides = "")
        assertEquals(legacy, legacy.editNursingIntervals(legacy.nursingIntervals(), 30000))
        assertTrue(runCatching { legacy.editNursingIntervals(legacy.nursingIntervals().drop(1), 30000) }.isFailure)
    }
}
