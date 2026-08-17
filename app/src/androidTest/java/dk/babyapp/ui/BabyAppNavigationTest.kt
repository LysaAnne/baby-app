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
import dk.babyapp.ui.navigation.BabyAppNavigation
import androidx.test.platform.app.InstrumentationRegistry
import dk.babyapp.R

class BabyAppNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allTopLevelDestinationsAreReachable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            BabyAppNavigation(
                profiles = emptyList(),
                activeChild = null,
                onSelectChild = {},
                onSaveProfile = { _, _ -> },
                onDeleteProfile = {},
                photoFile = { null },
                onPhotoSelected = { "" },
            )
        }

        composeRule.onAllNodesWithText(context.getString(R.string.nav_today)).assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.no_child_registered)).assertCountEquals(1)

        listOf(R.string.nav_timeline, R.string.nav_insights, R.string.nav_guide).forEach { navigationLabel ->
            val destination = context.getString(navigationLabel)
            composeRule
                .onAllNodesWithText(destination)
                .filter(hasClickAction())
                .onFirst()
                .performClick()
            composeRule.onAllNodesWithText(destination).assertCountEquals(2)
        }

        composeRule
            .onAllNodesWithText(context.getString(R.string.nav_family))
            .filter(hasClickAction())
            .onFirst()
            .performClick()
        composeRule.onAllNodesWithText(context.getString(R.string.child_profiles)).assertCountEquals(1)
    }
}
