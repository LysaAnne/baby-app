package dk.babyapp.ui.settings

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import dk.babyapp.data.preferences.AppPreferences
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.domain.calculateInsights
import dk.babyapp.domain.createCareCsv
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

private enum class DataAction { Csv, Pdf, Backup, Restore }

@Composable
fun ReminderSettingsDialog(preferences: AppPreferences, onUpdate: (Boolean, Int, Int) -> Unit, onUpdateBookReminders: (Boolean) -> Unit, onFrequency: (dk.babyapp.data.preferences.BookReminderFrequency) -> Unit = {}, onUpdateCustomReminders: (List<dk.babyapp.data.preferences.CustomReminder>) -> Unit = {}, onUpdateDevelopmentReminders: (Boolean) -> Unit = {}, onUpdateHealthReminders: ((Boolean) -> Unit)? = null, onDismiss: () -> Unit) {
    val context = LocalContext.current; var message by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<dk.babyapp.data.preferences.CustomReminder?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun askPermission() { if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.POST_NOTIFICATIONS) }
    val bookPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) onUpdateBookReminders(true) else message = "Notifikationstilladelsen blev ikke givet."
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Notifikationer") }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Barnets bog"); Text("Højst én påmindelse pr. valgt dag kl. 20, når der er en ledig side.", style = MaterialTheme.typography.bodySmall) }
            Switch(preferences.bookRemindersEnabled, { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) bookPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else onUpdateBookReminders(enabled)
            }, modifier = Modifier.semantics { contentDescription = "Bogpåmindelser" })
        }
        if (preferences.bookRemindersEnabled) dk.babyapp.ui.tracking.SelectionDropdown("Hyppighed", preferences.bookReminderFrequency.label, dk.babyapp.data.preferences.BookReminderFrequency.entries.map { it to it.label }, onFrequency)
        Text("Ugentligt: mandag. Månedligt: den 1. Tre gange: mandag, onsdag og fredag. Android kan forsinke leveringen.", style = MaterialTheme.typography.bodySmall)
        if (onUpdateHealthReminders != null) {
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Vaccinationer og undersøgelser"); Text("En uge før og på dagen, kl. 10.", style = MaterialTheme.typography.bodySmall) }
                Switch(preferences.healthRemindersEnabled, { enabled -> if (enabled) askPermission(); onUpdateHealthReminders(enabled) }, modifier = Modifier.semantics { contentDescription = "Påmindelser om vaccinationer og undersøgelser" })
            }
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Udvikling"); Text("Når et nyt spring begynder, når en milepæl typisk kommer, og når et nyt emne i psykologisk udvikling begynder. Kl. 9.", style = MaterialTheme.typography.bodySmall) }
            Switch(preferences.developmentRemindersEnabled, { enabled -> if (enabled) askPermission(); onUpdateDevelopmentReminders(enabled) }, modifier = Modifier.semantics { contentDescription = "Udviklingspåmindelser" })
        }
        HorizontalDivider()
        Text("Egne påmindelser", style = MaterialTheme.typography.titleSmall)
        preferences.customReminders.forEach { reminder ->
            Row(Modifier.fillMaxWidth().clickable { editing = reminder }, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(reminder.title); Text(reminderSummary(reminder), style = MaterialTheme.typography.bodySmall) }
                Switch(reminder.enabled, { on -> onUpdateCustomReminders(preferences.customReminders.map { if (it.id == reminder.id) it.copy(enabled = on) else it }) },
                    modifier = Modifier.semantics { contentDescription = "Påmindelse ${reminder.title}" })
            }
        }
        OutlinedButton(onClick = { askPermission(); editing = dk.babyapp.data.preferences.CustomReminder(title = "", days = (1..7).toSet(), hour = 10, minute = 0) }, modifier = Modifier.fillMaxWidth()) { Text("+ Ny påmindelse") }

    } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Luk") } })
    editing?.let { reminder ->
        val existing = preferences.customReminders.any { it.id == reminder.id }
        CustomReminderDialog(reminder, existing, onDismiss = { editing = null }, onDelete = { onUpdateCustomReminders(preferences.customReminders.filterNot { it.id == reminder.id }); editing = null }) { saved ->
            onUpdateCustomReminders(if (existing) preferences.customReminders.map { if (it.id == saved.id) saved else it } else preferences.customReminders + saved); editing = null
        }
    }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("Påmindelse") }, text = { Text(it) }, confirmButton = { Button(onClick = { message = null }) { Text("OK") } }) }
}

