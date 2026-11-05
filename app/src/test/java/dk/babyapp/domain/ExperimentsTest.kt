package dk.babyapp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExperimentsTest {
    @Test fun fiftyExperimentsWithFullDescriptionsAndSensibleAges() {
        assertEquals((1..90).toList(), homeExperiments.map { it.number })
        assertEquals(90, homeExperiments.map { it.key }.toSet().size)
        homeExperiments.forEach {
            assertTrue(it.title, it.fromMonths < it.toMonths)
            assertTrue(it.title, it.idea.isNotBlank() && it.research.isNotBlank() && it.ageLabel.isNotBlank())
            assertTrue(it.title, it.materials.isNotEmpty() && it.steps.size >= 2 && it.lookFor.isNotEmpty())
        }
    }

    @Test fun timelineIsChronologicalAndNowFollowsTheAge() {
        assertEquals(experimentTimeline.sortedBy { it.fromMonths }.map { it.fromMonths }, experimentTimeline.map { it.fromMonths })
        val baby = experimentsNow(1.0).map { it.title }
        assertTrue("Ansigter frem for alt" in baby)
        assertTrue("Sally og Anne" !in baby)
        assertTrue("Sally og Anne" in experimentsNow(50.0).map { it.title })
        assertEquals("exp-36", homeExperiments.first { it.title == "Sally og Anne" }.key)
    }

    @Test fun everyOneOfTheBooksFiftyPointsToOneExperiment() {
        assertEquals((1..50).toList(), experimentBookRefs.values.map { it.first }.sorted())
        assertTrue(experimentBookRefs.keys.all { key -> homeExperiments.any { it.number == key } })
        assertEquals(50, homeExperiments.count { it.bookRef != null })
        assertEquals(29, homeExperiments.first { it.title == "Broccoli eller kiks" }.bookRef?.first)
    }
}
