package dk.babyapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import dk.babyapp.R

// Nunito is bundled (res/font, SIL Open Font License in assets/licenses) so it works offline.
// One variable font file covers every weight.
@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: FontWeight) = Font(R.font.nunito, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val Nunito = FontFamily(
    nunito(FontWeight.Normal),
    nunito(FontWeight.Medium),
    nunito(FontWeight.SemiBold),
    nunito(FontWeight.Bold),
)

private val base = Typography()

private fun TextStyle.nunito(weight: FontWeight) = copy(fontFamily = Nunito, fontWeight = weight)

/**
 * Material's sizes, set in Nunito. Headings are semi-bold, body text regular and labels semi-bold
 * so buttons read clearly. Screens should mostly use headlineSmall, titleMedium, bodyMedium,
 * bodySmall and labelLarge.
 */
val BabyTypography = Typography(
    displayLarge = base.displayLarge.nunito(FontWeight.SemiBold),
    displayMedium = base.displayMedium.nunito(FontWeight.SemiBold),
    displaySmall = base.displaySmall.nunito(FontWeight.SemiBold),
    headlineLarge = base.headlineLarge.nunito(FontWeight.SemiBold),
    headlineMedium = base.headlineMedium.nunito(FontWeight.SemiBold),
    headlineSmall = base.headlineSmall.nunito(FontWeight.SemiBold),
    titleLarge = base.titleLarge.nunito(FontWeight.SemiBold),
    titleMedium = base.titleMedium.nunito(FontWeight.SemiBold),
    titleSmall = base.titleSmall.nunito(FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.nunito(FontWeight.Normal),
    bodyMedium = base.bodyMedium.nunito(FontWeight.Normal),
    bodySmall = base.bodySmall.nunito(FontWeight.Normal),
    labelLarge = base.labelLarge.nunito(FontWeight.SemiBold),
    labelMedium = base.labelMedium.nunito(FontWeight.SemiBold),
    labelSmall = base.labelSmall.nunito(FontWeight.SemiBold),
)
