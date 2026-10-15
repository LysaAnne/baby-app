package dk.babyapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import dk.babyapp.data.preferences.*
import dk.babyapp.data.teeth.ToothRecord
import dk.babyapp.ui.tracking.TeethDialog
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TeethAndReminderTest {
    @get:Rule val compose = createComposeRule()

    @Test fun toothSelectionEditsOnlySelectedChildAndCancelDoesNotSave() {
        var records by mutableStateOf(listOf(ToothRecord("child", 51, "2026-01-01"), ToothRecord("other", 51, lost = "2026-01-02")))
        var saves = 0
        compose.setContent { MaterialTheme { TeethDialog("child", records, { value ->
            saves++
            records = records.filterNot { it.childId == value.childId && it.tooth == value.tooth } + value
        }, {}) } }
        compose.onNodeWithContentDescription("Tand 51, Frembrudt").performClick()
        compose.onNodeWithText("Ryd").performClick()
        compose.onNodeWithText("Annuller").performClick()
        compose.runOnIdle { assertEquals(0, saves) }
        compose.onNodeWithContentDescription("Tand 51, Frembrudt").performClick()
        compose.onNodeWithText("Ryd").performClick()
        compose.onNodeWithText("Gem").performClick()
        compose.onNodeWithContentDescription("Tand 51, Ikke registreret").assertExists()
        compose.runOnIdle { assertEquals(1, saves); assertEquals("2026-01-02", records.single { it.childId == "other" }.lost) }
        compose.onNodeWithContentDescription("Tand 85, Ikke registreret").performScrollTo()
        compose.runOnIdle {
            val instrumentation = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
            val screenshot = instrumentation.uiAutomation.takeScreenshot()
            java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "teeth-chart.png").outputStream().use { screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            screenshot.recycle()
        }
        compose.onNodeWithContentDescription("Tand 85, Ikke registreret").performClick()
        compose.onNodeWithText("Tand 85").assertIsDisplayed()
    }

    @Test fun reminderSettingsHaveOnlyBookChoiceAndSaveFrequency() {
        var preferences by mutableStateOf(AppPreferences())
        compose.setContent { MaterialTheme {
            dk.babyapp.ui.settings.ReminderSettingsDialog(preferences, { _, _, _ -> }, { preferences = preferences.copy(bookRemindersEnabled = it) }, { preferences = preferences.copy(bookReminderFrequency = it) }, {})
        } }
        compose.onNodeWithText("Påmind om registrering").assertDoesNotExist()
        compose.onNodeWithText("Tre gange om ugen").performClick()
        compose.onNodeWithText("Månedligt").performClick()
        compose.runOnIdle { assertEquals(BookReminderFrequency.Monthly, preferences.bookReminderFrequency) }
        compose.onNodeWithContentDescription("Bogpåmindelser").performClick()
        compose.onNodeWithText("Månedligt").assertDoesNotExist()
    }
}
