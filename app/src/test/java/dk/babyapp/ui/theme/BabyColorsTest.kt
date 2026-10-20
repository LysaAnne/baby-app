package dk.babyapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import dk.babyapp.data.preferences.QuickActionCategory
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BabyColorsTest {
    private fun contrast(a: Color, b: Color): Double {
        val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (light + 0.05) / (dark + 0.05)
    }

    // Backgrounds of every built-in colour profile and the default theme.
    private val lightBackgrounds = listOf(0xFFFFFFFF, 0xFFFFF8F5, 0xFFE7F5FA, 0xFFEAF1E5, 0xFFEAF7F0, 0xFFEAF7FF, 0xFFFFEAE2, 0xFFFFEBDD, 0xFFFFEFF5, 0xFFFFF2C9, 0xFFFFF3C7, 0xFFFFF6CF).map(::Color)
    private val darkBackgrounds = listOf(0xFF07161E, 0xFF0B1721, 0xFF101713, 0xFF11180F, 0xFF171714, 0xFF181419, 0xFF1A111B, 0xFF1C1116, 0xFF1D1115, 0xFF211017).map(::Color)

    @Test fun everyCategoryHasColoursInBothThemes() {
        listOf(LightBabyColors, DarkBabyColors).forEach { colors ->
            assertEquals(CareCategory.entries.toSet(), colors.categories.keys)
        }
    }

    @Test fun categoryIconsAreReadableOnTheirCircleAndOnTheScreen() {
        // WCAG 2.1 asks for 3:1 for icons and other graphics.
        CareCategory.entries.forEach { category ->
            listOf(LightBabyColors to lightBackgrounds, DarkBabyColors to darkBackgrounds).forEach { (colors, backgrounds) ->
                val c = colors.category(category)
                assertTrue("$category accent on container", contrast(c.accent, c.container) >= 3.0)
                backgrounds.forEach { assertTrue("$category accent on $it", contrast(c.accent, it) >= 3.0) }
            }
        }
    }

    @Test fun errorTextIsReadableInLightAndDark() {
        lightBackgrounds.forEach { assertTrue(contrast(LightError, it) >= 4.5) }
        darkBackgrounds.forEach { assertTrue(contrast(DarkError, it) >= 4.5) }
    }

    @Test fun registrationsAndTodayCategoriesShareColours() {
        val event = CareEventEntity(childId = "child", type = CareEventType.Bottle, startedAt = 0)
        assertEquals(CareCategory.Feeding, event.careCategory)
        assertEquals(CareCategory.Sleep, event.copy(type = CareEventType.HeadSide).careCategory)
        assertEquals(CareCategory.Health, event.copy(type = CareEventType.Activity, activityType = ActivityType.Medicine).careCategory)
        assertEquals(CareCategory.Misc, event.copy(type = CareEventType.Activity, activityType = ActivityType.Bath).careCategory)
        assertEquals(QuickActionCategory.entries.size, QuickActionCategory.entries.map { it.careCategory }.distinct().size)
    }
}
