package dk.babyapp.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BabyChangingStation
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dk.babyapp.data.preferences.QuickActionCategory
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType

/** The kinds of registration the app colours and icons consistently everywhere. */
enum class CareCategory(val icon: ImageVector) {
    Feeding(Icons.Outlined.Restaurant),
    Sleep(Icons.Outlined.Bedtime),
    Diaper(Icons.Outlined.BabyChangingStation),
    Measurement(Icons.Outlined.Straighten),
    Health(Icons.Outlined.HealthAndSafety),
    SolidFood(Icons.Outlined.Restaurant),
    Misc(Icons.Outlined.ChildCare),
}

val CareEventEntity.careCategory: CareCategory
    get() = when (type) {
        CareEventType.Breastfeeding, CareEventType.Bottle, CareEventType.Pumping -> CareCategory.Feeding
        CareEventType.Sleep, CareEventType.HeadSide -> CareCategory.Sleep
        CareEventType.Diaper -> CareCategory.Diaper
        CareEventType.Measurement -> CareCategory.Measurement
        CareEventType.HealthVisit, CareEventType.Vaccination -> CareCategory.Health
        CareEventType.SolidFood -> CareCategory.SolidFood
        CareEventType.Activity -> if (activityType == ActivityType.Medicine) CareCategory.Health else CareCategory.Misc
    }

val QuickActionCategory.careCategory: CareCategory
    get() = when (this) {
        QuickActionCategory.Feeding -> CareCategory.Feeding
        QuickActionCategory.Sleep -> CareCategory.Sleep
        QuickActionCategory.Diaper -> CareCategory.Diaper
        QuickActionCategory.Measurements -> CareCategory.Measurement
        QuickActionCategory.Health -> CareCategory.Health
        QuickActionCategory.SolidFood -> CareCategory.SolidFood
        QuickActionCategory.Misc -> CareCategory.Misc
    }

/** A category colour: [accent] for icons and lines, [container] behind them. */
@Immutable
data class CategoryColors(val accent: Color, val container: Color)

/**
 * Colours the Material colour scheme has no slot for. Read them with `MaterialTheme.babyColors`
 * instead of writing colour codes in screens.
 */
@Immutable
data class BabyColors(
    val categories: Map<CareCategory, CategoryColors>,
    /** Form fields: light with dark text in light themes, a faint lift of the background in dark themes. */
    val fieldContainer: Color,
    val fieldText: Color,
    val fieldLabel: Color,
    val fieldLabelFocused: Color,
    val fieldIcon: Color,
    val fieldPlaceholder: Color,
    /** A section the user has hidden, e.g. a quick-registration category. */
    val hiddenContainer: Color,
    val hiddenContent: Color,
    /** Temperature chart. */
    val temperatureNormalBand: Color,
    val temperatureLow: Color,
    val temperatureNormal: Color,
    val temperatureElevated: Color,
    val temperatureFever: Color,
) {
    fun category(category: CareCategory): CategoryColors = categories.getValue(category)
}

private val sharedColors = BabyColors(
    categories = emptyMap(),
    fieldContainer = Color.White,
    fieldText = Color(0xFF202520),
    fieldLabel = Color(0xFF4A514C),
    fieldLabelFocused = Color(0xFF364B3D),
    fieldIcon = Color.DarkGray,
    fieldPlaceholder = Color.Gray,
    hiddenContainer = Color(0xFFE1E1E1),
    hiddenContent = Color(0xFF737373),
    temperatureNormalBand = Color(0xFF66A780),
    temperatureLow = Color(0xFF4380B1),
    temperatureNormal = Color(0xFF4E8C65),
    temperatureElevated = Color(0xFFB47B24),
    temperatureFever = Color(0xFFB54B4B),
)

internal val LightBabyColors = sharedColors.copy(
    categories = mapOf(
        CareCategory.Feeding to CategoryColors(Color(0xFFA85F24), Color(0xFFFBE3D0)),
        CareCategory.Sleep to CategoryColors(Color(0xFF4F59A0), Color(0xFFE1E3F7)),
        CareCategory.Diaper to CategoryColors(Color(0xFF237571), Color(0xFFD3EFEC)),
        CareCategory.Measurement to CategoryColors(Color(0xFF316C9A), Color(0xFFD8E8F5)),
        CareCategory.Health to CategoryColors(Color(0xFFA6405A), Color(0xFFF7DCE3)),
        CareCategory.SolidFood to CategoryColors(Color(0xFF52782C), Color(0xFFE2EFD3)),
        CareCategory.Misc to CategoryColors(Color(0xFF7A4F9C), Color(0xFFEADDF5)),
    ),
)

internal val DarkBabyColors = sharedColors.copy(
    fieldContainer = Color(0x14FFFFFF),
    fieldText = Color(0xFFECECEC),
    fieldLabel = Color(0xFFC4C4C4),
    fieldLabelFocused = Color(0xFFFFFFFF),
    fieldIcon = Color(0xFFC4C4C4),
    fieldPlaceholder = Color(0xFF9A9A9A),
    hiddenContainer = Color(0xFF2B2B2B),
    hiddenContent = Color(0xFF9A9A9A),
    temperatureNormalBand = Color(0xFF66A780),
    temperatureLow = Color(0xFF90C2EC),
    temperatureNormal = Color(0xFF9BD1AE),
    temperatureElevated = Color(0xFFF2C277),
    temperatureFever = Color(0xFFF2958C),
    categories = mapOf(
        CareCategory.Feeding to CategoryColors(Color(0xFFF2B98A), Color(0xFF4A3220)),
        CareCategory.Sleep to CategoryColors(Color(0xFFB9BFF0), Color(0xFF2D3254)),
        CareCategory.Diaper to CategoryColors(Color(0xFF8FD3CE), Color(0xFF1F4140)),
        CareCategory.Measurement to CategoryColors(Color(0xFF9CC7EA), Color(0xFF243A4D)),
        CareCategory.Health to CategoryColors(Color(0xFFF0A6B8), Color(0xFF4D2731)),
        CareCategory.SolidFood to CategoryColors(Color(0xFFB5D88F), Color(0xFF2F4220)),
        CareCategory.Misc to CategoryColors(Color(0xFFD1B3EC), Color(0xFF3C2A4D)),
    ),
)

internal val LocalBabyColors = staticCompositionLocalOf { LightBabyColors }

val MaterialTheme.babyColors: BabyColors
    @Composable @ReadOnlyComposable get() = LocalBabyColors.current
