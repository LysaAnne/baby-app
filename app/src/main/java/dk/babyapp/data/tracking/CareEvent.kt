package dk.babyapp.data.tracking

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class CareEventType { Breastfeeding, Bottle, Pumping, Diaper, Sleep }
enum class BreastSide { Left, Right }
enum class BottleContent { BreastMilk, Formula, Water, Other }
enum class DiaperType { Wet, Dirty, Both, Dry }
enum class SleepType { Nap, Night }
enum class SleepQuality { Restful, Mixed, Restless }

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
    val leftSeconds: Long = 0,
    val rightSeconds: Long = 0,
    val amountOfferedMl: Int? = null,
    val amountConsumedMl: Int? = null,
    val pumpedAmountMl: Int? = null,
    val bottleContent: BottleContent? = null,
    val diaperType: DiaperType? = null,
    val sleepType: SleepType? = null,
    val sleepLocation: String = "",
    val settlingMethod: String = "",
    val awakenings: Int? = null,
    val sleepQuality: SleepQuality? = null,
    val timerSegments: String = "",
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
