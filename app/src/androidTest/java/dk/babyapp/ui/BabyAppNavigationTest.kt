package dk.babyapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.ChildProfile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

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
                preferences = AppPreferences(hasSeenGettingStarted = true),
            )
        }

        composeRule.onAllNodesWithText(context.getString(R.string.nav_today)).filter(isSelectable()).assertCountEquals(1)
        composeRule.onAllNodesWithText("Dagens overblik").assertCountEquals(1)

        listOf(R.string.nav_timeline, R.string.nav_insights, R.string.nav_guide).forEach { navigationLabel ->
            val destination = context.getString(navigationLabel)
            composeRule
                .onAllNodesWithText(destination)
                .filter(hasClickAction())
                .onFirst()
                .performClick()
            composeRule.onAllNodesWithText(destination).filter(hasClickAction()).assertCountEquals(1)
        }

        composeRule
            .onAllNodesWithText(context.getString(R.string.nav_family))
            .filter(hasClickAction())
            .onFirst()
            .performClick()
        composeRule.onAllNodesWithText(context.getString(R.string.child_profiles)).assertCountEquals(1)
    }

    @Test
    fun todayDoesNotRestoreFamilyAfterChildAppearsWhileFamilyIsOpen() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var child by mutableStateOf<ChildProfile?>(null)
        composeRule.setContent {
            BabyAppNavigation(
                profiles = listOfNotNull(child),
                activeChild = child,
                onSelectChild = {},
                onSaveProfile = { _, _ -> },
                onDeleteProfile = {},
                photoFile = { null },
                onPhotoSelected = { "" },
                preferences = AppPreferences(hasSeenGettingStarted = true),
            )
        }

        composeRule.onAllNodesWithText(context.getString(R.string.nav_family)).filter(hasClickAction()).onFirst().performClick()
        composeRule.runOnIdle {
            child = ChildProfile(name = "Freja", birthDate = LocalDate.now(), sex = BiologicalSex.Female)
        }
        composeRule.onAllNodesWithText(context.getString(R.string.nav_today)).filter(hasClickAction()).onFirst().performClick()

        composeRule.onAllNodesWithText("Dagens overblik").assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.child_profiles)).assertCountEquals(0)
    }
    @Test fun previousDayOverviewMatchesJournalDateOverview() {
        val child = ChildProfile(id = "child", name = "Freja")
        val yesterday = LocalDate.now().minusDays(1)
        val at = yesterday.atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val event = dk.babyapp.data.tracking.CareEventEntity(childId = child.id, type = dk.babyapp.data.tracking.CareEventType.Bottle, startedAt = at, endedAt = at, amountConsumedMl = 125)
        composeRule.setContent { BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, careEvents = listOf(event), preferences = AppPreferences(hasSeenGettingStarted = true, dashboardMetrics = listOf(dk.babyapp.data.preferences.DashboardMetric.Feeding))) }
        composeRule.onNodeWithText("0 ml").assertExists()
        composeRule.onNodeWithContentDescription("Forrige dags overblik").performClick()
        composeRule.onNodeWithText("125 ml").assertExists()
        composeRule.onNodeWithContentDescription("Næste dags overblik").performClick()
        composeRule.onNodeWithText("0 ml").assertExists()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onAllNodesWithText(context.getString(R.string.nav_timeline)).filter(hasClickAction()).onFirst().performClick()
        composeRule.onNodeWithText("Vis dagsoversigt").performClick()
        composeRule.onNodeWithText("125 ml").assertExists()
    }

    @Test fun tappingTodayScrollsBackToTop() {
        val child = ChildProfile(id = "child", name = "Freja")
        composeRule.setContent {
            BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {},
                onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" },
                preferences = AppPreferences(hasSeenGettingStarted = true))
        }
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(3)
        composeRule.onNodeWithText("Dagens overblik").assertIsNotDisplayed()
        composeRule.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.nav_today)).filter(isSelectable()).onFirst().performClick()
        composeRule.onNodeWithText("Dagens overblik").assertIsDisplayed()
    }
}
