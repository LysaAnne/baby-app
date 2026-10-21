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
            records = records.filterNot { it.childId == value.childId && it.tooth == value.tooth } + listOfNotNull(value.takeIf { it.emerged != null || it.lost != null })
        }) {} } }
        compose.onNodeWithContentDescription("Øverste midterste fortand, barnets højre, kommet").performClick()
        compose.onNodeWithText("Annuller").performClick()
        compose.runOnIdle { assertEquals(0, saves) }
        compose.onNodeWithContentDescription("Øverste midterste fortand, barnets højre, kommet").performClick()
        compose.onNodeWithText("Fjern").performClick()
        compose.onNodeWithContentDescription("Øverste midterste fortand, barnets højre, ikke kommet endnu").assertExists()
        compose.runOnIdle { assertEquals(1, saves); assertEquals("2026-01-02", records.single { it.childId == "other" }.lost) }
        compose.onNodeWithContentDescription("Nederste anden kindtand, barnets højre, ikke kommet endnu").performClick()
        compose.onNodeWithText("Nederste anden kindtand").assertIsDisplayed()
    }

    @Test fun newToothIsRegisteredTodayWithSignsFromThePicker() {
        var saved: ToothRecord? = null
        compose.setContent { MaterialTheme { TeethDialog("child", emptyList(), { saved = it }) {} } }
        compose.onNodeWithText("Registrér ny tand", substring = true).performScrollTo().performClick()
        compose.onAllNodesWithText("Nederste midterste fortand").onFirst().performClick()
        compose.onNodeWithText("Savler").performClick()
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle {
            assertEquals(81, saved?.tooth)
            assertEquals(java.time.LocalDate.now().toString(), saved?.emerged)
            assertEquals("Savler", saved?.signs)
        }
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
