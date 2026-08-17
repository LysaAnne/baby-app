package dk.babyapp.ui.navigation

import org.junit.Assert.assertEquals
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
}

