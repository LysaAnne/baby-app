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
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.domain.ProgrammeItem
import dk.babyapp.domain.isHealthVisitor
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One line on the board: what, when, whether it is late, and the programme item to book when it is not booked. */
private data class BoardLine(val title: String, val whenText: String, val late: Boolean, val book: ProgrammeItem? = null)

/**
 * Opslagstavle at the bottom of the I dag card: always the next examination or health visitor visit (booked or not),
 * other planned visits and vaccinations, programme items due soon, and the family's own messages.
 * An item that is not booked can be marked as booked with its time. Long-press a message to change or remove it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BulletinBoard(child: ChildProfile?, childId: String, events: List<CareEventEntity>, notes: List<BoardNote>, onUpdateNotes: (List<BoardNote>) -> Unit, onSaveEvent: (CareEventEntity) -> Unit) {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val lines = remember(child, events, today) {
        val format = DateTimeFormatter.ofPattern("EEE d. MMM 'kl.' HH.mm", Locale.forLanguageTag("da"))
        fun bookedText(at: Long) = Instant.ofEpochMilli(at).atZone(zone).let { "Booket · ${dk.babyapp.domain.dueLabel(it.toLocalDate(), today)} · ${it.format(format)}" }
        fun programmeLine(item: ProgrammeItem) = if (item.scheduled != null) BoardLine(item.template.title, bookedText(item.scheduled.startedAt), false)
            else BoardLine(item.template.title, "Ikke booket · ${dk.babyapp.domain.dueLabel(item.due, today)}", item.due < today, item)
        val next = child?.let { dk.babyapp.domain.nextVisit(it, events, today) }
        val startOfToday = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val planned = events.filter {
            it.childId == childId && it.deletedAt == null && !it.isDraft && it.healthStatus == HealthRecordStatus.Scheduled &&
                (it.type == CareEventType.HealthVisit || it.type == CareEventType.Vaccination) && it.startedAt >= startOfToday && it.id != next?.scheduled?.id
        }.sortedBy { it.startedAt }.take(3).map { BoardLine(eventTitle(it), bookedText(it.startedAt), false) }
        val soon = child?.let { dk.babyapp.domain.upcomingProgramme(it, events, today) }.orEmpty()
            .filter { it.scheduled == null && it.template.key != next?.template?.key }.take(3).map(::programmeLine)
        listOfNotNull(next?.let(::programmeLine)) + planned + soon
    }
    val mine = notes.filter { it.childId == childId }
    var editing by remember { mutableStateOf<BoardNote?>(null) }
    var booking by remember { mutableStateOf<ProgrammeItem?>(null) }
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = Color.White.copy(alpha = 0.85f)) {
        Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.PushPin, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text("Opslagstavle", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = { editing = BoardNote(childId = childId, text = "") }) { Icon(Icons.Outlined.Add, "Ny besked") }
            }
            if (lines.isEmpty() && mine.isEmpty()) Text("Her vises næste sundhedsbesøg, vaccinationer og jeres egne beskeder.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
            lines.forEach { line ->
                Row(Modifier.padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(line.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(line.whenText, style = MaterialTheme.typography.bodySmall, color = if (line.late) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    line.book?.let { item -> TextButton(onClick = { booking = item }) { Text("Booket?") } }
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
    booking?.let { item -> BookVisitDialog(item, { booking = null }) { time -> onSaveEvent(bookedVisit(childId, item, time)); booking = null } }
}

/** A planned registration for a programme item, so it shows as booked and is completed later from the journal. */
internal fun bookedVisit(childId: String, item: ProgrammeItem, time: Long) = CareEventEntity(
    childId = childId, type = if (item.vaccination) CareEventType.Vaccination else CareEventType.HealthVisit, startedAt = time, endedAt = time,
    healthStatus = HealthRecordStatus.Scheduled, officialScheduleKey = item.template.key, healthTitle = item.template.title,
    healthVisitType = if (item.vaccination) null else if (item.template.isHealthVisitor) HealthVisitType.HealthVisitor else HealthVisitType.PreventiveExam,
    vaccineName = item.template.vaccineName, vaccineDose = item.template.dose,
)

@Composable
private fun BookVisitDialog(item: ProgrammeItem, onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    var time by remember { mutableLongStateOf(item.due.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) }
    FormDialog(
        category = dk.babyapp.ui.theme.CareCategory.Health,
        onDismissRequest = onDismiss,
        title = { Text("Booket tid") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.template.title, style = MaterialTheme.typography.titleSmall)
                DateTimeFields(time) { time = it }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
        confirmButton = { Button(onClick = { onSave(time) }) { Text("Gem") } },
    )
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
