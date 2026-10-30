package dk.babyapp.data.preferences

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val activeChildId: String? = null,
    val languageTag: String = "da",
    val region: DanishRegion = DanishRegion.Hovedstaden,
    val units: MeasurementUnits = MeasurementUnits.Metric,
    val theme: ThemePreference = ThemePreference.System,
    val showBreastfeedingQuickAction: Boolean = true,
    val showBottleQuickAction: Boolean = true,
    val showPumpingQuickAction: Boolean = true,
    val showDiaperQuickAction: Boolean = true,
    val hasSeenGettingStarted: Boolean = false,
    val dashboardMetrics: List<DashboardMetric> = DashboardMetric.defaults,
    val bookReminderFrequency: BookReminderFrequency = BookReminderFrequency.ThreeWeekly,
    val bookRemindersEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0,
    val insightDashboardMetrics: List<String> = listOf("Sleep", "Feedings", "Diapers", "TummyTime"),
    val quickActionCategoryOrder: List<QuickActionCategory> = emptyList(),
    val defaultNippleShield: Boolean = false,
    val journalQuickFilters: List<String> = JournalFilterKeys.defaults,
    val hiddenQuickActions: Set<String> = emptySet(),
    val headSideStatistics: HeadSideStatisticsSettings = HeadSideStatisticsSettings(),
    val measurementChartSettings: MeasurementChartSettings = MeasurementChartSettings(),
    val lastPumpingMethod: String = "",
    /** Cards shown on Indsigt, in order; cards left out are hidden. */
    val insightCards: List<InsightCard> = InsightCard.entries,
    /** Days shown in Indsigt; 0 means all time and -1 the chosen dates [insightFrom]–[insightThrough]. */
    val insightPeriodDays: Int = 7,
    val insightFrom: String? = null,
    val insightThrough: String? = null,
    /** Today's quick registration as compact icon rows instead of cards. */
    val simpleQuickRegistration: Boolean = false,
    /** The opslagstavle at the bottom of the I dag card. */
    val showBulletinBoard: Boolean = true,
    /** The development strip on I dag. */
    val showDevelopmentStrip: Boolean = true,
    /** A notification when a development spurt begins or a milestone's usual window opens. */
    val developmentRemindersEnabled: Boolean = false,
    /** Own reminders, e.g. mavetid on weekdays at 10. */
    val customReminders: List<CustomReminder> = emptyList(),
    /** Cards on I dag that are folded together: "today", "events", "development". */
    val collapsedCards: Set<String> = emptySet(),
    /** When an encrypted backup was last saved to a file, or null if never. */
    val lastBackupAt: Long? = null,
    /** Questions for the next health visit, per child; attached to a visit when it is registered. */
    val healthQuestions: List<HealthQuestion> = emptyList(),
    /** Own messages on the I dag opslagstavle. */
    val boardNotes: List<BoardNote> = emptyList(),
    /** Skills, games and milestones ticked off under Udvikling. */
    val developmentChecks: List<DevelopmentCheck> = emptyList(),
    /** Favourites, own lyrics and own songs in Sangbog; shared by the family, not per child. */
    val songbook: dk.babyapp.domain.SongbookData = dk.babyapp.domain.SongbookData(),
    /** Reminders a week before and on the day of examinations and vaccinations in the Danish programme. */
    val healthRemindersEnabled: Boolean = true,
)

/** An own reminder: [title] on the chosen weekdays ([days], 1 = Monday) at [hour]:[minute]. */
@kotlinx.serialization.Serializable
data class CustomReminder(val id: String = java.util.UUID.randomUUID().toString(), val title: String, val days: Set<Int>, val hour: Int, val minute: Int, val enabled: Boolean = true)

/** [key] is a leap item or a milestone; [date] is when it was ticked off, as yyyy-mm-dd. */
@kotlinx.serialization.Serializable
data class DevelopmentCheck(val childId: String, val key: String, val date: String)

@kotlinx.serialization.Serializable
data class BoardNote(val id: String = java.util.UUID.randomUUID().toString(), val childId: String, val text: String, val createdAt: Long = System.currentTimeMillis())

@kotlinx.serialization.Serializable
data class HealthQuestion(val id: String = java.util.UUID.randomUUID().toString(), val childId: String, val text: String, val createdAt: Long = System.currentTimeMillis())

/** The cards on Indsigt. Stored by name, so names must not change. */
enum class InsightCard(val label: String) { Sleep("Søvn"), Feeding("Madning"), Diapers("Bleer"), Rhythm("Døgnrytme"), Growth("Vækst"), HeadSide("Liggeside"), Health("Sundhed"), Misc("Diverse") }

@kotlinx.serialization.Serializable
data class MeasurementChartSettings(val period: String = "Month", val from: String? = null, val through: String? = null, val showGrowthReferences: Boolean = false)

enum class QuickAction(val label: String, val category: QuickActionCategory) {
    Breastfeeding("Amning", QuickActionCategory.Feeding), Bottle("Flaske", QuickActionCategory.Feeding), Pumping("Pumpning", QuickActionCategory.Feeding),
    Nap("Søvn", QuickActionCategory.Sleep), Night("Nattesøvn", QuickActionCategory.Sleep), HeadSide("Liggeside", QuickActionCategory.Sleep), Diaper("Ble", QuickActionCategory.Diaper),
    Weight("Vægt", QuickActionCategory.Measurements), Height("Højde", QuickActionCategory.Measurements), HeadCircumference("Hovedomkreds", QuickActionCategory.Measurements), Temperature("Temperatur", QuickActionCategory.Measurements),
    HealthVisit("Sundhedsbesøg", QuickActionCategory.Health), Vaccination("Vaccination", QuickActionCategory.Health), Medicine("Medicin", QuickActionCategory.Health), HealthNote("Helbredsnotat", QuickActionCategory.Health),
    SolidFood("Fast føde", QuickActionCategory.Feeding), TummyTime("Mavetid", QuickActionCategory.Misc), Bath("Bad", QuickActionCategory.Misc), Play("Leg", QuickActionCategory.Misc), OutdoorTime("Udetid", QuickActionCategory.Misc), Activity("Andet", QuickActionCategory.Misc),
}

fun orderedQuickActionCategories(saved: List<QuickActionCategory>): List<QuickActionCategory> {
    val categories = QuickAction.entries.map { it.category }.distinct()
    return (saved.filter { it in categories } + categories).distinct()
}

enum class DashboardMetric {
    Feeding,
    Diapers,
    Sleep,
    TummyTime,
    Weight,
    Height,
    HeadCircumference,
    Temperature,
    Pumping,
    SolidFood,
    Medicine,
    HealthVisits,
    Vaccinations,
    Activities,
    HeadSide;

    companion object {
        val defaults = listOf(Feeding, Diapers, Sleep, TummyTime)
    }
}

enum class DanishRegion {
    Hovedstaden,
    Midtjylland,
    Nordjylland,
    Sjaelland,
    Syddanmark,
}

enum class MeasurementUnits { Metric, Imperial }

enum class ThemePreference { System, Light, Dark }

enum class BookReminderFrequency(val label: String) { Daily("Dagligt"), ThreeWeekly("Tre gange om ugen"), Weekly("Ugentligt"), Monthly("Månedligt") }

@kotlinx.serialization.Serializable
data class HeadSideStatisticsSettings(
    val period: String = "Month",
    val from: String? = null,
    val through: String? = null,
    val correctionDates: Map<String, String> = emptyMap(),
)
