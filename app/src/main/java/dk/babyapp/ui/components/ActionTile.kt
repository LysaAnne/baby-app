package dk.babyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors

/**
 * A large tile for a main action, e.g. "Amning" or "Ble" on Today. [detail] is a short status
 * such as "for 2 t 10 m", read out together with the label.
 */
@Composable
fun ActionTile(
    label: String,
    category: CareCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.babyColors.category(category)
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 96.dp).semantics(mergeDescendants = true) {
            role = Role.Button
            contentDescription = listOfNotNull(label, detail).joinToString(", ")
        },
        colors = CardDefaults.cardColors(
            containerColor = colors.container,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = colors.container.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(BabySpacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BabySpacing.xs, Alignment.CenterVertically),
        ) {
            Icon(category.icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}
