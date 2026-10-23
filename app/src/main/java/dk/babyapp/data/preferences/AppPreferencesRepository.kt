package dk.babyapp.data.preferences

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    suspend fun updateHeadSideStatistics(settings: HeadSideStatisticsSettings)
    suspend fun updateMeasurementChartSettings(settings: MeasurementChartSettings)
    suspend fun updateLastPumpingMethod(method: String)
    val preferences: Flow<AppPreferences>
    suspend fun updateOnboarding(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
        activeChildId: String?,
    )
    suspend fun setActiveChild(id: String?)
    suspend fun setTheme(theme: ThemePreference)
    suspend fun updateSettings(languageTag: String, region: DanishRegion, units: MeasurementUnits, theme: ThemePreference)
    suspend fun updateQuickActions(showBreastfeeding: Boolean, showBottle: Boolean, showPumping: Boolean, showDiaper: Boolean)
    suspend fun updateDashboardMetrics(metrics: List<DashboardMetric>)
    suspend fun updateBookReminderFrequency(frequency: BookReminderFrequency)
    suspend fun updateBookReminders(enabled: Boolean)
    suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int)
    suspend fun updateInsightDashboardMetrics(metrics: List<String>)
    suspend fun markGettingStartedSeen()
    suspend fun updateInsightCards(cards: List<InsightCard>)
    suspend fun updateInsightPeriod(days: Int)
    suspend fun updateInsightRange(from: String, through: String)
    suspend fun updateSimpleQuickRegistration(enabled: Boolean)
    suspend fun markBackupSaved(at: Long)
    suspend fun updateHealthQuestions(questions: List<HealthQuestion>)
    suspend fun updateBoardNotes(notes: List<BoardNote>)
    suspend fun updateHealthReminders(enabled: Boolean)
    suspend fun updateQuickActionCategoryOrder(order: List<QuickActionCategory>)
    suspend fun updateDefaultNippleShield(enabled: Boolean)
    suspend fun updateJournalQuickFilters(filters: List<String>)
    suspend fun updateHiddenQuickActions(hidden: Set<String>)
}
