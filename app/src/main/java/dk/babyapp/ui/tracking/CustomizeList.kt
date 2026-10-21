package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * One row in a Tilpas window, as first used on Indsigt: a switch, the name, and arrows to move it.
 * Leave out [shown] for lists that only reorder, and the move callbacks for rows that only toggle.
 */
@Composable
internal fun CustomizeRow(
    label: String,
    shown: Boolean? = null,
    onShownChange: (Boolean) -> Unit = {},
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    indent: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth().padding(start = if (indent) 28.dp else 0.dp), verticalAlignment = Alignment.CenterVertically) {
        if (shown != null) Switch(shown, onShownChange, modifier = Modifier.semantics { contentDescription = "Vis $label" })
        leading?.invoke()
        Text(label, modifier = Modifier.weight(1f).padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
            style = if (indent) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            color = if (shown == false) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
        if (onMoveUp != null || onMoveDown != null) {
            IconButton(enabled = onMoveUp != null, onClick = { onMoveUp?.invoke() }) { Icon(Icons.Outlined.ArrowUpward, "Flyt $label op") }
            IconButton(enabled = onMoveDown != null, onClick = { onMoveDown?.invoke() }) { Icon(Icons.Outlined.ArrowDownward, "Flyt $label ned") }
        }
    }
}

internal fun <T> List<T>.moved(from: Int, to: Int): List<T> = toMutableList().apply { add(to, removeAt(from)) }

/** Up and down callbacks for the item at [index], or null at the ends. */
internal fun <T> List<T>.moveCallbacks(index: Int, onChange: (List<T>) -> Unit): Pair<(() -> Unit)?, (() -> Unit)?> =
    (if (index > 0) ({ onChange(moved(index, index - 1)) }) else null) to (if (index < lastIndex) ({ onChange(moved(index, index + 1)) }) else null)
