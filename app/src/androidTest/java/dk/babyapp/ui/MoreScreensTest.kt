package dk.babyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.domain.SongbookData
import dk.babyapp.ui.more.DevelopmentDialog
import dk.babyapp.ui.more.SongbookDialog
import dk.babyapp.ui.theme.BabyAppTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoreScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun developmentShowsTheCurrentSpurtAndTicksMilestones() {
        val child = ChildProfile(id = "c", name = "Freja", birthDate = LocalDate.now().minusWeeks(18), dueDate = LocalDate.now().minusWeeks(18))
        var checks by mutableStateOf(emptyList<DevelopmentCheck>())
        compose.setContent { BabyAppTheme { DevelopmentDialog(child, checks, { checks = it }) {} } }
        compose.onNodeWithText("Uge 18 fra termin").assertExists()
        compose.onNodeWithText("Spring 4 lige nu").assertExists()
        compose.onNodeWithText("Milepæle").performClick()
        compose.onNodeWithText("Vender sig").performClick()
        compose.runOnIdle { assertEquals(listOf("milestone-roll"), checks.map { it.key }) }
    }

    @Test fun songbookKeepsFavouritesAndOwnWords() {
        var data by mutableStateOf(SongbookData())
        compose.setContent { BabyAppTheme { SongbookDialog(data, { data = it }) {} } }
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Søg sang")).performTextInput("Mester")
        compose.onNodeWithContentDescription("Gør Mester Jakob til favorit").performClick()
        compose.onNodeWithText("Mester Jakob").performClick()
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Sangtekst")).performTextInput("Vores vers")
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle {
            assertEquals(setOf("mester-jakob"), data.favorites)
            assertEquals("Vores vers", data.lyrics["mester-jakob"])
        }
    }
}
