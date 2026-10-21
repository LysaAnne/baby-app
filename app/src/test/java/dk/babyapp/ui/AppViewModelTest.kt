package dk.babyapp.ui

import android.net.Uri
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.ThemePreference
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.ui.theme.defaultColorProfiles
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.tracking.TimerNotificationController
import dk.babyapp.ui.profile.ProfileDraft
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `completing onboarding saves profile and active selection`() = runTest(dispatcher) {
        val profiles = FakeProfilesRepository()
        val preferences = FakePreferencesRepository()
        val viewModel = AppViewModel(profiles, preferences, FakeParentRepository(), FakeCareEventRepository(), FakeTimerNotifications(), FakeColorProfileRepository())
        var result: Any? = Unit

        viewModel.completeOnboarding(settings = OnboardingSettings(languageTag = "da")) { result = null }
        advanceUntilIdle()

        assertNull(result)
        assertEquals(emptyList<ChildProfile>(), profiles.items.value)
        assertNull(preferences.items.value.activeChildId)
        assertEquals(true, preferences.items.value.onboardingCompleted)
    }

    @Test
    fun `dismissing getting started persists the choice`() = runTest(dispatcher) {
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val viewModel = AppViewModel(FakeProfilesRepository(), preferences, FakeParentRepository(), FakeCareEventRepository(), FakeTimerNotifications(), FakeColorProfileRepository())

        viewModel.dismissGettingStarted()
        advanceUntilIdle()

        assertEquals(true, preferences.items.value.hasSeenGettingStarted)
    }

    @Test
    fun `state holds recent events of the active child and history is loaded on demand`() = runTest(dispatcher) {
        val alma = ChildProfile(name = "Alma", birthDate = java.time.LocalDate.of(2024, 1, 1))
        val noah = ChildProfile(name = "Noah", birthDate = java.time.LocalDate.of(2026, 1, 1))
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        val events = FakeCareEventRepository().also { repo ->
            repo.items.value = listOf(
                CareEventEntity(id = "alma-today", childId = alma.id, type = CareEventType.Diaper, startedAt = now, endedAt = now),
                CareEventEntity(id = "alma-last-year", childId = alma.id, type = CareEventType.Diaper, startedAt = now - 365 * day, endedAt = now - 365 * day),
                CareEventEntity(id = "noah-running", childId = noah.id, type = CareEventType.Sleep, startedAt = now, runningSince = now),
            )
        }
        val preferences = FakePreferencesRepository(AppPreferences(true, alma.id))
        val viewModel = AppViewModel(FakeProfilesRepository(listOf(alma, noah)), preferences, FakeParentRepository(), events, FakeTimerNotifications(), FakeColorProfileRepository())
        val collector = launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertEquals(listOf("alma-today"), viewModel.state.value.recentEvents.map { it.id })
        assertEquals(listOf("noah-running"), viewModel.state.value.unfinishedEvents.map { it.id })
        assertNull(viewModel.childHistory.value)

        val history = launch { viewModel.childHistory.collect {} }
        advanceUntilIdle()
        assertEquals(setOf("alma-today", "alma-last-year"), viewModel.childHistory.value?.map { it.id }?.toSet())

        preferences.setActiveChild(noah.id)
        advanceUntilIdle()
        assertEquals(listOf("noah-running"), viewModel.state.value.recentEvents.map { it.id })
        assertEquals(listOf("noah-running"), viewModel.childHistory.value?.map { it.id })
        history.cancel()
        collector.cancel()
    }

    @Test
    fun `journal loads whole days from the last month and more on request`() = runTest(dispatcher) {
        val alma = ChildProfile(name = "Alma", birthDate = java.time.LocalDate.of(2024, 1, 1))
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        val events = FakeCareEventRepository().also { repo ->
            repo.items.value = listOf(10L, 40L, 200L).map { daysAgo ->
                CareEventEntity(id = "d$daysAgo", childId = alma.id, type = CareEventType.Diaper, startedAt = now - daysAgo * day, endedAt = now - daysAgo * day)
            }
        }
        val viewModel = AppViewModel(FakeProfilesRepository(listOf(alma)), FakePreferencesRepository(AppPreferences(true, alma.id)), FakeParentRepository(), events, FakeTimerNotifications(), FakeColorProfileRepository())
        val collector = launch { viewModel.journal.collect {} }
        advanceUntilIdle()
        assertEquals(listOf("d10"), viewModel.journal.value?.events?.map { it.id })
        assertEquals(true, viewModel.journal.value?.hasOlder)
        viewModel.loadOlderJournal(); advanceUntilIdle()
        assertEquals(setOf("d10", "d40"), viewModel.journal.value?.events?.map { it.id }?.toSet())
        repeat(2) { viewModel.loadOlderJournal() }; advanceUntilIdle()
        assertEquals(3, viewModel.journal.value?.events?.size)
        assertEquals(false, viewModel.journal.value?.hasOlder)
        collector.cancel()
    }
}
