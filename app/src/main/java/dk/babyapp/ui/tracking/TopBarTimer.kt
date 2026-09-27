package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import dk.babyapp.data.tracking.CareEventEntity
import kotlinx.coroutines.delay

@Composable
internal fun TopBarTimer(event: CareEventEntity, onOpen: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event.id, event.runningSince) { do { now = System.currentTimeMillis(); delay(1_000) } while (event.runningSince != null) }
    TextButton(onClick = onOpen) { Column {
        Text(if (event.runningSince == null) "På pause" else event.type.displayLabel(), style = MaterialTheme.typography.labelSmall)
        Text(formatDuration(event.elapsedSeconds(now)), style = MaterialTheme.typography.labelLarge)
    } }
}
