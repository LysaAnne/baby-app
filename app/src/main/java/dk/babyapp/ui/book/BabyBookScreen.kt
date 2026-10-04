package dk.babyapp.ui.book

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.book.*
import dk.babyapp.data.profile.ChildProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun BabyBookScreen(child: ChildProfile?, pages: List<BabyBookPage>, contentPadding: PaddingValues,
    onSave: suspend (BabyBookPage) -> Unit, photoFile: (String?) -> File?, onPhotoSelected: suspend (Uri) -> String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val childPages = pages.filter { it.childId == child?.id }
    var chapter by rememberSaveable(child?.id) { mutableStateOf<String?>(null) }
    var editingId by rememberSaveable(child?.id) { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var exportOpen by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    // Snapshot the selected child's book before Android opens its file picker.
    var exportText by rememberSaveable { mutableStateOf("") }
    var exportPhotos by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            exporting = true
            message = runCatching {
                withContext(Dispatchers.IO) {
                    ZipOutputStream(requireNotNull(context.contentResolver.openOutputStream(uri))).use { zip ->
                        zip.putNextEntry(ZipEntry("Barnets bog.txt")); zip.write(exportText.toByteArray(Charsets.UTF_8)); zip.closeEntry()
                        exportPhotos.forEach { name ->
                            val file = requireNotNull(photoFile(name)) { "Et billede mangler: $name" }
                            zip.putNextEntry(ZipEntry("billeder/${File(name).name}")); file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
                        }
                    }
                }
                "Barnets bog er eksporteret med tekst og billeder."
            }.getOrElse { "Eksporten mislykkedes. Prøv igen. ${it.message.orEmpty()}" }
            exporting = false
        }
    }
    val bookListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(child?.id, chapter) { bookListState.scrollToItem(0) }
    LazyColumn(state = bookListState, contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + 16.dp, bottom = contentPadding.calculateBottomPadding() + 24.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Barnets bog", style = MaterialTheme.typography.headlineSmall) }
        if (child == null) item { Text("Tilføj et barn under Familie for at begynde bogen.") }
        else {
            item {
                Text("${child.name} · det første år", style = MaterialTheme.typography.titleMedium)
                Text("Skriv lidt ad gangen. Alle felter er frivillige, og kapitlerne kan udfyldes i din egen rækkefølge.")
                OutlinedButton(enabled = !exporting, onClick = { exportOpen = true }) { Text(if (exporting) "Eksporterer…" else "Eksportér hele bogen") }
            }
            if (chapter == null) {
                items(babyBookTemplates.groupBy { it.chapter }.toList(), key = { it.first }) { (title, templates) ->
                    val filled = childPages.count { page -> templates.any { it.id == page.pageId } && (page.answers().values.any(String::isNotBlank) || page.photos().isNotEmpty()) }
                    Card(Modifier.fillMaxWidth().clickable { chapter = title }) {
                        Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text("$filled af ${templates.size} sider påbegyndt", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            } else {
                item { FilledTonalButton(onClick = { chapter = null }, modifier = Modifier.fillMaxWidth()) { Text("‹ Alle kapitler") }; Text(chapter!!, style = MaterialTheme.typography.titleLarge) }
                items(babyBookTemplates.filter { it.chapter == chapter }, key = { it.id }) { template ->
                    val saved = childPages.firstOrNull { it.pageId == template.id }
                    val completed = saved?.answers()?.values?.count(String::isNotBlank) ?: 0
                    val fieldCount = (template.prompts + saved?.answers()?.keys.orEmpty()).distinct().size
                    Card(Modifier.fillMaxWidth().clickable { editingId = template.id }) {
                        Column(Modifier.padding(16.dp)) { Text(template.title, style = MaterialTheme.typography.titleMedium); Text("$completed af $fieldCount felter · ${saved?.photos()?.size ?: 0} billeder", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                item { FilledTonalButton(onClick = { chapter = null }, modifier = Modifier.fillMaxWidth()) { Text("‹ Alle kapitler") } }
            }
        }
    }
    if (exportOpen && child != null) AlertDialog(onDismissRequest = { exportOpen = false }, title = { Text("Eksportér Barnets bog") }, text = { Text("Du får en ZIP-fil med alle gemte tekster i en redigerbar tekstfil og billeder i en separat mappe. Pak den ud og kopiér teksten til din fotobog.") }, confirmButton = { Button(onClick = {
        exportText = exportBabyBook(child.name, childPages); exportPhotos = childPages.flatMap { it.photos() }.distinct()
        exportOpen = false; export.launch("Barnets-bog.zip")
    }) { Text("Vælg placering") } }, dismissButton = { TextButton(onClick = { exportOpen = false }) { Text("Annuller") } })
    BackHandler(enabled = chapter != null && editingId == null) { chapter = null }
    val template = babyBookTemplates.firstOrNull { it.id == editingId }
    if (template != null && child != null) key(child.id, template.id) {
        BookPageEditor(child, template, childPages.firstOrNull { it.pageId == template.id }, onSave, photoFile, onPhotoSelected, onDismiss = { editingId = null }, onAllChapters = { editingId = null; chapter = null })
    }
    message?.let { text -> AlertDialog(onDismissRequest = { message = null }, title = { Text("Eksport") }, text = { Text(text) }, confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } }) }
}

@Composable
internal fun BookPageEditor(child: ChildProfile, template: BookPageTemplate, saved: BabyBookPage?,
    onSave: suspend (BabyBookPage) -> Unit, photoFile: (String?) -> File?, onPhotoSelected: suspend (Uri) -> String, onDismiss: () -> Unit, onAllChapters: () -> Unit = onDismiss) {
    var answersJson by rememberSaveable { mutableStateOf(saved?.answersJson ?: "{}") }
    var photosJson by rememberSaveable { mutableStateOf(saved?.photosJson ?: "[]") }
    var saving by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    var exitToChapters by remember { mutableStateOf(false) }
    val leave = { if (exitToChapters) onAllChapters() else onDismiss() }
    var error by remember { mutableStateOf<String?>(null) }
    val answers = Json.decodeFromString<Map<String, String>>(answersJson)
    val photos = Json.decodeFromString<List<String>>(photosJson)
    val scope = rememberCoroutineScope()
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            saving = true
            runCatching { onPhotoSelected(uri) }.onSuccess { name -> photosJson = Json.encodeToString(Json.decodeFromString<List<String>>(photosJson) + name) }.onFailure { error = "Billedet kunne ikke tilføjes." }
            saving = false
        }
    }
    val dismiss = { if (!saving) { if (answersJson != (saved?.answersJson ?: "{}") || photosJson != (saved?.photosJson ?: "[]")) discard = true else leave() }; Unit }
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(enabled = !saving, onClick = { exitToChapters = true; dismiss() }, modifier = Modifier.fillMaxWidth()) { Text("‹ Alle kapitler") }
                Text(template.title, style = MaterialTheme.typography.headlineSmall)
                Text(child.name, style = MaterialTheme.typography.labelLarge)
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (template.id == "birth-facts") TextButton(enabled = !saving, onClick = {
                        val birth = mapOf("Dato" to child.birthDate?.toString().orEmpty(), "Klokkeslæt" to child.birthTime?.toString().orEmpty(), "Fødested" to child.hospital, "Graviditetsuge" to listOfNotNull(child.gestationalWeeks?.toString(), child.gestationalDays?.toString()).joinToString(" + "), "Vægt (g)" to child.birthWeightGrams?.toString().orEmpty(), "Længde (cm)" to child.birthLengthCm?.toString().orEmpty(), "Hovedomfang (cm)" to child.birthHeadCircumferenceCm?.toString().orEmpty())
                        answersJson = Json.encodeToString(birth + answers.filterValues(String::isNotBlank))
                    }) { Text("Udfyld tomme felter fra barnets profil") }
                    (template.prompts + answers.keys).distinct().forEach { prompt -> OutlinedTextField(value = answers[prompt].orEmpty(), onValueChange = { answersJson = Json.encodeToString(answers + (prompt to it)) }, label = { Text(prompt) }, modifier = Modifier.fillMaxWidth(), minLines = if (prompt.contains("historien", true) || prompt.contains("minde", true)) 4 else 2, enabled = !saving) }
                    Text("Billeder", style = MaterialTheme.typography.titleMedium)
                    Text("Tilføj fotos, scanninger eller billeder af små minder. Appen gemmer billedkopier i reduceret størrelse; behold originalerne til tryk.", style = MaterialTheme.typography.bodySmall)
                    photos.forEachIndexed { index, name ->
                        val file = photoFile(name)
                        val bitmap = remember(name) { file?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() } }
                        if (bitmap != null) Image(bitmap, "Billede ${index + 1}", Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Fit)
                        else Text("Billedet kunne ikke åbnes")
                        TextButton(enabled = !saving, onClick = { photosJson = Json.encodeToString(photos.filterIndexed { i, _ -> i != index }) }) { Text("Fjern billede ${index + 1}") }
                    }
                    if (photos.size < 12) OutlinedButton(enabled = !saving, onClick = { pickPhoto.launch("image/*") }) { Text("Tilføj billede") }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                FilledTonalButton(enabled = !saving, onClick = { exitToChapters = true; dismiss() }, modifier = Modifier.fillMaxWidth()) { Text("‹ Alle kapitler") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(enabled = !saving, onClick = { exitToChapters = false; dismiss() }) { Text("Annuller") }
                    Button(enabled = !saving, onClick = { scope.launch {
                        saving = true; error = null
                        runCatching { onSave(BabyBookPage(child.id, template.id, answersJson, photosJson)) }.onSuccess { onDismiss() }.onFailure { error = "Kunne ikke gemme. Prøv igen." }
                        saving = false
                    } }) { Text(if (saving) "Gemmer…" else "Gem") }
                }
            }
        }
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false; exitToChapters = false }, title = { Text("Kassér ændringer?") }, text = { Text("Dine seneste ændringer er ikke gemt.") }, confirmButton = { TextButton(onClick = leave) { Text("Kassér") } }, dismissButton = { TextButton(onClick = { discard = false; exitToChapters = false }) { Text("Fortsæt med at skrive") } })
}
