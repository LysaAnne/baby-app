package dk.babyapp.data.preferences

import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.data.tracking.MeasurementType

/** Categories on the Today screen. The stored value is [name]; [label] can change freely. */
enum class QuickActionCategory(val label: String) {
    Feeding("Madning"),
    Sleep("Søvn"),
    Diaper("Ble"),
    Measurements("Mål"),
    Health("Sundhed"),
    SolidFood("Fast føde"),
    Misc("Diverse"),
    ;

    companion object {
        // Until October 2026 the Danish label itself was stored. Frozen so old values keep working after labels change.
        private val legacyLabels = mapOf(
            "Madning" to Feeding, "Søvn" to Sleep, "Ble" to Diaper, "Mål" to Measurements,
            "Sundhed" to Health, "Fast føde" to SolidFood, "Diverse" to Misc,
        )

        fun fromStored(value: String): QuickActionCategory? = entries.firstOrNull { it.name == value } ?: legacyLabels[value]
    }
}

/** Stable ids for the Journal filters. The quick filters are stored as these ids. */
object JournalFilterKeys {
    const val FEEDING_ALL = "feeding-all"
    const val HEALTH_ALL = "health-all"
    const val NAP = "nap"
    const val NIGHT_SLEEP = "night-sleep"
    const val MEDICINE = "medicine"
    const val TUMMY_TIME = "tummy-time"
    fun type(type: CareEventType) = "type:${type.name}"
    fun measurement(type: MeasurementType) = "measurement:${type.name}"
    fun activity(type: ActivityType) = "activity:${type.name}"
    fun diaper(type: DiaperType) = "diaper:${type.name}"
    fun sleepPlace(place: String) = "sleep-place:$place"
    fun visit(type: HealthVisitType) = "visit:${type.name}"
    const val WITH_NOTES = "with-notes"

    val defaults = listOf(FEEDING_ALL, type(CareEventType.Diaper), type(CareEventType.Sleep), HEALTH_ALL, MEDICINE)

    // Until October 2026 the Danish filter label itself was stored. Frozen so old values keep working after labels change.
    private val legacyLabels = mapOf(
        "Madning · alle" to FEEDING_ALL, "Sundhed · alle" to HEALTH_ALL, "Lur" to NAP, "Nattesøvn" to NIGHT_SLEEP,
        // Nap and night sleep became one Søvn in October 2026; their saved filters now show all sleep.
        NAP to type(CareEventType.Sleep), NIGHT_SLEEP to type(CareEventType.Sleep),
        "Medicin" to MEDICINE, "Mavetid" to TUMMY_TIME,
        "Favoritside" to type(CareEventType.HeadSide), "Fast føde" to type(CareEventType.SolidFood), "Amning" to type(CareEventType.Breastfeeding),
        "Flaske" to type(CareEventType.Bottle), "Pumpning" to type(CareEventType.Pumping), "Ble" to type(CareEventType.Diaper),
        "Søvn" to type(CareEventType.Sleep), "Mål" to type(CareEventType.Measurement), "Diverse" to type(CareEventType.Activity),
        "Sundhedsbesøg" to type(CareEventType.HealthVisit), "Vaccination" to type(CareEventType.Vaccination),
        "Vægt" to measurement(MeasurementType.Weight), "Højde/længde" to measurement(MeasurementType.Height),
        "Hovedomkreds" to measurement(MeasurementType.HeadCircumference), "Temperatur" to measurement(MeasurementType.Temperature),
    )

    fun normalize(stored: String): String = legacyLabels[stored]?.let { legacyLabels[it] ?: it } ?: stored
}
