package dk.babyapp.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class BabyAppNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allTopLevelDestinationsAreReachable() {
        composeRule.setContent {
            BabyApp()
        }

        composeRule.onAllNodesWithText("Today").assertCountEquals(2)

        listOf("Timeline", "Insights", "Guide", "Family").forEach { destination ->
            composeRule
                .onAllNodesWithText(destination)
                .filter(hasClickAction())
                .onFirst()
                .performClick()
            composeRule.onAllNodesWithText(destination).assertCountEquals(2)
        }
    }
}
