package dk.babyapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import dk.babyapp.data.book.*
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.ui.book.BookPageEditor
import dk.babyapp.ui.tracking.EventCard
import dk.babyapp.data.tracking.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BabyBookEditingTest {
    @get:Rule val compose = createComposeRule()
    @Test fun saveBookTextToCorrectChildAndPage() {
        var saved: BabyBookPage? = null
        compose.setContent { MaterialTheme { BookPageEditor(ChildProfile(id = "freja", name = "Freja"), babyBookTemplates.first(), null, { saved = it }, { null }, { "photo" }, {}) } }
        compose.onNodeWithText("En hilsen til dig").performTextInput("Kære Freja\nVi elsker dig")
        compose.onNodeWithText("Gem", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertEquals("freja", saved?.childId); assertEquals("cover", saved?.pageId); assertEquals("Kære Freja\nVi elsker dig", saved?.answers()?.get("En hilsen til dig")) }
    }
    @Test fun cancelRequiresDiscardAndDoesNotSave() {
        var saves = 0
        var dismissed = false
        compose.setContent { MaterialTheme { BookPageEditor(ChildProfile(id = "freja", name = "Freja"), babyBookTemplates.first(), null, { saves++ }, { null }, { "photo" }, { dismissed = true }) } }
        compose.onNodeWithText("Barnets navn").performTextInput("Freja")
        compose.onNodeWithText("Annuller").performClick()
        compose.onNodeWithText("Kassér ændringer?").assertExists()
        compose.onNodeWithText("Kassér").performClick()
        compose.runOnIdle { assertTrue(dismissed); assertEquals(0, saves) }
    }
    @Test fun journalDisplaysEachSideAndOffersResume() {
        var resumed = false
        val event = CareEventEntity(childId = "freja", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 10000, timerSegments = "1000-5000;5000-10000", timerSegmentSides = "Left;Right")
        compose.setContent { MaterialTheme { EventCard(event, expandAll = true, onEdit = {}, onDelete = {}, onResume = { resumed = true }) } }
        compose.onNodeWithText("Venstre", substring = true).assertExists()
        compose.onNodeWithText("Højre", substring = true).assertExists()
        compose.onNodeWithText("Fortsæt amning").performClick()
        compose.runOnIdle { assertTrue(resumed) }
    }
    @Test fun chapterButtonsProtectUnsavedTextBeforeReturning() {
        var returned = false
        compose.setContent { MaterialTheme { BookPageEditor(ChildProfile(id = "freja", name = "Freja"), babyBookTemplates.first(), null, {}, { null }, { "photo" }, onDismiss = {}, onAllChapters = { returned = true }) } }
        compose.onAllNodesWithText("‹ Alle kapitler").assertCountEquals(2)
        compose.onNodeWithText("Barnets navn").performTextInput("Freja")
        compose.onAllNodesWithText("‹ Alle kapitler").onFirst().performClick()
        compose.onNodeWithText("Kassér ændringer?").assertExists()
        compose.runOnIdle { assertFalse(returned) }
        compose.onNodeWithText("Kassér").performClick()
        compose.runOnIdle { assertTrue(returned) }
    }

}
