package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.teeth.*
import dk.babyapp.ui.theme.BabySpacing
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Typical eruption ages in months, by tooth type; upper and lower differ a little. */
private class ToothGroup(val name: String, val teeth: List<Int>, val from: Int, val to: Int)

private val toothGroups = listOf(
    ToothGroup("Nederste midterste fortænder", listOf(81, 71), 6, 10),
    ToothGroup("Øverste midterste fortænder", listOf(51, 61), 8, 12),
    ToothGroup("Øverste sidefortænder", listOf(52, 62), 9, 13),
    ToothGroup("Nederste sidefortænder", listOf(82, 72), 10, 16),
    ToothGroup("Øverste første kindtænder", listOf(54, 64), 13, 19),
    ToothGroup("Nederste første kindtænder", listOf(84, 74), 14, 18),
    ToothGroup("Øverste hjørnetænder", listOf(53, 63), 16, 22),
    ToothGroup("Nederste hjørnetænder", listOf(83, 73), 17, 23),
    ToothGroup("Nederste anden kindtænder", listOf(85, 75), 23, 31),
    ToothGroup("Øverste anden kindtænder", listOf(55, 65), 25, 33),
)

private fun groupOf(tooth: Int) = toothGroups.first { tooth in it.teeth }

internal fun toothName(tooth: Int): String {
    val jaw = if (tooth / 10 in listOf(5, 6)) "Øverste" else "Nederste"
    val kind = when (tooth % 10) { 1 -> "midterste fortand"; 2 -> "sidefortand"; 3 -> "hjørnetand"; 4 -> "første kindtand"; else -> "anden kindtand" }
    return "$jaw $kind"
}

private fun toothSide(tooth: Int) = if (tooth / 10 in listOf(5, 8)) "barnets højre" else "barnets venstre"

private val toothSigns = listOf("Savler", "Uro om natten", "Bider på ting", "Røde kinder", "Let feber")

private fun ToothRecord?.statusLabel() = when { this?.lost != null -> "tabt"; this?.emerged != null -> "kommet"; else -> "ikke kommet endnu" }

private val dateFormat = DateTimeFormatter.ofPattern("d. MMM yyyy", Locale.forLanguageTag("da"))

private fun ageAt(birth: LocalDate?, date: LocalDate): String? {
    birth ?: return null
    val months = Period.between(birth, date).toTotalMonths()
    return if (months < 1) null else if (months == 1L) "1 md." else "$months mdr."
}

