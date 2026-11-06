package dk.babyapp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DevelopmentProgressTest {
    @Test fun earlierSpurtsNotDoneStayCurrentAndDoneOnesMoveAway() {
        val first = leaps.first()
        // Week 6: spurt 1 (4.5–5.5) is over but nothing is ticked, so it is left over from earlier.
        assertEquals(DevState.Overdue, first.state(6.0, emptySet()))
        assertEquals(DevState.DonePast, first.state(6.0, first.tickKeys().toSet()))
        assertEquals(DevState.DoneNow, first.state(5.0, first.tickKeys().toSet()))
        assertEquals(DevState.Future, leaps.last().state(6.0, emptySet()))
        val progress = developmentProgress(DevArea.Leaps, 6.0, setOf(first.key("play", 0)))
        assertEquals(DevProgress(1, first.tickKeys().size), progress)
    }

    @Test fun progressCountsCurrentAndLeftOverItemsInEveryArea() {
        val weeks = 18.0
        val months = monthsFromWeeks(weeks)
        val milestonesNow = milestones.filter { it.fromMonths <= months }
        // Ticking one that is under way counts; a ticked one from earlier is done and moves out of the count.
        val roll = milestones.first { it.key == "roll" }
        assertEquals(DevProgress(1, milestonesNow.size), developmentProgress(DevArea.Milestones, weeks, setOf(roll.tickKey)))
        val smile = milestones.first { it.key == "smile" }
        assertEquals(DevProgress(0, milestonesNow.size - 1), developmentProgress(DevArea.Milestones, weeks, setOf(smile.tickKey)))
        // A phenomenon that goes on through childhood is never left over.
        assertTrue(psychTimeline.filter { it.toMonths == null && it.fromMonths <= months }.all { it.state(months, emptySet()) == DevState.Now })
        val all = experimentTimeline.filter { it.fromMonths <= months }.map { it.key }.toSet()
        assertTrue(developmentProgress(DevArea.Experiments, weeks, all).complete)
    }

    @Test fun milestoneAgesAreWholeMonthsOnTheCardAndSpeltOutInThePopup() {
        val sit = milestones.first { it.key == "sit" }
        assertEquals("4–9 mdr", sit.wholeMonthWindow())
        assertEquals("3 måneder og 3 uger", monthsAndWeeks(3.8))
        assertEquals("1 måned", monthsAndWeeks(1.0))
        assertEquals("10 måneder", monthsAndWeeks(9.95))
    }
}
