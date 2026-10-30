package dk.babyapp.domain

import dk.babyapp.data.profile.ChildProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DevelopmentTest {
    @Test fun weeksCountFromTheDueDateAndPhasesFollowTheSpurts() {
        val due = LocalDate.of(2026, 6, 1)
        // Born two weeks early: the weeks still count from the due date.
        val child = ChildProfile(name = "Freja", birthDate = due.minusWeeks(2), dueDate = due)
        assertEquals(5.0, weeksFromDue(child, due.plusWeeks(5))!!, 0.001)
        assertEquals(1, (leapPhase(5.0) as LeapPhase.InLeap).leap.number)
        val calm = leapPhase(6.0) as LeapPhase.Calm
        assertEquals(2, calm.next?.number); assertEquals(1.5, calm.weeksToNext!!, 0.001)
        assertEquals(LeapPhase.Calm(null, null), leapPhase(90.0))
        assertEquals(due.minusWeeks(2), developmentStart(child.copy(dueDate = null)))
    }

    @Test fun spurtsAreInOrderAndMilestoneWindowsAreSensible() {
        assertEquals((1..10).toList(), leaps.map { it.number })
        assertTrue(leaps.zipWithNext().all { (a, b) -> a.calmFromWeek <= b.fussyFromWeek })
        assertTrue(leaps.all { it.seen.isNotEmpty() && it.play.isNotEmpty() && it.hard.isNotEmpty() && it.skills.isNotEmpty() })
        assertTrue(milestones.all { it.fromMonths < it.toMonths })
        assertEquals("3,8–9,2 mdr (WHO)", milestones.first { it.key == "sit" }.window())
        assertEquals(songs.size, songs.map { it.id }.distinct().size)
    }
}
