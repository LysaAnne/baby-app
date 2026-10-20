package dk.babyapp.ui.theme

import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.profile.ChildColorTheme
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.roundToInt

/** Night mode is on from [NIGHT_STARTS] until [NIGHT_ENDS] when it is set to automatic. */
val NIGHT_STARTS: LocalTime = LocalTime.of(21, 0)
val NIGHT_ENDS: LocalTime = LocalTime.of(6, 0)

fun isNightTime(time: LocalTime): Boolean = !time.isBefore(NIGHT_STARTS) || time.isBefore(NIGHT_ENDS)

/**
 * The colour profile to show. A profile the user made dark is always shown as it is; a light
 * profile gets its dark counterpart in dark mode. Night mode replaces every profile.
 */
fun ColorProfile?.forAppearance(dark: Boolean, night: Boolean): ColorProfile? = when {
    night -> NightColorProfile
    this == null || isDark || !dark -> this
    else -> darkVariant()
}

// Hand-tuned dark palettes for the built-in light profiles that have one.
private val darkCounterparts = mapOf(
    ChildColorTheme.NeutralLight.name to ChildColorTheme.NeutralDark,
    ChildColorTheme.GirlLight.name to ChildColorTheme.GirlDark,
    ChildColorTheme.BoyLight.name to ChildColorTheme.BoyDark,
)

/** A dark profile in the same hues, so the child's colour still shows in dark mode. */
fun ColorProfile.darkVariant(): ColorProfile {
    darkCounterparts[id]?.let { counterpart ->
        val palette = childPaletteDefinition(counterpart)
        return copy(
            isDark = true, background = palette.background.hex(), primary = palette.primary.hex(),
            primaryContainer = palette.primaryContainer.hex(), secondary = palette.secondary.hex(), tertiary = palette.tertiary.hex(),
        )
    }
    val accent = hsl(primary)
    return copy(
        isDark = true,
        background = accent.copy(saturation = accent.saturation.coerceAtMost(0.25f), lightness = 0.08f).hex(),
        primary = accent.copy(saturation = accent.saturation.coerceAtMost(0.55f), lightness = 0.76f).hex(),
        primaryContainer = accent.copy(saturation = accent.saturation.coerceAtMost(0.40f), lightness = 0.24f).hex(),
        secondary = hsl(secondary).let { it.copy(saturation = it.saturation.coerceAtMost(0.45f), lightness = 0.80f) }.hex(),
        tertiary = hsl(tertiary).let { it.copy(saturation = it.saturation.coerceAtMost(0.45f), lightness = 0.82f) }.hex(),
    )
}

/** Warm, dim colours for night feeds: little blue light and no bright surfaces. */
val NightColorProfile = ColorProfile(
    id = "night", name = "Nat", isDark = true,
    background = "#120B08", primary = "#D9A270", primaryContainer = "#3D2618",
    secondary = "#C8A487", tertiary = "#D6B08E",
)

internal data class Hsl(val hue: Float, val saturation: Float, val lightness: Float) {
    fun hex(): String {
        val c = (1 - abs(2 * lightness - 1)) * saturation
        val x = c * (1 - abs((hue / 60f) % 2 - 1))
        val m = lightness - c / 2
        val (r, g, b) = when ((hue / 60f).toInt() % 6) {
            0 -> Triple(c, x, 0f); 1 -> Triple(x, c, 0f); 2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c); 4 -> Triple(x, 0f, c); else -> Triple(c, 0f, x)
        }
        fun channel(v: Float) = ((v + m) * 255).roundToInt().coerceIn(0, 255)
        return "#%02X%02X%02X".format(channel(r), channel(g), channel(b))
    }
}

internal fun hsl(hex: String): Hsl {
    val value = hex.removePrefix("#").toLong(16)
    val r = ((value shr 16) and 0xFF) / 255f
    val g = ((value shr 8) and 0xFF) / 255f
    val b = (value and 0xFF) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val lightness = (max + min) / 2
    val delta = max - min
    if (delta == 0f) return Hsl(0f, 0f, lightness)
    val saturation = delta / (1 - abs(2 * lightness - 1))
    val hue = when (max) {
        r -> 60 * (((g - b) / delta) % 6)
        g -> 60 * ((b - r) / delta + 2)
        else -> 60 * ((r - g) / delta + 4)
    }.let { if (it < 0) it + 360 else it }
    return Hsl(hue, saturation, lightness)
}

private fun Long.hex(): String = "#%06X".format(this and 0xFFFFFF)
