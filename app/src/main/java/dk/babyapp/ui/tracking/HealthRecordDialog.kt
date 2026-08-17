package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.HealthRecordStatus
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.data.tracking.danishPreventiveExaminationTemplates
import dk.babyapp.data.tracking.danishVaccinationTemplates
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordDialog(
    childId: String,
    vaccination: Boolean,
    providers: List<CareProvider>,
    existing: CareEventEntity? = null,
    onDismiss: () -> Unit,
    onSave: (CareEventEntity) -> Unit,
) {
    var time by remember { mutableLongStateOf(existing?.startedAt ?: System.currentTimeMillis()) }
    var status by remember { mutableStateOf(existing?.healthStatus ?: HealthRecordStatus.Completed) }
    var visitType by remember { mutableStateOf(existing?.healthVisitType ?: HealthVisitType.GpVisit) }
    var providerId by remember { mutableStateOf(existing?.providerId) }
    var providerName by remember { mutableStateOf(existing?.providerDisplayName.orEmpty()) }
    var title by remember { mutableStateOf(existing?.healthTitle.orEmpty()) }
    var reason by remember { mutableStateOf(existing?.healthReason.orEmpty()) }
    var observations by remember { mutableStateOf(existing?.healthObservations.orEmpty()) }
    var advice by remember { mutableStateOf(existing?.healthAdvice.orEmpty()) }
    var questions by remember { mutableStateOf(existing?.healthQuestions.orEmpty()) }
    var followUp by remember { mutableStateOf(existing?.followUp.orEmpty()) }
    var vaccine by remember { mutableStateOf(existing?.vaccineName.orEmpty()) }
    var dose by remember { mutableStateOf(existing?.vaccineDose.orEmpty()) }
    var batch by remember { mutableStateOf(existing?.vaccineBatchNumber.orEmpty()) }
    var site by remember { mutableStateOf(existing?.injectionSite.orEmpty()) }
    var reactions by remember { mutableStateOf(existing?.reactionNotes.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    var officialKey by remember { mutableStateOf(existing?.officialScheduleKey) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vaccination) "Registrér vaccination" else "Registrér sundhedsbesøg") },
        text = { Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Dette er dine egne noter og ikke en officiel sundhedsjournal.")
            DateOnlyButton(time) { time = it }
            SelectionDropdown("Status", statusLabel(status), HealthRecordStatus.entries.map { it to statusLabel(it) }) { status = it }
            if (vaccination) {
                val selectedTemplate = danishVaccinationTemplates.firstOrNull { it.key == officialKey }
                SelectionDropdown(
                    "Dansk vaccinationsprogram (valgfrit)",
                    selectedTemplate?.title ?: "Ingen standardskabelon",
                    listOf<String?>(null).map { it to "Ingen standardskabelon" } + danishVaccinationTemplates.map { it.key as String? to it.title },
                ) { key ->
                    officialKey = key
                    danishVaccinationTemplates.firstOrNull { it.key == key }?.let { template -> vaccine = template.vaccineName; dose = template.dose }
                }
                OutlinedTextField(vaccine, { vaccine = it }, label = { Text("Vaccine *") })
                OutlinedTextField(dose, { dose = it }, label = { Text("Dosis") })
                OutlinedTextField(batch, { batch = it }, label = { Text("Batchnummer") })
                OutlinedTextField(site, { site = it }, label = { Text("Injektionssted") })
                OutlinedTextField(reactions, { reactions = it }, label = { Text("Reaktioner") })
            } else {
                val selectedTemplate = danishPreventiveExaminationTemplates.firstOrNull { it.key == officialKey }
                SelectionDropdown(
                    "Forebyggende børneundersøgelse (valgfrit)",
                    selectedTemplate?.title ?: "Ingen standardskabelon",
                    listOf<String?>(null).map { it to "Ingen standardskabelon" } + danishPreventiveExaminationTemplates.map { it.key as String? to it.title },
                ) { key ->
                    officialKey = key
                    danishPreventiveExaminationTemplates.firstOrNull { it.key == key }?.let { template -> title = template.title; visitType = HealthVisitType.PreventiveExam }
                }
                SelectionDropdown("Besøgstype", visitTypeLabel(visitType), HealthVisitType.entries.map { it to visitTypeLabel(it) }) { visitType = it }
                if (providers.isNotEmpty()) {
                    SelectionDropdown(
                        "Registreret behandler (valgfrit)",
                        providers.firstOrNull { it.id == providerId }?.name ?: "Ingen valgt",
                        listOf<String?>(null).map { it to "Ingen valgt" } + providers.map { it.id as String? to it.name },
                    ) { id -> providerId = id; providerName = providers.firstOrNull { it.id == id }?.name.orEmpty() }
                }
                OutlinedTextField(providerName, { providerName = it; providerId = null }, label = { Text("Behandler") })
                OutlinedTextField(title, { title = it }, label = { Text("Titel") })
                OutlinedTextField(reason, { reason = it }, label = { Text("Årsag til besøget") })
                OutlinedTextField(observations, { observations = it }, label = { Text("Observationer og målinger") })
                OutlinedTextField(advice, { advice = it }, label = { Text("Råd og aftaler") })
                OutlinedTextField(questions, { questions = it }, label = { Text("Spørgsmål") })
                OutlinedTextField(followUp, { followUp = it }, label = { Text("Opfølgning") })
            }
            OutlinedTextField(notes, { notes = it }, label = { Text("Noter") })
        } },
        confirmButton = { Button(enabled = !vaccination || vaccine.isNotBlank(), onClick = {
            onSave((existing ?: CareEventEntity(childId = childId, type = if (vaccination) CareEventType.Vaccination else CareEventType.HealthVisit, startedAt = time, endedAt = time)).copy(
                startedAt = time, endedAt = time, healthStatus = status, healthVisitType = if (vaccination) null else visitType,
                providerId = providerId, providerDisplayName = providerName, healthTitle = title, healthReason = reason,
                healthObservations = observations, healthAdvice = advice, healthQuestions = questions, followUp = followUp,
                vaccineName = vaccine, vaccineDose = dose, vaccineBatchNumber = batch, injectionSite = site,
                reactionNotes = reactions, notes = notes,
                officialScheduleKey = officialKey,
            ))
        }) { Text("Gem") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SelectionDropdown(label: String, value: String, options: List<Pair<T, String>>, onSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (option, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelected(option); expanded = false }) }
        }
    }
}

