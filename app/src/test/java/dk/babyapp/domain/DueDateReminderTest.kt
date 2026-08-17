package dk.babyapp.domain

import dk.babyapp.data.profile.BirthStatus
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DueDateReminderTest {
    private val today = LocalDate.of(2026, 8, 17)

    @Test fun `expected child triggers reminder exactly fourteen days after due date`() {
        assertTrue(shouldShowDueDateReminder(BirthStatus.Expected, LocalDate.of(2026, 8, 3), today))
    }

    @Test fun `reminder is hidden before boundary and for born children`() {
        assertFalse(shouldShowDueDateReminder(BirthStatus.Expected, LocalDate.of(2026, 8, 4), today))
        assertFalse(shouldShowDueDateReminder(BirthStatus.Born, LocalDate.of(2026, 7, 1), today))
    }
}
