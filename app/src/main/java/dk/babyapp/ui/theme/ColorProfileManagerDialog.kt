package dk.babyapp.ui.theme

import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.color.ColorProfile
import dk.babyapp.data.color.duplicate
import dk.babyapp.data.color.normalizedHex
import java.util.UUID

@Composable
fun ColorProfileManagerDialog(
    profiles: List<ColorProfile>,
    usedProfileIds: Set<String>,
    onSave: (ColorProfile) -> Unit,
    onDelete: (String, (Boolean) -> Unit) -> Unit,
    onMove: (String, Int) -> Unit,
    exportJson: () -> String,
    onImport: (String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var editing by remember { mutableStateOf<ColorProfile?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(exportJson()) }
        }.onSuccess { message = "Farveprofilerne er eksporteret." }.onFailure { message = "Filen kunne ikke gemmes." }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val value = runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } }.getOrNull()
            if (value == null) message = "Filen kunne ikke læses." else onImport(value) { success ->
                message = if (success) "Farveprofilerne er importeret." else "Filen indeholder ikke gyldige farveprofiler."
            }
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Rediger farveprofiler", style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onDismiss) { Text("Luk") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { editing = newColorProfile(profiles) }) { Icon(Icons.Outlined.Add, null); Text("Ny") }
                    OutlinedButton(onClick = { exportLauncher.launch("child_color_profiles.json") }) { Text("Eksportér") }
                    OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }) { Text("Importér") }
                }
                Text("Tryk på et kort for at redigere farverne. Ændringer gemmes lokalt med det samme.")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(profiles, key = { it.id }) { profile ->
                        val group = profiles.filter { it.isDark == profile.isDark }
                        val position = group.indexOfFirst { it.id == profile.id }
                        ProfileManagerCard(
                            profile = profile,
                            isUsed = profile.id in usedProfileIds,
                            onEdit = { editing = profile },
                            onDuplicate = { onSave(profile.duplicate()) },
                            canMoveUp = position > 0,
                            canMoveDown = position in 0 until group.lastIndex,
                            onMoveUp = { onMove(profile.id, -1) },
                            onMoveDown = { onMove(profile.id, 1) },
                            onDelete = {
                                onDelete(profile.id) { deleted ->
                                    message = if (deleted) "Profilen er slettet." else "Profilen bruges af et barn og kan ikke slettes."
                                }
                            },
                        )
                    }
                }
            }
        }
    }
    editing?.let { profile -> ColorProfileEditDialog(profile, onSave = { onSave(it); editing = null }, onDismiss = { editing = null }) }
    message?.let { value -> AlertDialog(onDismissRequest = { message = null }, text = { Text(value) }, confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } }) }
}

@Composable
private fun ProfileManagerCard(
    profile: ColorProfile,
    isUsed: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = profile.colors()
    val foreground = if (profile.isDark) Color.White else Color(0xFF171817)
    Card(onClick = onEdit, colors = CardDefaults.cardColors(containerColor = colors.background, contentColor = foreground)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(profile.name, style = MaterialTheme.typography.titleMedium); Text(if (profile.isDark) "Mørk" else "Lys") }
                Text(if (isUsed) "I brug" else "")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp) { Icon(Icons.Outlined.ArrowUpward, "Flyt op") }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) { Icon(Icons.Outlined.ArrowDownward, "Flyt ned") }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Rediger") }
                IconButton(onClick = onDuplicate) { Icon(Icons.Outlined.ContentCopy, "Duplikér") }
                IconButton(onClick = onDelete, enabled = !isUsed) { Icon(Icons.Outlined.Delete, if (isUsed) "Profilen er i brug" else "Slet") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(colors.primary, colors.primaryContainer, colors.secondary, colors.tertiary).forEach {
                    Surface(Modifier.size(34.dp), color = it, border = BorderStroke(1.dp, foreground.copy(alpha = .25f))) {}
                }
            }
        }
    }
}

