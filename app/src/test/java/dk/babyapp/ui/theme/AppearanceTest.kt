package dk.babyapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import dk.babyapp.data.color.ColorProfile
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AppearanceTest {
    private fun color(hex: String) = Color(0xFF000000 or hex.removePrefix("#").toLong(16))
    private fun contrast(a: Color, b: Color): Double {
        val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (light + 0.05) / (dark + 0.05)
    }

    private val custom = ColorProfile(id = "custom", name = "Min", isDark = false, background = "#FFFFFF", primary = "#2E7D32", primaryContainer = "#C8E6C9", secondary = "#FFB74D", tertiary = "#5C6BC0")

    @Test fun automaticNightRunsFromNineInTheEveningToSixInTheMorning() {
        assertFalse(isNightTime(LocalTime.of(20, 59)))
        assertTrue(isNightTime(LocalTime.of(21, 0)))
        assertTrue(isNightTime(LocalTime.of(3, 0)))
        assertTrue(isNightTime(LocalTime.of(5, 59)))
        assertFalse(isNightTime(LocalTime.of(6, 0)))
        assertFalse(isNightTime(LocalTime.NOON))
    }

    @Test fun profilesFollowLightDarkAndNight() {
        val light = defaultColorProfiles.first()
        val userDark = custom.copy(id = "dark", isDark = true, background = "#101010")
        assertSame(light, light.forAppearance(dark = false, night = false))
        assertTrue(light.forAppearance(dark = true, night = false)!!.isDark)
        assertSame(userDark, userDark.forAppearance(dark = false, night = false))
        assertSame(NightColorProfile, light.forAppearance(dark = false, night = true))
        assertSame(NightColorProfile, (null as ColorProfile?).forAppearance(dark = false, night = true))
        // The dark variant keeps the profile's identity, so its id and name are unchanged.
        assertEquals(light.id, light.darkVariant().id)
    }

    @Test fun darkVariantsAreDarkAndReadable() {
        (defaultColorProfiles + custom).forEach { profile ->
            val dark = profile.darkVariant()
            val background = color(dark.background)
            assertTrue("${profile.name} background", background.luminance() < 0.05)
            assertTrue("${profile.name} primary", contrast(color(dark.primary), background) >= 4.5)
            assertTrue("${profile.name} white text on container", contrast(Color.White, color(dark.primaryContainer)) >= 4.5)
        }
    }

    @Test fun derivedDarkVariantKeepsTheHue() {
        val dark = custom.darkVariant()
        assertEquals(hsl(custom.primary).hue, hsl(dark.primary).hue, 3f)
    }

    @Test fun fieldsAndTextStayReadableInDarkAndNight() {
        val darkBackgrounds = listOf("#07161E", "#101713", "#211017", "#0B1721", "#120B08").map(::color)
        darkBackgrounds.forEach { background ->
            val field = DarkBabyColors.fieldContainer.compositeOver(background)
            assertTrue(contrast(DarkBabyColors.fieldText, field) >= 7.0)
            assertTrue(contrast(DarkBabyColors.fieldLabel, field) >= 4.5)
        }
        val night = color(NightColorProfile.background)
        assertTrue(contrast(NightBabyColors.fieldText, NightBabyColors.fieldContainer) >= 7.0)
        assertTrue(contrast(NightBabyColors.fieldLabel, NightBabyColors.fieldContainer) >= 4.5)
        assertTrue(contrast(color(NightColorProfile.primary), night) >= 4.5)
        CareCategory.entries.forEach { assertTrue(contrast(NightBabyColors.category(it).accent, night) >= 3.0) }
    }

    @Test fun hslRoundTrips() {
        listOf("#5A8D6F", "#A56B7F", "#FFFFFF", "#000000", "#2E7D32").forEach { hex ->
            val back = hsl(hex).hex()
            listOf(1, 3, 5).forEach { i -> assertEquals(hex, hex.substring(i, i + 2).toInt(16).toFloat(), back.substring(i, i + 2).toInt(16).toFloat(), 1f) }
        }
    }
}
