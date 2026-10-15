package dk.babyapp.ui.tracking

import android.app.DatePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.teeth.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.sin

private fun ToothRecord?.status() = when { this?.lost != null -> "Tabt"; this?.emerged != null -> "Frembrudt"; else -> "Ikke registreret" }

@Composable
internal fun TeethDialog(childId: String, records: List<ToothRecord>, onSave: (ToothRecord) -> Unit, onDismiss: () -> Unit) {
    var selected by remember(childId) { mutableStateOf<Int?>(null) }
    val teeth = records.filter { it.childId == childId }.associateBy { it.tooth }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tænder", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onDismiss) { Text("Luk") }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Mælketænder", style = MaterialTheme.typography.titleMedium)
                    Text("Tryk på en tand for at registrere frembrud eller tab.", style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Barnets højre", style = MaterialTheme.typography.labelSmall)
                        Text("Barnets venstre", style = MaterialTheme.typography.labelSmall)
                    }
                    Text("Overmund", style = MaterialTheme.typography.titleSmall)
                    ToothArch(upperMilkTeeth, teeth, false) { selected = it }
                    ToothArch(lowerMilkTeeth, teeth, true) { selected = it }
                    Text("Undermund", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(12.dp))
                    Text("○ Ikke registreret   ● Frembrudt   × Tabt", style = MaterialTheme.typography.bodySmall)
                    Text("${teeth.values.count { it.emerged != null && it.lost == null }} frembrudte · ${teeth.values.count { it.lost != null }} tabte", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    teeth.values.sortedBy { it.tooth }.forEach { tooth ->
                        TextButton(onClick = { selected = tooth.tooth }) { Text("Tand ${tooth.tooth}: ${tooth.status()}") }
                    }
                }
            }
        }
    }
    selected?.let { number ->
        ToothEditor(teeth[number] ?: ToothRecord(childId, number), onDismiss = { selected = null }, onSave = { onSave(it); selected = null })
    }
}

@Composable
private fun ToothArch(numbers: List<Int>, records: Map<Int, ToothRecord>, lower: Boolean, onSelect: (Int) -> Unit) {
    val outline = MaterialTheme.colorScheme.outline
    val filled = MaterialTheme.colorScheme.primaryContainer
    val lost = MaterialTheme.colorScheme.surfaceVariant
    BoxWithConstraints(Modifier.fillMaxWidth().height(216.dp)) {
        val half = maxWidth / 2
        numbers.forEachIndexed { index, number ->
            val distance = if (index < 5) 4 - index else index - 5
            val spread = 26.dp + (half - 50.dp) * sin(distance * Math.PI / 8).toFloat()
            val x = half - 24.dp + if (index < 5) -spread else spread
            val y = if (lower) 160 - distance * 40 else distance * 40
            val record = records[number]
            FilledTonalIconButton(onClick = { onSelect(number) }, modifier = Modifier.offset(x = x, y = y.dp).size(48.dp).semantics { contentDescription = "Tand $number, ${record.status()}" }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(42.dp)) {
                        val tooth = Path().apply {
                            moveTo(size.width * .18f, size.height * .1f)
                            cubicTo(size.width * .02f, 0f, 0f, size.height * .45f, size.width * .2f, size.height * .9f)
                            quadraticTo(size.width * .3f, size.height * 1.05f, size.width * .5f, size.height * .65f)
                            quadraticTo(size.width * .7f, size.height * 1.05f, size.width * .8f, size.height * .9f)
                            cubicTo(size.width, size.height * .45f, size.width * .98f, 0f, size.width * .82f, size.height * .1f)
                            quadraticTo(size.width * .5f, size.height * .2f, size.width * .18f, size.height * .1f)
                            close()
                        }
                        if (record != null) drawPath(tooth, if (record.lost != null) lost else filled)
                        drawPath(tooth, outline, style = Stroke(1.5.dp.toPx()))
                    }
                    Text(when { record?.lost != null -> "×"; record?.emerged != null -> "●"; else -> "○" }, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ToothEditor(record: ToothRecord, onDismiss: () -> Unit, onSave: (ToothRecord) -> Unit) {
    var emerged by remember(record) { mutableStateOf(record.emerged?.let(LocalDate::parse)) }
    var lost by remember(record) { mutableStateOf(record.lost?.let(LocalDate::parse)) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Tand ${record.tooth}") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val jaw = if (record.tooth / 10 in listOf(5, 6)) "Overmund" else "Undermund"
            val side = if (record.tooth / 10 in listOf(5, 8)) "højre" else "venstre"
            val kind = when (record.tooth % 10) { 1 -> "midterste fortand"; 2 -> "yderste fortand"; 3 -> "hjørnetand"; 4 -> "forreste kindtand"; else -> "bagerste kindtand" }
            Text("$jaw · barnets $side · $kind", style = MaterialTheme.typography.bodySmall)
            ToothDate("Frembrud", emerged) { emerged = it; error = null }
            ToothDate("Tabt", lost) { lost = it; error = null }
            Text("Datoen for frembrud må gerne være ukendt, selv om tanden er tabt.", style = MaterialTheme.typography.bodySmall)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = {
        val updated = record.copy(emerged = emerged?.toString(), lost = lost?.toString())
        runCatching { updated.validate() }.onSuccess { onSave(updated) }.onFailure { error = it.message }
    }) { Text("Gem") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } })
}

@Composable
private fun ToothDate(label: String, value: LocalDate?, onChange: (LocalDate?) -> Unit) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(modifier = Modifier.weight(1f), onClick = {
            val date = value ?: LocalDate.now()
            DatePickerDialog(context, { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) }, date.year, date.monthValue - 1, date.dayOfMonth).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }) { Text("$label: ${value?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Ikke angivet"}") }
        if (value != null) TextButton(onClick = { onChange(null) }) { Text("Ryd") }
    }
}
