package dk.babyapp.ui.profile

import android.net.Uri
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dk.babyapp.R
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.preferences.MeasurementUnits
import dk.babyapp.data.profile.ProfileAvatar
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.CareProviderType
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileForm(
    draft: ProfileDraft,
    onDraftChange: (ProfileDraft) -> Unit,
    photoFile: File?,
    onPhotoSelected: suspend (Uri) -> String,
    units: MeasurementUnits = MeasurementUnits.Metric,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var birthExpanded by remember { mutableStateOf(true) }
    var careExpanded by remember { mutableStateOf(false) }
    var healthExpanded by remember { mutableStateOf(false) }
    var providerMenuOpen by remember { mutableStateOf(false) }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                onDraftChange(draft.copy(photoFileName = onPhotoSelected(uri)))
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.profile_identity), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (photoFile != null) {
                    val bitmap = remember(photoFile.path, photoFile.lastModified()) {
                        BitmapFactory.decodeFile(photoFile.path)?.asImageBitmap()
                    }
                    if (bitmap != null) Image(
                        bitmap = bitmap,
                        contentDescription = stringResource(R.string.profile_photo_description),
                        modifier = Modifier.size(72.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(draft.avatar.symbol, style = MaterialTheme.typography.displayMedium)
                }
                Column {
                    TextButton(onClick = { photoLauncher.launch("image/*") }) {
                        Text(stringResource(R.string.choose_photo))
                    }
                    if (draft.photoFileName != null) {
                        TextButton(onClick = { onDraftChange(draft.copy(photoFileName = null)) }) {
                            Text(stringResource(R.string.remove_photo))
                        }
                    }
                }
            }
        }
        item {
            Text(stringResource(R.string.choose_avatar), style = MaterialTheme.typography.labelLarge)
            androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileAvatar.entries.forEach { avatar ->
                    FilterChip(
                        selected = avatar == draft.avatar,
                        onClick = { onDraftChange(draft.copy(avatar = avatar)) },
                        label = { Text(avatar.symbol) },
                    )
                }
            }
        }
        item { FormField(draft.name, { onDraftChange(draft.copy(name = it)) }, R.string.child_name, true) }
        item { FormField(draft.nickname, { onDraftChange(draft.copy(nickname = it)) }, R.string.nickname) }
        item { ChildThemeDropdown(draft.colorTheme) { onDraftChange(draft.copy(colorTheme = it)) } }
        item { CollapsibleHeader(R.string.birth_details, birthExpanded) { birthExpanded = !birthExpanded } }
        if (birthExpanded) {
            item { BirthStatusDropdown(draft.birthStatus) { status -> onDraftChange(draft.copy(birthStatus = status, birthDate = if (status == BirthStatus.Expected) "" else draft.birthDate, birthTime = if (status == BirthStatus.Expected) "" else draft.birthTime)) } }
            if (draft.birthStatus == BirthStatus.Born) {
                item { DateSelector(draft.birthDate, { onDraftChange(draft.copy(birthDate = it)) }, R.string.birth_date, true) }
                item { TimeSelector(draft.birthTime, { onDraftChange(draft.copy(birthTime = it)) }, R.string.birth_time) }
            }
            item { DateSelector(draft.dueDate, { onDraftChange(draft.copy(dueDate = it)) }, R.string.due_date, draft.birthStatus == BirthStatus.Expected) }
            item { SexDropdown(draft.sex) { onDraftChange(draft.copy(sex = it)) } }
            item { NumericField(draft.birthWeightGrams, { onDraftChange(draft.copy(birthWeightGrams = it)) }, if (units == MeasurementUnits.Metric) R.string.birth_weight else R.string.birth_weight_imperial, decimal = units == MeasurementUnits.Imperial) }
            item { NumericField(draft.birthLengthCm, { onDraftChange(draft.copy(birthLengthCm = it)) }, if (units == MeasurementUnits.Metric) R.string.birth_length else R.string.birth_length_imperial, decimal = true) }
            item { NumericField(draft.birthHeadCircumferenceCm, { onDraftChange(draft.copy(birthHeadCircumferenceCm = it)) }, if (units == MeasurementUnits.Metric) R.string.head_circumference else R.string.head_circumference_imperial, decimal = true) }
        }
        item { CollapsibleHeader(R.string.care_details, careExpanded) { careExpanded = !careExpanded } }
        if (careExpanded) {
            item {
                Box { Button(onClick = { providerMenuOpen = true }) { Text(stringResource(R.string.add_care_provider)) }
                    DropdownMenu(expanded = providerMenuOpen, onDismissRequest = { providerMenuOpen = false }) { CareProviderType.entries.forEach { type -> DropdownMenuItem(text = { Text(stringResource(type.labelRes())) }, onClick = { onDraftChange(draft.copy(careProviders = draft.careProviders + CareProvider(type = type))); providerMenuOpen = false }) } }
                }
            }
            items(draft.careProviders, key = { it.id }) { provider ->
                ProviderFields(
                    title = provider.type.labelRes(), name = provider.name, phone = provider.phone, email = provider.email,
                    address = provider.address, notes = provider.notes, customTitle = provider.customTitle,
                    onCustomTitleChange = { value -> onDraftChange(draft.copy(careProviders = draft.careProviders.map { if (it.id == provider.id) it.copy(customTitle = value) else it })) },
                    onRemove = { onDraftChange(draft.copy(careProviders = draft.careProviders.filterNot { it.id == provider.id })) },
                ) { n,p,e,a,no -> onDraftChange(draft.copy(careProviders = draft.careProviders.map { if (it.id == provider.id) it.copy(name=n,phone=p,email=e,address=a,notes=no) else it })) }
            }
        }
        item { CollapsibleHeader(R.string.health_information, healthExpanded) { healthExpanded = !healthExpanded } }
        if (healthExpanded) {
            item { FormField(draft.cprNumber, { onDraftChange(draft.copy(cprNumber = it)) }, R.string.cpr_number) }
            item { FormField(draft.allergies, { onDraftChange(draft.copy(allergies = it)) }, R.string.allergies) }
            item { FormField(draft.medicalNotes, { onDraftChange(draft.copy(medicalNotes = it)) }, R.string.medical_notes) }
        }
    }
}

