package dk.babyapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
)

@Composable
fun BabyAppTheme(
    childColorTheme: ColorProfile? = null,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = childColorTheme?.let(::childColors) ?: DefaultColors,
        typography = BabyTypography,
        shapes = BabyShapes,
        content = content,
    )
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
            error = it.tertiary,
            onError = Color(0xFF071315),
            errorContainer = it.tertiary,
            onErrorContainer = Color.White,
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
            error = it.tertiary,
            onError = Color.White,
            errorContainer = it.tertiary,
            onErrorContainer = Color(0xFF151515),
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
