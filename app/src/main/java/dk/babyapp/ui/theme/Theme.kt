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
import dk.babyapp.data.profile.ChildColorTheme

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

enum class AppThemeMode {
    System,
    Light,
    Dark,
}

@Composable
fun BabyAppTheme(
    themeMode: AppThemeMode = AppThemeMode.System,
    childColorTheme: ChildColorTheme? = null,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> {
            dynamicDarkColorScheme(context)
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicLightColorScheme(context)
        }
        darkTheme -> childColorTheme?.let(::childDarkColors) ?: DarkColors
        else -> childColorTheme?.let(::childLightColors) ?: LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = BabyTypography,
        shapes = BabyShapes,
        content = content,
    )
}

private fun childLightColors(theme: ChildColorTheme) = when (theme) {
    ChildColorTheme.Sage -> LightColors
    ChildColorTheme.Rose -> lightColorScheme(primary = Color(0xFF85515C), onPrimary = Color.White, primaryContainer = Color(0xFFF5CAD2), onPrimaryContainer = Color(0xFF351017), secondary = Color(0xFF74565D), secondaryContainer = Color(0xFFFFD9DF), tertiary = Color(0xFF7A5733), tertiaryContainer = Color(0xFFFFDDB7), background = Color(0xFFFFF8F7), surface = Color(0xFFFFF8F7))
    ChildColorTheme.Sky -> lightColorScheme(primary = Color(0xFF3E647A), onPrimary = Color.White, primaryContainer = Color(0xFFC9E3F5), onPrimaryContainer = Color(0xFF071E2A), secondary = Color(0xFF50606A), secondaryContainer = Color(0xFFD5E5F0), tertiary = Color(0xFF625A7C), tertiaryContainer = Color(0xFFE8DEFF), background = Color(0xFFF7FAFC), surface = Color(0xFFF7FAFC))
    ChildColorTheme.Lavender -> lightColorScheme(primary = Color(0xFF66558A), onPrimary = Color.White, primaryContainer = Color(0xFFDED0F2), onPrimaryContainer = Color(0xFF21133F), secondary = Color(0xFF625B70), secondaryContainer = Color(0xFFE8DEF0), tertiary = Color(0xFF7E5265), tertiaryContainer = Color(0xFFFFD8E6), background = Color(0xFFFCF8FF), surface = Color(0xFFFCF8FF))
    ChildColorTheme.Sunshine -> lightColorScheme(primary = Color(0xFF705D00), onPrimary = Color.White, primaryContainer = Color(0xFFF5E4A8), onPrimaryContainer = Color(0xFF221B00), secondary = Color(0xFF675F45), secondaryContainer = Color(0xFFEFE4C1), tertiary = Color(0xFF48664A), tertiaryContainer = Color(0xFFCBEBC8), background = Color(0xFFFFFBF0), surface = Color(0xFFFFFBF0))
}

private fun childDarkColors(theme: ChildColorTheme) = when (theme) {
    ChildColorTheme.Sage -> DarkColors
    ChildColorTheme.Rose -> darkColorScheme(primary = Color(0xFFFFB2C0), primaryContainer = Color(0xFF693A46), secondary = Color(0xFFE5BDC4), secondaryContainer = Color(0xFF5A4047), tertiary = Color(0xFFEFBF8F), tertiaryContainer = Color(0xFF60401F), background = Color(0xFF1A1113), surface = Color(0xFF1A1113))
    ChildColorTheme.Sky -> darkColorScheme(primary = Color(0xFF9DCCE8), primaryContainer = Color(0xFF244C61), secondary = Color(0xFFB9C9D4), secondaryContainer = Color(0xFF394A54), tertiary = Color(0xFFCBC1E9), tertiaryContainer = Color(0xFF4A4263), background = Color(0xFF0D1519), surface = Color(0xFF0D1519))
    ChildColorTheme.Lavender -> darkColorScheme(primary = Color(0xFFCDBAF1), primaryContainer = Color(0xFF4E3E71), secondary = Color(0xFFCBC2D7), secondaryContainer = Color(0xFF4A4458), tertiary = Color(0xFFF0B8CE), tertiaryContainer = Color(0xFF633B50), background = Color(0xFF15121A), surface = Color(0xFF15121A))
    ChildColorTheme.Sunshine -> darkColorScheme(primary = Color(0xFFE4C84F), primaryContainer = Color(0xFF544600), secondary = Color(0xFFD3C7A3), secondaryContainer = Color(0xFF4F482F), tertiary = Color(0xFFAFCFAE), tertiaryContainer = Color(0xFF314E34), background = Color(0xFF18160D), surface = Color(0xFF18160D))
}
