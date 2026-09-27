package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.SleepType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailyInsight(
    val date: LocalDate,
    val feedings: Float = 0f,
    val bottleMl: Float = 0f,
    val breastfeedingMinutes: Float = 0f,
    val napMinutes: Float = 0f,
    val nightMinutes: Float = 0f,
    val wetDiapers: Float = 0f,
    val dirtyDiapers: Float = 0f,
    val tummyMinutes: Float = 0f,
)

fun dailyInsightSeries(events: List<CareEventEntity>, childId: String, from: LocalDate, through: LocalDate, zone: ZoneId = ZoneId.systemDefault()): List<DailyInsight> {
    val byDay = events.filter { it.childId == childId && it.deletedAt == null && !it.isDraft }.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() }
    return generateSequence(from) { it.plusDays(1) }.takeWhile { it <= through }.map { date ->
        val day = byDay[date].orEmpty()
        DailyInsight(
            date = date,
            feedings = day.count { it.type == CareEventType.Breastfeeding || it.type == CareEventType.Bottle }.toFloat(),
            bottleMl = day.filter { it.type == CareEventType.Bottle }.sumOf { it.amountConsumedMl ?: 0 }.toFloat(),
            breastfeedingMinutes = day.filter { it.type == CareEventType.Breastfeeding }.sumOf { it.elapsedSeconds() }.toFloat() / 60f,
            napMinutes = day.filter { it.type == CareEventType.Sleep && it.sleepType == SleepType.Nap }.sumOf { it.elapsedSeconds() }.toFloat() / 60f,
            nightMinutes = day.filter { it.type == CareEventType.Sleep && it.sleepType == SleepType.Night }.sumOf { it.elapsedSeconds() }.toFloat() / 60f,
            wetDiapers = day.count { it.type == CareEventType.Diaper && it.diaperType in setOf(DiaperType.Wet, DiaperType.Both) }.toFloat(),
            dirtyDiapers = day.count { it.type == CareEventType.Diaper && it.diaperType in setOf(DiaperType.Dirty, DiaperType.Both) }.toFloat(),
            tummyMinutes = day.filter { it.type == CareEventType.Activity && it.activityType?.name == "TummyTime" }.sumOf { it.activityDurationSeconds ?: 0 }.toFloat() / 60f,
        )
    }.toList()
}

fun percentChange(current: Double, previous: Double): String = when {
    previous == 0.0 && current == 0.0 -> "Ingen ændring"
    previous == 0.0 -> "Ny aktivitet i perioden"
    else -> "%+.0f %% mod forrige periode".format((current - previous) / previous * 100)
}
