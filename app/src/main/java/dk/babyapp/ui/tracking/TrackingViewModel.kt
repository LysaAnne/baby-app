package dk.babyapp.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.medicine.MedicineRepository
import dk.babyapp.data.teeth.TeethRepository
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.teeth.ToothRecord
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperColor
import dk.babyapp.data.tracking.DiaperConsistency
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.data.tracking.closeSegment
import dk.babyapp.data.tracking.startSegment
import dk.babyapp.domain.accrueUntil
import dk.babyapp.domain.overlapsSleep
import dk.babyapp.domain.resumeNursing
import dk.babyapp.domain.switchNursingSide
import dk.babyapp.tracking.TimerNotificationController
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Creates, edits and deletes registrations and runs timers. Also owns medicine cards and teeth. */
@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val careEventRepository: CareEventRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val timerNotifications: TimerNotificationController,
    private val medicineRepository: MedicineRepository? = null,
    private val teethRepository: TeethRepository? = null,
) : ViewModel() {
    private val timerStartMutex = Mutex()

    private suspend fun sleepsNear(event: CareEventEntity) =
        careEventRepository.sleepsOverlapping(event.childId, event.startedAt, event.endedAt ?: System.currentTimeMillis())

    private suspend fun canStartTimer(childId: String, type: CareEventType) =
        dk.babyapp.domain.canStartTimer(careEventRepository.unfinished(), childId, type)

    fun startBreastfeeding(childId: String, side: BreastSide) = viewModelScope.launch { timerStartMutex.withLock {
        if (!canStartTimer(childId, CareEventType.Breastfeeding)) return@withLock
        val now = System.currentTimeMillis()
        val event = CareEventEntity(childId = childId, type = CareEventType.Breastfeeding, startedAt = now, runningSince = now, activeSide = side, nippleShield = false).startSegment(now)
        careEventRepository.save(event); timerNotifications.show(event.id)
    } }

    fun startPumping(childId: String, method: String = "") = viewModelScope.launch { timerStartMutex.withLock {
        if (!canStartTimer(childId, CareEventType.Pumping)) return@withLock
        val now = System.currentTimeMillis()
        val event = CareEventEntity(childId = childId, type = CareEventType.Pumping, startedAt = now, runningSince = now, pumpingMethod = method).startSegment(now)
        careEventRepository.save(event); timerNotifications.show(event.id)
    } }

    fun startSleep(childId: String, sleepType: SleepType, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch { timerStartMutex.withLock {
        if (!canStartTimer(childId, CareEventType.Sleep)) {
            onResult(false)
        } else {
            val now = System.currentTimeMillis()
            val event = CareEventEntity(childId = childId, type = CareEventType.Sleep, sleepType = sleepType, startedAt = now, runningSince = now).startSegment(now)
            careEventRepository.save(event)
            timerNotifications.show(event.id)
            onResult(true)
        }
    } }

    fun startActivity(childId: String, activityType: ActivityType, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch { timerStartMutex.withLock {
        if (!canStartTimer(childId, CareEventType.Activity)) {
            onResult(false)
        } else {
            val now = System.currentTimeMillis()
            val event = CareEventEntity(childId = childId, type = CareEventType.Activity, activityType = activityType, startedAt = now, runningSince = now).startSegment(now)
            careEventRepository.save(event)
            timerNotifications.show(event.id)
            onResult(true)
        }
    } }

    fun toggleTimer(event: CareEventEntity) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        if (event.runningSince == null) {
            careEventRepository.save(event.startSegment(now).copy(runningSince = now))
        } else {
            careEventRepository.save(event.accrueUntil(now).closeSegment(now).copy(runningSince = null))
        }
    }

    fun switchBreastSide(event: CareEventEntity) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        careEventRepository.save(event.switchNursingSide(now))
    }

    fun resumeBreastfeeding(event: CareEventEntity, side: BreastSide, onResult: (Boolean) -> Unit) = viewModelScope.launch { timerStartMutex.withLock {
        val saved = careEventRepository.get(event.id)
        val now = System.currentTimeMillis()
        if (saved == null || saved.type != CareEventType.Breastfeeding || saved.deletedAt != null || saved.endedAt == null || saved.endedAt > now || !canStartTimer(saved.childId, CareEventType.Breastfeeding)) {
            onResult(false)
        } else {
            careEventRepository.save(saved.resumeNursing(side, now))
            timerNotifications.show(saved.id)
            onResult(true)
        }
    } }

    fun stopTimer(event: CareEventEntity, amountMl: Int? = null, onComplete: (CareEventEntity) -> Unit = {}) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        val accrued = event.accrueUntil(now).closeSegment(now)
        val completed = accrued.copy(
            // Saved straight away; the window that follows only adds optional details.
            isDraft = false,
            endedAt = now,
            runningSince = null,
            pumpedAmountMl = amountMl ?: event.pumpedAmountMl,
            activityDurationSeconds = accrued.elapsedSeconds().takeIf { event.type == CareEventType.Activity } ?: event.activityDurationSeconds,
        )
        careEventRepository.save(completed)
        timerNotifications.hide()
        onComplete(careEventRepository.get(completed.id) ?: completed)
    }

    fun addBottle(childId: String, time: Long, content: BottleContent, offered: Int?, consumed: Int?, notes: String, ratio: String = "") =
        viewModelScope.launch { careEventRepository.save(CareEventEntity(childId = childId, type = CareEventType.Bottle, startedAt = time, endedAt = time, bottleContent = content, amountOfferedMl = listOfNotNull(offered, consumed).maxOrNull(), amountConsumedMl = consumed, notes = notes, bottleMixRatio = if (content == BottleContent.Mixed) ratio.trim() else "")) }

    fun addDiaper(childId: String, time: Long, type: DiaperType, color: DiaperColor?, consistency: DiaperConsistency?, observation: String, notes: String, onComplete: (CareEventEntity) -> Unit = {}) =
        viewModelScope.launch {
            val event = CareEventEntity(childId = childId, type = CareEventType.Diaper, startedAt = time, endedAt = time, diaperType = type, diaperColor = color, diaperConsistency = consistency, observation = observation, notes = notes)
            careEventRepository.save(event); onComplete(event)
        }

    fun addMeasurement(childId: String, time: Long, timeSpecified: Boolean, type: MeasurementType, value: Double, unit: String, notes: String) = viewModelScope.launch {
        careEventRepository.save(CareEventEntity(childId = childId, type = CareEventType.Measurement, startedAt = time, endedAt = time, timeSpecified = timeSpecified, measurementType = type, measurementValue = value, measurementUnit = unit, notes = notes))
    }

    fun addActivity(childId: String, start: Long, end: Long, type: ActivityType, notes: String) = viewModelScope.launch {
        careEventRepository.save(CareEventEntity(childId = childId, type = CareEventType.Activity, startedAt = start, endedAt = end, activityType = type, activityDurationSeconds = ((end - start).coerceAtLeast(0) / 1_000), notes = notes))
    }

    fun addManualTimer(childId: String, type: CareEventType, start: Long, end: Long, side: BreastSide?, amountMl: Int?, notes: String) = viewModelScope.launch {
        val seconds = ((end - start).coerceAtLeast(0) / 1_000)
        careEventRepository.save(CareEventEntity(childId = childId, type = type, startedAt = start, endedAt = end, activeSide = side, leftSeconds = if (side != BreastSide.Right) seconds else 0, rightSeconds = if (side == BreastSide.Right) seconds else 0, pumpedAmountMl = amountMl, notes = notes))
    }

    fun addSleep(
        childId: String,
        start: Long,
        end: Long,
        sleepType: SleepType,
        location: String,
        settlingMethod: String,
        awakenings: Int?,
        quality: SleepQuality?,
        notes: String,
        onResult: (Boolean) -> Unit = {},
    ) = viewModelScope.launch {
        val candidate = CareEventEntity(childId = childId, type = CareEventType.Sleep, startedAt = start, endedAt = end)
        val overlaps = overlapsSleep(candidate, sleepsNear(candidate))
        if (end <= start || overlaps) {
            onResult(false)
        } else {
            careEventRepository.save(
                CareEventEntity(
                    childId = childId, type = CareEventType.Sleep, startedAt = start, endedAt = end,
                    leftSeconds = (end - start) / 1_000, sleepType = sleepType, sleepLocation = location,
                    settlingMethod = settlingMethod, awakenings = awakenings, sleepQuality = quality, notes = notes,
                ),
            )
            onResult(true)
        }
    }

    fun updateCareEvent(event: CareEventEntity, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val current = careEventRepository.get(event.id)
        val changedDuringEdit = event.type == CareEventType.Breastfeeding && current != null && current.updatedAt != event.updatedAt
        val overlaps = overlapsSleep(event, sleepsNear(event))
        if (overlaps || changedDuringEdit) onResult(false) else {
            careEventRepository.save(event.copy(isDraft = false))
            if (event.type == CareEventType.Pumping) preferencesRepository.updateLastPumpingMethod(event.pumpingMethod)
            onResult(true)
        }
    }

    fun deleteCareEvent(event: CareEventEntity) = viewModelScope.launch {
        careEventRepository.softDelete(event)
        if (event.endedAt == null) timerNotifications.hide()
    }

    /** Brings back a registration deleted moments ago (Fortryd). */
    fun restoreCareEvent(event: CareEventEntity) = viewModelScope.launch { careEventRepository.save(event.copy(deletedAt = null)) }

    fun saveHealthRecord(event: CareEventEntity, onComplete: () -> Unit = {}) = viewModelScope.launch {
        careEventRepository.save(event)
        if (event.type == CareEventType.Pumping) preferencesRepository.updateLastPumpingMethod(event.pumpingMethod)
        onComplete()
    }

    fun updateDefaultNippleShield(enabled: Boolean) = viewModelScope.launch { preferencesRepository.updateDefaultNippleShield(enabled) }
    fun saveTooth(record: ToothRecord) = viewModelScope.launch { requireNotNull(teethRepository).save(record) }
    fun updateMedicines(medicines: List<MedicinePlan>) = viewModelScope.launch { requireNotNull(medicineRepository).replaceAll(medicines) }
}
