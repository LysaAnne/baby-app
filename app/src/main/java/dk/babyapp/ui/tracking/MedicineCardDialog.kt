package dk.babyapp.ui.tracking

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.ui.theme.CareCategory
import java.time.LocalTime

/** How often a medicine is given: as needed, or a number of times a day with that many reminder times. */
private enum class MedicineFrequency(val label: String, val times: List<String>) {
    AsNeeded("Efter behov (PN)", emptyList()),
    Once("1 gang dagligt", listOf("08:00")),
    Twice("2 gange dagligt", listOf("08:00", "20:00")),
    Three("3 gange dagligt", listOf("08:00", "14:00", "20:00")),
    Four("4 gange dagligt", listOf("08:00", "12:00", "16:00", "20:00")),
}

private fun MedicinePlan.frequency(): MedicineFrequency? = when {
    asNeeded -> MedicineFrequency.AsNeeded
    else -> MedicineFrequency.entries.firstOrNull { it != MedicineFrequency.AsNeeded && it.times.size == validTimes().size }
}

private fun MedicinePlan.frequencyText() = frequency()?.label ?: if (validTimes().isEmpty()) "Hyppighed ikke angivet" else "${validTimes().size} gange dagligt"

/** The medicine card: active and inactive medicines in two tabs, each one a short line that opens for details. */
@Composable
internal fun MedicineCardDialog(childId: String, all: List<MedicinePlan>, onSave: (List<MedicinePlan>) -> Unit, onDismiss: () -> Unit, events: List<CareEventEntity> = emptyList()) {
    var editing by remember { mutableStateOf<MedicinePlan?>(null) }
    var deleting by remember { mutableStateOf<MedicinePlan?>(null) }
    var showInactive by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf<String?>(null) }
    val plans = all.filter { it.childId == childId }
    val (active, inactive) = plans.partition { it.active }
    FormDialog(onDismissRequest = onDismiss, category = CareCategory.Health, title = { Text("Medicinkort") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = !showInactive, onClick = { showInactive = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Aktiv (${active.size})") }
                SegmentedButton(selected = showInactive, onClick = { showInactive = true }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Inaktiv (${inactive.size})") }
            }
            val shown = if (showInactive) inactive else active
            if (shown.isEmpty()) Text(if (showInactive) "Ingen inaktiv medicin." else "Ingen aktiv medicin. Tilføj medicin herunder.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            shown.forEach { plan ->
                val expanded = open == plan.id
                val turn by androidx.compose.animation.core.animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
                Surface(onClick = { open = if (expanded) null else plan.id }, shape = MaterialTheme.shapes.medium, color = Color.White) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                                if (plan.dose.isNotBlank()) Text(plan.dose, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Outlined.ExpandMore, if (expanded) "Skjul detaljer om ${plan.name}" else "Vis detaljer om ${plan.name}", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.rotate(turn))
                        }
                        AnimatedVisibility(expanded) {
                            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(listOfNotNull(plan.frequencyText(), plan.validTimes().takeIf { it.isNotEmpty() }?.joinToString(", ") { "kl. %02d.%02d".format(it.hour, it.minute) }).joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                                }
                                if (plan.reminders && !plan.asNeeded) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.NotificationsActive, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Påmindelser slået til", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (plan.instructions.isNotBlank()) Text(plan.instructions, style = MaterialTheme.typography.bodyMedium)
                                dk.babyapp.domain.lastDose(plan, events)?.let { dose ->
                                    Text("Sidste dosis ${java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(java.util.Date(dose.startedAt))} · for ${dk.babyapp.domain.timeSinceLabel(dose.startedAt, System.currentTimeMillis())} siden",
                                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Aktiv", style = MaterialTheme.typography.labelLarge)
                                    Switch(plan.active, { on -> onSave(all.map { if (it.id == plan.id) it.copy(active = on) else it }) }, modifier = Modifier.padding(start = 8.dp).scale(0.8f))
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { editing = plan }) { Icon(Icons.Outlined.Edit, "Rediger ${plan.name}") }
                                    IconButton(onClick = { deleting = plan }) { Icon(Icons.Outlined.Delete, "Slet ${plan.name}", tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            }
            FilledTonalButton(onClick = { editing = MedicinePlan(childId = childId, name = "") }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Add, null); Text(" Tilføj medicin") }
        }
    }, confirmButton = { Button(onClick = onDismiss) { Text("Luk") } })
    editing?.let { plan -> MedicinePlanEditor(plan, { editing = null }) { updated -> onSave(all.filterNot { it.id == updated.id } + updated); editing = null } }
    deleting?.let { plan -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Slet fra medicinkort?") }, text = { Text("${plan.name} fjernes fra kortet, og påmindelser stoppes. Tidligere registreringer bevares i journalen.") }, confirmButton = { Button(onClick = { onSave(all.filterNot { it.id == plan.id }); deleting = null }) { Text("Slet") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Annuller") } }) }
}

