package dk.babyapp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PsychologyTest {
    @Test fun all46PhenomenaAreThereInOrderWithSensibleAges() {
        assertEquals((1..46).toList(), psychPhenomena.map { it.number })
        assertTrue(psychPhenomena.all { it.toMonths == null || it.fromMonths <= it.toMonths!! })
        assertTrue(psychPhenomena.all { it.definition.isNotBlank() && it.experiment.isNotBlank() && it.conclusion.isNotBlank() })
        assertTrue(PsychArea.entries.all { area -> psychPhenomena.any { it.area == area } })
    }

    @Test fun nowAndSoonFollowTheAge() {
        // At 10 months: joint attention and social referencing are under way; mirror recognition is not yet.
        val now = psychNow(10.0).map { it.title }
        assertTrue("Fælles opmærksomhed" in now)
        assertTrue("Social referencing" in now)
        assertTrue("Selvgenkendelse i spejlet" !in now)
        assertTrue(psychSoon(10.0).all { it.fromMonths in 10.0..13.0 })
    }

    @Test fun aSevenWeekOldOnlyHasWhatBelongsToTheFirstMonthsAndTheTimelineIsChronological() {
        val now = psychNow(7 / 4.345).map { it.title }
        assertTrue("Årsag og virkning" !in now)
        assertTrue("Følelsesregulering" !in now)
        assertTrue("Tilknytning" in now)
        assertEquals(psychTimeline.map { it.fromMonths }.sorted(), psychTimeline.map { it.fromMonths })
        assertEquals("1–2 år", psychAgeBand(psychPhenomena.first { it.title == "Årsag og virkning" }.fromMonths))
    }

    @Test fun everyPhenomenonHasAFullTextWithStudiesStepsAndFindings() {
        assertEquals((1..46).toSet(), psychDetails.keys)
        psychDetails.forEach { (number, d) ->
            assertTrue("$number summary", d.summary.isNotBlank())
            assertTrue("$number theory", d.theory.isNotEmpty() && d.theory.all { it.isNotBlank() })
            assertTrue("$number concepts", d.concepts.isNotEmpty())
            assertTrue("$number studies", d.studies.isNotEmpty())
            d.studies.forEach { assertTrue("$number ${it.name}", it.who.isNotBlank() && it.steps.isNotEmpty() && it.findings.isNotEmpty()) }
            assertTrue("$number meaning/home/caveats", d.meaning.isNotEmpty() && d.atHome.isNotEmpty() && d.caveats.isNotEmpty())
        }
    }
}
