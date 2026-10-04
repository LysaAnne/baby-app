package dk.babyapp.domain

import dk.babyapp.data.tracking.segmentIntervals
import dk.babyapp.data.tracking.shieldForInterval
import dk.babyapp.data.tracking.startSegment
import dk.babyapp.data.tracking.closeSegment
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


fun CareEventEntity.switchNursingSide(now: Long): CareEventEntity {
    val changed = accrueUntil(now).closeSegment(now).copy(
        activeSide = if (activeSide == BreastSide.Left) BreastSide.Right else BreastSide.Left,
        runningSince = if (runningSince != null) now else null,
    )
    return if (runningSince != null) changed.startSegment(now) else changed
}

fun CareEventEntity.resumeNursing(side: BreastSide, now: Long): CareEventEntity {
    require(type == CareEventType.Breastfeeding && endedAt != null && deletedAt == null && now >= endedAt)
    val history = if (timerSegments.isBlank()) copy(timerSegments = "$startedAt-$endedAt", timerSegmentSides = "") else this
    return history.copy(endedAt = null, runningSince = now, activeSide = side, isDraft = false, nursingContinued = true).startSegment(now)
}


fun CareEventEntity.changeNippleShield(enabled: Boolean, now: Long): CareEventEntity {
    require(type == CareEventType.Breastfeeding && endedAt == null)
    if (enabled == nippleShield) return this
    val preserved = copy(timerSegmentShields = segmentIntervals().indices.joinToString(";") { shieldForInterval(it).toString() })
    if (runningSince == null) return preserved.copy(nippleShield = enabled)
    require(now >= runningSince)
    val flags = segmentIntervals().indices.map { shieldForInterval(it) }.toMutableList()
    if (flags.isNotEmpty()) flags[flags.lastIndex] = enabled
    return copy(nippleShield = enabled, timerSegmentShields = flags.joinToString(";"))
}
