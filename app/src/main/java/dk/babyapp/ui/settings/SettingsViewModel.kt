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
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context? = null,
) : ViewModel() {

    // Export is synchronous, so the current colour profiles are kept at hand.
    private val colorProfiles = colorProfileRepository.profiles.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateSettings(settings: OnboardingSettings) {
        viewModelScope.launch {
            preferencesRepository.updateSettings(settings.languageTag, settings.region, settings.units, settings.theme)
        }
    }

    fun updateInsightCards(cards: List<dk.babyapp.data.preferences.InsightCard>) = viewModelScope.launch { preferencesRepository.updateInsightCards(cards) }
    fun markBackupSaved() = viewModelScope.launch { preferencesRepository.markBackupSaved(System.currentTimeMillis()) }
    fun updateInsightPeriod(days: Int) = viewModelScope.launch { preferencesRepository.updateInsightPeriod(days) }
    fun updateInsightRange(from: String, through: String) = viewModelScope.launch { preferencesRepository.updateInsightRange(from, through) }
    fun updateSimpleQuickRegistration(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateSimpleQuickRegistration(enabled) }
    fun updateCustomReminders(reminders: List<dk.babyapp.data.preferences.CustomReminder>) = viewModelScope.launch {
        val previous = preferencesRepository.preferences.first().customReminders
        preferencesRepository.updateCustomReminders(reminders)
        appContext?.let { dk.babyapp.reminders.CustomReminderScheduler.update(it, previous, reminders) }
    }
    fun updateDevelopmentReminders(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.updateDevelopmentReminders(enabled)
        if (enabled) appContext?.let { dk.babyapp.reminders.DevelopmentReminderScheduler.ensureScheduled(it) }
    }
    fun updateShowDevelopmentStrip(shown: Boolean) = viewModelScope.launch { preferencesRepository.updateShowDevelopmentStrip(shown) }
    fun updateCollapsedCards(cards: Set<String>) = viewModelScope.launch { preferencesRepository.updateCollapsedCards(cards) }
    fun updateShowBulletinBoard(shown: Boolean) = viewModelScope.launch { preferencesRepository.updateShowBulletinBoard(shown) }
    fun updateHealthQuestions(questions: List<dk.babyapp.data.preferences.HealthQuestion>) = viewModelScope.launch { preferencesRepository.updateHealthQuestions(questions) }
    fun updateDevelopmentChecks(checks: List<dk.babyapp.data.preferences.DevelopmentCheck>) = viewModelScope.launch { preferencesRepository.updateDevelopmentChecks(checks) }
    fun updateSongbook(songbook: dk.babyapp.domain.SongbookData) = viewModelScope.launch { preferencesRepository.updateSongbook(songbook) }
    fun updateBoardNotes(notes: List<dk.babyapp.data.preferences.BoardNote>) = viewModelScope.launch { preferencesRepository.updateBoardNotes(notes) }
    fun updateHealthReminders(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateHealthReminders(enabled) }

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
}
