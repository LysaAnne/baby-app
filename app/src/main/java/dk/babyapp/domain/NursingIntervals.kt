package dk.babyapp.domain

import dk.babyapp.data.tracking.*

data class NursingInterval(val start: Long, val end: Long?, val side: BreastSide?, val nippleShield: Boolean = false)

fun CareEventEntity.nursingIntervals(): List<NursingInterval> {
    val intervals = segmentIntervals()
    if (intervals.isEmpty()) {
        if (leftSeconds + rightSeconds == 0L && runningSince == null) return emptyList()
        val side = if (leftSeconds > 0 && rightSeconds > 0) null else activeSide
        return listOf(NursingInterval(runningSince ?: startedAt, endedAt ?: if (runningSince == null) startedAt + (leftSeconds + rightSeconds) * 1000 else null, side, nippleShield))
    }
    return intervals.mapIndexed { index, (start, end) -> NursingInterval(start, end, BreastSide.entries.firstOrNull { it.name == timerSegmentSides.split(';').getOrNull(index) }, shieldForInterval(index)) }
}

fun CareEventEntity.editNursingIntervals(intervals: List<NursingInterval>, now: Long): CareEventEntity {
    require(type == CareEventType.Breastfeeding)
    // Preserve legacy side totals for metadata-only edits; never invent old sides.
    if (intervals == nursingIntervals()) return this
    val original = nursingIntervals()
    if (intervals.map { it.copy(nippleShield = false) } == original.map { it.copy(nippleShield = false) }) {
        return copy(timerSegmentShields = intervals.joinToString(";") { it.nippleShield.toString() }, nippleShield = intervals.lastOrNull()?.nippleShield ?: nippleShield)
    }
    val sorted = intervals.sortedBy { it.start }
    require(sorted.all { it.side != null && it.start <= now && (it.end == null || (it.end > it.start && it.end <= now)) }) { "Vælg side og gyldige tider. Slut skal være efter start og senest nu." }
    require(sorted.count { it.end == null } <= 1 && (sorted.none { it.end == null } || (isRunning && sorted.last().end == null))) { "Kun det sidste, igangværende interval må være åbent." }
    require(sorted.zipWithNext().all { (a, b) -> a.end != null && a.end <= b.start }) { "Intervaller må ikke overlappe." }
    val left = sorted.filter { it.side == BreastSide.Left && it.end != null }.sumOf { (it.end!! - it.start) / 1000 }
    val right = sorted.filter { it.side == BreastSide.Right && it.end != null }.sumOf { (it.end!! - it.start) / 1000 }
    return copy(
        startedAt = sorted.firstOrNull()?.start ?: startedAt,
        endedAt = if (endedAt != null) sorted.lastOrNull()?.end ?: endedAt else null,
        runningSince = sorted.lastOrNull()?.takeIf { it.end == null }?.start,
        activeSide = sorted.lastOrNull()?.side ?: activeSide,
        leftSeconds = left, rightSeconds = right,
        nippleShield = sorted.lastOrNull()?.nippleShield ?: nippleShield,
        timerSegmentShields = sorted.joinToString(";") { it.nippleShield.toString() },
        timerSegments = sorted.joinToString(";") { "${it.start}-${it.end ?: ""}" },
        timerSegmentSides = sorted.joinToString(";") { it.side!!.name },
    )
}

/** Intervals for any timer: nursing keeps its sides, sleep and the other timers have none. */
fun CareEventEntity.timerIntervals(): List<NursingInterval> {
    if (type == CareEventType.Breastfeeding) return nursingIntervals()
    val recorded = segmentIntervals().map { (start, end) -> NursingInterval(start, end, null) }
    if (recorded.isNotEmpty()) return recorded
    return when {
        runningSince != null -> listOf(NursingInterval(runningSince, null, null))
        endedAt != null && endedAt > startedAt -> listOf(NursingInterval(startedAt, endedAt, null))
        else -> emptyList()
    }
}

/** Saves edited intervals; pauses between them don't count. Nursing also keeps its sides. */
fun CareEventEntity.editTimerIntervals(intervals: List<NursingInterval>, now: Long): CareEventEntity {
    if (type == CareEventType.Breastfeeding) return editNursingIntervals(intervals, now)
    if (intervals == timerIntervals()) return this
    val sorted = intervals.sortedBy { it.start }
    require(sorted.isNotEmpty()) { "Behold mindst ét interval." }
    require(sorted.all { it.start <= now && (it.end == null || (it.end > it.start && it.end <= now)) }) { "Slut skal være efter start og senest nu." }
    require(sorted.count { it.end == null } <= 1 && (sorted.none { it.end == null } || (endedAt == null && sorted.last().end == null))) { "Kun det sidste, igangværende interval må være åbent." }
    require(sorted.zipWithNext().all { (a, b) -> a.end != null && a.end <= b.start }) { "Intervaller må ikke overlappe." }
    return copy(
        startedAt = sorted.first().start,
        endedAt = if (endedAt != null) sorted.last().end ?: endedAt else null,
        runningSince = sorted.last().takeIf { it.end == null }?.start,
        leftSeconds = sorted.filter { it.end != null }.sumOf { (it.end!! - it.start) / 1000 },
        rightSeconds = 0,
        timerSegments = sorted.joinToString(";") { "${it.start}-${it.end ?: ""}" },
    )
}

/** Moving a start earlier than the previous interval's end moves that end along, so they don't overlap. */
fun List<NursingInterval>.withStart(index: Int, start: Long): List<NursingInterval> = toMutableList().apply {
    this[index] = this[index].copy(start = start)
    val previous = getOrNull(index - 1)
    if (previous?.end != null && start < previous.end && start > previous.start) this[index - 1] = previous.copy(end = start)
}

/** Moving an end later than the next interval's start moves that start along. */
fun List<NursingInterval>.withEnd(index: Int, end: Long): List<NursingInterval> = toMutableList().apply {
    this[index] = this[index].copy(end = end)
    val next = getOrNull(index + 1)
    if (next != null && end > next.start && (next.end == null || end < next.end)) this[index + 1] = next.copy(start = end)
}
