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
}
