package dk.babyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.BabyTouch

/**
 * Pick one of a few options with one tap instead of a dropdown, e.g. diaper type or bottle content.
 * Tapping the selected option again clears it when [allowNone] is true.
 */
@Composable
fun <T> ChipSelector(
    options: List<T>,
    selected: T?,
    onSelect: (T?) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    title: String? = null,
    allowNone: Boolean = false,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BabySpacing.xs)) {
        if (title != null) Text(title, style = MaterialTheme.typography.labelLarge)
        FlowRow(
            Modifier.semantics { selectableGroup() },
            horizontalArrangement = Arrangement.spacedBy(BabySpacing.s),
            verticalArrangement = Arrangement.spacedBy(BabySpacing.xs),
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(if (isSelected && allowNone) null else option) },
                    label = { Text(label(option), style = MaterialTheme.typography.labelLarge) },
                    modifier = Modifier.heightIn(min = BabyTouch.minimum).semantics { role = Role.RadioButton },
                )
            }
        }
    }
}
