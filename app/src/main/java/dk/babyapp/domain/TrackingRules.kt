package dk.babyapp.domain

import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType

fun CareEventEntity.accrueUntil(now: Long): CareEventEntity {
    val seconds = runningSince?.let { ((now - it).coerceAtLeast(0) / 1_000) } ?: 0
    return when {
        type != CareEventType.Breastfeeding -> copy(leftSeconds = leftSeconds + seconds)
        activeSide == BreastSide.Right -> copy(rightSeconds = rightSeconds + seconds)
        else -> copy(leftSeconds = leftSeconds + seconds)
    }
}

fun overlapsSleep(
    candidate: CareEventEntity,
    events: Iterable<CareEventEntity>,
    now: Long = System.currentTimeMillis(),
): Boolean {
    if (candidate.type != CareEventType.Sleep) return false
    val candidateEnd = candidate.endedAt ?: now
    return events.any { other ->
        other.id != candidate.id && other.childId == candidate.childId && other.type == CareEventType.Sleep && other.deletedAt == null &&
            candidate.startedAt < (other.endedAt ?: now) && candidateEnd > other.startedAt
    }
}
