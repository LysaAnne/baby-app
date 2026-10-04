package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import java.time.*

/** Slice timer intervals at local midnight; a pause between intervals contributes nothing. */
fun eventsForDay(events: List<CareEventEntity>, date: LocalDate, zone: ZoneId = ZoneId.systemDefault(), now: Long = System.currentTimeMillis()): List<CareEventEntity> {
    val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return events.filter { !it.isDraft && it.deletedAt == null }.mapNotNull { event ->
        val intervals = event.segmentIntervals()
        if (intervals.isEmpty()) event.takeIf { it.startedAt in start until end }
        else {
            val clipped = intervals.mapNotNull { (from, to) ->
                val a = maxOf(start, from); val b = minOf(end, to ?: now)
                if (b > a) a to b else null
            }
            if (clipped.isEmpty()) null else {
                val seconds = clipped.sumOf { (a, b) -> (b - a) / 1000 }
                event.copy(startedAt = clipped.first().first, endedAt = clipped.last().second, runningSince = null,
                    leftSeconds = seconds, rightSeconds = 0, activityDurationSeconds = if (event.type == CareEventType.Activity) seconds else event.activityDurationSeconds)
            }
        }
    }
}

/** Uses the last actual interval, not a side selected while the timer was paused. */
fun lastNursingInterval(events: List<CareEventEntity>): Pair<Long, BreastSide?>? = events
    .filter { it.type == CareEventType.Breastfeeding }
    .flatMap { event ->
        val intervals = event.segmentIntervals()
        if (intervals.isEmpty()) listOf(event.startedAt to event.activeSide)
        else intervals.mapIndexedNotNull { index, (start, end) ->
            if (start >= (event.endedAt ?: Long.MAX_VALUE) || (end ?: Long.MAX_VALUE) <= event.startedAt) null
            else start to BreastSide.entries.firstOrNull { it.name == event.timerSegmentSides.split(';').getOrNull(index) }
        }
    }.maxByOrNull { it.first }

fun lastNursingSide(events: List<CareEventEntity>): BreastSide? = lastNursingInterval(events)?.second
