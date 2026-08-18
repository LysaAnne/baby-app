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
)

enum class DashboardMetric {
    Feeding,
    Diapers,
    Sleep,
    TummyTime,
    Weight,
    Height,
    HeadCircumference,
    Temperature;

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
