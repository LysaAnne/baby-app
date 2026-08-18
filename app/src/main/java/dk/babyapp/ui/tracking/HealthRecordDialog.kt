package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.HealthRecordStatus
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.data.tracking.danishPreventiveExaminationTemplates
import dk.babyapp.data.tracking.danishVaccinationTemplates

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
    var timeSpecified by remember { mutableStateOf(existing?.timeSpecified ?: false) }
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
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (vaccination) "Registrér vaccination" else "Registrér sundhedsbesøg", style = MaterialTheme.typography.headlineSmall)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Dette er dine egne noter og ikke en officiel sundhedsjournal.")
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            SelectionDropdown("Status *", status.displayLabel(), HealthRecordStatus.entries.map { it to it.displayLabel() }) { status = it }
            if (vaccination) {
                val selectedTemplate = danishVaccinationTemplates.firstOrNull { it.key == officialKey }
                SelectionDropdown(
                    "Dansk vaccinationsprogram",
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
                    "Forebyggende børneundersøgelse",
                    selectedTemplate?.title ?: "Ingen standardskabelon",
                    listOf<String?>(null).map { it to "Ingen standardskabelon" } + danishPreventiveExaminationTemplates.map { it.key as String? to it.title },
                ) { key ->
                    officialKey = key
                    danishPreventiveExaminationTemplates.firstOrNull { it.key == key }?.let { template -> title = template.title; visitType = HealthVisitType.PreventiveExam }
                }
                SelectionDropdown("Besøgstype *", visitType.displayLabel(), HealthVisitType.entries.map { it to it.displayLabel() }) { visitType = it }
                if (providers.isNotEmpty()) {
                    SelectionDropdown(
                        "Registreret behandler",
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
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Annuller") }
                    Button(enabled = !vaccination || vaccine.isNotBlank(), onClick = {
                        onSave((existing ?: CareEventEntity(childId = childId, type = if (vaccination) CareEventType.Vaccination else CareEventType.HealthVisit, startedAt = time, endedAt = time)).copy(
                            startedAt = time, endedAt = time, healthStatus = status, healthVisitType = if (vaccination) null else visitType,
                            providerId = providerId, providerDisplayName = providerName, healthTitle = title, healthReason = reason,
                            healthObservations = observations, healthAdvice = advice, healthQuestions = questions, followUp = followUp,
                            vaccineName = vaccine, vaccineDose = dose, vaccineBatchNumber = batch, injectionSite = site,
                            reactionNotes = reactions, notes = notes, officialScheduleKey = officialKey, timeSpecified = timeSpecified,
                        ))
                    }) { Text("Gem") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SelectionDropdown(label: String, value: String, options: List<Pair<T, String>>, onSelected: (T) -> Unit) {
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
