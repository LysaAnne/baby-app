package dk.babyapp.ui

import android.net.Uri
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.ThemePreference
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

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
        val viewModel = AppViewModel(profiles, preferences, FakePhotoStorage(), FakeParentRepository(), FakeCareEventRepository(), FakeTimerNotifications())
        var result: Any? = Unit

        viewModel.completeOnboarding(settings = OnboardingSettings(languageTag = "da")) { result = null }
        advanceUntilIdle()

        assertNull(result)
        assertEquals(emptyList<ChildProfile>(), profiles.items.value)
        assertNull(preferences.items.value.activeChildId)
        assertEquals(true, preferences.items.value.onboardingCompleted)
    }

    @Test
    fun `deleting active child selects remaining child`() = runTest(dispatcher) {
        val first = ChildProfile(name = "Alma", birthDate = java.time.LocalDate.of(2026, 1, 1))
        val second = ChildProfile(name = "Noah", birthDate = java.time.LocalDate.of(2025, 1, 1))
        val profiles = FakeProfilesRepository(listOf(first, second))
        val preferences = FakePreferencesRepository(AppPreferences(true, first.id))
        val viewModel = AppViewModel(profiles, preferences, FakePhotoStorage(), FakeParentRepository(), FakeCareEventRepository(), FakeTimerNotifications())
        val collector = launch { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.deleteProfile(first)
        advanceUntilIdle()

        assertEquals(listOf(second), profiles.items.value)
        assertEquals(second.id, preferences.items.value.activeChildId)
        collector.cancel()
    }

    @Test
    fun `developer test family creates linked profiles and sample records`() = runTest(dispatcher) {
        val profiles = FakeProfilesRepository()
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val parents = FakeParentRepository()
        val events = FakeCareEventRepository()
        val viewModel = AppViewModel(profiles, preferences, FakePhotoStorage(), parents, events, FakeTimerNotifications())

        viewModel.createDeveloperTestFamily()
        advanceUntilIdle()

        assertEquals("Freja", profiles.items.value.single().name)
        assertEquals(4, parents.parents.value.size)
        assertEquals(4, parents.links.value.size)
        assertEquals(4, events.items.value.size)
        assertEquals("developer-test-child-freja", preferences.items.value.activeChildId)
    }

    @Test
    fun `dismissing getting started persists the choice`() = runTest(dispatcher) {
        val preferences = FakePreferencesRepository(AppPreferences(onboardingCompleted = true))
        val viewModel = AppViewModel(FakeProfilesRepository(), preferences, FakePhotoStorage(), FakeParentRepository(), FakeCareEventRepository(), FakeTimerNotifications())

        viewModel.dismissGettingStarted()
        advanceUntilIdle()

        assertEquals(true, preferences.items.value.hasSeenGettingStarted)
    }
}

private class FakeProfilesRepository(initial: List<ChildProfile> = emptyList()) : ChildProfileRepository {
    val items = MutableStateFlow(initial)
    override val profiles: Flow<List<ChildProfile>> = items
    override val careProviders = MutableStateFlow<List<CareProvider>>(emptyList())
    override suspend fun get(id: String) = items.value.firstOrNull { it.id == id }
    override suspend fun save(profile: ChildProfile) {
        items.value = items.value.filterNot { it.id == profile.id } + profile
    }
    override suspend fun delete(profile: ChildProfile) {
        items.value = items.value.filterNot { it.id == profile.id }
    }
    override suspend fun setCareProviders(childId: String, providers: List<CareProvider>) { careProviders.value = providers.map { it.copy(childId = childId) } }
}

private class FakePreferencesRepository(initial: AppPreferences = AppPreferences()) : AppPreferencesRepository {
    val items = MutableStateFlow(initial)
    override val preferences: Flow<AppPreferences> = items
    override suspend fun updateOnboarding(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
        activeChildId: String?,
    ) {
        items.value = AppPreferences(true, activeChildId, languageTag, region, units, theme)
    }
    override suspend fun setActiveChild(id: String?) { items.value = items.value.copy(activeChildId = id) }
    override suspend fun setTheme(theme: ThemePreference) { items.value = items.value.copy(theme = theme) }
    override suspend fun updateSettings(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
    ) { items.value = items.value.copy(languageTag = languageTag, region = region, units = units, theme = theme) }
    override suspend fun updateQuickActions(showBreastfeeding: Boolean, showBottle: Boolean, showPumping: Boolean, showDiaper: Boolean) {
        items.value = items.value.copy(showBreastfeedingQuickAction = showBreastfeeding, showBottleQuickAction = showBottle, showPumpingQuickAction = showPumping, showDiaperQuickAction = showDiaper)
    }
    override suspend fun markGettingStartedSeen() { items.value = items.value.copy(hasSeenGettingStarted = true) }
}

private class FakePhotoStorage : ProfileImageStorage {
    override fun import(uri: Uri) = "photo"
    override fun file(fileName: String) = File(fileName)
    override fun delete(fileName: String) = Unit
}

private class FakeParentRepository : ParentProfileRepository {
    override val parents = MutableStateFlow<List<ParentProfile>>(emptyList())
    override val links = MutableStateFlow<List<ChildParentLink>>(emptyList())
    override suspend fun save(parent: ParentProfile) { parents.value = parents.value.filterNot { it.id == parent.id } + parent }
    override suspend fun delete(parent: ParentProfile) { parents.value = parents.value.filterNot { it.id == parent.id } }
    override suspend fun setParents(childId: String, parentIds: Set<String>) { links.value = parentIds.map { ChildParentLink(childId, it) } }
    override suspend fun setChildren(memberId: String, childIds: Set<String>) { links.value = childIds.map { ChildParentLink(it, memberId) } }
}

private class FakeCareEventRepository : CareEventRepository {
    val items = MutableStateFlow<List<CareEventEntity>>(emptyList())
    override val events: Flow<List<CareEventEntity>> = items
    override suspend fun save(event: CareEventEntity) { items.value = items.value.filterNot { it.id == event.id } + event }
    override suspend fun get(id: String) = items.value.firstOrNull { it.id == id }
    override suspend fun activeForChild(childId: String) = items.value.firstOrNull { it.childId == childId && it.isRunning }
    override suspend fun softDelete(event: CareEventEntity) { items.value = items.value.filterNot { it.id == event.id } }
}
private class FakeTimerNotifications : TimerNotificationController { override fun show(eventId: String) = Unit; override fun hide() = Unit }
