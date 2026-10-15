package dk.babyapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import dk.babyapp.data.preferences.*
import dk.babyapp.data.tracking.*
import dk.babyapp.domain.*
import dk.babyapp.ui.tracking.*
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class HeadSideTrackingTest {
    @get:Rule val compose = createComposeRule()

    @Test fun quickButtonsAddOneRecordPerTapAndDashboardShowsBothSides() {
        var records by mutableStateOf(emptyList<CareEventEntity>())
        compose.setContent { MaterialTheme { Column {
            HeadSideQuickButtons(records, "child") { records = records + it }
            DailyOverview(eventsForDay(records, LocalDate.now()), listOf(DashboardMetric.HeadSide), LocalDate.now())
        } } }
        compose.onNodeWithContentDescription("Registrer hovedside venstre").performClick().performClick()
        compose.onNodeWithContentDescription("Registrer hovedside højre").performClick()
        compose.onNodeWithText("Venstre 2 · Højre 1").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(3, records.size)
            assertTrue(records.all { it.endedAt == it.startedAt && it.runningSince == null && it.type == CareEventType.HeadSide })
        }
    }

    @Test fun statisticsSplitAtCorrectionAndRememberPeriodSelection() {
        val today = LocalDate.now()
        fun at(day: LocalDate) = day.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val records = listOf(headSideObservation("child", HeadSide.Left, at(today.minusDays(1))), headSideObservation("child", HeadSide.Right, at(today)))
        var settings by mutableStateOf(HeadSideStatisticsSettings("Week", correctionDates = mapOf("child" to today.toString())))
        compose.setContent { MaterialTheme { HeadSideStatisticsDialog("child", records, settings, { settings = it }, {}) } }
        compose.onNodeWithText("Venstre: 1 · 50,0 %").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Højre: 1 · 100,0 %"))
        compose.onNodeWithText("Højre: 1 · 100,0 %").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        compose.onNodeWithText("1D").performClick()
        compose.runOnIdle { assertEquals("Day", settings.period); assertEquals(today.toString(), settings.correctionDates["child"]) }
        compose.onNode(hasText("Venstre: 0 · 0,0 %") and hasParent(hasAnyChild(hasText("Favoritside · hele perioden")))).assertIsDisplayed()
    }

    @Test fun journalEditorChangesSideWithoutCreatingTimer() {
        val original = headSideObservation("child", HeadSide.Left)
        var saved: CareEventEntity? = null
        compose.setContent { MaterialTheme { EditEventDialog(original, {}, { saved = it }) } }
        compose.onNodeWithText("Venstre").performClick()
        compose.onNodeWithText("Højre").performClick()
        compose.onNodeWithText("Gem").performClick()
        compose.runOnIdle { assertEquals(original.id, saved?.id); assertEquals(HeadSide.Right, saved?.headSide); assertEquals(saved?.startedAt, saved?.endedAt); assertNull(saved?.runningSince) }
    }
}