/** Adding or editing a medicine: name, dose, how often (with one reminder time per daily dose), notes and active. */
@Composable
private fun MedicinePlanEditor(initial: MedicinePlan, onDismiss: () -> Unit, onSave: (MedicinePlan) -> Unit) {
    var draft by remember(initial.id) { mutableStateOf(initial) }
    val context = LocalContext.current
    var exactAllowed by remember { mutableStateOf(Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) }
    var notificationsAllowed by remember { mutableStateOf(androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        exactAllowed = Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        notificationsAllowed = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val frequency = draft.frequency()
    FormDialog(category = CareCategory.Health, onDismissRequest = onDismiss, title = { Text(if (initial.name.isBlank()) "Ny medicin" else "Rediger medicin") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RegistrationTextField(draft.name, { draft = draft.copy(name = it) }, label = { Text("Navn *") })
            RegistrationTextField(draft.dose, { draft = draft.copy(dose = it) }, label = { Text("Dosis, fx 2,5 ml") })
            SelectionDropdown("Hvor ofte", frequency?.label ?: draft.frequencyText(), MedicineFrequency.entries.map { it to it.label }) { picked ->
                // Keeps times already chosen and fills up or trims to the number of daily doses.
                val kept = draft.validTimes().map { it.toString() }
                val times = if (picked == MedicineFrequency.AsNeeded) emptyList() else (kept + picked.times.filterNot { it in kept }).take(picked.times.size).sorted()
                draft = draft.copy(asNeeded = picked == MedicineFrequency.AsNeeded, times = times, reminders = if (picked == MedicineFrequency.AsNeeded) false else draft.reminders)
            }
            if (!draft.asNeeded && draft.validTimes().isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Påmindelser", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Switch(draft.reminders, { draft = draft.copy(reminders = it) })
                }
                // One time per daily dose; tap to change it.
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    draft.validTimes().forEachIndexed { index, time ->
                        AssistChip(onClick = {
                            TimePickerDialog(context, { _, h, m ->
                                val times = draft.validTimes().map { it.toString() }.toMutableList().apply { this[index] = LocalTime.of(h, m).toString() }
                                draft = draft.copy(times = times.distinct().sorted())
                            }, time.hour, time.minute, true).show()
                        }, leadingIcon = { Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp)) }, label = { Text("Dosis ${index + 1} · kl. %02d.%02d".format(time.hour, time.minute)) })
                    }
                }
                if (draft.reminders && !notificationsAllowed) TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text("Tillad notifikationer") }
                if (draft.reminders && Build.VERSION.SDK_INT >= 31 && !exactAllowed) {
                    Text("Tillad præcise alarmer, så påmindelserne kommer til tiden.", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) }) { Text("Tillad præcise alarmer") }
                }
            }
            RegistrationTextField(draft.instructions, { draft = draft.copy(instructions = it) }, label = { Text("Vejledning / noter") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aktiv", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Switch(draft.active, { draft = draft.copy(active = it) })
            }
        }
    }, confirmButton = { Button(enabled = draft.name.isNotBlank(), onClick = { onSave(draft.copy(name = draft.name.trim(), reminders = draft.reminders && !draft.asNeeded && draft.validTimes().isNotEmpty())) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}
