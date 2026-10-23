package dk.babyapp.ui.tracking

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dk.babyapp.data.preferences.BoardNote
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.HealthRecordStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One line on the board: what, when, and whether it is late. */
private data class BoardLine(val title: String, val whenText: String, val late: Boolean)

/**
 * Opslagstavle on I dag: planned health visits and vaccinations, the next items in the Danish programme
 * that are not planned yet, and the family's own messages. Long-press a message to change or remove it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BulletinBoard(child: ChildProfile?, childId: String, events: List<CareEventEntity>, notes: List<BoardNote>, onUpdateNotes: (List<BoardNote>) -> Unit) {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val lines = remember(child, events, today) {
        val format = DateTimeFormatter.ofPattern("EEE d. MMM 'kl.' HH.mm", Locale.forLanguageTag("da"))
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val planned = events.filter {
            it.childId == childId && it.deletedAt == null && !it.isDraft && it.healthStatus == HealthRecordStatus.Scheduled &&
                (it.type == CareEventType.HealthVisit || it.type == CareEventType.Vaccination) && it.startedAt >= startOfToday
        }.sortedBy { it.startedAt }.take(3).map { event ->
            val at = Instant.ofEpochMilli(event.startedAt).atZone(zone)
            BoardLine(eventTitle(event), "${dk.babyapp.domain.dueLabel(at.toLocalDate(), today).replaceFirstChar { it.uppercase() }} · ${at.format(format)}", false)
        }
        val programme = child?.let { dk.babyapp.domain.upcomingProgramme(it, events, today) }.orEmpty()
            .filter { it.scheduled == null }.take(3)
            .map { BoardLine(it.template.title, "Ikke planlagt · ${dk.babyapp.domain.dueLabel(it.due, today)}", it.due < today) }
        planned + programme
    }
    val mine = notes.filter { it.childId == childId }
    var editing by remember { mutableStateOf<BoardNote?>(null) }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Text("Opslagstavle", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { editing = BoardNote(childId = childId, text = "") }) { Icon(Icons.Outlined.Add, "Ny besked") }
            }
            if (lines.isEmpty() && mine.isEmpty()) Text("Her vises planlagte sundhedsbesøg og vaccinationer og jeres egne beskeder.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
            lines.forEach { line ->
                Column(Modifier.padding(end = 8.dp)) {
                    Text(line.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(line.whenText, style = MaterialTheme.typography.bodySmall, color = if (line.late) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            mine.forEach { note ->
                var menu by remember { mutableStateOf(false) }
                Box {
                    Text("• ${note.text}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        .combinedClickable(onClickLabel = "Ret besked", onClick = { editing = note }, onLongClick = { menu = true }))
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem(text = { Text("Rediger") }, onClick = { menu = false; editing = note })
                        DropdownMenuItem(text = { Text("Slet") }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onUpdateNotes(notes - note) })
                    }
                }
            }
        }
    }
    editing?.let { note -> BoardNoteDialog(note, isNew = note !in notes, onDismiss = { editing = null }, onDelete = { onUpdateNotes(notes - note); editing = null }) { text ->
        onUpdateNotes(if (note in notes) notes.map { if (it.id == note.id) it.copy(text = text) else it } else notes + note.copy(text = text))
        editing = null
    } }
}

@Composable
private fun BoardNoteDialog(note: BoardNote, isNew: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(note.text) }
    FormDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Ny besked" else "Ret besked") },
        text = { RegistrationTextField(text, { text = it }, label = { Text("Besked") }) },
        dismissButton = if (isNew) null else ({ IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Slet besked", tint = MaterialTheme.colorScheme.error) }; androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }),
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Annuller") }
            Button(enabled = text.isNotBlank(), onClick = { onSave(text.trim()) }) { Text("Gem") }
        },
    )
}
