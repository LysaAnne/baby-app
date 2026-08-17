package dk.babyapp.data.preferences

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val activeChildId: String? = null,
    val languageTag: String = "da",
    val region: DanishRegion = DanishRegion.Hovedstaden,
    val units: MeasurementUnits = MeasurementUnits.Metric,
    val theme: ThemePreference = ThemePreference.System,
)

enum class DanishRegion {
    Hovedstaden,
    Midtjylland,
    Nordjylland,
    Sjaelland,
    Syddanmark,
}

enum class MeasurementUnits { Metric, Imperial }

enum class ThemePreference { System, Light, Dark }
