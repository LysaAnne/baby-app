package dk.babyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import dk.babyapp.data.color.ColorProfile

private val DefaultColors = lightColorScheme(
    primary = Sage40,
    onPrimary = Color.White,
    primaryContainer = Sage80,
    onPrimaryContainer = Color(0xFF102019),
    secondary = Blush40,
    secondaryContainer = Blush80,
    tertiary = Sky40,
    tertiaryContainer = Sky80,
    background = WarmSurface,
    surface = WarmSurface,
    error = LightError,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DefaultDarkColors = darkColorScheme(
    primary = Sage80,
    onPrimary = Color(0xFF0F2018),
    primaryContainer = Color(0xFF29483A),
    onPrimaryContainer = Color.White,
    secondary = Blush80,
    tertiary = Sky80,
    background = WarmSurfaceDark,
    surface = WarmSurfaceDark,
    error = DarkError,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

// Night: warm and dim. Text is softened instead of pure white so the screen stays gentle in a dark room.
private val NightText = Color(0xFFE3D2C2)
private val NightColors = darkColorScheme(
    primary = Color(0xFFD9A270),
    onPrimary = Color(0xFF2A1508),
    primaryContainer = Color(0xFF3D2618),
    onPrimaryContainer = NightText,
    secondary = Color(0xFFC8A487),
    onSecondary = Color(0xFF2A1508),
    secondaryContainer = Color(0xFF33231A),
    onSecondaryContainer = NightText,
    tertiary = Color(0xFFD6B08E),
    onTertiary = Color(0xFF2A1508),
    tertiaryContainer = Color(0xFF3A2A1E),
    onTertiaryContainer = NightText,
    error = Color(0xFFF2958C),
    onError = Color(0xFF3B0A06),
    errorContainer = Color(0xFF5C1A14),
    onErrorContainer = NightText,
    background = Color(0xFF120B08),
    onBackground = NightText,
    surface = Color(0xFF120B08),
    onSurface = NightText,
    surfaceVariant = Color(0xFF2A1D16),
    onSurfaceVariant = Color(0xFFBFA894),
    outline = Color(0xFF7A6352),
    outlineVariant = Color(0xFF4A3628),
    surfaceContainerLowest = Color(0xFF0D0805),
    surfaceContainerLow = Color(0xFF170F0B),
    surfaceContainer = Color(0xFF1C130E),
    surfaceContainerHigh = Color(0xFF241911),
    surfaceContainerHighest = Color(0xFF2C1F16),
    inverseSurface = NightText,
    inverseOnSurface = Color(0xFF120B08),
)

/**
 * [darkTheme] shows light colour profiles in their dark counterpart; [night] replaces every profile
 * with the warm night colours. Dark profiles the user made are always shown as they are.
 */
@Composable
fun BabyAppTheme(
    childColorTheme: ColorProfile? = null,
    darkTheme: Boolean = false,
    night: Boolean = false,
    content: @Composable () -> Unit,
) {
    val profile = childColorTheme.forAppearance(darkTheme, night)
    val babyColors = when {
        night -> NightBabyColors
        profile?.isDark == true || (profile == null && darkTheme) -> DarkBabyColors
        else -> LightBabyColors
    }
    CompositionLocalProvider(LocalBabyColors provides babyColors) {
        MaterialTheme(
            colorScheme = when {
                night -> NightColors
                profile != null -> childColors(profile)
                darkTheme -> DefaultDarkColors
                else -> DefaultColors
            },
            typography = BabyTypography,
            shapes = BabyShapes,
            content = content,
        )
    }
}

fun childThemeSwatches(theme: ColorProfile): List<Color> = theme.colors().let {
    listOf(it.primary, it.primaryContainer, it.secondary, it.tertiary)
}

private fun childColors(theme: ColorProfile) = theme.colors().let {
    if (theme.isDark) {
        darkColorScheme(
            primary = it.primary,
            onPrimary = Color(0xFF071315),
            primaryContainer = it.primaryContainer,
            onPrimaryContainer = Color.White,
            secondary = it.secondary,
            onSecondary = Color(0xFF201218),
            secondaryContainer = it.secondaryContainer,
            onSecondaryContainer = Color.White,
            tertiary = it.tertiary,
            onTertiary = Color(0xFF1F1605),
            tertiaryContainer = it.tertiaryContainer,
            onTertiaryContainer = Color.White,
            error = DarkError,
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            background = it.background,
            onBackground = Color.White,
            surface = it.background,
            onSurface = Color.White,
            surfaceVariant = it.primaryContainer,
            onSurfaceVariant = Color.White,
            outline = it.secondary,
            outlineVariant = it.tertiary,
            scrim = Color.Black,
            inverseSurface = it.primaryContainer,
            inverseOnSurface = Color.White,
            inversePrimary = it.primary,
            surfaceTint = it.primary,
            surfaceBright = it.background,
            surfaceDim = it.background,
            surfaceContainer = it.background,
            surfaceContainerHigh = it.primaryContainer,
            surfaceContainerHighest = it.primaryContainer,
            surfaceContainerLow = it.background,
            surfaceContainerLowest = it.background,
        )
    } else {
        lightColorScheme(
            primary = it.primary,
            onPrimary = Color.White,
            primaryContainer = it.primaryContainer,
            onPrimaryContainer = Color(0xFF151515),
            secondary = it.secondary,
            onSecondary = Color.White,
            secondaryContainer = it.secondaryContainer,
            onSecondaryContainer = Color(0xFF151515),
            tertiary = it.tertiary,
            onTertiary = Color.White,
            tertiaryContainer = it.tertiaryContainer,
            onTertiaryContainer = Color(0xFF151515),
            error = LightError,
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
            background = it.background,
            onBackground = Color(0xFF151515),
            surface = it.background,
            onSurface = Color(0xFF151515),
            surfaceVariant = it.primaryContainer,
            onSurfaceVariant = Color(0xFF151515),
            outline = it.secondary,
            outlineVariant = it.tertiary,
            scrim = Color.Black,
            inverseSurface = it.primary,
            inverseOnSurface = Color.White,
            inversePrimary = it.primary,
            surfaceTint = it.primary,
            surfaceBright = it.background,
            surfaceDim = it.background,
            surfaceContainer = it.background,
            surfaceContainerHigh = it.primaryContainer,
            surfaceContainerHighest = it.primaryContainer,
            surfaceContainerLow = it.background,
            surfaceContainerLowest = it.background,
        )
    }
}
