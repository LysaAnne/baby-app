package dk.babyapp.ui.tracking

import dk.babyapp.data.preferences.JournalFilterKeys
import dk.babyapp.data.tracking.*

/** [id] is what gets stored (see [JournalFilterKeys]); [label] is only shown. */
internal data class JournalFilter(val id: String, val label: String, val matches: (CareEventEntity) -> Boolean)

internal val journalFilters = listOf(
    JournalFilter(JournalFilterKeys.FEEDING_ALL, "Madning · alle") { it.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle, CareEventType.Pumping, CareEventType.SolidFood) },
    JournalFilter(JournalFilterKeys.HEALTH_ALL, "Sundhed · alle") { it.type in setOf(CareEventType.HealthVisit, CareEventType.Vaccination) || it.activityType == ActivityType.Medicine || it.measurementType == MeasurementType.Temperature },
    JournalFilter(JournalFilterKeys.NAP, "Lur") { it.type == CareEventType.Sleep && it.sleepType == SleepType.Nap },
    JournalFilter(JournalFilterKeys.NIGHT_SLEEP, "Nattesøvn") { it.type == CareEventType.Sleep && it.sleepType == SleepType.Night },
    JournalFilter(JournalFilterKeys.MEDICINE, "Medicin") { it.type == CareEventType.Activity && it.activityType == ActivityType.Medicine },
    JournalFilter(JournalFilterKeys.TUMMY_TIME, "Mavetid") { it.type == CareEventType.Activity && it.activityType == ActivityType.TummyTime },
) + CareEventType.entries.map { type -> JournalFilter(JournalFilterKeys.type(type), type.displayLabel()) { it.type == type || (type == CareEventType.Sleep && it.type == CareEventType.HeadSide) } } +
    MeasurementType.entries.map { type -> JournalFilter(JournalFilterKeys.measurement(type), type.displayLabel()) { it.type == CareEventType.Measurement && it.measurementType == type } }

/** Short label for chips and menus; an unknown id is shown as stored rather than hidden. */
internal fun journalFilterLabel(id: String) = when (id) {
    JournalFilterKeys.FEEDING_ALL -> "Madning"
    JournalFilterKeys.HEALTH_ALL -> "Sundhed"
    JournalFilterKeys.type(CareEventType.Diaper) -> "Bleer"
    else -> journalFilters.firstOrNull { it.id == id }?.label ?: id
}
