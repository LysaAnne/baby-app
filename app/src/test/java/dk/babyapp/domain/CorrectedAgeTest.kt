package dk.babyapp.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CorrectedAgeTest {
    @Test
    fun `term child has no corrected age`() {
        val age = calculateChildAge(
            birthDate = LocalDate.of(2026, 1, 1),
            dueDate = LocalDate.of(2026, 1, 1),
            onDate = LocalDate.of(2026, 4, 1),
        )

        assertNull(age.corrected)
        assertEquals(0, age.prematurityDays)
        assertEquals(3, age.chronological.months)
    }

    @Test
    fun `premature child age is corrected by days until due date`() {
        val age = calculateChildAge(
            birthDate = LocalDate.of(2026, 1, 1),
            dueDate = LocalDate.of(2026, 1, 29),
            onDate = LocalDate.of(2026, 4, 29),
        )

        assertEquals(28, age.prematurityDays)
        assertEquals(3, age.corrected?.months)
        assertEquals(0, age.corrected?.days)
    }

    @Test
    fun `corrected age is not shown after two corrected years`() {
        val age = calculateChildAge(
            birthDate = LocalDate.of(2024, 1, 1),
            dueDate = LocalDate.of(2024, 2, 1),
            onDate = LocalDate.of(2026, 2, 1),
        )

        assertNull(age.corrected)
    }
}
