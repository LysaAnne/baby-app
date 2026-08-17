package dk.babyapp.ui.navigation

import dk.babyapp.ui.profile.ProfileValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationTest {
    @Test
    fun `top level navigation contains five unique destinations`() {
        val destinations = AppDestination.topLevel

        assertEquals(5, destinations.size)
        assertEquals(destinations.size, destinations.map { it::class }.distinct().size)
        assertTrue(destinations.first() is AppDestination.Today)
    }

    @Test
    fun `successful first child save returns to today`() {
        assertTrue(shouldReturnToTodayAfterProfileSave(profilesWereEmpty = true, draftId = null, error = null))
        assertFalse(shouldReturnToTodayAfterProfileSave(profilesWereEmpty = false, draftId = null, error = null))
        assertFalse(shouldReturnToTodayAfterProfileSave(profilesWereEmpty = true, draftId = "existing", error = null))
        assertFalse(shouldReturnToTodayAfterProfileSave(profilesWereEmpty = true, draftId = null, error = ProfileValidationError.NameRequired))
    }
}
