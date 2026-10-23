package dk.babyapp.data.preferences

import androidx.test.platform.app.InstrumentationRegistry
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationPreferencesTest {
    @Test fun headSideRangeAndChildCorrectionDatesSurviveReload() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = DataStoreAppPreferencesRepository(context)
        val previous = repository.preferences.first().headSideStatistics
        val settings = HeadSideStatisticsSettings("Custom", "2026-09-01", "2026-10-15", mapOf("child" to "2026-10-01", "other" to "2026-10-05"))
        try {
            repository.updateHeadSideStatistics(settings)
            assertEquals(settings, DataStoreAppPreferencesRepository(context).preferences.first().headSideStatistics)
        } finally { repository.updateHeadSideStatistics(previous) }
    }

    @Test fun bookFrequencyPersists() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = DataStoreAppPreferencesRepository(context)
        val previous = repository.preferences.first()
        try {
            repository.updateBookReminderFrequency(BookReminderFrequency.Monthly)
            assertEquals(BookReminderFrequency.Monthly, DataStoreAppPreferencesRepository(context).preferences.first().bookReminderFrequency)
        } finally {
            repository.updateBookReminderFrequency(previous.bookReminderFrequency)
        }
    }

    @Test fun chartRangeAndPumpMethodPersistAcrossRepositoryInstances() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = DataStoreAppPreferencesRepository(context)
        val previous = repository.preferences.first()
        val settings = MeasurementChartSettings("Custom", "2026-01-01", "2026-10-01")
        try {
            repository.updateMeasurementChartSettings(settings)
            repository.updateLastPumpingMethod("Manuel pumpe")
            val reloaded = DataStoreAppPreferencesRepository(context).preferences.first()
            assertEquals(settings, reloaded.measurementChartSettings)
            assertEquals("Manuel pumpe", reloaded.lastPumpingMethod)
        } finally {
            repository.updateMeasurementChartSettings(previous.measurementChartSettings)
            repository.updateLastPumpingMethod(previous.lastPumpingMethod)
        }
    }

    @Test fun valuesStoredAsDanishLabelsAreReadAsStableIds() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = context.appPreferencesDataStore
        val saved = store.data.first()
        try {
            store.edit {
                it[androidx.datastore.preferences.core.stringPreferencesKey("journal_quick_filters")] = "Vægt,Lur,Ble,Sundhed · alle"
                it[androidx.datastore.preferences.core.stringPreferencesKey("quick_action_category_order")] = "Sundhed,Søvn,Madning"
            }
            val preferences = DataStoreAppPreferencesRepository(context).preferences.first()
            // Lur became plain Søvn in October 2026.
            assertEquals(listOf("measurement:Weight", "type:Sleep", "type:Diaper", "health-all", "medicine"), preferences.journalQuickFilters)
            assertEquals(listOf(QuickActionCategory.Health, QuickActionCategory.Sleep, QuickActionCategory.Feeding), preferences.quickActionCategoryOrder)
            // The old value is only replaced when the user saves a new choice.
            assertEquals("Sundhed,Søvn,Madning", store.data.first()[androidx.datastore.preferences.core.stringPreferencesKey("quick_action_category_order")])
            DataStoreAppPreferencesRepository(context).updateQuickActionCategoryOrder(listOf(QuickActionCategory.Misc))
            assertEquals("Misc", store.data.first()[androidx.datastore.preferences.core.stringPreferencesKey("quick_action_category_order")])
        } finally {
            store.edit { it.clear(); it += saved }
        }
    }
}
