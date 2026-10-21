package dk.babyapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.babyapp.data.backup.EncryptedBackupService
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.data.color.normalizedHex
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DashboardMetric
import dk.babyapp.data.preferences.HeadSideStatisticsSettings
import dk.babyapp.data.preferences.MeasurementChartSettings
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.reminders.DailyReminderScheduler
import dk.babyapp.ui.OnboardingSettings
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** App settings, screen customisation, the daily reminder, colour profiles and encrypted backup. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: AppPreferencesRepository,
    private val profilesRepository: ChildProfileRepository,
    private val colorProfileRepository: ColorProfileRepository,
    private val backupService: EncryptedBackupService? = null,
    private val reminderScheduler: DailyReminderScheduler? = null,
) : ViewModel() {
    private val colorProfileJson = Json { ignoreUnknownKeys = true; prettyPrint = true }

    // Export is synchronous, so the current colour profiles are kept at hand.
    private val colorProfiles = colorProfileRepository.profiles.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSettings(settings: OnboardingSettings) {
        viewModelScope.launch {
            preferencesRepository.updateSettings(settings.languageTag, settings.region, settings.units, settings.theme)
        }
    }

    fun updateInsightCards(cards: List<dk.babyapp.data.preferences.InsightCard>) = viewModelScope.launch { preferencesRepository.updateInsightCards(cards) }
    fun updateInsightPeriod(days: Int) = viewModelScope.launch { preferencesRepository.updateInsightPeriod(days) }

    fun updateQuickActions(showBreastfeeding: Boolean, showBottle: Boolean, showPumping: Boolean, showDiaper: Boolean) = viewModelScope.launch {
        preferencesRepository.updateQuickActions(showBreastfeeding, showBottle, showPumping, showDiaper)
    }
    fun updateQuickActionCategoryOrder(order: List<dk.babyapp.data.preferences.QuickActionCategory>) = viewModelScope.launch { preferencesRepository.updateQuickActionCategoryOrder(order) }
    fun updateHiddenQuickActions(hidden: Set<String>) = viewModelScope.launch { preferencesRepository.updateHiddenQuickActions(hidden) }
    fun updateDashboardMetrics(metrics: List<DashboardMetric>) = viewModelScope.launch { preferencesRepository.updateDashboardMetrics(metrics) }
    fun updateJournalQuickFilters(filters: List<String>) = viewModelScope.launch { preferencesRepository.updateJournalQuickFilters(filters) }
    fun updateHeadSideStatistics(settings: HeadSideStatisticsSettings) = viewModelScope.launch { preferencesRepository.updateHeadSideStatistics(settings) }
    fun updateMeasurementChartSettings(settings: MeasurementChartSettings) = viewModelScope.launch { preferencesRepository.updateMeasurementChartSettings(settings) }

    fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) = viewModelScope.launch {
        preferencesRepository.updateDailyReminder(enabled, hour, minute)
        reminderScheduler?.update(enabled, hour, minute)
    }

    suspend fun createEncryptedBackup(password: CharArray): ByteArray = withContext(Dispatchers.IO) { requireNotNull(backupService).create(password) }
    suspend fun restoreEncryptedBackup(bytes: ByteArray, password: CharArray) = withContext(Dispatchers.IO) { requireNotNull(backupService).restore(bytes, password) }

    fun saveColorProfile(profile: ColorProfile, onComplete: () -> Unit = {}) = viewModelScope.launch {
        colorProfileRepository.save(profile)
        onComplete()
    }

    fun deleteColorProfile(id: String, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        if (profilesRepository.profiles.first().any { it.colorTheme == id }) {
            onResult(false)
        } else {
            colorProfileRepository.delete(id)
            onResult(true)
        }
    }

    fun moveColorProfile(id: String, direction: Int) = viewModelScope.launch {
        colorProfileRepository.move(id, direction)
    }

    fun exportColorProfiles(): String = colorProfileJson.encodeToString(colorProfiles.value)

    fun importColorProfiles(value: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val imported = runCatching {
            colorProfileJson.decodeFromString<List<ColorProfile>>(value)
        }.getOrNull()
        val usedIds = profilesRepository.profiles.first().map { it.colorTheme }.toSet()
        val valid = imported?.takeIf { profiles ->
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
}
