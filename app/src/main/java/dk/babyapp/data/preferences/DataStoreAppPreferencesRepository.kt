package dk.babyapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

class DataStoreAppPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppPreferencesRepository {
    override val preferences: Flow<AppPreferences> = context.appPreferencesDataStore.data.map { values ->
        AppPreferences(
            onboardingCompleted = values[ONBOARDING_COMPLETED] ?: false,
            activeChildId = values[ACTIVE_CHILD_ID],
            languageTag = values[LANGUAGE_TAG] ?: "da",
            region = enumValueOrDefault(values[REGION], DanishRegion.Hovedstaden),
            units = enumValueOrDefault(values[UNITS], MeasurementUnits.Metric),
            theme = enumValueOrDefault(values[THEME], ThemePreference.System),
            showBreastfeedingQuickAction = values[SHOW_BREASTFEEDING] ?: true,
            showBottleQuickAction = values[SHOW_BOTTLE] ?: true,
            showPumpingQuickAction = values[SHOW_PUMPING] ?: true,
            showDiaperQuickAction = values[SHOW_DIAPER] ?: true,
            hasSeenGettingStarted = values[GETTING_STARTED_SEEN] ?: false,
            dashboardMetrics = values[DASHBOARD_METRICS]
                ?.split(',')
                ?.mapNotNull { stored -> DashboardMetric.entries.firstOrNull { it.name == stored } }
                ?.takeIf { it.size == 4 && it.distinct().size == 4 }
                ?: DashboardMetric.defaults,
        )
    }

    override suspend fun updateOnboarding(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
        activeChildId: String?,
    ) {
        context.appPreferencesDataStore.edit { values ->
            values[ONBOARDING_COMPLETED] = true
            values[LANGUAGE_TAG] = languageTag
            values[REGION] = region.name
            values[UNITS] = units.name
            values[THEME] = theme.name
            if (activeChildId == null) values.remove(ACTIVE_CHILD_ID) else values[ACTIVE_CHILD_ID] = activeChildId
        }
    }

    override suspend fun setActiveChild(id: String?) {
        context.appPreferencesDataStore.edit { values ->
            if (id == null) values.remove(ACTIVE_CHILD_ID) else values[ACTIVE_CHILD_ID] = id
        }
    }

    override suspend fun setTheme(theme: ThemePreference) {
        context.appPreferencesDataStore.edit { values -> values[THEME] = theme.name }
    }

    override suspend fun updateSettings(
        languageTag: String,
        region: DanishRegion,
        units: MeasurementUnits,
        theme: ThemePreference,
    ) {
        context.appPreferencesDataStore.edit { values ->
            values[LANGUAGE_TAG] = languageTag
            values[REGION] = region.name
            values[UNITS] = units.name
            values[THEME] = theme.name
        }
    }

    override suspend fun updateQuickActions(showBreastfeeding: Boolean, showBottle: Boolean, showPumping: Boolean, showDiaper: Boolean) {
        context.appPreferencesDataStore.edit { values ->
            values[SHOW_BREASTFEEDING] = showBreastfeeding
            values[SHOW_BOTTLE] = showBottle
            values[SHOW_PUMPING] = showPumping
            values[SHOW_DIAPER] = showDiaper
        }
    }

    override suspend fun updateDashboardMetrics(metrics: List<DashboardMetric>) {
        require(metrics.size == 4 && metrics.distinct().size == 4)
        context.appPreferencesDataStore.edit { values -> values[DASHBOARD_METRICS] = metrics.joinToString(",") { it.name } }
    }

    override suspend fun markGettingStartedSeen() {
        context.appPreferencesDataStore.edit { values -> values[GETTING_STARTED_SEEN] = true }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val ACTIVE_CHILD_ID = stringPreferencesKey("active_child_id")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val REGION = stringPreferencesKey("region")
        val UNITS = stringPreferencesKey("units")
        val THEME = stringPreferencesKey("theme")
        val SHOW_BREASTFEEDING = booleanPreferencesKey("show_breastfeeding")
        val SHOW_BOTTLE = booleanPreferencesKey("show_bottle")
        val SHOW_PUMPING = booleanPreferencesKey("show_pumping")
        val SHOW_DIAPER = booleanPreferencesKey("show_diaper")
        val GETTING_STARTED_SEEN = booleanPreferencesKey("getting_started_seen")
        val DASHBOARD_METRICS = stringPreferencesKey("dashboard_metrics")
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
