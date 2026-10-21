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
    val insightPeriodDays: Int = 7,
    /** When an encrypted backup was last saved to a file, or null if never. */
    val lastBackupAt: Long? = null,
    /** Questions for the next health visit, per child; attached to a visit when it is registered. */
    val healthQuestions: List<HealthQuestion> = emptyList(),
    /** Reminders a week before and on the day of examinations and vaccinations in the Danish programme. */
    val healthRemindersEnabled: Boolean = true,
)

@kotlinx.serialization.Serializable
data class HealthQuestion(val id: String = java.util.UUID.randomUUID().toString(), val childId: String, val text: String, val createdAt: Long = System.currentTimeMillis())

/** The cards on Indsigt. Stored by name, so names must not change. */
enum class InsightCard(val label: String) { Sleep("Søvn"), Feeding("Madning"), Diapers("Bleer"), Rhythm("Døgnrytme"), Growth("Vækst"), HeadSide("Favoritside") }

@kotlinx.serialization.Serializable
data class MeasurementChartSettings(val period: String = "Month", val from: String? = null, val through: String? = null, val showGrowthReferences: Boolean = false)

enum class QuickAction(val label: String, val category: QuickActionCategory) {
    Breastfeeding("Amning", QuickActionCategory.Feeding), Bottle("Flaske", QuickActionCategory.Feeding), Pumping("Pumpning", QuickActionCategory.Feeding),
    HeadSide("Favoritside", QuickActionCategory.Sleep), Diaper("Ble", QuickActionCategory.Diaper), Nap("Lur", QuickActionCategory.Sleep), Night("Nattesøvn", QuickActionCategory.Sleep),
    Weight("Vægt", QuickActionCategory.Measurements), Height("Højde", QuickActionCategory.Measurements), HeadCircumference("Hovedomkreds", QuickActionCategory.Measurements), Temperature("Temperatur", QuickActionCategory.Measurements),
    HealthVisit("Sundhedsbesøg", QuickActionCategory.Health), Vaccination("Vaccination", QuickActionCategory.Health), Medicine("Medicin", QuickActionCategory.Health), HealthNote("Helbredsnotat", QuickActionCategory.Health),
    SolidFood("Måltid / smagsprøve", QuickActionCategory.SolidFood), TummyTime("Mavetid", QuickActionCategory.Misc), Bath("Bad", QuickActionCategory.Misc), Play("Leg", QuickActionCategory.Misc), OutdoorTime("Udetid", QuickActionCategory.Misc), Activity("Andet", QuickActionCategory.Misc),
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
