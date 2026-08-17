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
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.data.color.normalizedHex
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.CareProviderType
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.FamilyMemberRole
import dk.babyapp.data.profile.ProfileAvatar
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.closeSegment
import dk.babyapp.data.tracking.startSegment
import dk.babyapp.tracking.TimerNotificationController
import dk.babyapp.ui.profile.ProfileDraft
import dk.babyapp.ui.profile.ProfileValidationError
import dk.babyapp.ui.profile.toProfile
import dk.babyapp.ui.profile.validate
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class AppUiState(
    val preferences: AppPreferences = AppPreferences(),
    val profiles: List<ChildProfile> = emptyList(),
    val loaded: Boolean = false,
    val parents: List<ParentProfile> = emptyList(),
    val parentLinks: List<ChildParentLink> = emptyList(),
    val careProviders: List<CareProvider> = emptyList(),
    val careEvents: List<CareEventEntity> = emptyList(),
    val colorProfiles: List<ColorProfile> = emptyList(),
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
    private val careEventRepository: CareEventRepository,
    private val timerNotifications: TimerNotificationController,
    private val colorProfileRepository: ColorProfileRepository,
) : ViewModel() {
    private val colorProfileJson = Json { ignoreUnknownKeys = true; prettyPrint = true }
    val state: StateFlow<AppUiState> = combine(
        preferencesRepository.preferences, profilesRepository.profiles, parentRepository.parents,
        parentRepository.links, profilesRepository.careProviders, careEventRepository.events,
        colorProfileRepository.profiles,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AppUiState(
            preferences = values[0] as AppPreferences,
            profiles = values[1] as List<ChildProfile>,
            loaded = true,
            parents = values[2] as List<ParentProfile>,
            parentLinks = values[3] as List<ChildParentLink>,
            careProviders = values[4] as List<CareProvider>,
            careEvents = values[5] as List<CareEventEntity>,
            colorProfiles = values[6] as List<ColorProfile>,
        )
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
    fun reorderChildren(ids: List<String>) { viewModelScope.launch { profilesRepository.setOrder(ids) } }
    fun reorderFamily(ids: List<String>) { viewModelScope.launch { parentRepository.setOrder(ids) } }

    suspend fun importPhoto(uri: Uri): String = withContext(Dispatchers.IO) { photoStore.import(uri) }

    fun photoFile(fileName: String?): File? = fileName?.let(photoStore::file)?.takeIf(File::exists)

    fun startBreastfeeding(childId: String, side: BreastSide) = viewModelScope.launch {
        stopExistingTimer(childId)
        val now = System.currentTimeMillis()
        val event = CareEventEntity(childId = childId, type = CareEventType.Breastfeeding, startedAt = now, runningSince = now, activeSide = side).startSegment(now)
        careEventRepository.save(event); timerNotifications.show(event.id)
    }

    fun startPumping(childId: String) = viewModelScope.launch {
        stopExistingTimer(childId)
        val now = System.currentTimeMillis()
        val event = CareEventEntity(childId = childId, type = CareEventType.Pumping, startedAt = now, runningSince = now).startSegment(now)
        careEventRepository.save(event); timerNotifications.show(event.id)
    }

    fun startSleep(childId: String, sleepType: SleepType, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        if (careEventRepository.activeForChild(childId) != null) {
            onResult(false)
        } else {
            val now = System.currentTimeMillis()
            val event = CareEventEntity(childId = childId, type = CareEventType.Sleep, sleepType = sleepType, startedAt = now, runningSince = now).startSegment(now)
            careEventRepository.save(event)
            timerNotifications.show(event.id)
            onResult(true)
        }
    }

    fun toggleTimer(event: CareEventEntity) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        if (event.runningSince == null) {
            careEventRepository.save(event.startSegment(now).copy(runningSince = now))
        } else {
            careEventRepository.save(accrue(event, now).closeSegment(now).copy(runningSince = null))
        }
    }

    fun switchBreastSide(event: CareEventEntity) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val accrued = accrue(event, now)
        careEventRepository.save(accrued.copy(activeSide = if (event.activeSide == BreastSide.Left) BreastSide.Right else BreastSide.Left, runningSince = if (event.runningSince != null) now else null))
    }

    fun stopTimer(event: CareEventEntity, amountMl: Int? = null, onComplete: (CareEventEntity) -> Unit = {}) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val completed = accrue(event, now).closeSegment(now).copy(endedAt = now, runningSince = null, pumpedAmountMl = amountMl ?: event.pumpedAmountMl)
        careEventRepository.save(completed)
        timerNotifications.hide()
        onComplete(completed)
    }

    fun addBottle(childId: String, time: Long, content: BottleContent, offered: Int?, consumed: Int?, notes: String) =
        viewModelScope.launch { careEventRepository.save(CareEventEntity(childId = childId, type = CareEventType.Bottle, startedAt = time, endedAt = time, bottleContent = content, amountOfferedMl = offered, amountConsumedMl = consumed, notes = notes)) }

    fun addDiaper(childId: String, time: Long, type: DiaperType, observation: String, notes: String, onComplete: (CareEventEntity) -> Unit = {}) =
        viewModelScope.launch {
            val event = CareEventEntity(childId = childId, type = CareEventType.Diaper, startedAt = time, endedAt = time, diaperType = type, observation = observation, notes = notes)
            careEventRepository.save(event); onComplete(event)
        }

    fun addManualTimer(childId: String, type: CareEventType, start: Long, end: Long, side: BreastSide?, amountMl: Int?, notes: String) = viewModelScope.launch {
        val seconds = ((end - start).coerceAtLeast(0) / 1_000)
        careEventRepository.save(CareEventEntity(childId = childId, type = type, startedAt = start, endedAt = end, activeSide = side, leftSeconds = if (side == BreastSide.Left) seconds else 0, rightSeconds = if (side == BreastSide.Right) seconds else 0, pumpedAmountMl = amountMl, notes = notes))
    }

    fun addSleep(
        childId: String,
        start: Long,
        end: Long,
        sleepType: SleepType,
        location: String,
        settlingMethod: String,
        awakenings: Int?,
        quality: SleepQuality?,
        notes: String,
        onResult: (Boolean) -> Unit = {},
    ) = viewModelScope.launch {
        val overlaps = state.value.careEvents.any { event ->
            event.childId == childId && event.type == CareEventType.Sleep && event.deletedAt == null &&
                start < (event.endedAt ?: System.currentTimeMillis()) && end > event.startedAt
        }
        if (end <= start || overlaps) {
            onResult(false)
        } else {
            careEventRepository.save(
                CareEventEntity(
                    childId = childId, type = CareEventType.Sleep, startedAt = start, endedAt = end,
                    leftSeconds = (end - start) / 1_000, sleepType = sleepType, sleepLocation = location,
                    settlingMethod = settlingMethod, awakenings = awakenings, sleepQuality = quality, notes = notes,
                ),
            )
            onResult(true)
        }
    }

    fun updateCareEvent(event: CareEventEntity, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val overlaps = event.type == CareEventType.Sleep && state.value.careEvents.any { other ->
            other.id != event.id && other.childId == event.childId && other.type == CareEventType.Sleep && other.deletedAt == null &&
                event.startedAt < (other.endedAt ?: System.currentTimeMillis()) && (event.endedAt ?: System.currentTimeMillis()) > other.startedAt
        }
        if (overlaps) onResult(false) else { careEventRepository.save(event); onResult(true) }
    }
    fun deleteCareEvent(event: CareEventEntity) = viewModelScope.launch { careEventRepository.softDelete(event) }
    fun updateQuickActions(showBreastfeeding: Boolean, showBottle: Boolean, showPumping: Boolean, showDiaper: Boolean) = viewModelScope.launch {
        preferencesRepository.updateQuickActions(showBreastfeeding, showBottle, showPumping, showDiaper)
    }
    fun restoreTimerNotification(event: CareEventEntity?) { if (event != null && event.endedAt == null) timerNotifications.show(event.id) }
    fun dismissGettingStarted() = viewModelScope.launch { preferencesRepository.markGettingStartedSeen() }

    fun saveColorProfile(profile: ColorProfile, onComplete: () -> Unit = {}) = viewModelScope.launch {
        colorProfileRepository.save(profile)
        onComplete()
    }

    fun deleteColorProfile(id: String, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        if (state.value.profiles.any { it.colorTheme == id }) {
            onResult(false)
        } else {
            colorProfileRepository.delete(id)
            onResult(true)
        }
    }

    fun moveColorProfile(id: String, direction: Int) = viewModelScope.launch {
        colorProfileRepository.move(id, direction)
    }

    fun exportColorProfiles(): String = colorProfileJson.encodeToString(state.value.colorProfiles)

    fun importColorProfiles(value: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val imported = runCatching {
            colorProfileJson.decodeFromString<List<ColorProfile>>(value)
        }.getOrNull()
        val valid = imported?.takeIf { profiles ->
            val usedIds = state.value.profiles.map { it.colorTheme }.toSet()
            profiles.isNotEmpty() && profiles.map { it.id }.toSet().containsAll(usedIds) && profiles.map { it.id }.distinct().size == profiles.size && profiles.all { profile ->
                profile.id.isNotBlank() && profile.name.isNotBlank() && listOf(
                    profile.background, profile.primary, profile.primaryContainer, profile.secondary, profile.tertiary,
                ).all { it.normalizedHex() != null }
            }
        }
        if (valid == null) onResult(false) else {
            colorProfileRepository.replaceAll(valid)
            onResult(true)
        }
    }

    fun createDeveloperTestFamily(onComplete: () -> Unit = {}) = viewModelScope.launch {
        val childId = "developer-test-child-freja"
        val hectorId = "developer-test-child-hector"
        val child = ChildProfile(
            id = childId,
            name = "Freja",
            nickname = "Fre",
            birthStatus = BirthStatus.Born,
            birthDate = LocalDate.now().minusMonths(4),
            birthTime = LocalTime.of(9, 42),
            dueDate = LocalDate.now().minusMonths(4).plusDays(5),
            sex = BiologicalSex.Female,
            birthWeightGrams = 3_480,
            birthLengthCm = 52.0,
            birthHeadCircumferenceCm = 35.0,
            gestationalWeeks = 39,
            gestationalDays = 2,
            cprNumber = "TEST-FREJA",
            fullName = "Freja Testfamilie Jensen",
            registeredAddress = "Testvej 12, 2100 København Ø",
            nationality = "Dansk",
            allergies = "Ingen kendte allergier",
            medicalNotes = "Dette er en testprofil og indeholder ikke rigtige helbredsoplysninger.",
            avatar = ProfileAvatar.Bunny,
            colorTheme = ChildColorTheme.GirlLight.name,
        )
        profilesRepository.save(child)
        profilesRepository.save(
            ChildProfile(
                id = hectorId,
                name = "Hector",
                nickname = "Hec",
                birthStatus = BirthStatus.Born,
                birthDate = LocalDate.now().minusYears(2),
                sex = BiologicalSex.Male,
                cprNumber = "TEST-HECTOR",
                fullName = "Hector Testfamilie Jensen",
                registeredAddress = "Testvej 12, 2100 København Ø",
                nationality = "Dansk",
                avatar = ProfileAvatar.Fox,
                colorTheme = ChildColorTheme.BoyLight.name,
            ),
        )
        profilesRepository.setCareProviders(
            childId,
            listOf(
                CareProvider(id = "developer-provider-hospital", childId = childId, type = CareProviderType.Hospital, name = "Rigshospitalet – test", phone = "+45 35 45 35 45", address = "Blegdamsvej 9, 2100 København Ø", notes = "Testfødested"),
                CareProvider(id = "developer-provider-gp", childId = childId, type = CareProviderType.Gp, name = "Lægehuset Solsikken", phone = "+45 12 34 56 78", email = "test@laegehuset.example", notes = "Fiktiv testkontakt"),
                CareProvider(id = "developer-provider-health", childId = childId, type = CareProviderType.HealthVisitor, name = "Mette Testsen", phone = "+45 22 33 44 55"),
                CareProvider(id = "developer-provider-midwife", childId = childId, type = CareProviderType.Midwife, name = "Anne Jordemoder", phone = "+45 33 44 55 66"),
            ),
        )
        val members = listOf(
            ParentProfile(id = "developer-parent-sofie", name = "Sofie", phone = "+45 20 11 22 33", email = "sofie@example.test", cprNumber = "TEST-SOFIE", avatar = ProfileAvatar.Butterfly, role = FamilyMemberRole.Mother, notes = "Freja og Hectors mor · testprofil"),
            ParentProfile(id = "developer-parent-jonas", name = "Jonas", phone = "+45 21 44 55 66", email = "jonas@example.test", cprNumber = "TEST-JONAS", avatar = ProfileAvatar.Fox, role = FamilyMemberRole.Father, notes = "Freja og Hectors far · testprofil"),
            ParentProfile(id = "developer-grandparent-karen", name = "Karen", phone = "+45 24 55 66 77", email = "karen@example.test", avatar = ProfileAvatar.Panda, role = FamilyMemberRole.Grandmother, notes = "Mormor · testprofil"),
            ParentProfile(id = "developer-grandparent-poul", name = "Poul", phone = "+45 26 77 88 99", email = "poul@example.test", avatar = ProfileAvatar.Lion, role = FamilyMemberRole.Grandfather, notes = "Morfar · testprofil"),
        )
        members.forEach { parentRepository.save(it) }
        parentRepository.setParents(childId, members.map { it.id }.toSet())
        parentRepository.setParents(hectorId, members.map { it.id }.toSet())
        val now = System.currentTimeMillis()
        listOf(
            CareEventEntity(id = "developer-event-breast", childId = childId, type = CareEventType.Breastfeeding, startedAt = now - 3 * 60 * 60 * 1_000, endedAt = now - 2 * 60 * 60 * 1_000 - 42 * 60 * 1_000, activeSide = BreastSide.Right, leftSeconds = 480, rightSeconds = 600, notes = "Rolig amning"),
            CareEventEntity(id = "developer-event-bottle", childId = childId, type = CareEventType.Bottle, startedAt = now - 90 * 60 * 1_000, endedAt = now - 90 * 60 * 1_000, bottleContent = BottleContent.BreastMilk, amountOfferedMl = 120, amountConsumedMl = 105, notes = "Testregistrering"),
            CareEventEntity(id = "developer-event-diaper", childId = childId, type = CareEventType.Diaper, startedAt = now - 35 * 60 * 1_000, endedAt = now - 35 * 60 * 1_000, diaperType = DiaperType.Both, observation = "Normal", notes = "Testregistrering"),
            CareEventEntity(id = "developer-event-pump", childId = childId, type = CareEventType.Pumping, startedAt = now - 5 * 60 * 60 * 1_000, endedAt = now - 5 * 60 * 60 * 1_000 + 15 * 60 * 1_000, leftSeconds = 900, pumpedAmountMl = 85),
        ).forEach { careEventRepository.save(it) }
        preferencesRepository.setActiveChild(childId)
        onComplete()
    }

    fun createDeveloperPaletteChildren(onComplete: () -> Unit = {}) = viewModelScope.launch {
        val colorProfiles = colorProfileRepository.profiles.first()
        colorProfiles.forEach { profile ->
            profilesRepository.save(
                ChildProfile(
                    id = "developer-palette-${profile.id}",
                    name = profile.name,
                    birthDate = LocalDate.now().minusMonths(1),
                    sex = BiologicalSex.PreferNotToSay,
                    colorTheme = profile.id,
                ),
            )
        }
        colorProfiles.firstOrNull()?.let { preferencesRepository.setActiveChild("developer-palette-${it.id}") }
        onComplete()
    }

    private suspend fun stopExistingTimer(childId: String) {
        careEventRepository.activeForChild(childId)?.let { current ->
            val now = System.currentTimeMillis()
            careEventRepository.save(accrue(current, now).closeSegment(now).copy(endedAt = now, runningSince = null))
        }
    }

    private fun accrue(event: CareEventEntity, now: Long): CareEventEntity {
        val seconds = event.runningSince?.let { ((now - it).coerceAtLeast(0) / 1_000) } ?: 0
        return when {
            event.type != CareEventType.Breastfeeding -> event.copy(leftSeconds = event.leftSeconds + seconds)
            event.activeSide == BreastSide.Right -> event.copy(rightSeconds = event.rightSeconds + seconds)
            else -> event.copy(leftSeconds = event.leftSeconds + seconds)
        }
    }
}