@Composable
private fun DateOnlyButton(value: Long, onChange: (Long) -> Unit) {
    val context = LocalContext.current
    val date = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
    Button(onClick = {
        DatePickerDialog(context, { _, year, month, day ->
            onChange(LocalDate.of(year, month + 1, day).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }, date.year, date.monthValue - 1, date.dayOfMonth).show()
    }) { Text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(value))) }
}

internal fun statusLabel(status: HealthRecordStatus) = when (status) {
    HealthRecordStatus.Scheduled -> "Planlagt"
    HealthRecordStatus.Completed -> "Gennemført"
    HealthRecordStatus.Postponed -> "Udsat"
    HealthRecordStatus.Cancelled -> "Aflyst"
    HealthRecordStatus.Declined -> "Fravalgt"
}

private fun visitTypeLabel(type: HealthVisitType) = when (type) {
    HealthVisitType.PreventiveExam -> "Forebyggende børneundersøgelse"
    HealthVisitType.GpVisit -> "Egen læge"
    HealthVisitType.HealthVisitor -> "Sundhedsplejerske"
    HealthVisitType.Midwife -> "Jordemoder"
    HealthVisitType.Hospital -> "Hospital eller ambulatorium"
    HealthVisitType.Specialist -> "Speciallæge"
    HealthVisitType.Dental -> "Tandpleje"
    HealthVisitType.Other -> "Andet"
}
