package dk.babyapp.ui.tracking

import dk.babyapp.data.preferences.JournalFilterKeys
import dk.babyapp.data.tracking.*

/** [id] is what gets stored (see [JournalFilterKeys]); [label] is only shown; [group] is the heading under Flere filtre. */
internal data class JournalFilter(val id: String, val label: String, val group: String = "", val matches: (CareEventEntity) -> Boolean)

private fun type(type: CareEventType, group: String) =
    JournalFilter(JournalFilterKeys.type(type), type.displayLabel(), group) { it.type == type || (type == CareEventType.Sleep && it.type == CareEventType.HeadSide) }
private fun activity(type: ActivityType) =
    JournalFilter(JournalFilterKeys.activity(type), type.displayLabel(), "Diverse") { it.type == CareEventType.Activity && it.activityType == type }
private fun measurement(type: MeasurementType, group: String) =
    JournalFilter(JournalFilterKeys.measurement(type), type.displayLabel(), group) { it.type == CareEventType.Measurement && it.measurementType == type }
private fun visit(type: HealthVisitType, label: String) =
    JournalFilter(JournalFilterKeys.visit(type), label, "Sundhed") { it.type == CareEventType.HealthVisit && it.healthVisitType == type }
private fun sleepPlace(place: String) =
    JournalFilter(JournalFilterKeys.sleepPlace(place), "Søvn i ${place.lowercase()}", "Søvn") { it.type == CareEventType.Sleep && it.sleepLocation.equals(place, ignoreCase = true) }

/** Every filter, in the order and groups they are shown under Flere filtre. Ids never change once stored. */
internal val journalFilters = listOf(
    JournalFilter(JournalFilterKeys.FEEDING_ALL, "Madning · alle", "Madning") { it.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle, CareEventType.Pumping, CareEventType.SolidFood) },
    type(CareEventType.Breastfeeding, "Madning"), type(CareEventType.Bottle, "Madning"), type(CareEventType.Pumping, "Madning"), type(CareEventType.SolidFood, "Madning"),
    type(CareEventType.Sleep, "Søvn"), type(CareEventType.HeadSide, "Søvn"),
) + listOf("Seng", "Barnevogn", "Lift", "Kontaktlur", "Bil", "Bæresele").map(::sleepPlace) + listOf(
    type(CareEventType.Diaper, "Bleer"),
    JournalFilter(JournalFilterKeys.diaper(DiaperType.Wet), "Våd ble", "Bleer") { it.type == CareEventType.Diaper && (it.diaperType == DiaperType.Wet || it.diaperType == DiaperType.Both) },
    JournalFilter(JournalFilterKeys.diaper(DiaperType.Dirty), "Beskidt ble", "Bleer") { it.type == CareEventType.Diaper && (it.diaperType == DiaperType.Dirty || it.diaperType == DiaperType.Both) },
    type(CareEventType.Activity, "Diverse"),
    activity(ActivityType.Bath),
    JournalFilter(JournalFilterKeys.TUMMY_TIME, "Mavetid", "Diverse") { it.type == CareEventType.Activity && it.activityType == ActivityType.TummyTime },
    activity(ActivityType.OutdoorTime), activity(ActivityType.Play), activity(ActivityType.Other),
    type(CareEventType.Measurement, "Vækst og mål"),
    measurement(MeasurementType.Weight, "Vækst og mål"), measurement(MeasurementType.Height, "Vækst og mål"), measurement(MeasurementType.HeadCircumference, "Vækst og mål"),
    JournalFilter(JournalFilterKeys.HEALTH_ALL, "Sundhed · alle", "Sundhed") { it.type in setOf(CareEventType.HealthVisit, CareEventType.Vaccination) || it.activityType == ActivityType.Medicine || it.measurementType == MeasurementType.Temperature },
    type(CareEventType.HealthVisit, "Sundhed"),
    visit(HealthVisitType.HealthVisitor, "Sundhedsplejerske"), visit(HealthVisitType.PreventiveExam, "Børneundersøgelse"), visit(HealthVisitType.GpVisit, "Egen læge"),
    type(CareEventType.Vaccination, "Sundhed"),
    JournalFilter(JournalFilterKeys.MEDICINE, "Medicin", "Sundhed") { it.type == CareEventType.Activity && it.activityType == ActivityType.Medicine },
    measurement(MeasurementType.Temperature, "Sundhed"),
    JournalFilter(JournalFilterKeys.WITH_NOTES, "Med noter", "Andet") { it.notes.isNotBlank() },
)

/** Short label for chips and menus; an unknown id is shown as stored rather than hidden. */
internal fun journalFilterLabel(id: String) = when (id) {
    JournalFilterKeys.FEEDING_ALL -> "Madning"
    JournalFilterKeys.HEALTH_ALL -> "Sundhed"
    JournalFilterKeys.type(CareEventType.Diaper) -> "Bleer"
    else -> journalFilters.firstOrNull { it.id == id }?.label ?: id
}