@Composable
private fun CollapsibleHeader(title: Int, expanded: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(title) + if (expanded) "  ▲" else "  ▼")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthStatusDropdown(value: BirthStatus, onChange: (BirthStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(if (value == BirthStatus.Born) R.string.already_born else R.string.not_born_yet),
            onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            label = { Text(stringResource(R.string.birth_status)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BirthStatus.entries.forEach { status -> DropdownMenuItem(
                text = { Text(stringResource(if (status == BirthStatus.Born) R.string.already_born else R.string.not_born_yet)) },
                onClick = { onChange(status); expanded = false },
            ) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildThemeDropdown(value: ChildColorTheme, onChange: (ChildColorTheme) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    @Composable fun label(theme: ChildColorTheme) = stringResource(when(theme) { ChildColorTheme.Sage -> R.string.color_sage; ChildColorTheme.Rose -> R.string.color_rose; ChildColorTheme.Sky -> R.string.color_sky; ChildColorTheme.Lavender -> R.string.color_lavender; ChildColorTheme.Sunshine -> R.string.color_sunshine })
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(value = label(value), onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), label = { Text(stringResource(R.string.child_color_theme)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded, { expanded = false }) { ChildColorTheme.entries.forEach { theme -> DropdownMenuItem(text = { Text(label(theme)) }, onClick = { onChange(theme); expanded = false }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SexDropdown(value: BiologicalSex, onChange: (BiologicalSex) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(value.labelRes()), onValueChange = {}, readOnly = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(), label = { Text(stringResource(R.string.biological_sex)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BiologicalSex.entries.filterNot { it == BiologicalSex.Unselected }.forEach { sex -> DropdownMenuItem(
                text = { Text(stringResource(sex.labelRes())) }, onClick = { onChange(sex); expanded = false },
            ) }
        }
    }
}

@Composable
private fun ProviderFields(
    title: Int, name: String, phone: String, email: String, address: String, notes: String,
    customTitle: String = "",
    onCustomTitleChange: (String) -> Unit = {},
    onRemove: () -> Unit,
    onChange: (String, String, String, String, String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium) {
      Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
            Text((customTitle.ifBlank { stringResource(title) }) + if (expanded) "  ▲" else "  ▼")
        }
        if (expanded) {
            if (title == R.string.other_health_professional) FormField(customTitle, onCustomTitleChange, R.string.custom_provider_title)
            FormField(name, { onChange(it, phone, email, address, notes) }, R.string.provider_name)
            FormField(phone, { onChange(name, it, email, address, notes) }, R.string.phone_number)
            FormField(email, { onChange(name, phone, it, address, notes) }, R.string.email)
            FormField(address, { onChange(name, phone, email, it, notes) }, R.string.address)
            FormField(notes, { onChange(name, phone, email, address, it) }, R.string.notes)
            TextButton(onClick = onRemove) { Text(stringResource(R.string.remove_provider), color = MaterialTheme.colorScheme.error) }
        }
      }
    }
}

private fun CareProviderType.labelRes() = when(this) {
    CareProviderType.Hospital -> R.string.birth_hospital
    CareProviderType.Gp -> R.string.gp
    CareProviderType.HealthVisitor -> R.string.health_visitor
    CareProviderType.Midwife -> R.string.midwife
    CareProviderType.Specialist -> R.string.specialist
    CareProviderType.Other -> R.string.other_health_professional
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(value: String, onValueChange: (String) -> Unit, label: Int, required: Boolean) {
    var open by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = {}, modifier = Modifier.fillMaxWidth().clickable { open = true },
        label = { Text(stringResource(label) + if (required) " *" else "") }, readOnly = true, enabled = false,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
    if (open) {
        val initial = runCatching { LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
        val state = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton(onClick = {
                state.selectedDateMillis?.let { onValueChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
                open = false
            }) { Text(stringResource(R.string.select)) } },
            dismissButton = { TextButton(onClick = { onValueChange(""); open = false }) { Text(stringResource(R.string.clear)) } },
        ) { DatePicker(state = state) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSelector(value: String, onValueChange: (String) -> Unit, label: Int) {
    var open by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = {}, modifier = Modifier.fillMaxWidth().clickable { open = true },
        label = { Text(stringResource(label)) }, readOnly = true, enabled = false,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
    if (open) {
        val parsed = runCatching { LocalTime.parse(value) }.getOrNull()
        val state = androidx.compose.material3.rememberTimePickerState(parsed?.hour ?: 12, parsed?.minute ?: 0, true)
        AlertDialog(
            onDismissRequest = { open = false }, text = { TimePicker(state = state) },
            confirmButton = { TextButton(onClick = { onValueChange("%02d:%02d".format(state.hour, state.minute)); open = false }) { Text(stringResource(R.string.select)) } },
            dismissButton = { TextButton(onClick = { onValueChange(""); open = false }) { Text(stringResource(R.string.clear)) } },
        )
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    required: Boolean = false,
    hint: Int? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(label) + if (required) " *" else "") },
        supportingText = hint?.let { { Text(stringResource(it)) } },
        singleLine = true,
    )
}

@Composable
private fun NumericField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    decimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(stringResource(label)) },
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        singleLine = true,
    )
}

private fun BiologicalSex.labelRes() = when (this) {
    BiologicalSex.Unselected -> R.string.select_sex
    BiologicalSex.PreferNotToSay -> R.string.sex_not_specified
    BiologicalSex.Female -> R.string.sex_female
    BiologicalSex.Male -> R.string.sex_male
    BiologicalSex.Other -> R.string.sex_other
}

fun ProfileValidationError.messageRes() = when (this) {
    ProfileValidationError.NameRequired -> R.string.error_name_required
    ProfileValidationError.InvalidBirthDate -> R.string.error_birth_date
    ProfileValidationError.DueDateRequired -> R.string.error_due_date_required
    ProfileValidationError.BirthDateInFuture -> R.string.error_birth_date_future
    ProfileValidationError.InvalidBirthTime -> R.string.error_birth_time
    ProfileValidationError.InvalidDueDate -> R.string.error_due_date
    ProfileValidationError.SexRequired -> R.string.error_sex_required
    ProfileValidationError.InvalidWeight -> R.string.error_weight
    ProfileValidationError.InvalidLength -> R.string.error_length
    ProfileValidationError.InvalidHeadCircumference -> R.string.error_head
    ProfileValidationError.InvalidGestation -> R.string.error_gestation
}
