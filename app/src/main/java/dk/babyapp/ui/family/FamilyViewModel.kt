package dk.babyapp.ui.family

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.ui.profile.ProfileDraft
import dk.babyapp.ui.profile.ProfileValidationError
import dk.babyapp.ui.profile.toProfile
import dk.babyapp.ui.profile.validate
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Children, parents and other family members, including their photos. */
@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val profilesRepository: ChildProfileRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val parentRepository: ParentProfileRepository,
    private val photoStore: ProfileImageStorage,
) : ViewModel() {
    // Validation runs synchronously so errors are reported before the dialog closes.
    private val preferences = preferencesRepository.preferences.stateIn(viewModelScope, SharingStarted.Eagerly, AppPreferences())

    fun saveProfile(
        draft: ProfileDraft,
        onResult: (ProfileValidationError?) -> Unit,
    ) {
        val units = preferences.value.units
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
            if (preferencesRepository.preferences.first().activeChildId == null) {
                preferencesRepository.setActiveChild(profile.id)
            }
            onResult(null)
        }
    }

    fun deleteProfile(profile: ChildProfile) {
        viewModelScope.launch {
            val next = profilesRepository.profiles.first().firstOrNull { it.id != profile.id }
            profilesRepository.delete(profile)
            val currentPreferences = preferencesRepository.preferences.first()
            preferencesRepository.updateMedicines(currentPreferences.medicines.filterNot { it.childId == profile.id })
            preferencesRepository.replaceTeeth(currentPreferences.teeth.filterNot { it.childId == profile.id })
            preferencesRepository.updateHeadSideStatistics(currentPreferences.headSideStatistics.copy(correctionDates = currentPreferences.headSideStatistics.correctionDates - profile.id))
            if (currentPreferences.activeChildId == profile.id) {
                preferencesRepository.setActiveChild(next?.id)
            }
        }
    }

    fun saveFamilyMember(parent: ParentProfile, childIds: Set<String>) { viewModelScope.launch { parentRepository.save(parent); parentRepository.setChildren(parent.id, childIds) } }
    fun deleteParent(parent: ParentProfile) { viewModelScope.launch { parentRepository.delete(parent) } }
    fun reorderChildren(ids: List<String>) { viewModelScope.launch { profilesRepository.setOrder(ids) } }
    fun reorderFamily(ids: List<String>) { viewModelScope.launch { parentRepository.setOrder(ids) } }

    suspend fun importPhoto(uri: Uri): String = withContext(Dispatchers.IO) { photoStore.import(uri) }

    fun photoFile(fileName: String?): File? = fileName?.let(photoStore::file)?.takeIf(File::exists)
}