@Composable
private fun ColorProfileEditDialog(initial: ColorProfile, onSave: (ColorProfile) -> Unit, onDismiss: () -> Unit) {
    var draft by remember(initial.id) { mutableStateOf(initial) }
    var picking by remember { mutableStateOf<Pair<String, String>?>(null) }
    var todayPreviewOpen by remember { mutableStateOf(false) }
    val fields = listOf(
        "Baggrund" to draft.background,
        "Primære knapper" to draft.primary,
        "Fremhævede kort" to draft.primaryContainer,
        "Sekundær accent" to draft.secondary,
        "Tredje accent" to draft.tertiary,
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val preview = runCatching { draft.colors() }.getOrNull()
        Surface(Modifier.fillMaxSize(), color = preview?.background ?: MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Rediger farveprofil", style = MaterialTheme.typography.headlineSmall, color = if (draft.isDark) Color.White else Color.Black)
                OutlinedTextField(draft.name, { draft = draft.copy(name = it) }, label = { Text("Navn") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) { Text("Mørk profil", color = if (draft.isDark) Color.White else Color.Black); Switch(draft.isDark, { draft = draft.copy(isDark = it) }) }
                preview?.let { colors ->
                    LivePalettePreview(draft, colors)
                    val foreground = if (draft.isDark) Color.White else Color(0xFF171817)
                    val buttonForeground = if (draft.isDark) Color(0xFF071315) else Color.White
                    if (contrastRatio(colors.background, foreground) < 4.5 || contrastRatio(colors.primary, buttonForeground) < 4.5) {
                        Text("⚠ Farverne har lav kontrast. Tekst kan være svær at læse.", color = if (draft.isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A))
                    }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(fields) { (label, value) ->
                        ColorField(label, value, onClick = { picking = label to value }, onHexChange = { update -> draft = draft.withColor(label, update) })
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        enabled = draft.name.isNotBlank() && fields.all { it.second.normalizedHex() != null },
                        onClick = { todayPreviewOpen = true },
                    ) { Text("Forhåndsvis") }
                    TextButton(onClick = onDismiss) { Text("Annuller") }
                    Button(enabled = draft.name.isNotBlank() && fields.all { it.second.normalizedHex() != null }, onClick = { onSave(draft.normalizeColors()) }) { Text("Gem") }
                }
            }
        }
    }
    picking?.let { (label, value) -> ColorPickerDialog(label, value, onSelect = { draft = draft.withColor(label, it); picking = null }, onDismiss = { picking = null }) }
    if (todayPreviewOpen) TodayPalettePreviewDialog(draft.normalizeColors(), onDismiss = { todayPreviewOpen = false })
}

