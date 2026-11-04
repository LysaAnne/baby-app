package dk.babyapp.ui.tracking

import dk.babyapp.data.preferences.JournalFilterKeys
import dk.babyapp.data.preferences.QuickActionCategory
import dk.babyapp.data.preferences.orderedQuickActionCategories
import org.junit.Assert.assertEquals
import org.junit.Test

class StoredKeysTest {
    // Every value the app could store before October 2026, with the filter it meant.
    private val legacyJournalLabels = mapOf(
        "Madning · alle" to "Madning", "Sundhed · alle" to "Sundhed", "Medicin" to "Medicin",
        "Mavetid" to "Mavetid", "Favoritside" to "Liggeside", "Fast føde" to "Fast føde", "Amning" to "Amning", "Flaske" to "Flaske",
        "Pumpning" to "Pumpning", "Ble" to "Bleer", "Søvn" to "Søvn", "Mål" to "Mål", "Diverse" to "Diverse", "Sundhedsbesøg" to "Sundhedsbesøg",
        "Vaccination" to "Vaccination", "Vægt" to "Vægt", "Højde/længde" to "Højde/længde", "Hovedomkreds" to "Hovedomkreds", "Temperatur" to "Temperatur",
    )

    @Test fun everyOldJournalLabelBecomesAnIdShowingTheSameFilter() {
        // November 2026 added more filters (bad, våd ble, sovested …); the old ones are all still there.
        assertEquals(19 + 16, journalFilters.size)
        // Lur and Nattesøvn became plain Søvn.
        listOf("Lur", "Nattesøvn", "nap", "night-sleep").forEach { assertEquals("Søvn", journalFilterLabel(JournalFilterKeys.normalize(it))) }
        legacyJournalLabels.forEach { (stored, shown) ->
            val id = JournalFilterKeys.normalize(stored)
            assertEquals(stored, shown, journalFilterLabel(id))
            assertEquals(stored, 1, journalFilters.count { it.id == id })
        }
    }

    @Test fun idsAreUniqueStableAndDefaultsMatchTheOldDefaults() {
        assertEquals(journalFilters.size, journalFilters.map { it.id }.distinct().size)
        journalFilters.forEach { assertEquals(it.id, JournalFilterKeys.normalize(it.id)) }
        // The old four defaults, plus Medicin added in October 2026.
        assertEquals(listOf("Madning · alle", "Ble", "Søvn", "Sundhed · alle", "Medicin").map(JournalFilterKeys::normalize), JournalFilterKeys.defaults)
        assertEquals("ukendt", journalFilterLabel(JournalFilterKeys.normalize("ukendt")))
    }

    @Test fun oldCategoryLabelsAndNewIdsReadAsTheSameCategory() {
        val old = listOf("Madning", "Søvn", "Ble", "Mål", "Sundhed", "Fast føde", "Diverse")
        assertEquals(QuickActionCategory.entries, old.map(QuickActionCategory::fromStored))
        assertEquals(QuickActionCategory.entries, QuickActionCategory.entries.map { QuickActionCategory.fromStored(it.name) })
        assertEquals(null, QuickActionCategory.fromStored("ukendt"))
        // Fast føde moved into the Madning card in October 2026.
        assertEquals(old - "Fast føde", orderedQuickActionCategories(emptyList()).map { it.label })
    }

    @Test fun bathFilterOnlyShowsBaths() {
        val bath = journalFilters.first { it.id == JournalFilterKeys.activity(dk.babyapp.data.tracking.ActivityType.Bath) }
        fun activity(type: dk.babyapp.data.tracking.ActivityType) = dk.babyapp.data.tracking.CareEventEntity(childId = "a", type = dk.babyapp.data.tracking.CareEventType.Activity, startedAt = 0, activityType = type)
        assertEquals("Bad", bath.label)
        assertEquals(true, bath.matches(activity(dk.babyapp.data.tracking.ActivityType.Bath)))
        assertEquals(false, bath.matches(activity(dk.babyapp.data.tracking.ActivityType.Play)))
    }
}
