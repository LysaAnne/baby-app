package dk.babyapp.domain

import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.*
import dk.babyapp.ui.tracking.details
import dk.babyapp.ui.tracking.journalFilters
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class RegistrationVisibilityTest {
    @Test fun draftsAreExcludedFromChartsAndExport() {
        val date = LocalDate.of(2026, 9, 27)
        val time = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val draft = CareEventEntity(id = "draft-id", childId = "freja", type = CareEventType.Bottle, startedAt = time, endedAt = time, amountConsumedMl = 100, isDraft = true)
        assertEquals(0, calculateInsights(listOf(draft), "freja", date, date, ZoneOffset.UTC).feedingCount)
        assertEquals(0f, dailyInsightSeries(listOf(draft), "freja", date, date, ZoneOffset.UTC).single().bottleMl)
        assertFalse(createCareCsv(ChildProfile(id = "freja", name = "Freja"), listOf(draft), date, date, "Metric", ZoneOffset.UTC).contains("draft-id"))
    }

    @Test fun everyRegistrationTypeIncludesNotesInDetails() {
        CareEventType.entries.forEach { type ->
            assertTrue("Missing notes for $type", CareEventEntity(childId = "freja", type = type, startedAt = 0, endedAt = 0, notes = "Min note").details().contains("Min note"))
        }
    }

    @Test fun feedingFilterIncludesMilkPumpingAndFoodButNotDiapers() {
        val filter = journalFilters.first { it.label == "Madning · alle" }
        listOf(CareEventType.Breastfeeding, CareEventType.Bottle, CareEventType.Pumping, CareEventType.SolidFood).forEach { assertTrue(filter.matches(CareEventEntity(childId = "freja", type = it, startedAt = 0))) }
        assertFalse(filter.matches(CareEventEntity(childId = "freja", type = CareEventType.Diaper, startedAt = 0)))
    }
}