@Composable
private fun TodayPalettePreviewDialog(profile: ColorProfile, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BabyAppTheme(childColorTheme = profile) {
            Scaffold(
                topBar = {
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, shape = MaterialTheme.shapes.large) {
                                Text("🧸 Eksempelbarn⌄", Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium)
                            }
                            TextButton(onClick = onDismiss) { Text("Tilbage") }
                        }
                    }
                },
                bottomBar = {
                    NavigationBar {
                        listOf("I dag", "Tidslinje", "Overblik", "Guide", "Familie").forEachIndexed { index, label ->
                            NavigationBarItem(selected = index == 0, onClick = {}, icon = { Text(listOf("●", "◷", "▥", "♡", "♧")[index]) }, label = { Text(label) })
                        }
                    }
                },
            ) { padding ->
                LazyColumn(Modifier.fillMaxSize().padding(padding).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Dagens overblik", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Amning: 24 min.  •  Flaske: 120 ml  •  Bleer: 4", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    item { Text("Hurtig registrering  ⚙", style = MaterialTheme.typography.titleLarge) }
                    item { PreviewActionSection("🍼  Madning", listOf("Venstre bryst", "Højre bryst", "Flaske", "Pumpning")) }
                    item { PreviewActionSection("🧷  Ble", listOf("Våd", "Afføring", "Begge", "Tør")) }
                    item {
                        OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("＋  Manuel registrering") }
                    }
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("Seneste", style = MaterialTheme.typography.titleLarge)
                                Text("Flaske · 120 ml · kl. 10.42")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewActionSection(title: String, actions: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            actions.forEach { action ->
                Button(onClick = {}, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 3.dp)) {
                    Text(action, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun LivePalettePreview(profile: ColorProfile, colors: ChildPaletteColors) {
    val foreground = if (profile.isDark) Color.White else Color(0xFF171817)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = colors.primary, contentColor = if (profile.isDark) Color(0xFF071315) else Color.White, shape = MaterialTheme.shapes.medium) {
                Text("Knap", Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
            }
            Surface(color = colors.primaryContainer, contentColor = foreground, shape = MaterialTheme.shapes.medium) {
                Text("Fremhævet kort", Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(colors.secondary, colors.tertiary).forEach { color ->
                Surface(Modifier.size(36.dp), color = color, border = BorderStroke(1.dp, foreground.copy(alpha = .25f))) {}
            }
        }
    }
}

@Composable
private fun ColorField(label: String, value: String, onClick: () -> Unit, onHexChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        val color = runCatching { Color(AndroidColor.parseColor(value)) }.getOrDefault(Color.Transparent)
        Surface(Modifier.size(48.dp).clip(MaterialTheme.shapes.medium).clickable(onClick = onClick), color = color, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {}
        OutlinedTextField(value, onHexChange, label = { Text(label) }, modifier = Modifier.weight(1f), singleLine = true)
    }
}

@Composable
private fun ColorPickerDialog(label: String, initial: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val initialColor = runCatching { AndroidColor.parseColor(initial) }.getOrDefault(AndroidColor.WHITE)
    val hsv = FloatArray(3).also { AndroidColor.colorToHSV(initialColor, it) }
    var hue by remember { mutableFloatStateOf(hsv[0]) }
    var saturation by remember { mutableFloatStateOf(hsv[1]) }
    var brightness by remember { mutableFloatStateOf(hsv[2]) }
    var hex by remember { mutableStateOf(initial.normalizedHex() ?: "#FFFFFF") }
    fun updateFromSliders() { hex = String.format("#%06X", 0xFFFFFF and AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness))) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(Modifier.fillMaxWidth().size(90.dp), color = runCatching { Color(AndroidColor.parseColor(hex)) }.getOrDefault(Color.Transparent)) {}
                Text("Farvetone"); Slider(hue, { hue = it; updateFromSliders() }, valueRange = 0f..360f)
                Text("Farvestyrke"); Slider(saturation, { saturation = it; updateFromSliders() })
                Text("Lysstyrke"); Slider(brightness, { brightness = it; updateFromSliders() })
                OutlinedTextField(hex, { value -> value.normalizedHex()?.let { hex = it } ?: run { hex = value } }, label = { Text("HEX-kode") })
            }
        },
        confirmButton = { Button(enabled = hex.normalizedHex() != null, onClick = { onSelect(hex.normalizedHex()!!) }) { Text("Vælg") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
    )
}

private fun newColorProfile(profiles: List<ColorProfile>): ColorProfile = profiles.firstOrNull()?.copy(
    id = UUID.randomUUID().toString(), name = "Ny farveprofil", builtIn = false,
) ?: ColorProfile(name = "Ny farveprofil", isDark = false, background = "#F7F7F7", primary = "#52665A", primaryContainer = "#D5E5DA", secondary = "#655D55", tertiary = "#50636A")

private fun ColorProfile.withColor(label: String, value: String) = when (label) {
    "Baggrund" -> copy(background = value); "Primære knapper" -> copy(primary = value); "Fremhævede kort" -> copy(primaryContainer = value)
    "Sekundær accent" -> copy(secondary = value); else -> copy(tertiary = value)
}

private fun ColorProfile.normalizeColors() = copy(
    background = background.normalizedHex()!!, primary = primary.normalizedHex()!!, primaryContainer = primaryContainer.normalizedHex()!!,
    secondary = secondary.normalizedHex()!!, tertiary = tertiary.normalizedHex()!!,
)

private fun contrastRatio(first: Color, second: Color): Double {
    val light = maxOf(first.luminance(), second.luminance()).toDouble()
    val dark = minOf(first.luminance(), second.luminance()).toDouble()
    return (light + 0.05) / (dark + 0.05)
}