@Composable
fun DataManagementDialog(child: ChildProfile?, events: List<CareEventEntity>, preferences: AppPreferences, createBackup: suspend (CharArray) -> ByteArray, restoreBackup: suspend (ByteArray, CharArray) -> Unit, onDismiss: () -> Unit, onBackupSaved: () -> Unit = {}) {
    val context = LocalContext.current; val scope = rememberCoroutineScope(); var from by remember { mutableStateOf(LocalDate.now().minusDays(29)) }; var through by remember { mutableStateOf(LocalDate.now()) }
    var warning by remember { mutableStateOf<DataAction?>(null) }; var passwordAction by remember { mutableStateOf<DataAction?>(null) }; var password by remember { mutableStateOf("") }; var restoreBytes by remember { mutableStateOf<ByteArray?>(null) }; var message by remember { mutableStateOf<String?>(null) }
    val csv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if (uri != null && child != null) context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(createCareCsv(child, events, from, through, if (preferences.units == MeasurementUnits.Metric) "Metrisk" else "Imperial")) } }
    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri -> if (uri != null && child != null) context.contentResolver.openOutputStream(uri)?.use { writeHealthPdf(it, child, events, from, through, if (preferences.units == MeasurementUnits.Metric) "Metrisk" else "Imperial") } }
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> if (uri != null) scope.launch { runCatching { createBackup(password.toCharArray()) }.onSuccess { bytes -> context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }; onBackupSaved(); message = "Backup gemt" }.onFailure { message = it.message ?: "Backup kunne ikke oprettes" }; password = "" } }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { restoreBytes = context.contentResolver.openInputStream(uri)?.readBytes(); passwordAction = DataAction.Restore } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) { Surface(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Eksport og backup", style = MaterialTheme.typography.headlineSmall); TextButton(onClick = onDismiss) { Text("Luk") } }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Eksportperiode", style = MaterialTheme.typography.titleMedium); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { SettingsDateButton("Fra", from) { from = it.coerceAtMost(through) }; SettingsDateButton("Til", through) { through = it; if (from > it) from = it } } } }
            Text("Eksporter indeholder følsomme oplysninger og kræver altid din bekræftelse.")
            OutlinedButton(enabled = child != null, modifier = Modifier.fillMaxWidth(), onClick = { warning = DataAction.Csv }) { Text("Eksportér CSV") }
            OutlinedButton(enabled = child != null, modifier = Modifier.fillMaxWidth(), onClick = { warning = DataAction.Pdf }) { Text("PDF til sundhedsbesøg") }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { warning = DataAction.Backup }) { Text("Opret krypteret backup") }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { warning = DataAction.Restore }) { Text("Gendan backup") }
        }
    } } }
    warning?.let { action -> AlertDialog(onDismissRequest = { warning = null }, title = { Text("Følsomme oplysninger") }, text = { Text(if (action == DataAction.Restore) "Gendannelse erstatter lokale profiler, registreringer, fotos og indstillinger med indholdet af backuppen." else "Gem og del kun filen et sted, du har tillid til.") }, confirmButton = { Button(onClick = { warning = null; when (action) { DataAction.Csv -> csv.launch("${child?.name}-${from}-${through}.csv"); DataAction.Pdf -> pdf.launch("${child?.name}-sundhedsoversigt.pdf"); DataAction.Backup -> passwordAction = action; DataAction.Restore -> restore.launch(arrayOf("application/octet-stream", "application/*")) } }) { Text("Fortsæt") } }, dismissButton = { TextButton(onClick = { warning = null }) { Text("Annuller") } }) }
    passwordAction?.let { action -> AlertDialog(onDismissRequest = { passwordAction = null }, title = { Text(if (action == DataAction.Backup) "Beskyt backup" else "Åbn backup") }, text = { Column { Text("Brug mindst 8 tegn. Adgangskoden kan ikke gendannes."); OutlinedTextField(password, { password = it }, label = { Text("Adgangskode *") }) } }, confirmButton = { Button(enabled = password.length >= 8, onClick = { passwordAction = null; if (action == DataAction.Backup) backup.launch("babylog-backup.babybackup") else scope.launch { runCatching { restoreBackup(requireNotNull(restoreBytes), password.toCharArray()) }.onSuccess { message = "Backup gendannet" }.onFailure { message = "Backup kunne ikke åbnes" }; password = ""; restoreBytes = null } }) { Text(if (action == DataAction.Backup) "Vælg placering" else "Gendan") } }, dismissButton = { TextButton(onClick = { passwordAction = null; password = "" }) { Text("Annuller") } }) }
    message?.let { AlertDialog(onDismissRequest = { message = null }, title = { Text("Status") }, text = { Text(it) }, confirmButton = { Button(onClick = { message = null }) { Text("OK") } }) }
}

