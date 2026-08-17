package dk.babyapp.ui.theme

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.ChildThemeAppearance

/**
 * REDIGER BARNETS FARVEPROFILER HER.
 *
 * Find først profilens blok, f.eks. ChildColorTheme.NeutralLight. Udskift kun
 * HEX-koden ud for den rolle, du vil ændre. Behold kommaet efter koden.
 *
 * Brug 0xFF efterfulgt af den normale farvekode uden #.
 * Eksempel: #A8CDB5 skrives som 0xFFA8CDB5.
 *
 * Gem filen, kør appen igen, og åbn:
 * Indstillinger > Udviklerværktøjer > Vis farveprofiler.
 */
data class ChildPaletteDefinition(
    val background: Long,
    val primary: Long,
    val primaryContainer: Long,
    val secondary: Long,
    val tertiary: Long,
)

data class ChildPaletteColors(
    val background: Color,
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
)

fun ColorProfile.colors() = ChildPaletteColors(
    background = Color(AndroidColor.parseColor(background)),
    primary = Color(AndroidColor.parseColor(primary)),
    primaryContainer = Color(AndroidColor.parseColor(primaryContainer)),
    secondary = Color(AndroidColor.parseColor(secondary)),
    secondaryContainer = Color(AndroidColor.parseColor(secondary)),
    tertiary = Color(AndroidColor.parseColor(tertiary)),
    tertiaryContainer = Color(AndroidColor.parseColor(tertiary)),
)

