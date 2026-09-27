package dk.babyapp.ui.tracking

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.babyapp.data.medicine.MedicinePlan
import java.time.LocalTime

@Composable
internal fun MedicineCardDialog(childId: String, all: List<MedicinePlan>, onSave: (List<MedicinePlan>) -> Unit, onDismiss: () -> Unit) {
    var editing by remember { mutableStateOf<MedicinePlan?>(null) }
    var deleting by remember { mutableStateOf<MedicinePlan?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Medicinkort") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Barnets medicin og dosering efter den aftale, I har med behandleren.")
            val plans = all.filter { it.childId == childId }
            if (plans.isEmpty()) Text("Der er endnu ingen medicin på kortet.")
            plans.forEach { plan -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                Text(plan.dose)
                Text(if (plan.asNeeded) "PN · efter behov" else plan.times.joinToString(" · ").ifBlank { "Ingen faste tidspunkter" })
                Text(if (!plan.active) "Afsluttet" else if (plan.reminders && !plan.asNeeded) "Påmindelser slået til" else "Ingen påmindelser", style = MaterialTheme.typography.bodySmall)
                if (plan.instructions.isNotBlank()) Text(plan.instructions)
                Row { TextButton(onClick = { editing = plan }) { Text("Rediger") }; TextButton(onClick = { deleting = plan }) { Text("Slet") } }
            } } }
            Button(onClick = { editing = MedicinePlan(childId = childId, name = "") }) { Text("Tilføj medicin") }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Luk") } })
    editing?.let { plan -> MedicinePlanEditor(plan, { editing = null }) { updated -> onSave(all.filterNot { it.id == updated.id } + updated); editing = null } }
    deleting?.let { plan -> AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Slet fra medicinkort?") }, text = { Text("${plan.name} fjernes fra kortet, og påmindelser stoppes. Tidligere registreringer bevares i journalen.") }, confirmButton = { Button(onClick = { onSave(all.filterNot { it.id == plan.id }); deleting = null }) { Text("Slet") } }, dismissButton = { TextButton(onClick = { deleting = null }) { Text("Annuller") } }) }
}

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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Medicin og påmindelser") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectionDropdown("Vælg type eller skriv eget navn", draft.name.ifBlank { "Vælg" }, listOf("D-vitamin", "Smertestillende/febernedsættende", "Antibiotika", "Allergimedicin", "Inhalationsmedicin").map { it to it }) { draft = draft.copy(name = it) }
            OutlinedTextField(draft.name, { draft = draft.copy(name = it) }, label = { Text("Medicinens navn *") })
            OutlinedTextField(draft.dose, { draft = draft.copy(dose = it) }, label = { Text("Dosis og styrke") })
            OutlinedTextField(draft.instructions, { draft = draft.copy(instructions = it) }, label = { Text("Vejledning / noter") })
            Row { Checkbox(draft.active, { draft = draft.copy(active = it) }); Text("Aktiv medicin", Modifier.padding(top = 12.dp)) }
            Row { Checkbox(draft.asNeeded, { draft = draft.copy(asNeeded = it, reminders = if (it) false else draft.reminders) }); Text("PN · efter behov", Modifier.padding(top = 12.dp)) }
            if (!draft.asNeeded) {
                Text("Daglige tidspunkter", style = MaterialTheme.typography.titleSmall)
                draft.validTimes().forEach { time -> Row { Text(time.toString(), Modifier.weight(1f).padding(top = 12.dp)); TextButton(onClick = { draft = draft.copy(times = draft.times - time.toString()) }) { Text("Fjern") } } }
                TextButton(onClick = { TimePickerDialog(context, { _, h, m -> draft = draft.copy(times = (draft.times + LocalTime.of(h, m).toString()).distinct().sorted()) }, 8, 0, true).show() }) { Text("Tilføj tidspunkt") }
                Row { Checkbox(draft.reminders, { draft = draft.copy(reminders = it) }); Text("Påmind mig", Modifier.padding(top = 12.dp)) }
                if (draft.reminders) {
                    if (!notificationsAllowed) {
                        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text("Tillad notifikationer") }
                    }
                    if (Build.VERSION.SDK_INT >= 31 && !exactAllowed) {
                        Text("Tillad præcise alarmer for påmindelser på de valgte klokkeslæt. Ellers kan Android forsinke dem.", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) }) { Text("Tillad præcise alarmer") }
                    }
                }
            }
        }
    }, confirmButton = { Button(enabled = draft.name.isNotBlank() && (!draft.reminders || draft.asNeeded || draft.times.isNotEmpty()), onClick = { onSave(draft.copy(name = draft.name.trim(), reminders = draft.reminders && !draft.asNeeded)) }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}
