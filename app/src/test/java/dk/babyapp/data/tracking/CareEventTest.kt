package dk.babyapp.data.tracking

import java.time.Instant
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

    @Test
    fun `sleep duration remains absolute across daylight saving transition`() {
        val start = Instant.parse("2026-03-29T00:30:00Z").toEpochMilli()
        val end = Instant.parse("2026-03-29T02:30:00Z").toEpochMilli()
        val event = CareEventEntity(
            childId = "child-1",
            type = CareEventType.Sleep,
            sleepType = SleepType.Night,
            startedAt = start,
            endedAt = end,
            leftSeconds = (end - start) / 1_000,
        )

        assertEquals(7_200, event.elapsedSeconds())
    }

    @Test
    fun `timer segments preserve every interval across pauses`() {
        val event = CareEventEntity(childId = "child-1", type = CareEventType.Sleep, startedAt = 1_000)
            .startSegment(1_000)
            .closeSegment(5_000)
            .startSegment(9_000)
            .closeSegment(15_000)

        assertEquals(listOf(1_000L to 5_000L, 9_000L to 15_000L), event.segmentIntervals())
    }
}
