package dk.babyapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dk.babyapp.data.medicine.MedicinePlan

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
                ?.takeIf { it.isNotEmpty() && it.distinct().size == it.size }
                ?: DashboardMetric.defaults,
            dailyReminderEnabled = values[DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderHour = values[DAILY_REMINDER_HOUR] ?: 20,
            dailyReminderMinute = values[DAILY_REMINDER_MINUTE] ?: 0,
            insightDashboardMetrics = values[INSIGHT_DASHBOARD_METRICS]?.split(',')?.filter(String::isNotBlank)?.takeIf { it.size in 3..6 } ?: listOf("Sleep", "Feedings", "Diapers", "TummyTime"),
            quickActionCategoryOrder = values[QUICK_ACTION_CATEGORY_ORDER]?.split(',') ?: emptyList(),
            defaultNippleShield = values[DEFAULT_NIPPLE_SHIELD] ?: false,
            journalQuickFilters = values[JOURNAL_QUICK_FILTERS]?.split(',') ?: AppPreferences().journalQuickFilters,
            hiddenQuickActions = values[HIDDEN_QUICK_ACTIONS]?.split(',')?.filter(String::isNotBlank)?.toSet() ?: emptySet(),
            medicines = values[MEDICINES]?.let { runCatching { Json.decodeFromString<List<MedicinePlan>>(it) }.getOrNull() } ?: emptyList(),
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
        require(metrics.isNotEmpty() && metrics.distinct().size == metrics.size)
        context.appPreferencesDataStore.edit { values -> values[DASHBOARD_METRICS] = metrics.joinToString(",") { it.name } }
    }

    override suspend fun updateDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.appPreferencesDataStore.edit { values ->
            values[DAILY_REMINDER_ENABLED] = enabled
            values[DAILY_REMINDER_HOUR] = hour.coerceIn(0, 23)
            values[DAILY_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }
    override suspend fun updateInsightDashboardMetrics(metrics: List<String>) {
        require(metrics.size in 3..6 && metrics.distinct().size == metrics.size)
        context.appPreferencesDataStore.edit { it[INSIGHT_DASHBOARD_METRICS] = metrics.joinToString(",") }
    }

    override suspend fun markGettingStartedSeen() {
        context.appPreferencesDataStore.edit { values -> values[GETTING_STARTED_SEEN] = true }
    }

    override suspend fun updateQuickActionCategoryOrder(order: List<String>) {
        context.appPreferencesDataStore.edit { it[QUICK_ACTION_CATEGORY_ORDER] = order.joinToString(",") }
    }

    override suspend fun updateDefaultNippleShield(enabled: Boolean) {
        context.appPreferencesDataStore.edit { it[DEFAULT_NIPPLE_SHIELD] = enabled }
    }

    override suspend fun updateJournalQuickFilters(filters: List<String>) {
        require(filters.size == 4 && filters.distinct().size == 4)
        context.appPreferencesDataStore.edit { it[JOURNAL_QUICK_FILTERS] = filters.joinToString(",") }
    }

    override suspend fun updateHiddenQuickActions(hidden: Set<String>) {
        context.appPreferencesDataStore.edit { it[HIDDEN_QUICK_ACTIONS] = hidden.joinToString(",") }
    }

    override suspend fun updateMedicines(medicines: List<MedicinePlan>) {
        context.appPreferencesDataStore.edit { it[MEDICINES] = Json.encodeToString(medicines) }
        dk.babyapp.reminders.MedicineReminderScheduler(context).sync(medicines)
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
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val INSIGHT_DASHBOARD_METRICS = stringPreferencesKey("insight_dashboard_metrics")
        val QUICK_ACTION_CATEGORY_ORDER = stringPreferencesKey("quick_action_category_order")
        val DEFAULT_NIPPLE_SHIELD = booleanPreferencesKey("default_nipple_shield")
        val JOURNAL_QUICK_FILTERS = stringPreferencesKey("journal_quick_filters")
        val HIDDEN_QUICK_ACTIONS = stringPreferencesKey("hidden_quick_actions")
        val MEDICINES = stringPreferencesKey("medicines")
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
