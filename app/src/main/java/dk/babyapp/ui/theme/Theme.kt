package dk.babyapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
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

private val DarkColors = darkColorScheme(
    primary = Sage80,
    primaryContainer = Sage40,
    secondary = Blush80,
    secondaryContainer = Blush40,
    tertiary = Sky80,
    tertiaryContainer = Sky40,
    background = WarmSurfaceDark,
    surface = WarmSurfaceDark,
    onBackground = Cream90,
    onSurface = Cream90,
)

private val NightColors = DarkColors.copy(
    background = NightSurface,
    surface = NightSurface,
    surfaceContainer = Color(0xFF101310),
)

enum class AppThemeMode {
    System,
    Light,
    Dark,
    Night,
}

@Composable
fun BabyAppTheme(
    themeMode: AppThemeMode = AppThemeMode.System,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark, AppThemeMode.Night -> true
    }
    val context = LocalContext.current
    val colors = when {
        themeMode == AppThemeMode.Night -> NightColors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(context)
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = BabyTypography,
        shapes = BabyShapes,
        content = content,
    )
}

