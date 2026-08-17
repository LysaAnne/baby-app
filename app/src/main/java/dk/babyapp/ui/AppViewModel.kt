package dk.babyapp.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import dk.babyapp.ui.profile.ProfileDraft
import dk.babyapp.ui.profile.ProfileValidationError
import dk.babyapp.ui.profile.toProfile
import dk.babyapp.ui.profile.validate
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppUiState(
    val preferences: AppPreferences = AppPreferences(),
    val profiles: List<ChildProfile> = emptyList(),
    val loaded: Boolean = false,
    val parents: List<ParentProfile> = emptyList(),
    val parentLinks: List<ChildParentLink> = emptyList(),
    val careProviders: List<CareProvider> = emptyList(),
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

@HiltViewModel
class AppViewModel @Inject constructor(
    private val profilesRepository: ChildProfileRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val photoStore: ProfileImageStorage,
    private val parentRepository: ParentProfileRepository,
) : ViewModel() {
    val state: StateFlow<AppUiState> = combine(
        preferencesRepository.preferences,
        profilesRepository.profiles, parentRepository.parents, parentRepository.links, profilesRepository.careProviders,
    ) { preferences, profiles, parents, links, providers ->
        AppUiState(preferences = preferences, profiles = profiles, loaded = true, parents = parents, parentLinks = links, careProviders = providers)
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

    fun saveProfile(
        draft: ProfileDraft,
        onResult: (ProfileValidationError?) -> Unit,
    ) {
        val units = state.value.preferences.units
        val error = draft.validate(units = units)
        if (error != null) {
            onResult(error)
            return
        }
        viewModelScope.launch {
            val existing = draft.id?.let { profilesRepository.get(it) }
            val profile = draft.toProfile(existing, units)
            profilesRepository.save(profile)
            profilesRepository.setCareProviders(profile.id, draft.careProviders)
            parentRepository.setParents(profile.id, draft.parentIds)
            if (state.value.preferences.activeChildId == null) {
                preferencesRepository.setActiveChild(profile.id)
            }
            onResult(null)
        }
    }

    fun selectChild(id: String) {
        viewModelScope.launch { preferencesRepository.setActiveChild(id) }
    }

    fun updateSettings(settings: OnboardingSettings) {
        viewModelScope.launch {
            preferencesRepository.updateSettings(settings.languageTag, settings.region, settings.units, settings.theme)
        }
    }

    fun deleteProfile(profile: ChildProfile) {
        viewModelScope.launch {
            profilesRepository.delete(profile)
            val next = state.value.profiles.firstOrNull { it.id != profile.id }
            if (state.value.preferences.activeChildId == profile.id) {
                preferencesRepository.setActiveChild(next?.id)
            }
        }
    }

    fun saveParent(parent: ParentProfile) { viewModelScope.launch { parentRepository.save(parent) } }
    fun saveFamilyMember(parent: ParentProfile, childIds: Set<String>) { viewModelScope.launch { parentRepository.save(parent); parentRepository.setChildren(parent.id, childIds) } }
    fun deleteParent(parent: ParentProfile) { viewModelScope.launch { parentRepository.delete(parent) } }

    suspend fun importPhoto(uri: Uri): String = withContext(Dispatchers.IO) { photoStore.import(uri) }

    fun photoFile(fileName: String?): File? = fileName?.let(photoStore::file)?.takeIf(File::exists)
}
