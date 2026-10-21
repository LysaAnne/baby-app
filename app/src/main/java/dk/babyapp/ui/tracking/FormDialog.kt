package dk.babyapp.ui.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors

/**
 * The shared look of every registration window: the category's icon beside the title, the form,
 * and Annuller/Gem at the bottom. Takes the same slots as AlertDialog so forms only change the call.
 */
@Composable
internal fun FormDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    category: CareCategory? = null,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier.fillMaxWidth(0.92f).widthIn(max = 560.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(start = BabySpacing.xl, end = BabySpacing.xl, top = BabySpacing.xl, bottom = BabySpacing.m),
                    horizontalArrangement = Arrangement.spacedBy(BabySpacing.m),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (category != null) {
                        val colors = MaterialTheme.babyColors.category(category)
                        Box(Modifier.size(40.dp).background(colors.container, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
                        }
                    }
                    Box(Modifier.weight(1f)) { ProvideTextStyle(MaterialTheme.typography.titleLarge) { title?.invoke() } }
                }
                if (text != null) Box(Modifier.weight(1f, fill = false).padding(horizontal = BabySpacing.xl)) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() }
                }
                HorizontalDivider(Modifier.padding(top = BabySpacing.l), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = BabySpacing.l, vertical = BabySpacing.m),
                    horizontalArrangement = Arrangement.spacedBy(BabySpacing.s, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

/** The heading used by the full-screen registration windows, matching [FormDialog]. */
@Composable
internal fun FormHeader(title: String, category: CareCategory?) {
    Row(Modifier.fillMaxWidth().padding(bottom = BabySpacing.s), horizontalArrangement = Arrangement.spacedBy(BabySpacing.m), verticalAlignment = Alignment.CenterVertically) {
        if (category != null) {
            val colors = MaterialTheme.babyColors.category(category)
            Box(Modifier.size(40.dp).background(colors.container, CircleShape), contentAlignment = Alignment.Center) {
                Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }
        androidx.compose.material3.Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
    }
}
