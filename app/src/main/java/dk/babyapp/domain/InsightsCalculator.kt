package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.SleepType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CareInsights(
    val feedingCount: Int,
    val breastfeedingMinutes: Long,
    val bottleMl: Int,
    val diaperCount: Int,
    val wetDiapers: Int,
    val dirtyDiapers: Int,
    val diaperObservations: Int,
    val sleepMinutes: Long,
    val napCount: Int,
    val longestSleepMinutes: Long,
    val commonSleepPeriod: String,
)

fun calculateInsights(events: List<CareEventEntity>, childId: String, from: LocalDate, through: LocalDate, zone: ZoneId = ZoneId.systemDefault()): CareInsights {
    val selected = events.filter { event ->
        event.childId == childId && event.deletedAt == null && !event.isDraft && Instant.ofEpochMilli(event.startedAt).atZone(zone).toLocalDate() in from..through
    }
    val feeding = selected.filter { it.type in setOf(CareEventType.Breastfeeding, CareEventType.Bottle) }
    val diapers = selected.filter { it.type == CareEventType.Diaper }
    val sleep = selected.filter { it.type == CareEventType.Sleep }
    val sleepHours = sleep.groupingBy { Instant.ofEpochMilli(it.startedAt).atZone(zone).hour / 6 }.eachCount()
    val commonPeriod = sleepHours.maxByOrNull { it.value }?.key?.let { listOf("nat", "morgen", "eftermiddag", "aften")[it] } ?: "-"
    return CareInsights(
        feedingCount = feeding.size,
        breastfeedingMinutes = feeding.filter { it.type == CareEventType.Breastfeeding }.sumOf { it.elapsedSeconds() } / 60,
        bottleMl = feeding.filter { it.type == CareEventType.Bottle }.sumOf { it.amountConsumedMl ?: 0 },
        diaperCount = diapers.size,
        wetDiapers = diapers.count { it.diaperType?.name in setOf("Wet", "Both") },
        dirtyDiapers = diapers.count { it.diaperType?.name in setOf("Dirty", "Both") },
        diaperObservations = diapers.count { it.observation.isNotBlank() || it.diaperColor != null || it.diaperConsistency != null },
        sleepMinutes = sleep.sumOf { it.elapsedSeconds() } / 60,
        napCount = sleep.count { it.sleepType == SleepType.Nap },
        longestSleepMinutes = (sleep.maxOfOrNull { it.elapsedSeconds() } ?: 0) / 60,
        commonSleepPeriod = commonPeriod,
    )
}