private val palettes = mapOf(
    ChildColorTheme.NeutralLight to ChildPaletteDefinition(
        background = 0xFFFFFFFF, // hele appens baggrund
        primary = 0xFF5A8D6F, // primære knapper og aktive ikoner
        primaryContainer = 0xFFDBE8E1, // fremhævede kort og valgte menupunkter
        secondary = 0xFFA4D6BA, // sekundær accentfarve
        tertiary = 0xFF45616A, // tredje accentfarve
    ),
    ChildColorTheme.NeutralDark to ChildPaletteDefinition(
        background = 0xFF101713, // hele appens baggrund
        primary = 0xFFA9CDB6, // primære knapper og aktive ikoner
        primaryContainer = 0xFF29483A, // fremhævede kort og valgte menupunkter
        secondary = 0xFFCADCDC, // sekundær accentfarve
        tertiary = 0xFFD7DCCB, // tredje accentfarve
    ),
    ChildColorTheme.BoyLight to ChildPaletteDefinition(
        background = 0xFFEAF7FF, // hele appens baggrund
        primary = 0xFF356684, // primære knapper og aktive ikoner
        primaryContainer = 0xFFAEDDF5, // fremhævede kort og valgte menupunkter
        secondary = 0xFF53627D, // sekundær accentfarve
        tertiary = 0xFF526B65, // tredje accentfarve
    ),
    ChildColorTheme.BoyDark to ChildPaletteDefinition(
        background = 0xFF0B1721, // hele appens baggrund
        primary = 0xFF8ECDF1, // primære knapper og aktive ikoner
        primaryContainer = 0xFF1D4968, // fremhævede kort og valgte menupunkter
        secondary = 0xFFC3E3E4, // sekundær accentfarve
        tertiary = 0xFFD0D9F1, // tredje accentfarve
    ),
    ChildColorTheme.GirlLight to ChildPaletteDefinition(
        background = 0xFFFFEFF5, // hele appens baggrund
        primary = 0xFF8B4C65, // primære knapper og aktive ikoner
        primaryContainer = 0xFFF7C4D6, // fremhævede kort og valgte menupunkter
        secondary = 0xFF795563, // sekundær accentfarve
        tertiary = 0xFF765B47, // tredje accentfarve
    ),
    ChildColorTheme.GirlDark to ChildPaletteDefinition(
        background = 0xFF211017, // hele appens baggrund
        primary = 0xFFF0AAC3, // primære knapper og aktive ikoner
        primaryContainer = 0xFF672C49, // fremhævede kort og valgte menupunkter
        secondary = 0xFFECD7CA, // sekundær accentfarve
        tertiary = 0xFFF0D0D9, // tredje accentfarve
    ),
    ChildColorTheme.PastelYellow to ChildPaletteDefinition(
        background = 0xFFFFF6CF, // hele appens baggrund
        primary = 0xFF6E5B00, // primære knapper og aktive ikoner
        primaryContainer = 0xFFFFDF75, // fremhævede kort og valgte menupunkter
        secondary = 0xFF665E42, // sekundær accentfarve
        tertiary = 0xFF526746, // tredje accentfarve
    ),
    ChildColorTheme.NeonNight to ChildPaletteDefinition(
        background = 0xFF07161E, // hele appens baggrund
        primary = 0xFF7DD3F0, // primære knapper og aktive ikoner
        primaryContainer = 0xFF00445E, // fremhævede kort og valgte menupunkter
        secondary = 0xFFFFD8E7, // sekundær accentfarve
        tertiary = 0xFFFFD9DC, // tredje accentfarve
    ),
    ChildColorTheme.ButtercupSky to ChildPaletteDefinition(
        background = 0xFFFFF3C7, // hele appens baggrund
        primary = 0xFF42667B, // primære knapper og aktive ikoner
        primaryContainer = 0xFFB8DDF2, // fremhævede kort og valgte menupunkter
        secondary = 0xFF796000, // sekundær accentfarve
        tertiary = 0xFF5D6750, // tredje accentfarve
    ),
    ChildColorTheme.SunsetCoast to ChildPaletteDefinition(
        background = 0xFFFFEBDD, // hele appens baggrund
        primary = 0xFF006A68, // primære knapper og aktive ikoner
        primaryContainer = 0xFF83E8DF, // fremhævede kort og valgte menupunkter
        secondary = 0xFF8B3C56, // sekundær accentfarve
        tertiary = 0xFF765B00, // tredje accentfarve
    ),
    ChildColorTheme.NeonGrove to ChildPaletteDefinition(
        background = 0xFF11180F, // hele appens baggrund
        primary = 0xFF9DDB8D, // primære knapper og aktive ikoner
        primaryContainer = 0xFF19510F, // fremhævede kort og valgte menupunkter
        secondary = 0xFFD7E8CE, // sekundær accentfarve
        tertiary = 0xFFCDE7EF, // tredje accentfarve
    ),
    ChildColorTheme.PlumMist to ChildPaletteDefinition(
        background = 0xFF181419, // hele appens baggrund
        primary = 0xFFD0C0D8, // primære knapper og aktive ikoner
        primaryContainer = 0xFF493D51, // fremhævede kort og valgte menupunkter
        secondary = 0xFFF1DDE8, // sekundær accentfarve
        tertiary = 0xFFEFE2D8, // tredje accentfarve
    ),
    ChildColorTheme.PeachTwilight to ChildPaletteDefinition(
        background = 0xFF1C1116, // hele appens baggrund
        primary = 0xFFFFB0C9, // primære knapper og aktive ikoner
        primaryContainer = 0xFF61213E, // fremhævede kort og valgte menupunkter
        secondary = 0xFFFFDBCA, // sekundær accentfarve
        tertiary = 0xFFE8DFFF, // tredje accentfarve
    ),
    ChildColorTheme.SunlitMeadow to ChildPaletteDefinition(
        background = 0xFFFFF2C9, // hele appens baggrund
        primary = 0xFF536621, // primære knapper og aktive ikoner
        primaryContainer = 0xFFCDE77E, // fremhævede kort og valgte menupunkter
        secondary = 0xFF765B00, // sekundær accentfarve
        tertiary = 0xFF8B4F36, // tredje accentfarve
    ),
    ChildColorTheme.BerryPop to ChildPaletteDefinition(
        background = 0xFF1A111B, // hele appens baggrund
        primary = 0xFFE5B4F5, // primære knapper og aktive ikoner
        primaryContainer = 0xFF3C0755, // fremhævede kort og valgte menupunkter
        secondary = 0xFFFFD9DD, // sekundær accentfarve
        tertiary = 0xFFFFE07B, // tredje accentfarve
    ),
    ChildColorTheme.OliveStone to ChildPaletteDefinition(
        background = 0xFF171714, // hele appens baggrund
        primary = 0xFFCBC6B8, // primære knapper og aktive ikoner
        primaryContainer = 0xFF47443A, // fremhævede kort og valgte menupunkter
        secondary = 0xFFD8E7D7, // sekundær accentfarve
        tertiary = 0xFFF1DEC4, // tredje accentfarve
    ),
    ChildColorTheme.DesertBloom to ChildPaletteDefinition(
        background = 0xFFFFEBDD, // hele appens baggrund
        primary = 0xFF8A4A32, // primære knapper og aktive ikoner
        primaryContainer = 0xFFF6C2AE, // fremhævede kort og valgte menupunkter
        secondary = 0xFF71614B, // sekundær accentfarve
        tertiary = 0xFF52676B, // tredje accentfarve
    ),
    ChildColorTheme.GlacierBlue to ChildPaletteDefinition(
        background = 0xFFE7F5FA, // hele appens baggrund
        primary = 0xFF3B6678, // primære knapper og aktive ikoner
        primaryContainer = 0xFFAEDCEC, // fremhævede kort og valgte menupunkter
        secondary = 0xFF52636D, // sekundær accentfarve
        tertiary = 0xFF665C77, // tredje accentfarve
    ),
    ChildColorTheme.NordicForest to ChildPaletteDefinition(
        background = 0xFFEAF1E5, // hele appens baggrund
        primary = 0xFF53604F, // primære knapper og aktive ikoner
        primaryContainer = 0xFFC5D8BE, // fremhævede kort og valgte menupunkter
        secondary = 0xFF50666B, // sekundær accentfarve
        tertiary = 0xFF746B56, // tredje accentfarve
    ),
    ChildColorTheme.MintBlush to ChildPaletteDefinition(
        background = 0xFFEAF7F0, // hele appens baggrund
        primary = 0xFF437063, // primære knapper og aktive ikoner
        primaryContainer = 0xFFA9E4D2, // fremhævede kort og valgte menupunkter
        secondary = 0xFF895463, // sekundær accentfarve
        tertiary = 0xFF6D5D47, // tredje accentfarve
    ),
    ChildColorTheme.BerryRose to ChildPaletteDefinition(
        background = 0xFF1D1115, // hele appens baggrund
        primary = 0xFFFFB0C7, // primære knapper og aktive ikoner
        primaryContainer = 0xFF6C0A37, // fremhævede kort og valgte menupunkter
        secondary = 0xFFFFD9E0, // sekundær accentfarve
        tertiary = 0xFFFFE17B, // tredje accentfarve
    ),
    ChildColorTheme.PeachCream to ChildPaletteDefinition(
        background = 0xFFFFEAE2, // hele appens baggrund
        primary = 0xFF915040, // primære knapper og aktive ikoner
        primaryContainer = 0xFFFFC2B3, // fremhævede kort og valgte menupunkter
        secondary = 0xFF765B52, // sekundær accentfarve
        tertiary = 0xFF6D6046, // tredje accentfarve
    ),
)

