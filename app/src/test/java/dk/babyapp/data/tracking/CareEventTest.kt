package dk.babyapp.data.tracking

import org.junit.Assert.assertEquals
import org.junit.Test

class CareEventTest {
    @Test
    fun `elapsed time combines stored segments and running segment`() {
        val event = CareEventEntity(
            childId = "child-1",
            type = CareEventType.Breastfeeding,
            startedAt = 1_000,
            runningSince = 11_000,
            leftSeconds = 12,
            rightSeconds = 8,
            activeSide = BreastSide.Right,
        )

        assertEquals(25, event.elapsedSeconds(now = 16_000))
    }

    @Test
    fun `completed event uses only its stored duration`() {
        val event = CareEventEntity(
            childId = "child-1",
            type = CareEventType.Pumping,
            startedAt = 1_000,
            endedAt = 31_000,
            leftSeconds = 30,
        )

        assertEquals(30, event.elapsedSeconds(now = 999_000))
    }
}
