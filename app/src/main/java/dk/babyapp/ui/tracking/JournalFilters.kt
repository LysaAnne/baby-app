package dk.babyapp.ui.tracking

import dk.babyapp.data.tracking.*

internal data class JournalFilter(val label: String, val matches: (CareEventEntity) -> Boolean)

internal val journalFilters = listOf(
    JournalFilter("Madning · alle") { it.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle, CareEventType.Pumping, CareEventType.SolidFood) },
    JournalFilter("Sundhed · alle") { it.type in setOf(CareEventType.HealthVisit, CareEventType.Vaccination) || it.activityType == ActivityType.Medicine || it.measurementType == MeasurementType.Temperature },
    JournalFilter("Lur") { it.type == CareEventType.Sleep && it.sleepType == SleepType.Nap },
    JournalFilter("Nattesøvn") { it.type == CareEventType.Sleep && it.sleepType == SleepType.Night },
    JournalFilter("Medicin") { it.type == CareEventType.Activity && it.activityType == ActivityType.Medicine },
    JournalFilter("Mavetid") { it.type == CareEventType.Activity && it.activityType == ActivityType.TummyTime },
) + CareEventType.entries.map { type -> JournalFilter(type.displayLabel()) { it.type == type } } +
    MeasurementType.entries.map { type -> JournalFilter(type.displayLabel()) { it.type == CareEventType.Measurement && it.measurementType == type } }
