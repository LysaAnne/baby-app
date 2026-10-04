package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import org.junit.Assert.*
import org.junit.Test

class NursingShieldTest {
    private fun running() = CareEventEntity(childId = "child", type = CareEventType.Breastfeeding, startedAt = 1000, runningSince = 1000, activeSide = BreastSide.Left).startSegment(1000)
    @Test fun togglingUpdatesCurrentIntervalWithoutSplitting() {
        val event = running().changeNippleShield(true, 61000).switchNursingSide(121000).changeNippleShield(false, 181000)
        assertEquals(listOf(true, false), event.nursingIntervals().map { it.nippleShield })
        assertEquals(240L, event.elapsedSeconds(241000))
        assertEquals("Left;Right", event.timerSegmentSides)
        assertEquals(event, event.changeNippleShield(false, 190000))
    }
    @Test fun pausedToggleDoesNotChangeEarlierIntervals() {
        val paused = running().accrueUntil(61000).closeSegment(61000).copy(runningSince = null)
        val changed = paused.changeNippleShield(true, 121000)
        assertEquals(paused.timerSegments, changed.timerSegments)
        assertFalse(changed.shieldForInterval(0))
        val resumed = changed.copy(runningSince = 181000).startSegment(181000)
        assertEquals(listOf(false, true), resumed.nursingIntervals().map { it.nippleShield })
        assertEquals(60L, resumed.elapsedSeconds(181000))
    }
    @Test fun legacyShieldAndUnknownSidesSurviveIndividualShieldEdit() {
        val legacy = CareEventEntity(childId = "child", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 61000, leftSeconds = 20, rightSeconds = 40, nippleShield = true, timerSegments = "1000-21000;21000-61000")
        val intervals = legacy.nursingIntervals()
        assertTrue(intervals.all { it.nippleShield })
        val edited = legacy.editNursingIntervals(listOf(intervals[0].copy(nippleShield = false), intervals[1]), 100000)
        assertEquals(listOf(false, true), edited.nursingIntervals().map { it.nippleShield })
        assertEquals(20L, edited.leftSeconds); assertEquals(40L, edited.rightSeconds)
    }
}
