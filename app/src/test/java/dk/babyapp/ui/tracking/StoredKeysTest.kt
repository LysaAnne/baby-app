package dk.babyapp.ui.tracking

import dk.babyapp.data.preferences.JournalFilterKeys
import dk.babyapp.data.preferences.QuickActionCategory
import dk.babyapp.data.preferences.orderedQuickActionCategories
import org.junit.Assert.assertEquals
import org.junit.Test

class StoredKeysTest {
    // Every value the app could store before October 2026, with the filter it meant.
    private val legacyJournalLabels = mapOf(
        "Madning · alle" to "Madning", "Sundhed · alle" to "Sundhed", "Lur" to "Lur", "Nattesøvn" to "Nattesøvn", "Medicin" to "Medicin",
        "Mavetid" to "Mavetid", "Favoritside" to "Liggeside", "Fast føde" to "Fast føde", "Amning" to "Amning", "Flaske" to "Flaske",
        "Pumpning" to "Pumpning", "Ble" to "Bleer", "Søvn" to "Søvn", "Mål" to "Mål", "Diverse" to "Diverse", "Sundhedsbesøg" to "Sundhedsbesøg",
        "Vaccination" to "Vaccination", "Vægt" to "Vægt", "Højde/længde" to "Højde/længde", "Hovedomkreds" to "Hovedomkreds", "Temperatur" to "Temperatur",
    )

    @Test fun everyOldJournalLabelBecomesAnIdShowingTheSameFilter() {
        assertEquals(journalFilters.size, legacyJournalLabels.size)
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
        assertEquals(old, orderedQuickActionCategories(emptyList()).map { it.label })
    }
}
