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
}
