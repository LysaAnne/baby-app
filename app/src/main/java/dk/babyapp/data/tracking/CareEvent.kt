package dk.babyapp.data.tracking

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class CareEventType { Breastfeeding, Bottle, Pumping, Diaper, Sleep, Measurement, Activity, HealthVisit, Vaccination }
enum class BreastSide { Left, Right }
enum class BreastfeedingIssue { PainfulLatch, SoreNipples, CrackedNipples, Engorgement, BlockedDuct, MastitisSymptoms, Other }
enum class BottleContent { BreastMilk, Formula, Water, Other }
enum class DiaperType { Wet, Dirty, Both, Dry }
enum class DiaperColor { Yellow, Brown, Green, Black, Red, White, Other }
enum class DiaperConsistency { Watery, Loose, Soft, Formed, Hard, Mucous }
enum class SleepType { Nap, Night }
enum class SleepQuality { Restful, Mixed, Restless }
enum class MeasurementType { Weight, Height, HeadCircumference, Temperature }
enum class ActivityType { TummyTime, Bath, OutdoorTime, Play, Medicine, Other }
enum class HealthVisitType { PreventiveExam, GpVisit, HealthVisitor, Midwife, Hospital, Specialist, Dental, Other }
enum class HealthRecordStatus { Scheduled, Completed, Postponed, Cancelled, Declined }

@Entity(
    tableName = "care_events",
    indices = [Index("childId"), Index("startedAt")],
)
data class CareEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val type: CareEventType,
    val startedAt: Long,
    val endedAt: Long? = null,
    val runningSince: Long? = null,
    val activeSide: BreastSide? = null,
    val breastfeedingIssue: BreastfeedingIssue? = null,
    val leftSeconds: Long = 0,
    val rightSeconds: Long = 0,
    val amountOfferedMl: Int? = null,
    val amountConsumedMl: Int? = null,
    val pumpedAmountMl: Int? = null,
    val bottleContent: BottleContent? = null,
    val diaperType: DiaperType? = null,
    val diaperColor: DiaperColor? = null,
    val diaperConsistency: DiaperConsistency? = null,
    val sleepType: SleepType? = null,
    val sleepLocation: String = "",
    val settlingMethod: String = "",
    val awakenings: Int? = null,
    val sleepQuality: SleepQuality? = null,
    val timerSegments: String = "",
    val measurementType: MeasurementType? = null,
    val measurementValue: Double? = null,
    val measurementUnit: String = "",
    val activityType: ActivityType? = null,
    val activityDurationSeconds: Long? = null,
    val timeSpecified: Boolean = true,
    val medicationName: String = "",
    val medicationDose: String = "",
    val healthVisitType: HealthVisitType? = null,
    val healthStatus: HealthRecordStatus? = null,
    val providerId: String? = null,
    val providerDisplayName: String = "",
    val healthTitle: String = "",
    val healthReason: String = "",
    val healthObservations: String = "",
    val healthAdvice: String = "",
    val healthQuestions: String = "",
    val followUp: String = "",
    val vaccineName: String = "",
    val vaccineDose: String = "",
    val vaccineBatchNumber: String = "",
    val injectionSite: String = "",
    val reactionNotes: String = "",
    val officialScheduleKey: String? = null,
    val observation: String = "",
    val notes: String = "",
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val isRunning: Boolean get() = endedAt == null && runningSince != null

    fun elapsedSeconds(now: Long = System.currentTimeMillis()): Long {
        val stored = leftSeconds + rightSeconds
        return stored + if (runningSince != null) ((now - runningSince).coerceAtLeast(0) / 1_000) else 0
    }
}

fun CareEventEntity.startSegment(at: Long): CareEventEntity = copy(
    timerSegments = (timerSegments.takeIf { it.isNotBlank() }?.plus(";") ?: "") + "$at-",
)

fun CareEventEntity.closeSegment(at: Long): CareEventEntity {
    if (timerSegments.isBlank() || !timerSegments.endsWith("-")) return this
    return copy(timerSegments = timerSegments + at)
}

fun CareEventEntity.segmentIntervals(): List<Pair<Long, Long?>> = timerSegments.split(';').mapNotNull { value ->
    val parts = value.split('-', limit = 2)
    val start = parts.firstOrNull()?.toLongOrNull() ?: return@mapNotNull null
    start to parts.getOrNull(1)?.toLongOrNull()
}
