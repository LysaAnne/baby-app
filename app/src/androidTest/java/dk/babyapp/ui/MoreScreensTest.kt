package dk.babyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.domain.SongbookData
import dk.babyapp.domain.songLyrics
import dk.babyapp.ui.more.DevelopmentDialog
import dk.babyapp.ui.more.SongbookDialog
import dk.babyapp.ui.theme.BabyAppTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoreScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun builtInLyricsOpenForReadingAndCancelDiscardsEdits() {
        val data = SongbookData()
        compose.setContent { BabyAppTheme { SongbookDialog(data, { error("Cancel must not save") }) {} } }
        compose.onNodeWithText("Søg sang").performTextInput("Der bor en bager")
        compose.onNodeWithText("tekst tilføjet", substring = true).assertExists()
        compose.onNode(androidx.compose.ui.test.hasText("Der bor en bager") and !hasSetTextAction()).performClick()
        val lyrics = songLyrics.getValue("ugle-der-bor-en-bager")
        compose.onNodeWithText(lyrics).assertExists()
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Sangtekst")).assertDoesNotExist()
        compose.onNodeWithContentDescription("Rediger sangteksten").performClick()
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText(lyrics)).assertExists()
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Sangtekst")).performTextReplacement("Kasseret ændring")
        compose.onNodeWithText("Annuller").performClick()
        compose.onNodeWithText(lyrics).assertExists()
        compose.onNodeWithText("Kasseret ændring").assertDoesNotExist()
    }

    @Test fun ownLyricsOverrideBuiltInLyricsAndEmptySaveRestoresThem() {
        val id = "ugle-der-bor-en-bager"
        var data by mutableStateOf(SongbookData(favorites = setOf(id), lyrics = mapOf(id to "Familiens bagersang", "mester-jakob" to "Bevar mig")))
        compose.setContent { BabyAppTheme { SongbookDialog(data, { data = it }) {} } }
        compose.onNodeWithText("Søg sang").performTextInput("Der bor en bager")
        compose.onNode(androidx.compose.ui.test.hasText("Der bor en bager") and !hasSetTextAction()).performClick()
        compose.onNodeWithText("Familiens bagersang").assertExists()
        compose.onNodeWithText(songLyrics.getValue(id)).assertDoesNotExist()
        compose.onNodeWithContentDescription("Rediger sangteksten").performClick()
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Sangtekst")).performTextReplacement("")
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle {
            assertEquals(mapOf("mester-jakob" to "Bevar mig"), data.lyrics)
            assertEquals(setOf(id), data.favorites)
        }
        compose.onNode(androidx.compose.ui.test.hasText("Der bor en bager") and !hasSetTextAction()).performClick()
        compose.onNodeWithText(songLyrics.getValue(id)).assertExists()
    }

    @Test fun searchMatchesTheDisplayedLyrics() {
        val data = SongbookData(lyrics = mapOf("bro-bro-brille" to "Familiens hemmelige brovers"))
        compose.setContent { BabyAppTheme { SongbookDialog(data, {}) {} } }
        compose.onNodeWithText("Søg sang").performTextInput("sukkersager")
        compose.onNodeWithText("Der bor en bager").assertExists()
        compose.onNodeWithText("Søg sang").performTextReplacement("hemmelige brovers")
        compose.onNodeWithText("Bro, bro, brille").assertExists()
        compose.onNodeWithText("Søg sang").performTextReplacement("kejseren")
        compose.onNodeWithText("Bro, bro, brille").assertDoesNotExist()
    }

    @Test fun developmentShowsTheCurrentSpurtAndTicksMilestones() {
        val child = ChildProfile(id = "c", name = "Freja", birthDate = LocalDate.now().minusWeeks(18), dueDate = LocalDate.now().minusWeeks(18))
        var checks by mutableStateOf(emptyList<DevelopmentCheck>())
        compose.setContent { BabyAppTheme { DevelopmentDialog(child, checks, { checks = it }) {} } }
        compose.onNodeWithText("Uge 18 fra termin").assertExists()
        compose.onNodeWithText("Spring 4 lige nu", substring = true).assertExists()
        compose.onAllNodesWithText("Milepæle")[0].performClick() // the tab; the Nu card has the same title
        compose.onAllNodesWithContentDescription("Ikke endnu: Vender sig")[0].performClick()
        compose.runOnIdle { assertEquals(listOf("milestone-roll"), checks.map { it.key }) }
    }

    @Test fun songbookKeepsFavouritesAndOwnWords() {
        var data by mutableStateOf(SongbookData())
        compose.setContent { BabyAppTheme { SongbookDialog(data, { data = it }) {} } }
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Søg sang")).performTextInput("Mester")
        compose.onNodeWithContentDescription("Gør Mester Jakob til favorit").performClick()
        compose.onNodeWithText("Mester Jakob").performClick()
        // The words open for editing with the pencil.
        compose.onNodeWithContentDescription("Rediger sangteksten").performClick()
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Sangtekst")).performTextInput("Vores vers")
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle {
            assertEquals(setOf("mester-jakob"), data.favorites)
            assertEquals("Vores vers", data.lyrics["mester-jakob"])
        }
    }

    @Test fun importedWordsGoToTheirSongsAndNewSongsAreAdded() {
        var data by mutableStateOf(SongbookData())
        compose.setContent { BabyAppTheme { SongbookDialog(data, { data = it }) {} } }
        compose.onNodeWithContentDescription("Importér sangtekster").performClick()
        // Made-up words keep import assertions independent of built-in lyrics.
        compose.onNode(hasSetTextAction() and androidx.compose.ui.test.hasText("Eller indsæt teksten her")).performTextInput("Mester Jakob\nvores vers\n# Ny sang\nla la")
        compose.onNodeWithText("Importér").performClick()
        compose.runOnIdle {
            assertEquals("vores vers", data.lyrics["mester-jakob"])
            assertEquals(listOf("Ny sang"), data.ownSongs.map { it.title })
            assertEquals("la la", data.lyrics["own-${data.ownSongs.single().id}"])
        }
    }

    @Test fun searchForWordsIsOnlyShownWithoutWords() {
        compose.setContent { BabyAppTheme { SongbookDialog(SongbookData(), {}) {} } }
        // A song with built-in words has no search; one without words has.
        compose.onNodeWithText("Søg sang").performTextInput("Der bor en bager")
        compose.onNode(androidx.compose.ui.test.hasText("Der bor en bager") and !hasSetTextAction()).performClick()
        compose.onNodeWithText("Søg efter teksten", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Luk").performClick()
        compose.onNodeWithText("Søg sang").performTextReplacement("Mester Jakob")
        compose.onNode(androidx.compose.ui.test.hasText("Mester Jakob") and !hasSetTextAction()).performClick()
        compose.onNodeWithText("Søg efter teksten", substring = true).assertExists()
    }

    @Test fun psychologyTabShowsWhatIsUnderWayAndOpensAPhenomenon() {
        val child = ChildProfile(id = "c", name = "Freja", birthDate = LocalDate.now().minusMonths(10), dueDate = LocalDate.now().minusMonths(10))
        var checks by mutableStateOf(emptyList<DevelopmentCheck>())
        compose.setContent { BabyAppTheme { DevelopmentDialog(child, checks, { checks = it }) {} } }
        compose.onAllNodesWithText("Psykologi")[0].performClick() // the tab; the Nu card has the same title
        compose.onNodeWithText("Lige nu", substring = true).assertExists()
        compose.onNode(androidx.compose.ui.test.hasScrollAction()).performScrollToNode(androidx.compose.ui.test.hasContentDescription("Læs om Fælles opmærksomhed"))
        compose.onAllNodesWithContentDescription("Læs om Fælles opmærksomhed")[0].performClick()
        compose.onAllNodesWithContentDescription("Ikke læst: Fælles opmærksomhed")[0].performClick()
        compose.runOnIdle { assertEquals(listOf("psych-10"), checks.map { it.key }) }
    }

    @Test fun experimentsCanBeReadAndTickedOff() {
        val child = ChildProfile(id = "c", name = "Freja", birthDate = LocalDate.now().minusMonths(16), dueDate = LocalDate.now().minusMonths(16))
        var checks by mutableStateOf(emptyList<DevelopmentCheck>())
        compose.setContent { BabyAppTheme { DevelopmentDialog(child, checks, { checks = it }) {} } }
        val broccoli = dk.babyapp.domain.homeExperiments.first { it.title == "Broccoli eller kiks" }.let { "${dk.babyapp.domain.experimentOrder.getValue(it.number)}. ${it.title}" }
        compose.onAllNodesWithText("Eksperimenter")[0].performClick() // the tab; the Nu card has the same title
        compose.onNodeWithText("Om eksperimenter").performClick()
        compose.onNodeWithText("0 af 90 lavet").assertExists()
        compose.onNodeWithText("Luk").performClick()
        compose.onNode(androidx.compose.ui.test.hasScrollAction()).performScrollToNode(androidx.compose.ui.test.hasContentDescription("Læs om $broccoli"))
        // The card opens as a popup, which ticks it off with its own button.
        compose.onNodeWithContentDescription("Læs om $broccoli").performClick()
        compose.onNodeWithText("Du skal bruge").assertExists()
        compose.onNodeWithText("Markér som lavet").performClick()
        compose.runOnIdle { assertEquals(listOf("exp-26"), checks.map { it.key }) }
    }
}
