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
class DeveloperViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `developer test family creates linked profiles and sample records`() = runTest(dispatcher) {
        val profiles = FakeProfilesRepository()
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val parents = FakeParentRepository()
        val events = FakeCareEventRepository()
        val viewModel = dk.babyapp.ui.developer.DeveloperViewModel(profiles, preferences, parents, events, FakeColorProfileRepository())

        viewModel.createDeveloperTestFamily()
        advanceUntilIdle()

        assertEquals(setOf("Freja", "Hector"), profiles.items.value.map { it.name }.toSet())
        assertEquals(ChildColorTheme.GirlLight.name, profiles.items.value.first { it.name == "Freja" }.colorTheme)
        assertEquals(ChildColorTheme.BoyLight.name, profiles.items.value.first { it.name == "Hector" }.colorTheme)
        assertEquals(4, parents.parents.value.size)
        assertEquals(8, parents.links.value.size)
        assertTrue(events.items.value.size > 600)
        assertTrue(events.items.value.all { it.childId == "developer-test-child-freja" })
        assertEquals(8, events.items.value.count { it.measurementType == MeasurementType.Weight })
        assertEquals(4, events.items.value.count { it.measurementType == MeasurementType.Height })
        assertEquals("developer-test-child-freja", preferences.items.value.activeChildId)
    }

    @Test
    fun `developer palette children creates one minimal child per selectable theme`() = runTest(dispatcher) {
        val profiles = FakeProfilesRepository()
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val viewModel = dk.babyapp.ui.developer.DeveloperViewModel(profiles, preferences, FakeParentRepository(), FakeCareEventRepository(), FakeColorProfileRepository())

        viewModel.createDeveloperPaletteChildren()
        advanceUntilIdle()

        assertEquals(4, profiles.items.value.size)
        assertEquals(defaultColorProfiles.map { it.id }.toSet(), profiles.items.value.map { it.colorTheme }.toSet())
        assertEquals(4, profiles.items.value.map { it.name }.distinct().size)
        assertEquals(true, profiles.items.value.all { it.nickname.isBlank() && it.dueDate == null && it.sex == BiologicalSex.PreferNotToSay })
        assertEquals("developer-palette-NeutralLight", preferences.items.value.activeChildId)
    }
}
