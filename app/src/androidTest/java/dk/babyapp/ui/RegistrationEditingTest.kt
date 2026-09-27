package dk.babyapp.ui

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
}
