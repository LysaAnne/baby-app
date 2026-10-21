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
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `color profiles can be saved but a profile in use cannot be deleted`() = runTest(dispatcher) {
        val colorProfiles = FakeColorProfileRepository()
        val used = defaultColorProfiles.first()
        val child = ChildProfile(name = "Alma", birthDate = java.time.LocalDate.of(2026, 1, 1), colorTheme = used.id)
        val viewModel = dk.babyapp.ui.settings.SettingsViewModel(FakePreferencesRepository(), FakeProfilesRepository(listOf(child)), colorProfiles)
        advanceUntilIdle()
        val custom = used.copy(id = "custom-test", name = "Min profil", builtIn = false)

        viewModel.saveColorProfile(custom)
        advanceUntilIdle()
        var deleted: Boolean? = null
        viewModel.deleteColorProfile(used.id) { deleted = it }
        advanceUntilIdle()

        assertEquals(true, colorProfiles.profiles.first().any { it.id == custom.id })
        assertEquals(false, deleted)
        assertEquals(true, colorProfiles.profiles.first().any { it.id == used.id })
    }

    @Test
    fun `moving a color profile changes the shared picker order`() = runTest(dispatcher) {
        val colorProfiles = FakeColorProfileRepository()
        val viewModel = dk.babyapp.ui.settings.SettingsViewModel(FakePreferencesRepository(), FakeProfilesRepository(), colorProfiles)
        advanceUntilIdle()
        val lightProfiles = colorProfiles.profiles.first().filterNot { it.isDark }
        val profileToMove = lightProfiles[1]

        viewModel.moveColorProfile(profileToMove.id, -1)
        advanceUntilIdle()

        assertEquals(profileToMove.id, colorProfiles.profiles.first().filterNot { it.isDark }.first().id)
    }
}
