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
    suspend fun markGettingStartedSeen()
}
