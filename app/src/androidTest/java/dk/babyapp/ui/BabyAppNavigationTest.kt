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

    @Test fun individualActivitiesWorkInQuickAndBothManualMenus() {
        val child = ChildProfile(id = "child", name = "Freja")
        var selected: dk.babyapp.data.tracking.ActivityType? = null
        composeRule.setContent { BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, preferences = AppPreferences(hasSeenGettingStarted = true),
            onStartActivity = { _, type, done -> selected = type; done(true) }, onAddActivity = { _, _, _, type, _ -> selected = type }) }
        val options = listOf("Mavetid" to dk.babyapp.data.tracking.ActivityType.TummyTime, "Bad" to dk.babyapp.data.tracking.ActivityType.Bath, "Leg" to dk.babyapp.data.tracking.ActivityType.Play, "Udetid" to dk.babyapp.data.tracking.ActivityType.OutdoorTime, "Andet" to dk.babyapp.data.tracking.ActivityType.Other)
        options.forEach { (label, type) ->
            composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText(label) and SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button))
            composeRule.onNode(hasText(label) and SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button)).assertIsEnabled().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick) { it() }
            composeRule.runOnIdle { org.junit.Assert.assertEquals(type, selected) }
        }
        repeat(2) { screen ->
            if (screen == 1) {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                composeRule.onAllNodesWithText(context.getString(R.string.nav_timeline)).filter(isSelectable()).onFirst().performClick()
            }
            options.forEach { (label, type) ->
                composeRule.onNodeWithContentDescription("Manuel registrering").performClick()
                composeRule.onNode(hasText(label) and hasAnyAncestor(isDialog())).performScrollTo().performClick()
                composeRule.onNode(hasText(label) and hasAnyAncestor(isDialog())).assertExists()
                composeRule.onNodeWithText("Gem").performClick()
                composeRule.runOnIdle { org.junit.Assert.assertEquals(type, selected) }
            }
        }
    }

    @Test fun nursingTimerShieldToggleUpdatesSavedDefault() {
        val child = ChildProfile(id = "child", name = "Freja")
        val now = System.currentTimeMillis()
        var event by mutableStateOf(dk.babyapp.data.tracking.CareEventEntity(childId = child.id, type = dk.babyapp.data.tracking.CareEventType.Breastfeeding, startedAt = now, runningSince = now, activeSide = dk.babyapp.data.tracking.BreastSide.Left, timerSegments = "$now-", timerSegmentSides = "Left"))
        var savedDefault = false
        composeRule.setContent { BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, careEvents = listOf(event), preferences = AppPreferences(hasSeenGettingStarted = true), onUpdateCareEvent = { updated, done -> event = updated; done(true) }, onUpdateDefaultNippleShield = { savedDefault = it }) }
        composeRule.onNodeWithContentDescription("Udvid tæller").performClick()
        composeRule.onNodeWithContentDescription("Ammebrik").performClick()
        composeRule.runOnIdle { org.junit.Assert.assertTrue(savedDefault); org.junit.Assert.assertEquals("$now-", event.timerSegments) }
        composeRule.onNodeWithContentDescription("Ammebrik").performClick()
        composeRule.runOnIdle { org.junit.Assert.assertFalse(savedDefault) }
    }

    @Test fun measurementChartsOpenFromTodayAndJournal() {
        val child = ChildProfile(id = "child", name = "Freja")
        val event = dk.babyapp.data.tracking.CareEventEntity(childId = child.id, type = dk.babyapp.data.tracking.CareEventType.Measurement, startedAt = System.currentTimeMillis(), endedAt = System.currentTimeMillis(), measurementType = dk.babyapp.data.tracking.MeasurementType.Weight, measurementValue = 4.5, measurementUnit = "kg")
        composeRule.setContent { BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, careEvents = listOf(event), preferences = AppPreferences(hasSeenGettingStarted = true)) }
        composeRule.onNodeWithContentDescription("Ammebrik ved ny amning").assertDoesNotExist()
        composeRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasContentDescription("Vis målegrafer"))
        composeRule.onNodeWithContentDescription("Vis målegrafer").performClick()
        composeRule.onNodeWithText("Målenes udvikling").assertIsDisplayed()
        composeRule.onNodeWithText("Luk").performClick()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onAllNodesWithText(context.getString(R.string.nav_timeline)).filter(isSelectable()).onFirst().performClick()
        composeRule.onNodeWithContentDescription("Vis målegrafer").performClick()
        composeRule.onNodeWithText("Målenes udvikling").assertIsDisplayed()
    }

    @Test fun bookNotificationOpensExactChildAndPage() {
        val child = ChildProfile(id = "target", name = "Freja")
        var selected = ""
        var target by mutableStateOf<dk.babyapp.reminders.BookNotificationTarget?>(null)
        composeRule.setContent { BabyAppNavigation(bookTarget = target, onBookTargetHandled = { target = null }, profiles = listOf(child), activeChild = child,
            onSelectChild = { selected = it }, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, preferences = AppPreferences(hasSeenGettingStarted = true)) }
        composeRule.runOnIdle { target = dk.babyapp.reminders.BookNotificationTarget.from(android.content.Intent().setData(android.net.Uri.parse("babyapp://book/${child.id}/month-2"))) }
        composeRule.onNode(hasText("2 måneder") and hasAnyAncestor(isDialog())).assertIsDisplayed()
        composeRule.onNodeWithText("Gem", useUnmergedTree = true).assertIsDisplayed()
        composeRule.runOnIdle { org.junit.Assert.assertEquals(child.id, selected); org.junit.Assert.assertNull(target) }
    }

    @Test fun journalManualRegistrationButtonIsVisibleAndOpensMenu() {
        val child = ChildProfile(id = "child", name = "Freja")
        composeRule.setContent { BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, preferences = AppPreferences(hasSeenGettingStarted = true)) }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onAllNodesWithText(context.getString(R.string.nav_timeline)).filter(isSelectable()).onFirst().performClick()
        composeRule.onNodeWithContentDescription("Manuel registrering").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Flaske").assertIsDisplayed()
    }

    @Test fun journalInitiallyOpensOnlyNearestDateWithClosedRecords() {
        val child = ChildProfile(id = "child", name = "Freja")
        val today = LocalDate.now().atTime(12, 0).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val recent = dk.babyapp.data.tracking.CareEventEntity(id = "recent", childId = "child", type = dk.babyapp.data.tracking.CareEventType.Bottle, startedAt = today, endedAt = today, amountConsumedMl = 90)
        val older = recent.copy(id = "older", startedAt = today - 86400000, endedAt = today - 86400000, amountConsumedMl = 60)
        composeRule.setContent { BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" }, careEvents = listOf(recent, older), preferences = AppPreferences(hasSeenGettingStarted = true)) }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onAllNodesWithText(context.getString(R.string.nav_timeline)).filter(isSelectable()).onFirst().performClick()
        composeRule.onAllNodesWithContentDescription("Vis detaljer").assertCountEquals(1)
        composeRule.onNodeWithText("Rediger").assertDoesNotExist()
        composeRule.onNodeWithText("Udfold datoer").performClick()
        composeRule.onAllNodesWithContentDescription("Vis detaljer").assertCountEquals(2)
        composeRule.onNodeWithText("Kollaps datoer").performClick()
        composeRule.onAllNodesWithContentDescription("Vis detaljer").assertCountEquals(0)
        composeRule.onNodeWithText("Fjern filtre").assertIsNotEnabled()
    }

    @Test fun twoActiveTimerCardsHaveIndependentControls() {
        val child = ChildProfile(id = "child", name = "Freja")
        val now = System.currentTimeMillis()
        val nursing = dk.babyapp.data.tracking.CareEventEntity(id = "nursing", childId = "child", type = dk.babyapp.data.tracking.CareEventType.Breastfeeding, startedAt = now, activeSide = dk.babyapp.data.tracking.BreastSide.Left)
        val activity = dk.babyapp.data.tracking.CareEventEntity(id = "activity", childId = "child", type = dk.babyapp.data.tracking.CareEventType.Activity, activityType = dk.babyapp.data.tracking.ActivityType.TummyTime, startedAt = now, runningSince = now)
        var toggled = ""
        composeRule.setContent {
            BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" },
                careEvents = listOf(nursing, activity), preferences = AppPreferences(hasSeenGettingStarted = true), onToggleTimer = { toggled = it.id })
        }
        composeRule.onNodeWithContentDescription("Fortsæt").assertIsDisplayed().performClick()
        composeRule.runOnIdle { org.junit.Assert.assertEquals("nursing", toggled) }
        composeRule.onNodeWithContentDescription("Pause").assertIsDisplayed().performClick()
        composeRule.runOnIdle { org.junit.Assert.assertEquals("activity", toggled) }
    }

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

        listOf(R.string.nav_timeline, R.string.nav_guide).forEach { navigationLabel ->
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
        composeRule.onNodeWithText("Opsummer dag").performClick()
        composeRule.onNodeWithText("125 ml spist").assertExists()
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
