package dk.babyapp.domain

import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeSinceTest {
    private val minute = 60_000L

    @Test fun labelsRoundDownToWholeMinutesHoursAndDays() {
        assertEquals("lige nu", timeSinceLabel(0, 59_000))
        assertEquals("12 min", timeSinceLabel(0, 12 * minute + 30_000))
        assertEquals("3 t", timeSinceLabel(0, 180 * minute))
        assertEquals("3 t 5 min", timeSinceLabel(0, 185 * minute))
        assertEquals("2 d 4 t", timeSinceLabel(0, (2 * 1_440 + 250) * minute))
        assertEquals("lige nu", timeSinceLabel(10 * minute, 0))
    }

    @Test fun lastDoseMatchesTheCardByIdOrOldRegistrationsByName() {
        val plan = MedicinePlan(id = "p", childId = "c", name = "Paracetamol")
        fun dose(at: Long, id: String = "", name: String = "") = CareEventEntity(childId = "c", type = CareEventType.Activity, activityType = ActivityType.Medicine, startedAt = at, endedAt = at, medicineId = id, medicationName = name)
        val events = listOf(dose(1, id = "p"), dose(5, name = "paracetamol "), dose(9, id = "other", name = "Paracetamol"), dose(7, id = "p").copy(deletedAt = 8), dose(8, id = "p").copy(childId = "x"))
        assertEquals(5L, lastDose(plan, events)?.startedAt)
        assertNull(lastDose(plan.copy(id = "q", name = "Ibuprofen"), events))
    }
}
