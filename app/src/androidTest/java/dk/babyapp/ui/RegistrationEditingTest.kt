package dk.babyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dk.babyapp.domain.changeNippleShield
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import dk.babyapp.data.tracking.*
import dk.babyapp.ui.tracking.EditEventDialog
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RegistrationEditingTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cancelDoesNotSaveDiaper() {
        var saves = 0
        var dismissed = false
        val event = CareEventEntity(childId = "freja", type = CareEventType.Diaper, startedAt = 1_000, endedAt = 1_000, diaperType = DiaperType.Wet)
        compose.setContent { MaterialTheme { EditEventDialog(event, { dismissed = true }) { saves++ } } }
        compose.onNodeWithText("Annuller").performClick()
        compose.runOnIdle { assertTrue(dismissed); assertEquals(0, saves) }
    }

    @Test fun diaperTypeCanBeEdited() {
        var saved: CareEventEntity? = null
        val event = CareEventEntity(childId = "freja", type = CareEventType.Diaper, startedAt = 1_000, endedAt = 1_000, diaperType = DiaperType.Wet)
        compose.setContent { MaterialTheme { EditEventDialog(event, {}) { saved = it } } }
        compose.onNodeWithText("Våd").performClick()
        compose.onNodeWithText("Afføring").performClick()
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle { assertEquals(DiaperType.Dirty, saved?.diaperType) }
    }

    @Test fun editingPumpAmountPreservesPausedDurationAndIntervals() {
        var saved: CareEventEntity? = null
        val event = CareEventEntity(childId = "freja", type = CareEventType.Pumping, startedAt = 1_000, endedAt = 1_201_000,
            leftSeconds = 600, pumpedAmountMl = 40, pumpingMethod = "Hånd", timerSegments = "1000-301000;901000-1201000", notes = "Min note")
        compose.setContent { MaterialTheme { EditEventDialog(event, {}) { saved = it } } }
        compose.onNodeWithText("Pumpet (ml)").performScrollTo().performTextReplacement("75")
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle {
            assertEquals(75, saved?.pumpedAmountMl)
            assertEquals(600L, saved?.elapsedSeconds())
            assertEquals(event.timerSegments, saved?.timerSegments)
            assertEquals("Min note", saved?.notes)
            assertEquals("Hånd", saved?.pumpingMethod)
        }
    }
    @Test fun nursingIntervalCanBeDeletedAndShieldSaved() {
        var saved: CareEventEntity? = null
        val event = CareEventEntity(childId = "freja", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 21000, leftSeconds = 10, rightSeconds = 5, timerSegments = "1000-11000;16000-21000", timerSegmentSides = "Left;Right")
        compose.setContent { MaterialTheme { EditEventDialog(event, {}) { saved = it } } }
        compose.onAllNodesWithText("Slet interval").onFirst().performScrollTo().performClick()
        compose.onNodeWithContentDescription("Ammebrik").performScrollTo().performClick()
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle { assertEquals(0L, saved?.leftSeconds); assertEquals(5L, saved?.rightSeconds); assertEquals(true, saved?.nippleShield); assertEquals("16000-21000", saved?.timerSegments) }
    }

    @Test fun activeTimerHasDirectShieldToggleWithoutChangingKeepAwake() {
        var event by androidx.compose.runtime.mutableStateOf(CareEventEntity(childId = "freja", type = CareEventType.Breastfeeding, startedAt = 1000, runningSince = 1000, activeSide = BreastSide.Left).startSegment(1000))
        var awake by androidx.compose.runtime.mutableStateOf(false)
        compose.setContent { MaterialTheme { dk.babyapp.ui.tracking.ActiveTimerCard(event, {}, {}, {}, {}, {}, { current, enabled -> event = current.changeNippleShield(enabled, 61000) }, awake, { awake = it }) } }
        compose.onNodeWithContentDescription("Ammebrik").performClick()
        compose.runOnIdle { assertTrue(event.nippleShield); assertFalse(awake); assertEquals("true", event.timerSegmentShields); assertEquals("1000-", event.timerSegments) }
    }

    @Test fun compactTimerKeepsEssentialControls() {
        var minimized by androidx.compose.runtime.mutableStateOf(false)
        var event by androidx.compose.runtime.mutableStateOf(CareEventEntity(childId = "freja", type = CareEventType.Breastfeeding, startedAt = 1000, activeSide = BreastSide.Left))
        var switches = 0
        var stops = 0
        compose.setContent { MaterialTheme {
            dk.babyapp.ui.tracking.ActiveTimerCard(event, { event = it.copy(runningSince = System.currentTimeMillis()) }, { switches++ }, { stops++ }, {}, {}, { _, _ -> }, false, {}, minimized = minimized, onMinimizedChange = { minimized = it })
        } }
        compose.onNodeWithContentDescription("Minimer tæller").performClick()
        compose.onNodeWithText("Hold skærmen tændt").assertDoesNotExist()
        compose.onNodeWithText("Ret intervaller").assertDoesNotExist()
        compose.onNodeWithText("Fortsæt").assertDoesNotExist()
        compose.onNodeWithContentDescription("Fortsæt").performClick()
        compose.onNodeWithContentDescription("Pause").assertExists()
        compose.onNodeWithText("Skift side").performClick()
        compose.onNodeWithText("Stop").performClick()
        compose.runOnIdle { assertEquals(1, switches); assertEquals(1, stops) }
        compose.onNodeWithContentDescription("Udvid tæller").performClick()
        compose.onNodeWithText("Hold skærmen tændt").assertExists()
    }
}
