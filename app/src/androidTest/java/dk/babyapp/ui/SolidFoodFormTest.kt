package dk.babyapp.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasSetTextAction
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.ui.theme.BabyAppTheme
import dk.babyapp.ui.tracking.SolidFoodDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SolidFoodFormTest {
    @get:Rule val compose = createComposeRule()

    @Test fun picksFromTheListAndAddsOwnFoods() {
        val earlier = CareEventEntity(childId = "c", type = CareEventType.SolidFood, startedAt = 1, endedAt = 1, foodName = "Gulerod")
        var saved: CareEventEntity? = null
        var listOpened = false
        compose.setContent { BabyAppTheme { SolidFoodDialog("c", null, {}, listOf(earlier), onOpenList = { listOpened = true }) { saved = it } } }
        compose.onNodeWithText("100 før 1 · 1 af 100 smagt").assertExists()
        compose.onNodeWithText("Se hele listen").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("gu")
        // Gulerod was had once; it is shown with its count.
        compose.onNodeWithText("Gulerod · 1×").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Mormors grød")
        compose.onNodeWithText("Tilføj \"Mormors grød\" som egen").performClick()
        compose.onNodeWithContentDescription("Fjern Gulerod").assertExists()
        compose.onNodeWithText("Gem").performClick()
        assertEquals("Gulerod, Mormors grød", saved?.foodName)
        assertEquals(true, listOpened)
    }

    @Test fun anOldFoodNameIsKeptAsWrittenWhenNotChanged() {
        val old = CareEventEntity(childId = "c", type = CareEventType.SolidFood, startedAt = 1, endedAt = 1, foodName = "gulerod,kartoffel ")
        var saved: CareEventEntity? = null
        compose.setContent { BabyAppTheme { SolidFoodDialog("c", old, {}) { saved = it } } }
        compose.onNodeWithText("Gem").performClick()
        assertEquals("gulerod,kartoffel", saved?.foodName)
    }
}
