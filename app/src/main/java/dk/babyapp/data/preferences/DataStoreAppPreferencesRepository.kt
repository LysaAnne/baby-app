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

internal val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

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
            bookReminderFrequency = enumValueOrDefault(values[BOOK_FREQUENCY], BookReminderFrequency.ThreeWeekly),
            bookRemindersEnabled = values[BOOK_REMINDERS_ENABLED] ?: true,
            dailyReminderEnabled = false,
            dailyReminderHour = values[DAILY_REMINDER_HOUR] ?: 20,
            dailyReminderMinute = values[DAILY_REMINDER_MINUTE] ?: 0,
            insightDashboardMetrics = values[INSIGHT_DASHBOARD_METRICS]?.split(',')?.filter(String::isNotBlank)?.takeIf { it.size in 3..6 } ?: listOf("Sleep", "Feedings", "Diapers", "TummyTime"),
            quickActionCategoryOrder = values[QUICK_ACTION_CATEGORY_ORDER]?.split(',')?.mapNotNull(QuickActionCategory::fromStored) ?: emptyList(),
            defaultNippleShield = values[DEFAULT_NIPPLE_SHIELD] ?: false,
            headSideStatistics = values[HEAD_SIDE_STATISTICS]?.let { Json.decodeFromString<HeadSideStatisticsSettings>(it) } ?: HeadSideStatisticsSettings(),
            measurementChartSettings = values[MEASUREMENT_CHART_SETTINGS]?.let { runCatching { Json.decodeFromString<MeasurementChartSettings>(it) }.getOrNull() } ?: MeasurementChartSettings(),
            lastPumpingMethod = values[LAST_PUMPING_METHOD] ?: "",
            journalQuickFilters = values[JOURNAL_QUICK_FILTERS]?.split(',')?.map(JournalFilterKeys::normalize)?.distinct()
                // Medicin was added as a fifth quick filter; saved choices keep their four and get it at the end.
                ?.let { saved -> if (saved.size == 4 && JournalFilterKeys.MEDICINE !in saved) saved + JournalFilterKeys.MEDICINE else saved } ?: AppPreferences().journalQuickFilters,
            insightCards = values[INSIGHT_CARDS]?.split(',')?.mapNotNull { name -> InsightCard.entries.firstOrNull { it.name == name } }?.let { saved ->
                // Cards added in a later version are shown; cards the user hid stay hidden.
                val known = values[INSIGHT_CARDS_KNOWN]?.split(',')?.toSet() ?: (InsightCard.entries.map { it.name }.toSet() - InsightCard.Health.name)
                saved + InsightCard.entries.filter { it.name !in known && it !in saved }
            } ?: InsightCard.entries,
            insightPeriodDays = values[INSIGHT_PERIOD]?.takeIf { it in setOf(7, 30, 90, 180, 365, 0, -1) } ?: 7,
            insightFrom = values[INSIGHT_FROM],
            insightThrough = values[INSIGHT_THROUGH],
            simpleQuickRegistration = values[SIMPLE_QUICK] ?: false,
            showBulletinBoard = values[SHOW_BULLETIN_BOARD] ?: true,
            lastBackupAt = values[LAST_BACKUP_AT],
            healthQuestions = values[HEALTH_QUESTIONS]?.let { runCatching { Json.decodeFromString<List<HealthQuestion>>(it) }.getOrNull() } ?: emptyList(),
            boardNotes = values[BOARD_NOTES]?.let { runCatching { Json.decodeFromString<List<BoardNote>>(it) }.getOrNull() } ?: emptyList(),
            healthRemindersEnabled = values[HEALTH_REMINDERS_ENABLED] ?: true,
            hiddenQuickActions = values[HIDDEN_QUICK_ACTIONS]?.split(',')?.filter(String::isNotBlank)?.toSet() ?: emptySet(),
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

    override suspend fun updateQuickActionCategoryOrder(order: List<QuickActionCategory>) {
        context.appPreferencesDataStore.edit { it[QUICK_ACTION_CATEGORY_ORDER] = order.joinToString(",") { category -> category.name } }
    }

    override suspend fun updateBookReminderFrequency(frequency: BookReminderFrequency) {
        context.appPreferencesDataStore.edit { it[BOOK_FREQUENCY] = frequency.name }
    }

    override suspend fun updateBookReminders(enabled: Boolean) {
        context.appPreferencesDataStore.edit { it[BOOK_REMINDERS_ENABLED] = enabled }
    }

    override suspend fun updateDefaultNippleShield(enabled: Boolean) {
        context.appPreferencesDataStore.edit { it[DEFAULT_NIPPLE_SHIELD] = enabled }
    }

    override suspend fun updateHeadSideStatistics(settings: HeadSideStatisticsSettings) {
        context.appPreferencesDataStore.edit { it[HEAD_SIDE_STATISTICS] = Json.encodeToString(settings) }
    }

    override suspend fun updateMeasurementChartSettings(settings: MeasurementChartSettings) {
        context.appPreferencesDataStore.edit { it[MEASUREMENT_CHART_SETTINGS] = Json.encodeToString(settings) }
    }

    override suspend fun updateLastPumpingMethod(method: String) {
        context.appPreferencesDataStore.edit { it[LAST_PUMPING_METHOD] = method }
    }

    override suspend fun updateJournalQuickFilters(filters: List<String>) {
        require(filters.size in 4..5 && filters.distinct().size == filters.size)
        context.appPreferencesDataStore.edit { it[JOURNAL_QUICK_FILTERS] = filters.joinToString(",") }
    }

    override suspend fun updateInsightCards(cards: List<InsightCard>) {
        context.appPreferencesDataStore.edit { it[INSIGHT_CARDS] = cards.joinToString(",") { card -> card.name }; it[INSIGHT_CARDS_KNOWN] = InsightCard.entries.joinToString(",") { card -> card.name } }
    }

    override suspend fun updateHealthQuestions(questions: List<HealthQuestion>) {
        context.appPreferencesDataStore.edit { it[HEALTH_QUESTIONS] = Json.encodeToString(questions) }
    }

    override suspend fun updateBoardNotes(notes: List<BoardNote>) {
        context.appPreferencesDataStore.edit { it[BOARD_NOTES] = Json.encodeToString(notes) }
    }

    override suspend fun updateHealthReminders(enabled: Boolean) {
        context.appPreferencesDataStore.edit { it[HEALTH_REMINDERS_ENABLED] = enabled }
    }

    override suspend fun markBackupSaved(at: Long) {
        context.appPreferencesDataStore.edit { it[LAST_BACKUP_AT] = at }
    }

    override suspend fun updateInsightPeriod(days: Int) {
        context.appPreferencesDataStore.edit { it[INSIGHT_PERIOD] = days }
    }

    override suspend fun updateInsightRange(from: String, through: String) {
        context.appPreferencesDataStore.edit { it[INSIGHT_FROM] = from; it[INSIGHT_THROUGH] = through }
    }

    override suspend fun updateSimpleQuickRegistration(enabled: Boolean) {
        context.appPreferencesDataStore.edit { it[SIMPLE_QUICK] = enabled }
    }

    override suspend fun updateShowBulletinBoard(shown: Boolean) {
        context.appPreferencesDataStore.edit { it[SHOW_BULLETIN_BOARD] = shown }
    }

    override suspend fun updateHiddenQuickActions(hidden: Set<String>) {
        context.appPreferencesDataStore.edit { it[HIDDEN_QUICK_ACTIONS] = hidden.joinToString(",") }
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
        val BOOK_REMINDERS_ENABLED = booleanPreferencesKey("book_reminders_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val INSIGHT_DASHBOARD_METRICS = stringPreferencesKey("insight_dashboard_metrics")
        val QUICK_ACTION_CATEGORY_ORDER = stringPreferencesKey("quick_action_category_order")
        val DEFAULT_NIPPLE_SHIELD = booleanPreferencesKey("default_nipple_shield")
        val BOOK_FREQUENCY = stringPreferencesKey("book_reminder_frequency")
        val HEAD_SIDE_STATISTICS = stringPreferencesKey("head_side_statistics")
        val MEASUREMENT_CHART_SETTINGS = stringPreferencesKey("measurement_chart_settings")
        val LAST_PUMPING_METHOD = stringPreferencesKey("last_pumping_method")
        val JOURNAL_QUICK_FILTERS = stringPreferencesKey("journal_quick_filters")
        val HIDDEN_QUICK_ACTIONS = stringPreferencesKey("hidden_quick_actions")
        val INSIGHT_CARDS = stringPreferencesKey("insight_cards")
        val INSIGHT_PERIOD = intPreferencesKey("insight_period")
        val INSIGHT_FROM = stringPreferencesKey("insight_from")
        val INSIGHT_THROUGH = stringPreferencesKey("insight_through")
        val INSIGHT_CARDS_KNOWN = stringPreferencesKey("insight_cards_known")
        val SIMPLE_QUICK = booleanPreferencesKey("simple_quick_registration")
        val SHOW_BULLETIN_BOARD = booleanPreferencesKey("show_bulletin_board")
        val LAST_BACKUP_AT = androidx.datastore.preferences.core.longPreferencesKey("last_backup_at")
        val HEALTH_QUESTIONS = stringPreferencesKey("health_questions")
        val BOARD_NOTES = stringPreferencesKey("board_notes")
        val HEALTH_REMINDERS_ENABLED = booleanPreferencesKey("health_reminders_enabled")
    }
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
