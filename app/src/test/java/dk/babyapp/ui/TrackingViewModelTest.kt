package dk.babyapp.ui

import android.net.Uri
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DanishRegion
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.preferences.ThemePreference
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.ui.theme.defaultColorProfiles
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepType
import dk.babyapp.tracking.TimerNotificationController
import dk.babyapp.ui.profile.ProfileDraft
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test fun `saving and editing pumping remembers method without starting timer`() = runTest(dispatcher) {
        val preferences = FakePreferencesRepository()
        val events = FakeCareEventRepository()
        val vm = dk.babyapp.ui.tracking.TrackingViewModel(events, preferences, FakeTimerNotifications())
        val event = dk.babyapp.data.tracking.CareEventEntity(childId = "child", type = dk.babyapp.data.tracking.CareEventType.Pumping, startedAt = 1000, endedAt = 1000, pumpingMethod = "Manuel pumpe", pumpedAmountMl = 85)
        vm.saveHealthRecord(event); advanceUntilIdle()
        assertEquals("Manuel pumpe", preferences.items.value.lastPumpingMethod)
        assertEquals(null, events.items.value.single().runningSince)
        vm.updateCareEvent(event.copy(pumpingMethod = "Maskine")); advanceUntilIdle()
        assertEquals("Maskine", preferences.items.value.lastPumpingMethod)
        assertEquals(85, events.items.value.single().pumpedAmountMl)
    }

    @Test fun `two timers run independently and a third cannot replace either`() = runTest(dispatcher) {
        val events = FakeCareEventRepository()
        val vm = dk.babyapp.ui.tracking.TrackingViewModel(events, FakePreferencesRepository(), FakeTimerNotifications())
        vm.startBreastfeeding("child", dk.babyapp.data.tracking.BreastSide.Left)
        vm.startActivity("child", dk.babyapp.data.tracking.ActivityType.TummyTime)
        vm.startPumping("child", "Manuel pumpe")
        advanceUntilIdle()
        assertEquals(2, events.items.value.size)
        val nursing = events.items.value.single { it.type == CareEventType.Breastfeeding }
        val activity = events.items.value.single { it.type == CareEventType.Activity }
        vm.toggleTimer(nursing)
        advanceUntilIdle()
        assertNull(events.get(nursing.id)?.runningSince)
        assertEquals(activity, events.get(activity.id))
        vm.stopTimer(activity)
        advanceUntilIdle()
        assertNull(events.get(nursing.id)?.endedAt)
        vm.startPumping("child", "Manuel pumpe")
        advanceUntilIdle()
        assertEquals("Manuel pumpe", events.items.value.single { it.type == CareEventType.Pumping }.pumpingMethod)
    }

    @Test fun `bottle defaults offered to consumed and preserves mixture`() = runTest(dispatcher) {
        val events = FakeCareEventRepository()
        val vm = dk.babyapp.ui.tracking.TrackingViewModel(events, FakePreferencesRepository(), FakeTimerNotifications())
        vm.addBottle("child", 1000, dk.babyapp.data.tracking.BottleContent.Mixed, null, 90, "", "2:1")
        advanceUntilIdle()
        assertEquals(90, events.items.value.single().amountOfferedMl)
        assertEquals("2:1", events.items.value.single().bottleMixRatio)
    }

    @Test
    fun `sleep starts as a running timer and can be stopped`() = runTest(dispatcher) {
        val events = FakeCareEventRepository()
        val viewModel = dk.babyapp.ui.tracking.TrackingViewModel(events, FakePreferencesRepository(), FakeTimerNotifications())
        var started: Boolean? = null

        viewModel.startSleep("child-1", SleepType.Nap) { started = it }
        advanceUntilIdle()
        val running = events.items.value.single()
        viewModel.stopTimer(running, null)
        advanceUntilIdle()

        assertEquals(true, started)
        assertEquals(CareEventType.Sleep, running.type)
        assertEquals(SleepType.Nap, running.sleepType)
        assertEquals(true, running.isRunning)
        assertEquals(false, events.items.value.single().isRunning)
    }

    @Test
    fun `stopped timer is saved at once and can still be thrown away`() = runTest(dispatcher) {
        val events = FakeCareEventRepository()
        val viewModel = dk.babyapp.ui.tracking.TrackingViewModel(events, FakePreferencesRepository(), FakeTimerNotifications())
        viewModel.startSleep("child-1", SleepType.Nap)
        advanceUntilIdle()
        viewModel.stopTimer(events.items.value.single())
        advanceUntilIdle()
        val stopped = events.items.value.single()
        assertFalse(stopped.isDraft)
        viewModel.deleteCareEvent(stopped)
        advanceUntilIdle()
        assertTrue(events.items.value.isEmpty())

        viewModel.startSleep("child-1", SleepType.Nap)
        advanceUntilIdle()
        viewModel.stopTimer(events.items.value.single())
        advanceUntilIdle()
        viewModel.updateCareEvent(events.items.value.single().copy(notes = "Sov godt"))
        advanceUntilIdle()
        assertEquals(false, events.items.value.single().isDraft)
        assertEquals("Sov godt", events.items.value.single().notes)
    }

    @Test
    fun `manual sleep rejects an overlapping interval`() = runTest(dispatcher) {
        val existing = CareEventEntity(childId = "child-1", type = CareEventType.Sleep, sleepType = SleepType.Nap, startedAt = 1_000, endedAt = 5_000, leftSeconds = 4)
        val events = FakeCareEventRepository().also { it.items.value = listOf(existing) }
        val viewModel = dk.babyapp.ui.tracking.TrackingViewModel(events, FakePreferencesRepository(), FakeTimerNotifications())
        advanceUntilIdle()
        var saved: Boolean? = null

        viewModel.addSleep("child-1", 4_000, 7_000, SleepType.Nap, "", "", null, null, "") { saved = it }
        advanceUntilIdle()

        assertEquals(false, saved)
        assertEquals(1, events.items.value.size)
    }
    @Test
    fun `continued nursing remains saved after stopping and rejects another nursing timer`() = runTest(dispatcher) {
        val original = CareEventEntity(id = "saved", childId = "child", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 6000, leftSeconds = 5, notes = "Bevar mig", timerSegments = "1000-6000", timerSegmentSides = "Left")
        val other = CareEventEntity(id = "other", childId = "child", type = CareEventType.Breastfeeding, startedAt = 7000)
        val events = FakeCareEventRepository().also { it.items.value = listOf(original, other) }
        val viewModel = dk.babyapp.ui.tracking.TrackingViewModel(events, FakePreferencesRepository(), FakeTimerNotifications())
        var success = true
        viewModel.resumeBreastfeeding(original, dk.babyapp.data.tracking.BreastSide.Right) { success = it }
        advanceUntilIdle()
        assertFalse(success)
        assertEquals(original, events.get("saved"))
        events.softDelete(other)
        viewModel.resumeBreastfeeding(original, dk.babyapp.data.tracking.BreastSide.Right) { success = it }
        advanceUntilIdle()
        assertTrue(success)
        viewModel.stopTimer(requireNotNull(events.get("saved")))
        advanceUntilIdle()
        val stopped = requireNotNull(events.get("saved"))
        assertFalse(stopped.isDraft)
        assertEquals("Bevar mig", stopped.notes)
        assertEquals("Left;Right", stopped.timerSegmentSides)
        assertEquals(1, events.items.value.size)
    }

    @Test fun `new nursing ignores legacy shield preference`() = runTest(dispatcher) {
        val preferences = FakePreferencesRepository()
        val events = FakeCareEventRepository()
        val vm = dk.babyapp.ui.tracking.TrackingViewModel(events, preferences, FakeTimerNotifications())
        vm.updateDefaultNippleShield(true); advanceUntilIdle()
        vm.startBreastfeeding("child", dk.babyapp.data.tracking.BreastSide.Left); advanceUntilIdle()
        assertFalse(events.items.value.single().nippleShield)
        assertEquals("false", events.items.value.single().timerSegmentShields)
        vm.updateDefaultNippleShield(false); advanceUntilIdle()
        vm.startBreastfeeding("other", dk.babyapp.data.tracking.BreastSide.Right); advanceUntilIdle()
        assertFalse(events.items.value.first { it.childId == "other" }.nippleShield)
    }
}
