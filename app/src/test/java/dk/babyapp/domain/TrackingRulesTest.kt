package dk.babyapp.domain

import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingRulesTest {
    @Test
    fun `accrual adds running breastfeeding time to active side`() {
        val event = CareEventEntity(
            childId = "child",
            type = CareEventType.Breastfeeding,
            startedAt = 1_000,
            runningSince = 2_000,
            activeSide = BreastSide.Right,
            leftSeconds = 4,
            rightSeconds = 3,
        )

        val result = event.accrueUntil(7_000)

        assertEquals(4, result.leftSeconds)
        assertEquals(8, result.rightSeconds)
    }

    @Test
    fun `sleep overlap ignores deleted and unrelated records`() {
        val candidate = sleep("candidate", "child", 2_000, 4_000)
        val unrelated = sleep("other-child", "other", 2_500, 3_000)
        val deleted = sleep("deleted", "child", 2_500, 3_000).copy(deletedAt = 5_000)

        assertFalse(overlapsSleep(candidate, listOf(unrelated, deleted), now = 10_000))
    }

    @Test
    fun `sleep overlap detects intersecting record but permits adjacent record`() {
        val candidate = sleep("candidate", "child", 2_000, 4_000)

        assertTrue(overlapsSleep(candidate, listOf(sleep("overlap", "child", 3_000, 5_000)), now = 10_000))
        assertFalse(overlapsSleep(candidate, listOf(sleep("adjacent", "child", 4_000, 5_000)), now = 10_000))
    }

    private fun sleep(id: String, childId: String, start: Long, end: Long) = CareEventEntity(
        id = id,
        childId = childId,
        type = CareEventType.Sleep,
        startedAt = start,
        endedAt = end,
    )
}
