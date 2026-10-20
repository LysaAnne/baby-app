package dk.babyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.babyapp.data.book.BabyBookPage
import dk.babyapp.data.book.BabyBookRepository
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.medicine.MedicineRepository
import dk.babyapp.data.teeth.ToothRecord
import dk.babyapp.data.teeth.TeethRepository
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.ThemePreference
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.reminders.DailyReminderScheduler
import dk.babyapp.tracking.TimerNotificationController
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val preferences: AppPreferences = AppPreferences(),
    val profiles: List<ChildProfile> = emptyList(),
    val loaded: Boolean = false,
    val parents: List<ParentProfile> = emptyList(),
    val parentLinks: List<ChildParentLink> = emptyList(),
    val careProviders: List<CareProvider> = emptyList(),
    /** The active child's registrations from [recentFrom] on, plus unfinished timers and drafts. */
    val recentEvents: List<CareEventEntity> = emptyList(),
    val recentFrom: LocalDate? = null,
    /** Started timers of every child. */
    val unfinishedEvents: List<CareEventEntity> = emptyList(),
    val bookPages: List<BabyBookPage> = emptyList(),
    val colorProfiles: List<ColorProfile> = emptyList(),
    val medicines: List<MedicinePlan> = emptyList(),
    val teeth: List<ToothRecord> = emptyList(),
) {
    val activeChild: ChildProfile?
        get() = profiles.firstOrNull { it.id == preferences.activeChildId } ?: profiles.firstOrNull()
}

data class OnboardingSettings(
    val languageTag: String = "da",
    val region: DanishRegion = DanishRegion.Hovedstaden,
    val units: MeasurementUnits = MeasurementUnits.Metric,
    val theme: ThemePreference = ThemePreference.System,
)

/**
 * The state every screen reads, plus app-wide actions (onboarding, active child, reminders).
 * Changes to data live in the feature ViewModels: tracking, family, settings, book and developer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppViewModel @Inject constructor(
    profilesRepository: ChildProfileRepository,
    private val preferencesRepository: AppPreferencesRepository,
    parentRepository: ParentProfileRepository,
    careEventRepository: CareEventRepository,
    private val timerNotifications: TimerNotificationController,
    colorProfileRepository: ColorProfileRepository,
    bookRepository: BabyBookRepository? = null,
    private val reminderScheduler: DailyReminderScheduler? = null,
    medicineRepository: MedicineRepository? = null,
    teethRepository: TeethRepository? = null,
) : ViewModel() {
    private val profileState = combine(
        preferencesRepository.preferences,
        profilesRepository.profiles,
        parentRepository.parents,
        parentRepository.links,
    ) { preferences, profiles, parents, links ->
        AppUiState(preferences = preferences, profiles = profiles, parents = parents, parentLinks = links)
    }

    private val careLists = combine(
        medicineRepository?.plans ?: flowOf(emptyList()),
        teethRepository?.records ?: flowOf(emptyList()),
    ) { medicines, teeth -> medicines to teeth }

    private val activeChildId = profileState.map { it.activeChild?.id }.distinctUntilChanged()

    // Today only needs recent days; loading years of history after every change took about a second.
    private val recentEvents = activeChildId.flatMapLatest { id ->
        if (id == null) flowOf(null to emptyList()) else {
            val from = LocalDate.now().minusDays(RECENT_DAYS)
            careEventRepository.recentEventsForChild(id, from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()).map { from to it }
        }
    }

    private val appearance = combine(colorProfileRepository.profiles, bookRepository?.pages ?: flowOf(emptyList())) { colors, pages -> colors to pages }

    val state: StateFlow<AppUiState> = combine(
        combine(profileState, careLists) { base, (medicines, teeth) -> base.copy(medicines = medicines, teeth = teeth) },
        profilesRepository.careProviders,
        recentEvents,
        careEventRepository.unfinishedEvents,
        appearance,
    ) { base, careProviders, (recentFrom, recent), unfinished, (colorProfiles, bookPages) ->
        base.copy(
            loaded = true, bookPages = bookPages, careProviders = careProviders, colorProfiles = colorProfiles,
            recentEvents = recent, recentFrom = recentFrom, unfinishedEvents = unfinished,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

    /**
     * The active child's whole history, for Journal, Overview, charts and export. It is only loaded
     * while collected. Null until loaded, including right after switching child.
     */
    val childHistory: StateFlow<List<CareEventEntity>?> = activeChildId.flatMapLatest { id ->
        flow {
            emit(null)
            emitAll(if (id == null) flowOf(emptyList()) else careEventRepository.eventsForChild(id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun completeOnboarding(
        settings: OnboardingSettings,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            preferencesRepository.updateOnboarding(
                languageTag = settings.languageTag,
                region = settings.region,
                units = settings.units,
                theme = settings.theme,
                activeChildId = null,
            )
            onComplete()
        }
    }

    fun selectChild(id: String) {
        viewModelScope.launch { preferencesRepository.setActiveChild(id) }
    }

    fun dismissGettingStarted() = viewModelScope.launch { preferencesRepository.markGettingStartedSeen() }

    fun syncDailyReminder() {
        val preferences = state.value.preferences
        reminderScheduler?.update(preferences.dailyReminderEnabled, preferences.dailyReminderHour, preferences.dailyReminderMinute)
    }

    fun restoreTimerNotification(event: CareEventEntity?) { if (event != null && event.endedAt == null) timerNotifications.show(event.id) }

    private companion object {
        const val RECENT_DAYS = 35L
    }
}
