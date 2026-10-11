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

    @Test fun chartSelectionSurvivesClosingAndReopening() {
        var settings by androidx.compose.runtime.mutableStateOf(dk.babyapp.data.preferences.MeasurementChartSettings())
        var open by androidx.compose.runtime.mutableStateOf(true)
        compose.setContent { MaterialTheme { if (open) dk.babyapp.ui.tracking.MeasurementChartsDialog(emptyList(), "child", settings, { settings = it }) { open = false } } }
        compose.onNodeWithText("En måned").performClick()
        compose.onNodeWithText("6 måneder").performClick()
        compose.onNodeWithText("Luk").performClick()
        compose.runOnIdle { assertEquals("SixMonths", settings.period); open = true }
        compose.onNodeWithText("6 måneder").assertExists()
    }

    @Test fun manualPumpingUsesLastMethodAndSavesWithoutTimer() {
        var saved: CareEventEntity? = null
        compose.setContent { MaterialTheme { dk.babyapp.ui.tracking.PumpingDialog("child", "Manuel pumpe", {}) { saved = it } } }
        compose.onNodeWithText("Manuel pumpe").assertExists()
        compose.onNodeWithText("Pumpet (ml)").performTextInput("85")
        compose.onNodeWithText("Noter").performTextInput("Test")
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle {
            assertEquals("Manuel pumpe", saved?.pumpingMethod)
            assertEquals(85, saved?.pumpedAmountMl)
            assertEquals("Test", saved?.notes)
            assertEquals(saved?.startedAt, saved?.endedAt)
            assertNull(saved?.runningSince)
            assertEquals(0L, saved?.elapsedSeconds())
        }
    }

    @Test fun bookRemindersCanBeDisabledInNotificationSettings() {
        var enabled = true
        compose.setContent { MaterialTheme { dk.babyapp.ui.settings.ReminderSettingsDialog(dk.babyapp.data.preferences.AppPreferences(bookRemindersEnabled = true), { _, _, _ -> }, { enabled = it }, {}) } }
        compose.onNodeWithContentDescription("Bogpåmindelser").performClick()
        compose.runOnIdle { assertFalse(enabled) }
    }

    @Test fun chartsOfferAllPeriodsAndCustomDates() {
        compose.setContent { MaterialTheme { dk.babyapp.ui.tracking.MeasurementChartsDialog(emptyList(), "child", onDismiss = {}) } }
        compose.onNodeWithText("Vægt (kg)").assertIsDisplayed()
        compose.onNodeWithText("En måned").performClick()
        compose.onNodeWithText("Al tid").performClick()
        compose.onNodeWithText("Al tid").performClick()
        compose.onNodeWithText("Valgfri periode").performClick()
        compose.onNodeWithText("Fra ", substring = true).assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(5)
        compose.onNodeWithText("Temperatur (°C)").assertIsDisplayed()
    }

    @Test fun customizationCanHideWholeSectionAndReorderFields() {
        var hidden = emptySet<String>()
        compose.setContent { MaterialTheme {
            dk.babyapp.ui.tracking.AllQuickActionsDialog(emptySet(), emptyList(), {}) { value, _ -> hidden = value }
        } }
        compose.onAllNodesWithText("Skjul").onFirst().performClick()
        compose.onNodeWithContentDescription("Fjern Amning").assertDoesNotExist()
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle { assertTrue(dk.babyapp.data.preferences.QuickAction.entries.filter { it.category == "Madning" }.all { it.name in hidden }) }
    }

    @Test fun dashboardIconButtonsReorderAndRemoveFields() {
        var saved = emptyList<dk.babyapp.data.preferences.DashboardMetric>()
        compose.setContent { MaterialTheme {
            dk.babyapp.ui.tracking.DashboardMetricDialog(listOf(dk.babyapp.data.preferences.DashboardMetric.Feeding, dk.babyapp.data.preferences.DashboardMetric.Diapers), {}) { saved = it }
        } }
        compose.onNodeWithContentDescription("Flyt felt 1 ned").performClick()
        compose.onNodeWithContentDescription("Fjern felt 2").performScrollTo().performClick()
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle { assertEquals(listOf(dk.babyapp.data.preferences.DashboardMetric.Diapers), saved) }
    }

    @Test fun mixtureAmountsAreRestoredAndRecalculated() {
        var ratio by androidx.compose.runtime.mutableStateOf("60 ml + 30 ml (2:1)")
        compose.setContent { MaterialTheme { dk.babyapp.ui.tracking.BottleRatioField(ratio) { ratio = it } } }
        compose.onNodeWithText("Modermælk (ml)").assertTextContains("60")
        compose.onNodeWithText("Erstatning (ml)").performTextReplacement("60")
        compose.runOnIdle { assertEquals("60 ml + 60 ml (1:1)", ratio) }
    }

    @Test fun bottleCanSaveMixtureAndDefaultOfferedAmount() {
        var savedRatio = ""
        var savedOffered: Int? = null
        compose.setContent { MaterialTheme {
            dk.babyapp.ui.tracking.BottleDialog({}) { _, content, offered, _, _, ratio ->
                assertEquals(BottleContent.Mixed, content); savedRatio = ratio; savedOffered = offered
            }
        } }
        compose.onNodeWithText("Modermælk").performClick()
        compose.onNodeWithText("Modermælk + erstatning").performClick()
        compose.onNodeWithText("Modermælk (ml)").performScrollTo().performTextInput("60")
        compose.onNodeWithText("Erstatning (ml)").performScrollTo().performTextInput("30")
        compose.onNodeWithText("Spist (ml)").performScrollTo().performTextInput("90")
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle { assertEquals("60 ml + 30 ml (2:1)", savedRatio); assertEquals(90, savedOffered) }
    }

    @Test fun cancellingPumpDialogDoesNotSave() {
        var saves = 0
        var dismissed = false
        compose.setContent { MaterialTheme { dk.babyapp.ui.tracking.PumpingDialog("child", "Manuel pumpe", { dismissed = true }) { saves++ } } }
        compose.onNodeWithText("Manuel pumpe").assertExists()
        compose.onNodeWithText("Annuller").performClick()
        compose.runOnIdle { assertTrue(dismissed); assertEquals(0, saves) }
    }

    @Test fun nursingEditorShowsIntervalMinutes() {
        val event = CareEventEntity(childId = "child", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 121000, timerSegments = "1000-121000", timerSegmentSides = "Left", leftSeconds = 120)
        compose.setContent { MaterialTheme { EditEventDialog(event, {}) {} } }
        compose.onNodeWithText("Interval 1 · 2 min").assertExists()
    }

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
        compose.setContent { MaterialTheme { dk.babyapp.ui.tracking.ActiveTimerCard(event, {}, {}, {}, {}, {}, { current, enabled -> event = current.changeNippleShield(enabled, 61000) }, awake, { awake = it }, minimized = false) } }
        compose.onNodeWithContentDescription("Ammebrik").performClick()
        compose.runOnIdle { assertTrue(event.nippleShield); assertFalse(awake); assertEquals("true", event.timerSegmentShields); assertEquals("1000-", event.timerSegments) }
    }

    @Test fun compactTimerKeepsEssentialControls() {
        var minimized by androidx.compose.runtime.mutableStateOf(true)
        var awake by androidx.compose.runtime.mutableStateOf(false)
        var event by androidx.compose.runtime.mutableStateOf(CareEventEntity(childId = "freja", type = CareEventType.Breastfeeding, startedAt = 1000, activeSide = BreastSide.Left))
        var switches = 0
        var stops = 0
        compose.setContent { MaterialTheme {
            dk.babyapp.ui.tracking.ActiveTimerCard(event, { event = it.copy(runningSince = System.currentTimeMillis()) }, { switches++ }, { stops++ }, {}, {}, { _, _ -> }, awake, { awake = it }, minimized = minimized, onMinimizedChange = { minimized = it })
        } }
        compose.onNodeWithContentDescription("Udvid tæller").assertExists()
        compose.onNodeWithContentDescription("Hold skærmen tændt").performClick()
        compose.runOnIdle { assertTrue(awake) }
        compose.onNodeWithText("Ret intervaller").assertDoesNotExist()
        compose.onNodeWithText("Fortsæt").assertDoesNotExist()
        compose.onNodeWithContentDescription("Fortsæt").performClick()
        compose.onNodeWithContentDescription("Pause").assertExists()
        compose.onNodeWithText("Skift side").performClick()
        compose.onNodeWithText("Stop").performClick()
        compose.runOnIdle { assertEquals(1, switches); assertEquals(1, stops) }
        compose.onNodeWithContentDescription("Udvid tæller").performClick()
        compose.onNodeWithContentDescription("Hold skærmen tændt").assertExists()
    }
}
