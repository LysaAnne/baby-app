package dk.babyapp.ui.tracking

import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors

/**
 * Lets the category's colour lead inside a registration: buttons, chips, switches and focused fields
 * use the accent, so a form reads as Ble, Søvn or Sundhed at a glance. Without a category nothing changes.
 */
@Composable
internal fun CategoryFormTheme(category: CareCategory?, content: @Composable () -> Unit) {
    if (category == null) return content()
    val colors = MaterialTheme.babyColors.category(category)
    val scheme = MaterialTheme.colorScheme
    MaterialTheme(
        colorScheme = scheme.copy(
            primary = colors.accent, onPrimary = Color.White,
            primaryContainer = colors.container, onPrimaryContainer = scheme.onSurface,
            secondaryContainer = colors.container, onSecondaryContainer = scheme.onSurface,
        ),
        typography = MaterialTheme.typography, shapes = MaterialTheme.shapes, content = content,
    )
}

/** Tinted backgrounds for a form: a light body, a deeper header and footer, so white fields stand out. */
@Composable
internal fun formBody(category: CareCategory?): Color = category?.let { lerp(MaterialTheme.colorScheme.background, MaterialTheme.babyColors.category(it).container, 0.5f) } ?: MaterialTheme.colorScheme.background

@Composable
private fun formBand(category: CareCategory?): Color = category?.let { MaterialTheme.babyColors.category(it).container } ?: MaterialTheme.colorScheme.surfaceContainer

@Composable
private fun FormBand(title: @Composable () -> Unit, category: CareCategory?, subtitle: String? = null, leading: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(formBand(category)).padding(start = if (leading != null) BabySpacing.xs else BabySpacing.l, end = BabySpacing.l, top = BabySpacing.l, bottom = BabySpacing.m),
        horizontalArrangement = Arrangement.spacedBy(BabySpacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        if (category != null) {
            val colors = MaterialTheme.babyColors.category(category)
            Box(Modifier.size(40.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) { title() }
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

/**
 * The shared look of every registration window: a coloured header with the category's icon, the form on a
 * lightly tinted background, and Annuller/Gem at the bottom. Takes the same slots as AlertDialog.
 */
@Composable
internal fun FormDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable RowScope.() -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    category: CareCategory? = null,
    subtitle: String? = null,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CategoryFormTheme(category) {
            Surface(
                modifier.fillMaxWidth(0.92f).widthIn(max = 560.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = formBody(category),
                shadowElevation = 8.dp,
            ) {
                Column {
                    FormBand({ title?.invoke() }, category, subtitle)
                    if (text != null) Box(Modifier.weight(1f, fill = false).padding(start = BabySpacing.l, end = BabySpacing.l, top = BabySpacing.m)) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = BabySpacing.m).background(formBand(category).copy(alpha = 0.6f)).padding(horizontal = BabySpacing.l, vertical = BabySpacing.s),
                        horizontalArrangement = Arrangement.spacedBy(BabySpacing.s, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dismissButton?.invoke(this)
                        confirmButton()
                    }
                }
            }
        }
    }
}

/** A longer registration: the same rounded card as [FormDialog], only taller, so every form looks alike. */
@Composable
internal fun FormScreen(
    title: String,
    category: CareCategory?,
    onDismissRequest: () -> Unit,
    footer: @Composable () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        CategoryFormTheme(category) {
            Box(Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(vertical = BabySpacing.l), contentAlignment = Alignment.Center) {
            Surface(Modifier.fillMaxWidth(0.92f).widthIn(max = 560.dp), shape = MaterialTheme.shapes.extraLarge, color = formBody(category), shadowElevation = 8.dp) {
                Column {
                    FormBand({ Text(title) }, category, subtitle)
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = BabySpacing.l, vertical = BabySpacing.m), verticalArrangement = Arrangement.spacedBy(BabySpacing.m), content = content)
                    Row(
                        Modifier.fillMaxWidth().background(formBand(category).copy(alpha = 0.6f)).padding(horizontal = BabySpacing.l, vertical = BabySpacing.s),
                        horizontalArrangement = Arrangement.spacedBy(BabySpacing.s, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) { footer() }
                }
            }
            }
        }
    }
}

/** The heading used inside older full-screen windows, matching [FormDialog]. */
@Composable
internal fun FormHeader(title: String, category: CareCategory?) {
    Row(Modifier.fillMaxWidth().padding(bottom = BabySpacing.s), horizontalArrangement = Arrangement.spacedBy(BabySpacing.m), verticalAlignment = Alignment.CenterVertically) {
        if (category != null) {
            val colors = MaterialTheme.babyColors.category(category)
            Box(Modifier.size(40.dp).background(colors.container, CircleShape), contentAlignment = Alignment.Center) {
                Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
    }
}

/** Short option sets as chips: the chosen one filled with the form's colour. Tap again to clear when [allowNone]. */
@Composable
internal fun <T> ChoiceChips(options: List<Pair<T, String>>, selected: T?, allowNone: Boolean = false, label: String? = null, onSelect: (T?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        label?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { (option, text) ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(if (allowNone && option == selected) null else option) },
                    label = { Text(text) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    border = if (option == selected) null else FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                )
            }
        }
    }
}

/**
 * Fields that are rarely needed, folded away behind one line that says what is inside.
 * Opens by itself when [hasContent], so nothing already written is hidden.
 */
@Composable
internal fun MoreDetails(summary: String, hasContent: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    var open by rememberSaveable { mutableStateOf(hasContent) }
    val turn by androidx.compose.animation.core.animateFloatAsState(if (open) 180f else 0f, label = "chevron")
    val bring = remember { androidx.compose.foundation.relocation.BringIntoViewRequester() }
    LaunchedEffect(open) { if (open) { kotlinx.coroutines.delay(250); bring.bringIntoView() } }
    // A clear, tappable bar; the fields slide open below it and are scrolled into view.
    Column(Modifier.bringIntoViewRequester(bring)) {
        Surface(onClick = { open = !open }, shape = MaterialTheme.shapes.medium, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(if (open) "Færre detaljer" else "Flere detaljer", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    if (!open) Text(summary.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Outlined.ExpandMore, if (open) "Skjul detaljer" else "Vis flere detaljer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.rotate(turn))
            }
        }
        AnimatedVisibility(open, enter = androidx.compose.animation.expandVertically(androidx.compose.animation.core.tween(250)) + androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)),
            exit = androidx.compose.animation.shrinkVertically(androidx.compose.animation.core.tween(200)) + androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(150))) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

/** A millilitre amount with −/+ steps, so the usual change is one tap. */
@Composable
internal fun AmountField(value: String, onChange: (String) -> Unit, label: String, step: Int = 10) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        RegistrationTextField(value, { onChange(it.filter(Char::isDigit)) }, Modifier.weight(1f), label = { Text(label) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        FilledTonalIconButton(onClick = { onChange(((value.toIntOrNull() ?: 0) - step).coerceAtLeast(0).toString()) }) { Text("−$step", style = MaterialTheme.typography.labelSmall) }
        FilledTonalIconButton(onClick = { onChange(((value.toIntOrNull() ?: 0) + step).toString()) }) { Text("+$step", style = MaterialTheme.typography.labelSmall) }
    }
}
