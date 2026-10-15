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

    @Test fun teethAndBookFrequencyPersistAndKeepOtherChild() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = DataStoreAppPreferencesRepository(context)
        val previous = repository.preferences.first()
        try {
            val first = dk.babyapp.data.teeth.ToothRecord("first", 51, "2026-01-01")
            val second = dk.babyapp.data.teeth.ToothRecord("second", 51, lost = "2026-02-01")
            repository.replaceTeeth(listOf(first, second))
            repository.saveTooth(first.copy(lost = "2026-03-01"))
            repository.updateBookReminderFrequency(BookReminderFrequency.Monthly)
            val reloaded = DataStoreAppPreferencesRepository(context).preferences.first()
            assertEquals(BookReminderFrequency.Monthly, reloaded.bookReminderFrequency)
            assertEquals(second, reloaded.teeth.single { it.childId == "second" })
            repository.saveTooth(first.copy(emerged = null))
            assertEquals(listOf(second), repository.preferences.first().teeth)
        } finally {
            repository.replaceTeeth(previous.teeth)
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
