package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import dk.babyapp.data.tracking.danishVisitTemplates
import dk.babyapp.data.tracking.danishVaccinationTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthRecordDialog(
    childId: String,
    vaccination: Boolean,
    providers: List<CareProvider>,
    existing: CareEventEntity? = null,
    onDismiss: () -> Unit,
    openQuestions: List<dk.babyapp.data.preferences.HealthQuestion> = emptyList(),
    onQuestionsAttached: (List<dk.babyapp.data.preferences.HealthQuestion>) -> Unit = {},
    onSave: (CareEventEntity) -> Unit,
) {
    // Open questions for this child, ticked by default; ticked ones are added to the visit when it is saved.
    val childQuestions = openQuestions.filter { it.childId == childId }
    var attach by remember { mutableStateOf(childQuestions.map { it.id }.toSet()) }
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
    // The essentials first; everything else is folded away and opens by itself when it has content.
    FormScreen(if (vaccination) "Vaccination" else "Sundhedsbesøg", dk.babyapp.ui.theme.CareCategory.Health, onDismiss, footer = {
        TextButton(onClick = onDismiss) { Text("Annuller") }
        Button(enabled = !vaccination || vaccine.isNotBlank(), onClick = {
                        if (!vaccination) childQuestions.filter { it.id in attach }.takeIf { it.isNotEmpty() }?.let(onQuestionsAttached)
                        onSave((existing ?: CareEventEntity(childId = childId, type = if (vaccination) CareEventType.Vaccination else CareEventType.HealthVisit, startedAt = time, endedAt = time)).copy(
                            startedAt = time, endedAt = time, healthStatus = status, healthVisitType = if (vaccination) null else visitType,
                            providerId = providerId, providerDisplayName = providerName, healthTitle = title, healthReason = reason,
                            healthObservations = observations, healthAdvice = advice, healthQuestions = if (vaccination) questions else (listOf(questions.trim()) + childQuestions.filter { it.id in attach }.map { "• ${it.text}" }).filter(String::isNotBlank).joinToString("\n"), followUp = followUp,
                            vaccineName = vaccine, vaccineDose = dose, vaccineBatchNumber = batch, injectionSite = site,
                            reactionNotes = reactions, notes = notes, officialScheduleKey = officialKey, timeSpecified = timeSpecified,
                        ))
                    }) { Text("Gem") }
    }) {
        if (vaccination) {
            val selectedTemplate = danishVaccinationTemplates.firstOrNull { it.key == officialKey }
            SelectionDropdown(
                "Fra programmet",
                selectedTemplate?.let { "${it.title} · ${it.dose.lowercase()}" } ?: "Ingen – udfyld selv",
                listOf<String?>(null).map { it to "Ingen – udfyld selv" } + danishVaccinationTemplates.map { it.key as String? to "${it.title} · ${it.dose.lowercase()}" },
            ) { key ->
                officialKey = key
                danishVaccinationTemplates.firstOrNull { it.key == key }?.let { template -> vaccine = template.vaccineName; dose = template.dose }
            }
            if (selectedTemplate == null) {
                RegistrationTextField(vaccine, { vaccine = it }, label = { Text("Vaccine *") })
                RegistrationTextField(dose, { dose = it }, label = { Text("Dosis") })
            }
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            ChoiceChips(HealthRecordStatus.entries.map { it to it.displayLabel() }, status, label = "Status") { it?.let { status = it } }
            MoreDetails("batch, injektionssted, reaktioner, noter", listOf(batch, site, reactions, notes).any(String::isNotBlank)) {
                if (selectedTemplate != null) {
                    RegistrationTextField(vaccine, { vaccine = it }, label = { Text("Vaccine *") })
                    RegistrationTextField(dose, { dose = it }, label = { Text("Dosis") })
                }
                RegistrationTextField(batch, { batch = it }, label = { Text("Batchnummer") })
                RegistrationTextField(site, { site = it }, label = { Text("Injektionssted") })
                RegistrationTextField(reactions, { reactions = it }, label = { Text("Reaktioner") })
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        } else {
            val selectedTemplate = danishVisitTemplates.firstOrNull { it.key == officialKey }
            SelectionDropdown(
                "Børneundersøgelse eller sundhedsplejerske",
                selectedTemplate?.title ?: "Ingen – andet besøg",
                listOf<String?>(null).map { it to "Ingen – andet besøg" } + danishVisitTemplates.map { it.key as String? to it.title },
            ) { key ->
                officialKey = key
                danishVisitTemplates.firstOrNull { it.key == key }?.let { template -> title = template.title; visitType = if (template.key.startsWith("visitor-")) HealthVisitType.HealthVisitor else HealthVisitType.PreventiveExam }
            }
            if (selectedTemplate == null) ChoiceChips(HealthVisitType.entries.map { it to it.displayLabel() }, visitType, label = "Besøgstype") { it?.let { visitType = it } }
            DateAndOptionalTimeFields(time, timeSpecified, { time = it }, { timeSpecified = it })
            ChoiceChips(HealthRecordStatus.entries.map { it to it.displayLabel() }, status, label = "Status") { it?.let { status = it } }
            if (childQuestions.isNotEmpty()) {
                Text("Dine spørgsmål", style = MaterialTheme.typography.titleSmall)
                childQuestions.forEach { question ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(question.id in attach, { on -> attach = if (on) attach + question.id else attach - question.id })
                        Text(question.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text("De afkrydsede spørgsmål gemmes på besøget og fjernes fra listen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            MoreDetails("behandler, årsag, observationer, råd, spørgsmål, opfølgning, noter",
                listOf(providerName, reason, observations, advice, questions, followUp, notes).any(String::isNotBlank) || (title.isNotBlank() && selectedTemplate == null)) {
                if (providers.isNotEmpty()) {
                    SelectionDropdown(
                        "Registreret behandler",
                        providers.firstOrNull { it.id == providerId }?.name ?: "Ingen valgt",
                        listOf<String?>(null).map { it to "Ingen valgt" } + providers.map { it.id as String? to it.name },
                    ) { id -> providerId = id; providerName = providers.firstOrNull { it.id == id }?.name.orEmpty() }
                }
                RegistrationTextField(providerName, { providerName = it; providerId = null }, label = { Text("Behandler") })
                if (selectedTemplate == null) RegistrationTextField(title, { title = it }, label = { Text("Titel") })
                RegistrationTextField(reason, { reason = it }, label = { Text("Årsag til besøget") })
                RegistrationTextField(observations, { observations = it }, label = { Text("Observationer og målinger") })
                RegistrationTextField(advice, { advice = it }, label = { Text("Råd og aftaler") })
                RegistrationTextField(questions, { questions = it }, label = { Text("Spørgsmål") })
                RegistrationTextField(followUp, { followUp = it }, label = { Text("Opfølgning") })
                RegistrationTextField(notes, { notes = it }, label = { Text("Noter") })
            }
        }
        Text("Dine egne noter – ikke en officiel sundhedsjournal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SelectionDropdown(label: String, value: String, options: List<Pair<T, String>>, onSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        RegistrationTextField(
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
