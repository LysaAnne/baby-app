package dk.babyapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import dk.babyapp.ui.components.ActionTile
import dk.babyapp.ui.components.AmountStepper
import dk.babyapp.ui.components.BabyEmptyState
import dk.babyapp.ui.components.ChipSelector
import dk.babyapp.ui.components.EntrySheet
import dk.babyapp.ui.components.EventRow
import dk.babyapp.ui.components.TimerBar
import dk.babyapp.ui.components.formatTimer
import dk.babyapp.ui.theme.BabyAppTheme
import dk.babyapp.ui.theme.CareCategory
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun actionTileReadsLabelAndDetailAndRespectsEnabled() {
        var clicks = 0
        var enabled by mutableStateOf(true)
        compose.setContent { BabyAppTheme { ActionTile("Ble", CareCategory.Diaper, onClick = { clicks++ }, detail = "for 1 t", enabled = enabled) } }
        compose.onNodeWithContentDescription("Ble, for 1 t").assertIsEnabled().performClick()
        enabled = false
        compose.onNodeWithContentDescription("Ble, for 1 t").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }

    @Test fun eventRowShowsTimeAndSummaryAndCanBeTapped() {
        var opened = false
        compose.setContent { BabyAppTheme { EventRow("Flaske", "14.20", CareCategory.Feeding, summary = "90 ml", onClick = { opened = true }) } }
        compose.onNodeWithText("14.20  ·  90 ml").assertExists()
        compose.onNodeWithText("Flaske").performClick()
        compose.runOnIdle { assertEquals(true, opened) }
    }

    @Test fun chipSelectorSelectsOneOptionAndCanClearIt() {
        var selected by mutableStateOf<String?>(null)
        compose.setContent { BabyAppTheme { ChipSelector(listOf("Våd", "Beskidt", "Begge"), selected, { selected = it }, { it }, allowNone = true) } }
        compose.onNodeWithText("Begge").performClick()
        compose.onNodeWithText("Begge").assertIsSelected()
        compose.onNodeWithText("Våd").assertIsNotSelected()
        compose.onNodeWithText("Våd").performClick()
        compose.runOnIdle { assertEquals("Våd", selected) }
        compose.onNodeWithText("Våd").performClick()
        compose.runOnIdle { assertEquals(null, selected) }
    }

    @Test fun amountStepperStepsTypesAndStaysInRange() {
        var amount by mutableStateOf<Int?>(null)
        compose.setContent { BabyAppTheme { AmountStepper(amount, { amount = it }, unit = "ml", label = "Spist", step = 10, range = 0..200) } }
        compose.onNodeWithContentDescription("10 ml mere").performClick()
        compose.runOnIdle { assertEquals(10, amount) }
        compose.onNodeWithContentDescription("10 ml mindre").performClick()
        compose.onNodeWithContentDescription("10 ml mindre").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, amount) }
        compose.onNodeWithText("0").performTextReplacement("950")
        compose.runOnIdle { assertEquals(200, amount) }
        compose.onNodeWithContentDescription("10 ml mere").assertIsNotEnabled()
        compose.onNodeWithText("200").performTextReplacement("")
        compose.runOnIdle { assertEquals(null, amount) }
    }

    @Test fun timerBarShowsTimeAndOffersPauseResumeAndStop() {
        var running by mutableStateOf(true)
        var elapsed by mutableLongStateOf(65)
        var stopped = false
        compose.setContent { BabyAppTheme { TimerBar("Lur", CareCategory.Sleep, running, { elapsed }, onToggle = { running = !running }, onStop = { stopped = true }) } }
        compose.onNodeWithText("1:05").assertExists()
        compose.onNodeWithContentDescription("Pause Lur").performClick()
        elapsed = 3725
        compose.onNodeWithText("1:02:05").assertExists()
        compose.onNodeWithContentDescription("Fortsæt Lur").assertExists()
        compose.onNodeWithContentDescription("Stop Lur").performClick()
        compose.runOnIdle { assertEquals(true, stopped) }
        assertEquals("0:00", formatTimer(-5))
    }

    @Test fun entrySheetSavesOnlyWhenAllowedAndCancelSavesNothing() {
        var saved = 0
        var dismissed = 0
        var canSave by mutableStateOf(false)
        compose.setContent { BabyAppTheme { EntrySheet("Tilføj flaske", onDismiss = { dismissed++ }, onSave = { saved++ }, canSave = canSave) { Text("Indhold") } } }
        compose.onNodeWithText("Tilføj flaske").assertExists()
        compose.onNodeWithText("Gem").assertIsNotEnabled()
        canSave = true
        compose.onNodeWithText("Gem").assertIsEnabled().performClick()
        compose.onNodeWithText("Annuller").performClick()
        compose.runOnIdle { assertEquals(1, saved); assertEquals(1, dismissed) }
    }

    @Test fun emptyStateOffersItsAction() {
        var acted = false
        compose.setContent { BabyAppTheme { BabyEmptyState(Icons.Outlined.History, "Ingen registreringer", "Det, du registrerer, vises her.", "Registrer") { acted = true } } }
        compose.onNodeWithText("Det, du registrerer, vises her.").assertTextContains("vises her", substring = true)
        compose.onNodeWithText("Registrer").performClick()
        compose.runOnIdle { assertEquals(true, acted) }
    }
}