fun childPaletteDefinition(theme: ChildColorTheme): ChildPaletteDefinition = when (theme) {
    ChildColorTheme.Sage -> palettes.getValue(ChildColorTheme.NeutralLight)
    ChildColorTheme.Rose -> palettes.getValue(ChildColorTheme.GirlLight)
    ChildColorTheme.Sky -> palettes.getValue(ChildColorTheme.BoyLight)
    ChildColorTheme.Lavender -> palettes.getValue(ChildColorTheme.PlumMist)
    ChildColorTheme.Sunshine -> palettes.getValue(ChildColorTheme.PastelYellow)
    else -> palettes.getValue(theme)
}

private val profileNames = mapOf(
    ChildColorTheme.NeutralLight to "Neutral lys",
    ChildColorTheme.NeutralDark to "Neutral mørk",
    ChildColorTheme.BoyLight to "Dreng lys",
    ChildColorTheme.BoyDark to "Dreng mørk",
    ChildColorTheme.GirlLight to "Pige lys",
    ChildColorTheme.GirlDark to "Pige mørk",
    ChildColorTheme.PastelYellow to "Pastelgul",
    ChildColorTheme.NeonNight to "Neonnat",
    ChildColorTheme.ButtercupSky to "Smørblomst",
    ChildColorTheme.SunsetCoast to "Solnedgang",
    ChildColorTheme.NeonGrove to "Neonlund",
    ChildColorTheme.PlumMist to "Blommedis",
    ChildColorTheme.PeachTwilight to "Ferskenskumring",
    ChildColorTheme.SunlitMeadow to "Soleng",
    ChildColorTheme.BerryPop to "Bærpop",
    ChildColorTheme.OliveStone to "Olivesten",
    ChildColorTheme.DesertBloom to "Ørkenblomst",
    ChildColorTheme.GlacierBlue to "Gletsjerblå",
    ChildColorTheme.NordicForest to "Nordisk skov",
    ChildColorTheme.MintBlush to "Mynterosa",
    ChildColorTheme.BerryRose to "Bærrose",
    ChildColorTheme.PeachCream to "Ferskencreme",
)

private fun defaultProfile(theme: ChildColorTheme, name: String): ColorProfile {
    val definition = childPaletteDefinition(theme)
    return ColorProfile(
        id = theme.name,
        name = name,
        isDark = theme.appearance == ChildThemeAppearance.Dark,
        background = definition.background.toHex(),
        primary = definition.primary.toHex(),
        primaryContainer = definition.primaryContainer.toHex(),
        secondary = definition.secondary.toHex(),
        tertiary = definition.tertiary.toHex(),
        builtIn = true,
    )
}

val defaultColorProfiles: List<ColorProfile> = listOf(
    defaultProfile(ChildColorTheme.NeutralLight, "Salvie"),
    ColorProfile(
        id = ChildColorTheme.PastelYellow.name, name = "Solgul", isDark = false,
        background = "#FFFFFF", primary = "#9A7B32", primaryContainer = "#F3E9C9",
        secondary = "#E7CD76", tertiary = "#6A6045", builtIn = true,
    ),
    ColorProfile(
        id = ChildColorTheme.GirlLight.name, name = "Lyserød", isDark = false,
        background = "#FFFFFF", primary = "#A56B7F", primaryContainer = "#F0DFE5",
        secondary = "#DDA9BB", tertiary = "#66505A", builtIn = true,
    ),
    ColorProfile(
        id = ChildColorTheme.BoyLight.name, name = "Lyseblå", isDark = false,
        background = "#FFFFFF", primary = "#5B819C", primaryContainer = "#DDEAF1",
        secondary = "#A9D1E5", tertiary = "#465F70", builtIn = true,
    ),
)

private fun Long.toHex(): String = "#%06X".format(this and 0xFFFFFF)
