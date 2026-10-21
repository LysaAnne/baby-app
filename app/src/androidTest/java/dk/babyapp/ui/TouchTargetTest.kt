package dk.babyapp.ui

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.filter
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import dk.babyapp.R
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.ui.navigation.BabyAppNavigation
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Everything people can tap must be at least 48 × 48 dp, also with one hand at night. */
class TouchTargetTest {
    @get:Rule val compose = createComposeRule()

    private fun assertTapTargetsAreLargeEnough(screen: String) {
        val minimum = with(compose.density) { 48.dp.toPx() } - 1f
        // Material widens small buttons' tap area beyond their layout, so the tap area is measured. Elements cut off
        // by the screen edge or a scrolling list are skipped, because only their visible part is measured.
        val visible = compose.onAllNodes(hasClickAction()).fetchSemanticsNodes().filter { node ->
            node.size.height > 0 && node.boundsInRoot.height >= node.size.height - 1 && node.boundsInRoot.width >= node.size.width - 1
        }
        assertTrue("$screen: only ${visible.size} tappable elements measured", visible.size >= 8)
        val tooSmall = visible.filter { node -> node.touchBoundsInRoot.let { it.width < minimum || it.height < minimum } }
        assertTrue("$screen: too small to tap: ${tooSmall.map(::describe)}", tooSmall.isEmpty())
    }

    private fun describe(node: SemanticsNode) = (node.config.getOrNull(SemanticsProperties.Text)?.joinToString()
        ?: node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString() ?: "node ${node.id}") +
        " ${node.touchBoundsInRoot.width.toInt()}×${node.touchBoundsInRoot.height.toInt()} px"

    @Test fun todayAndJournalHaveLargeEnoughTapTargets() {
        val child = ChildProfile(id = "child", name = "Freja")
        val now = System.currentTimeMillis()
        val events = listOf(CareEventEntity(childId = child.id, type = CareEventType.Diaper, startedAt = now, endedAt = now, diaperType = DiaperType.Wet))
        compose.setContent {
            BabyAppNavigation(profiles = listOf(child), activeChild = child, onSelectChild = {}, onSaveProfile = { _, _ -> }, onDeleteProfile = {}, photoFile = { null }, onPhotoSelected = { "" },
                preferences = AppPreferences(hasSeenGettingStarted = true), careEvents = events)
        }
        assertTapTargetsAreLargeEnough("I dag")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onAllNodesWithText(context.getString(R.string.nav_timeline)).filter(isSelectable()).onFirst().performClick()
        compose.waitForIdle()
        assertTapTargetsAreLargeEnough("Journal")
    }
}
