package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.HeadSide
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.segmentIntervals
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** One day of the period. Sleep that crosses midnight is split between the two days. */
data class DayInsight(
    val date: LocalDate,
    val napMinutes: Long,
    val nightMinutes: Long,
    val feedings: Int,
    val nursingMinutes: Long,
    val bottleMl: Int,
    val diapers: Int,
    val wetDiapers: Int,
    val dirtyDiapers: Int,
    /** Sleep as fractions of the day, 0 = midnight, 1 = next midnight. */
    val sleepBlocks: List<ClosedFloatingPointRange<Float>>,
    /** Start of each feeding as a fraction of the day. */
    val feedingMarks: List<Float>,
) {
    val sleepMinutes get() = napMinutes + nightMinutes
}

data class Measurement(val value: Double, val unit: String, val at: Long)

data class PeriodInsights(
    /** Every day of the period, oldest first, ending today. */
    val days: List<DayInsight>,
    /** Days the averages are taken over: finished days since the first registration. */
    val averagedDays: Int,
    val avgSleepMinutes: Long,
    val avgNapMinutes: Long,
    val avgNightMinutes: Long,
    val napsPerDay: Double,
    val longestSleepMinutes: Long,
    val avgAwakenings: Double?,
    val bedtime: ClosedRange<LocalTime>?,
    val avgFeedings: Double,
    val avgNursingMinutes: Long,
    val avgBottleMl: Long,
    val avgDiapers: Double,
    val avgWet: Double,
    val avgDirty: Double,
    val latest: Map<MeasurementType, Measurement>,
    /** Weight now minus the last weight before the period, in the weight's unit. */
    val weightChange: Double?,
    val headLeft: Int,
    val headRight: Int,
)

fun periodInsights(
    events: List<CareEventEntity>,
    childId: String,
    days: Int,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault(),
    now: Long = System.currentTimeMillis(),
): PeriodInsights {
    val own = events.filter { it.childId == childId && it.deletedAt == null && !it.isDraft }
    val first = today.minusDays(days - 1L)
    fun dayOf(at: Long) = Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
    fun start(date: LocalDate) = date.atStartOfDay(zone).toInstant().toEpochMilli()
    val perDay = generateSequence(first) { it.plusDays(1) }.takeWhile { it <= today }.map { date ->
        val from = start(date)
        val to = start(date.plusDays(1))
        val length = (to - from).toFloat()
        val started = own.filter { dayOf(it.startedAt) == date }
        // Sleep and nursing are measured by the time that falls inside this day.
        fun clipped(event: CareEventEntity): List<Pair<Long, Long>> =
            event.segmentIntervals().ifEmpty { listOf(event.startedAt to event.endedAt) }.mapNotNull { (a, b) ->
                val s = maxOf(a, from)
                val e = minOf(b ?: now, to, now)
                if (e > s) s to e else null
            }
        val sleeps = own.filter { it.type == CareEventType.Sleep }
        fun sleepMinutes(night: Boolean) = sleeps.filter { (it.sleepType == SleepType.Night) == night }.sumOf { event -> clipped(event).sumOf { (s, e) -> e - s } } / 60_000
        val diapers = started.filter { it.type == CareEventType.Diaper }
        DayInsight(
            date = date,
            napMinutes = sleepMinutes(night = false),
            nightMinutes = sleepMinutes(night = true),
            feedings = started.count { it.type == CareEventType.Breastfeeding || it.type == CareEventType.Bottle },
            nursingMinutes = own.filter { it.type == CareEventType.Breastfeeding }.sumOf { event -> clipped(event).sumOf { (s, e) -> e - s } } / 60_000,
            bottleMl = started.filter { it.type == CareEventType.Bottle }.sumOf { it.amountConsumedMl ?: 0 },
            diapers = diapers.size,
            wetDiapers = diapers.count { it.diaperType == DiaperType.Wet || it.diaperType == DiaperType.Both },
            dirtyDiapers = diapers.count { it.diaperType == DiaperType.Dirty || it.diaperType == DiaperType.Both },
            sleepBlocks = sleeps.flatMap { clipped(it) }.map { (s, e) -> ((s - from) / length)..((e - from) / length) }.sortedBy { it.start },
            feedingMarks = started.filter { it.type == CareEventType.Breastfeeding || it.type == CareEventType.Bottle }.map { (it.startedAt - from) / length }.sorted(),
        )
    }.toList()

    // Averages use finished days only, and not days before the child's first registration.
    val firstRegistration = own.minOfOrNull { it.startedAt }?.let(::dayOf)
    val averaged = perDay.filter { it.date < today && (firstRegistration == null || it.date >= firstRegistration) }
        .ifEmpty { perDay.filter { it.date == today } }
    fun avg(value: (DayInsight) -> Number) = if (averaged.isEmpty()) 0.0 else averaged.sumOf { value(it).toDouble() } / averaged.size

    val periodStart = start(first)
    val inPeriod = own.filter { it.startedAt >= periodStart }
    val periodSleeps = inPeriod.filter { it.type == CareEventType.Sleep }
    val nights = periodSleeps.filter { it.sleepType == SleepType.Night }
    val bedtimes = nights.map { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalTime() }.filter { it.hour >= 15 }.sorted()
    val measurements = own.filter { it.type == CareEventType.Measurement && it.measurementType != null && it.measurementValue != null }
    val latest = measurements.groupBy { it.measurementType!! }.mapValues { (_, list) -> list.maxBy { it.startedAt }.let { Measurement(it.measurementValue!!, it.measurementUnit, it.startedAt) } }
    val weightBefore = measurements.filter { it.measurementType == MeasurementType.Weight && it.startedAt < periodStart }.maxByOrNull { it.startedAt }
    val weightNow = latest[MeasurementType.Weight]
    val heads = inPeriod.filter { it.type == CareEventType.HeadSide }

    return PeriodInsights(
        days = perDay,
        averagedDays = averaged.size,
        avgSleepMinutes = avg { it.sleepMinutes }.toLong(),
        avgNapMinutes = avg { it.napMinutes }.toLong(),
        avgNightMinutes = avg { it.nightMinutes }.toLong(),
        napsPerDay = if (averaged.isEmpty()) 0.0 else periodSleeps.count { it.sleepType != SleepType.Night && dayOf(it.startedAt) in averaged.map(DayInsight::date) }.toDouble() / averaged.size,
        longestSleepMinutes = (periodSleeps.maxOfOrNull { it.elapsedSeconds(now) } ?: 0) / 60,
        avgAwakenings = nights.mapNotNull { it.awakenings }.takeIf { it.isNotEmpty() }?.average(),
        // The middle half of the evening bedtimes, so one late night doesn't move it.
        bedtime = bedtimes.takeIf { it.size >= 3 }?.let { it[it.size / 4]..it[(it.size * 3 / 4).coerceAtMost(it.size - 1)] },
        avgFeedings = avg { it.feedings },
        avgNursingMinutes = avg { it.nursingMinutes }.toLong(),
        avgBottleMl = avg { it.bottleMl }.toLong(),
        avgDiapers = avg { it.diapers },
        avgWet = avg { it.wetDiapers },
        avgDirty = avg { it.dirtyDiapers },
        latest = latest,
        weightChange = if (weightNow != null && weightBefore != null && weightNow.at >= periodStart && weightBefore.measurementUnit == weightNow.unit) weightNow.value - weightBefore.measurementValue!! else null,
        headLeft = heads.count { it.headSide == HeadSide.Left },
        headRight = heads.count { it.headSide == HeadSide.Right },
    )
}
