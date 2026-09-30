package dk.babyapp.data.preferences

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
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
    suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int)
    suspend fun updateInsightDashboardMetrics(metrics: List<String>)
    suspend fun markGettingStartedSeen()
    suspend fun updateQuickActionCategoryOrder(order: List<String>)
    suspend fun updateJournalQuickFilters(filters: List<String>)
    suspend fun updateHiddenQuickActions(hidden: Set<String>)
    suspend fun updateMedicines(medicines: List<dk.babyapp.data.medicine.MedicinePlan>)
}
