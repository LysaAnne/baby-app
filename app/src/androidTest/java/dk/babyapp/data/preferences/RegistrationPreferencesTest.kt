package dk.babyapp.data.preferences

import androidx.test.platform.app.InstrumentationRegistry
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
}
