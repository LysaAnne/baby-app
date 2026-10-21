package dk.babyapp.ui.tracking

import dk.babyapp.data.preferences.DashboardMetric
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import org.junit.Assert.assertEquals
import org.junit.Test

class JournalDayLineTest {
    private val base = CareEventEntity(childId = "child", type = CareEventType.Diaper, startedAt = 0, endedAt = 0, diaperType = DiaperType.Wet)

    @Test fun showsTheChosenOverviewFieldsInOrder() {
        val events = listOf(
            base.copy(id = "b", type = CareEventType.Bottle, amountConsumedMl = 90),
            base.copy(id = "n", type = CareEventType.Breastfeeding, leftSeconds = 600),
            base.copy(id = "d1"), base.copy(id = "d2"),
            base.copy(id = "s", type = CareEventType.Sleep, endedAt = 33_300_000, leftSeconds = 33_300),
        )
        assertEquals("2 madninger · 90 ml · 2 bleer · 9 t 15 m søvn", journalDayLine(events, listOf(DashboardMetric.Feeding, DashboardMetric.Diapers, DashboardMetric.Sleep)))
        assertEquals("2 bleer · 2 madninger · 90 ml", journalDayLine(events, listOf(DashboardMetric.Diapers, DashboardMetric.Feeding)))
    }

    @Test fun leavesOutEmptyFieldsAndIsNullWhenNothingMatches() {
        assertEquals("1 ble", journalDayLine(listOf(base), DashboardMetric.defaults))
        assertEquals(null, journalDayLine(emptyList(), DashboardMetric.defaults))
    }
}