/** The teeth screen: the mouth, the teeth that usually come next, and a timeline. Tap a tooth to register it. */
@Composable
internal fun TeethDialog(childId: String, records: List<ToothRecord>, onSave: (ToothRecord) -> Unit, child: ChildProfile? = null, onDismiss: () -> Unit) {
    var selected by remember(childId) { mutableStateOf<Int?>(null) }
    var picking by remember { mutableStateOf(false) }
    val teeth = records.filter { it.childId == childId }.associateBy { it.tooth }
    val emergedCount = teeth.values.count { it.lost == null }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(start = BabySpacing.xs, end = BabySpacing.l, top = BabySpacing.s), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Luk") }
                    Column(Modifier.weight(1f)) {
                        Text("Tænder", style = MaterialTheme.typography.titleLarge)
                        Text(listOfNotNull(child?.name, "$emergedCount af 20 mælketænder").joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = BabySpacing.l, vertical = BabySpacing.s), verticalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
                    TeethCard {
                        Mouth(teeth) { selected = it }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BabySpacing.l, Alignment.CenterHorizontally)) {
                            Legend(MaterialTheme.colorScheme.primary, null, "Kommet")
                            Legend(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outlineVariant, "Ikke endnu")
                            Legend(MaterialTheme.colorScheme.surfaceVariant, null, "Tabt")
                        }
                    }
                    val next = toothGroups.filter { group -> group.teeth.any { teeth[it] == null } }.take(2)
                    if (next.isNotEmpty()) TeethCard {
                        Text("Kommer typisk snart", style = MaterialTheme.typography.titleSmall)
                        next.forEach { group ->
                            Row(Modifier.fillMaxWidth()) {
                                Text(group.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("${group.from}–${group.to} mdr.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("Tænderne kommer i meget forskelligt tempo. Tal med sundhedsplejersken, hvis du er i tvivl.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val timeline = teeth.values.flatMap { record ->
                        listOfNotNull(record.emerged?.let { LocalDate.parse(it) to "${toothName(record.tooth)}, ${toothSide(record.tooth).removePrefix("barnets ")}" },
                            record.lost?.let { LocalDate.parse(it) to "${toothName(record.tooth)}, ${toothSide(record.tooth).removePrefix("barnets ")}, tabt" }).map { it to record.tooth }
                    }.sortedBy { it.first.first }
                    if (timeline.isNotEmpty()) TeethCard {
                        Text("Tidslinje", style = MaterialTheme.typography.titleSmall)
                        timeline.forEach { (entry, tooth) ->
                            val (date, text) = entry
                            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { selected = tooth }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.width(96.dp)) {
                                    Text(date.format(DateTimeFormatter.ofPattern("d. MMM", Locale.forLanguageTag("da"))), style = MaterialTheme.typography.bodySmall)
                                    ageAt(child?.birthDate, date)?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                                Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    if (teeth.size < 20) Button(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text(" Registrér ny tand") }
                }
            }
        }
    }
    if (picking) ToothPicker(teeth, onDismiss = { picking = false }) { picking = false; selected = it }
    selected?.let { number ->
        ToothSheet(teeth[number] ?: ToothRecord(childId, number), exists = teeth[number] != null, onDismiss = { selected = null }, onSave = { onSave(it); selected = null })
    }
}

@Composable
private fun TeethCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
        Column(Modifier.fillMaxWidth().padding(BabySpacing.m), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

@Composable
private fun Legend(fill: androidx.compose.ui.graphics.Color, border: androidx.compose.ui.graphics.Color?, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(fill, RoundedCornerShape(3.dp)).then(if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(3.dp)) else Modifier))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Both jaws seen from the front: the child's right is on the left of the screen, as in the dentist's chart. */
@Composable
private fun Mouth(teeth: Map<Int, ToothRecord>, onSelect: (Int) -> Unit) {
    val came = MaterialTheme.colorScheme.primary
    val lost = MaterialTheme.colorScheme.surfaceVariant
    val empty = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1.15f)) {
        val w = maxWidth; val h = maxHeight
        Box(Modifier.fillMaxSize().padding(2.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), RoundedCornerShape(50)))
        Text("H", Modifier.align(Alignment.CenterStart).padding(start = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("V", Modifier.align(Alignment.CenterEnd).padding(end = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("overmund", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("undermund", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        listOf(upperMilkTeeth to true, lowerMilkTeeth to false).forEach { (row, upper) ->
            row.forEachIndexed { index, tooth ->
                // Along an arc: front teeth at the top (or bottom), back teeth towards the corners of the mouth.
                val angle = PI * (index + 0.5) / row.size
                val toothWidth: Dp = w * when (tooth % 10) { 1, 2 -> 0.075f; 3 -> 0.08f; else -> 0.095f }
                val toothHeight: Dp = w * 0.085f
                val cx = w / 2 - (w * 0.38f) * cos(angle).toFloat()
                val cy = if (upper) h / 2 - h * 0.06f - (h * 0.36f) * sin(angle).toFloat() else h / 2 + h * 0.06f + (h * 0.36f) * sin(angle).toFloat()
                val record = teeth[tooth]
                val fill = when { record?.lost != null -> lost; record != null -> came; else -> empty }
                Box(
                    Modifier.offset(x = cx - toothWidth / 2, y = cy - toothHeight / 2).size(toothWidth, toothHeight)
                        .clip(RoundedCornerShape(35)).background(fill)
                        .border(1.dp, if (record != null && record.lost == null) came else outline, RoundedCornerShape(35))
                        .clickable { onSelect(tooth) }
                        .semantics { contentDescription = "${toothName(tooth)}, ${toothSide(tooth)}, ${record.statusLabel()}" },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToothPicker(teeth: Map<Int, ToothRecord>, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = BabySpacing.l).padding(bottom = BabySpacing.xl).verticalScroll(rememberScrollState())) {
            Text("Hvilken tand?", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = BabySpacing.s))
            // In the order they usually come.
            toothGroups.flatMap { group -> group.teeth.filter { teeth[it] == null }.map { it to group } }.forEach { (tooth, group) ->
                Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { onPick(tooth) }.padding(vertical = 12.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(toothName(tooth), style = MaterialTheme.typography.bodyLarge)
                        Text(toothSide(tooth).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${group.from}–${group.to} mdr.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToothSheet(record: ToothRecord, exists: Boolean, onDismiss: () -> Unit, onSave: (ToothRecord) -> Unit) {
    val today = LocalDate.now()
    var emerged by remember(record) { mutableStateOf(record.emerged?.let(LocalDate::parse) ?: today.takeIf { !exists }) }
    var lost by remember(record) { mutableStateOf(record.lost?.let(LocalDate::parse)) }
    var showLost by remember(record) { mutableStateOf(record.lost != null) }
    var signs by remember(record) { mutableStateOf(record.signs.split(',').map(String::trim).filter(String::isNotBlank).toSet()) }
    var note by remember(record) { mutableStateOf(record.note) }
    var error by remember { mutableStateOf<String?>(null) }
    val group = groupOf(record.tooth)
    fun save(updated: ToothRecord) = runCatching { updated.validate() }.onSuccess { onSave(updated) }.onFailure { error = it.message }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = BabySpacing.l).padding(bottom = BabySpacing.xl).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
            Row(horizontalArrangement = Arrangement.spacedBy(BabySpacing.m), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                Column {
                    Text(toothName(record.tooth), style = MaterialTheme.typography.titleLarge)
                    Text("${toothSide(record.tooth).replaceFirstChar { it.uppercase() }} · kommer typisk ${group.from}–${group.to} mdr.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("Hvornår kom den?", style = MaterialTheme.typography.labelLarge)
            DateChoice(emerged, onChange = { emerged = it; error = null })
            Text("Første tegn (valgfrit)", style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                toothSigns.forEach { sign -> FilterChip(selected = sign in signs, onClick = { signs = if (sign in signs) signs - sign else signs + sign }, label = { Text(sign) }) }
            }
            RegistrationTextField(note, { note = it }, label = { Text("Note") })
            if (showLost) {
                Text("Hvornår blev den tabt?", style = MaterialTheme.typography.labelLarge)
                DateChoice(lost, onChange = { lost = it; error = null })
            } else if (exists) TextButton(onClick = { showLost = true; lost = today }) { Text("Registrér tab") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BabySpacing.s, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                // A record without dates is removed, so this clears the tooth.
                if (exists) TextButton(onClick = { save(record.copy(emerged = null, lost = null)) }) { Text("Fjern", color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onDismiss) { Text("Annuller") }
                Button(onClick = { save(record.copy(emerged = emerged?.toString(), lost = lost?.takeIf { showLost }?.toString(), signs = toothSigns.filter { it in signs }.joinToString(", "), note = note.trim())) }) { Text("Gem") }
            }
        }
    }
}

/** I dag, I går or a chosen date; the chosen date shows on its own chip. Dates can't be in the future. */
@Composable
private fun DateChoice(value: LocalDate?, onChange: (LocalDate?) -> Unit) {
    val context = LocalContext.current
    val today = LocalDate.now()
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = value == today, onClick = { onChange(today) }, label = { Text("I dag") })
        FilterChip(selected = value == today.minusDays(1), onClick = { onChange(today.minusDays(1)) }, label = { Text("I går") })
        val other = value?.takeIf { it != today && it != today.minusDays(1) }
        FilterChip(selected = other != null, onClick = {
            val date = value ?: today
            DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, date.year, date.monthValue - 1, date.dayOfMonth).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }, leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, Modifier.size(16.dp)) }, label = { Text(other?.format(dateFormat) ?: "Vælg dato") })
        if (value == null) AssistChip(onClick = {}, enabled = false, label = { Text("Ukendt dato") })
    }
}
