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
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0,
    val insightDashboardMetrics: List<String> = listOf("Sleep", "Feedings", "Diapers", "TummyTime"),
    val quickActionCategoryOrder: List<String> = emptyList(),
    val hiddenQuickActions: Set<String> = emptySet(),
    val medicines: List<dk.babyapp.data.medicine.MedicinePlan> = emptyList(),
)

enum class QuickAction(val label: String, val category: String) {
    Breastfeeding("Amning", "Madning"), Bottle("Flaske", "Madning"), Pumping("Pumpning", "Madning"),
    Diaper("Ble", "Ble"), Nap("Lur", "Søvn"), Night("Nattesøvn", "Søvn"),
    Weight("Vægt", "Mål"), Height("Højde", "Mål"), HeadCircumference("Hovedomkreds", "Mål"), Temperature("Temperatur", "Mål"),
    HealthVisit("Sundhedsbesøg", "Sundhed"), Vaccination("Vaccination", "Sundhed"), Medicine("Medicin", "Sundhed"), HealthNote("Helbredsnotat", "Sundhed"),
    SolidFood("Måltid / smagsprøve", "Fast føde"), TummyTime("Mavetid", "Diverse"), Activity("Anden aktivitet", "Diverse"),
}

fun orderedQuickActionCategories(saved: List<String>): List<String> {
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
    Activities;

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
