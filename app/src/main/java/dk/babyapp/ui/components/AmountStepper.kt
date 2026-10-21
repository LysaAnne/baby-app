package dk.babyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.BabyTouch

/**
 * An amount that can be changed with large − and + buttons, or typed. Empty means "not given".
 * Values are kept within [range]; the buttons start from [start] when the amount is empty.
 */
@Composable
fun AmountStepper(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    unit: String,
    label: String,
    modifier: Modifier = Modifier,
    step: Int = 10,
    range: IntRange = 0..2000,
    start: Int = range.first,
) {
    fun clamp(amount: Int) = amount.coerceIn(range)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(BabySpacing.xs)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(BabySpacing.s), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = { onValueChange(clamp((value ?: start) - step)) },
                enabled = value == null || value > range.first,
                modifier = Modifier.size(BabyTouch.primary),
            ) { Icon(Icons.Outlined.Remove, contentDescription = "$step $unit mindre") }
            OutlinedTextField(
                value = value?.toString().orEmpty(),
                onValueChange = { text -> onValueChange(text.filter(Char::isDigit).take(5).toIntOrNull()?.let(::clamp)) },
                modifier = Modifier.widthIn(min = 96.dp, max = 140.dp),
                singleLine = true,
                suffix = { Text(unit) },
                textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = null,
            )
            FilledTonalIconButton(
                onClick = { onValueChange(clamp((value ?: start) + step)) },
                enabled = value == null || value < range.last,
                modifier = Modifier.size(BabyTouch.primary),
            ) { Icon(Icons.Outlined.Add, contentDescription = "$step $unit mere") }
        }
    }
}
