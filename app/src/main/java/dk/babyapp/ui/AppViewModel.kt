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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppUiState(
    val preferences: AppPreferences = AppPreferences(),
    val profiles: List<ChildProfile> = emptyList(),
    val loaded: Boolean = false,
    val parents: List<ParentProfile> = emptyList(),
    val parentLinks: List<ChildParentLink> = emptyList(),
    val careProviders: List<CareProvider> = emptyList(),
    val careEvents: List<CareEventEntity> = emptyList(),
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

    val state: StateFlow<AppUiState> = combine(
        combine(profileState, careLists) { base, (medicines, teeth) -> base.copy(medicines = medicines, teeth = teeth) },
        profilesRepository.careProviders,
        careEventRepository.events,
        colorProfileRepository.profiles,
        bookRepository?.pages ?: flowOf(emptyList()),
    ) { base, careProviders, careEvents, colorProfiles, bookPages ->
        base.copy(loaded = true, bookPages = bookPages, careProviders = careProviders, careEvents = careEvents, colorProfiles = colorProfiles)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUiState())

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
}