@Composable private fun SettingsDateButton(label: String, value: LocalDate, onChange: (LocalDate) -> Unit) { val context = LocalContext.current; OutlinedButton(onClick = { DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, value.year, value.monthValue - 1, value.dayOfMonth).show() }) { Text("$label: $value") } }
private fun minutes(value: Long) = if (value < 60) "$value min" else "${value / 60} t ${value % 60} min"
private fun writeHealthPdf(output: java.io.OutputStream, child: ChildProfile, events: List<CareEventEntity>, from: LocalDate, through: LocalDate, units: String) { val data = calculateInsights(events, child.id, from, through); val document = PdfDocument(); val page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create()); val canvas = page.canvas; val paint = Paint().apply { isAntiAlias = true }; var y = 54f; fun line(text: String, size: Float = 14f) { paint.textSize = size; canvas.drawText(text.take(88), 42f, y, paint); y += size + 10 }
    line("BabyLog – oversigt til sundhedsbesøg", 22f); line("Barn: ${child.name}"); line("Periode: $from til $through"); line("Enheder: $units · Tidszone: ${ZoneId.systemDefault().id}"); line("Madning", 18f); line("${data.feedingCount} registreringer · amning ${data.breastfeedingMinutes} min · flaske ${data.bottleMl} ml"); line("Bleer", 18f); line("${data.diaperCount} registreringer · våde ${data.wetDiapers} · afføring ${data.dirtyDiapers}"); line("Søvn", 18f); line("I alt ${minutes(data.sleepMinutes)} · ${data.sleepCount} gange · længste ${minutes(data.longestSleepMinutes)}"); val questions = events.filter { it.childId == child.id && it.healthQuestions.isNotBlank() }.map { it.healthQuestions }.distinct(); if (questions.isNotEmpty()) { line("Spørgsmål og noter", 18f); questions.take(10).forEach { line("• $it") } }; line("Egne registreringer – ikke en officiel sundhedsjournal.", 11f); document.finishPage(page); document.writeTo(output); document.close() }

private val weekdayLetters = listOf("M", "T", "O", "T", "F", "L", "S")

/** "Hverdage · 10.00", "Hver dag · 19.30" or "Man, ons · 8.00". */
internal fun reminderSummary(reminder: dk.babyapp.data.preferences.CustomReminder): String {
    val names = listOf("man", "tir", "ons", "tor", "fre", "lør", "søn")
    val days = when (reminder.days) {
        (1..7).toSet() -> "Hver dag"
        (1..5).toSet() -> "Hverdage"
        setOf(6, 7) -> "Weekend"
        else -> reminder.days.sorted().joinToString(", ") { names[it - 1] }.replaceFirstChar { it.uppercase() }
    }
    return "$days · %d.%02d".format(reminder.hour, reminder.minute)
}

/** One own reminder: what, which weekdays and when. */
@Composable
private fun CustomReminderDialog(reminder: dk.babyapp.data.preferences.CustomReminder, existing: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit, onSave: (dk.babyapp.data.preferences.CustomReminder) -> Unit) {
    val context = LocalContext.current
    var draft by remember { mutableStateOf(reminder) }
    dk.babyapp.ui.tracking.FormDialog(onDismissRequest = onDismiss, title = { Text(if (existing) "Ret påmindelse" else "Ny påmindelse") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            dk.babyapp.ui.tracking.RegistrationTextField(draft.title, { draft = draft.copy(title = it) }, label = { Text("Hvad skal du mindes om?") }, singleLine = true)
            dk.babyapp.ui.tracking.ChoiceChips(listOf("Mavetid", "Bad", "Vægtmåling", "D-vitamin", "Klippe negle").map { it to it }, draft.title.takeIf { it.isNotBlank() }, allowNone = true) { it?.let { title -> draft = draft.copy(title = title) } }
            Text("Dage", style = MaterialTheme.typography.labelMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                weekdayLetters.forEachIndexed { index, letter ->
                    val day = index + 1
                    FilterChip(day in draft.days, { draft = draft.copy(days = if (day in draft.days) draft.days - day else draft.days + day) }, label = { Text(letter) }, modifier = Modifier.weight(1f))
                }
            }
            OutlinedButton(onClick = { android.app.TimePickerDialog(context, { _, h, m -> draft = draft.copy(hour = h, minute = m) }, draft.hour, draft.minute, true).show() }, modifier = Modifier.fillMaxWidth()) {
                Text("Kl. %d.%02d".format(draft.hour, draft.minute))
            }
        }
    }, dismissButton = if (existing) ({ androidx.compose.material3.IconButton(onClick = onDelete) { androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Outlined.Delete, "Slet påmindelse", tint = MaterialTheme.colorScheme.error) }; androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }) else null,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Annuller") }
            Button(enabled = draft.title.isNotBlank() && draft.days.isNotEmpty(), onClick = { onSave(draft.copy(title = draft.title.trim())) }) { Text("Gem") }
        })
}
