package dk.babyapp.domain

import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType

/** "lige nu", "12 min", "3 t 5 min" or "2 d 4 t": how long ago, without seconds. */
fun timeSinceLabel(at: Long, now: Long): String {
    val minutes = ((now - at).coerceAtLeast(0) / 60_000)
    return when {
        minutes < 1 -> "lige nu"
        minutes < 60 -> "$minutes min"
        minutes < 1_440 -> "${minutes / 60} t" + (minutes % 60).takeIf { it > 0 }?.let { " $it min" }.orEmpty()
        else -> "${minutes / 1_440} d" + ((minutes % 1_440) / 60).takeIf { it > 0 }?.let { " $it t" }.orEmpty()
    }
}

/**
 * The latest registered dose of a medicine on the child's card. Older registrations without a link to the
 * card are matched by name. Only for information: the app never works out when or how much to give.
 */
fun lastDose(plan: MedicinePlan, events: List<CareEventEntity>): CareEventEntity? = events.filter { event ->
    event.childId == plan.childId && event.deletedAt == null && !event.isDraft &&
        event.type == CareEventType.Activity && event.activityType == ActivityType.Medicine &&
        (event.medicineId == plan.id || (event.medicineId.isBlank() && event.medicationName.trim().equals(plan.name.trim(), ignoreCase = true)))
}.maxByOrNull { it.startedAt }
