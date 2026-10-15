package dk.babyapp.domain

import dk.babyapp.data.tracking.*
import java.time.*

enum class StatisticsPeriod(val label: String) { Day("1D"), Week("1U"), Month("1M"), ThreeMonths("3M"), SixMonths("6M"), Year("1Å"), Custom("Valgfri") }

data class StatisticsRange(val from: LocalDate, val through: LocalDate)

fun statisticsRange(period: StatisticsPeriod, today: LocalDate, customFrom: LocalDate, customThrough: LocalDate): StatisticsRange = when (period) {
    StatisticsPeriod.Day -> StatisticsRange(today, today)
    StatisticsPeriod.Week -> StatisticsRange(today.minusDays(6), today)
    StatisticsPeriod.Month -> StatisticsRange(today.minusMonths(1).plusDays(1), today)
    StatisticsPeriod.ThreeMonths -> StatisticsRange(today.minusMonths(3).plusDays(1), today)
    StatisticsPeriod.SixMonths -> StatisticsRange(today.minusMonths(6).plusDays(1), today)
    StatisticsPeriod.Year -> StatisticsRange(today.minusYears(1).plusDays(1), today)
    StatisticsPeriod.Custom -> StatisticsRange(customFrom, customThrough)
}

data class HeadSideTotals(val left: Int, val right: Int, val unknown: Int = 0) {
    val total get() = left + right
    val leftPercent: Double? get() = if (total == 0) null else left * 100.0 / total
    val rightPercent: Double? get() = if (total == 0) null else right * 100.0 / total
}

data class HeadSideDay(val date: LocalDate, val counts: HeadSideTotals, val cumulative: HeadSideTotals)
data class HeadSideStatistics(val total: HeadSideTotals, val before: HeadSideTotals?, val after: HeadSideTotals?, val days: List<HeadSideDay>)

fun headSideTotals(events: List<CareEventEntity>) = events.filter { it.type == CareEventType.HeadSide && !it.isDraft && it.deletedAt == null && it.endedAt != null }.let { records ->
    HeadSideTotals(records.count { it.headSide == HeadSide.Left }, records.count { it.headSide == HeadSide.Right }, records.count { it.headSide == null })
}

fun headSideStatistics(events: List<CareEventEntity>, childId: String, range: StatisticsRange, correction: LocalDate? = null, zone: ZoneId = ZoneId.systemDefault()): HeadSideStatistics {
    require(range.from <= range.through)
    val start = range.from.atStartOfDay(zone).toInstant().toEpochMilli()
    val end = range.through.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val records = events.filter { it.childId == childId && it.type == CareEventType.HeadSide && !it.isDraft && it.deletedAt == null && it.endedAt != null && it.startedAt in start until end }
    val split = correction?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
    var left = 0; var right = 0; var unknown = 0
    val days = records.groupBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate() }.toSortedMap().map { (date, entries) ->
        val counts = headSideTotals(entries)
        left += counts.left; right += counts.right; unknown += counts.unknown
        HeadSideDay(date, counts, HeadSideTotals(left, right, unknown))
    }
    return HeadSideStatistics(headSideTotals(records), split?.let { at -> headSideTotals(records.filter { it.startedAt < at }) }, split?.let { at -> headSideTotals(records.filter { it.startedAt >= at }) }, days)
}

fun headSideObservation(childId: String, side: HeadSide, at: Long = System.currentTimeMillis()) = CareEventEntity(childId = childId, type = CareEventType.HeadSide, startedAt = at, endedAt = at, headSide = side)
