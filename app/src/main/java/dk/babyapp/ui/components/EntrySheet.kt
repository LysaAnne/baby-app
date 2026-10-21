package dk.babyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.BabyTouch

/**
 * A bottom sheet for adding or editing one registration. Nothing is saved until [onSave];
 * dismissing the sheet (swipe down, back, Annuller) calls [onDismiss] and saves nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    title: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean = true,
    saveLabel: String = "Gem",
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = BabySpacing.l).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(BabySpacing.m),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            content()
            Row(Modifier.fillMaxWidth().padding(vertical = BabySpacing.s), horizontalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).heightIn(min = BabyTouch.primary)) { Text("Annuller") }
                Button(onClick = onSave, enabled = canSave, modifier = Modifier.weight(1f).heightIn(min = BabyTouch.primary)) { Text(saveLabel) }
            }
        }
    }
}
