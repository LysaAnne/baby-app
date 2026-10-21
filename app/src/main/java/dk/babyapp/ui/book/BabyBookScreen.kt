package dk.babyapp.ui.book

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChildFriendly
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.book.*
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.domain.bookAgeMonths
import dk.babyapp.domain.bookPageAvailable
import dk.babyapp.domain.bookPageHasContent
import dk.babyapp.domain.nextBookPage
import dk.babyapp.ui.theme.BabySpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val bookChapters = babyBookTemplates.groupBy { it.chapter }

private fun chapterIcon(chapter: String): ImageVector = when (chapter) {
    "Før du kom" -> Icons.Outlined.FavoriteBorder
    "Fødslen" -> Icons.Outlined.ChildFriendly
    "Den første tid hjemme" -> Icons.Outlined.Home
    "Måned for måned" -> Icons.Outlined.CalendarMonth
    "De første gange" -> Icons.Outlined.StarOutline
    "Det særlige ved dig" -> Icons.Outlined.AutoAwesome
    "Verden omkring dig" -> Icons.Outlined.Public
    "Din første fødselsdag" -> Icons.Outlined.Cake
    else -> Icons.AutoMirrored.Outlined.MenuBook
}

private enum class PageStatus { Empty, Started, Done }

/** Done when every question has an answer; started when anything is written or a photo is added. */
private fun pageStatus(template: BookPageTemplate, page: BabyBookPage?): PageStatus {
    if (!bookPageHasContent(page)) return PageStatus.Empty
    val answers = page!!.answers()
    return if (template.prompts.all { answers[it]?.isNotBlank() == true }) PageStatus.Done else PageStatus.Started
}

private fun monthsLabel(months: Int) = when (months) { 0 -> "nyfødt"; 1 -> "1 måned"; else -> "$months måneder" }

@Composable
fun BabyBookScreen(child: ChildProfile?, pages: List<BabyBookPage>, contentPadding: PaddingValues,
    onSave: suspend (BabyBookPage) -> Unit, photoFile: (String?) -> File?, onPhotoSelected: suspend (Uri) -> String, initialPageId: String? = null,
    reminderSettings: (@Composable (onDismiss: () -> Unit) -> Unit)? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val childPages = pages.filter { it.childId == child?.id }
    val today = LocalDate.now()
    var chapter by rememberSaveable(child?.id) { mutableStateOf(babyBookTemplates.firstOrNull { it.id == initialPageId }?.chapter) }
    var editingId by rememberSaveable(child?.id) { mutableStateOf(initialPageId?.takeIf { id -> babyBookTemplates.any { it.id == id } }) }
    var message by remember { mutableStateOf<String?>(null) }
    var exportOpen by remember { mutableStateOf(false) }
    var remindersOpen by remember { mutableStateOf(false) }
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
    fun pageOf(template: BookPageTemplate) = childPages.firstOrNull { it.pageId == template.id }
    // Retired pages only show when something is written on them.
    val chapters = bookChapters.mapValues { (_, templates) -> templates.filter { !it.retired || bookPageHasContent(pageOf(it)) } }
    val allPages = chapters.values.flatten()
    val bookListState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(child?.id, chapter) { bookListState.scrollToItem(0) }
    LazyColumn(state = bookListState, contentPadding = PaddingValues(top = contentPadding.calculateTopPadding() + BabySpacing.l, bottom = contentPadding.calculateBottomPadding() + BabySpacing.xl, start = BabySpacing.l, end = BabySpacing.l), verticalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
        val open = chapter
        if (child == null) {
            item { Text("Barnets bog", style = MaterialTheme.typography.headlineSmall) }
            item { Text("Tilføj et barn under Familie for at begynde bogen.") }
        } else if (open == null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Barnets bog", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        if (reminderSettings != null) remindersOpen = true
                        else context.startActivity(android.content.Intent(android.provider.Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                            .putExtra(android.provider.Settings.EXTRA_CHANNEL_ID, dk.babyapp.reminders.BOOK_REMINDER_CHANNEL))
                    }) { Icon(Icons.Outlined.NotificationsNone, contentDescription = "Bogpåmindelser") }
                    if (exporting) CircularProgressIndicator(Modifier.padding(12.dp).size(24.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = { exportOpen = true }) { Icon(Icons.Outlined.Download, contentDescription = "Eksportér hele bogen") }
                }
            }
            item {
                val started = allPages.count { bookPageHasContent(pageOf(it)) }
                val photoCount = childPages.sumOf { it.photos().size }
                val coverPhoto = pageOf(babyBookTemplates.first())?.photos()?.firstOrNull() ?: child.photoFileName ?: childPages.firstNotNullOfOrNull { it.photos().firstOrNull() }
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(BabySpacing.l), horizontalArrangement = Arrangement.spacedBy(BabySpacing.l), verticalAlignment = Alignment.CenterVertically) {
                        PhotoThumb(photoFile(coverPhoto), Icons.AutoMirrored.Outlined.MenuBook, 60.dp, CircleShape, MaterialTheme.colorScheme.surface)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${child.name} · det første år", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("$started af ${allPages.size} sider · $photoCount ${if (photoCount == 1) "billede" else "billeder"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            LinearProgressIndicator(progress = { started / allPages.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp), trackColor = MaterialTheme.colorScheme.surface, drawStopIndicator = {})
                        }
                    }
                }
            }
            nextBookPage(child, childPages, today)?.let { next ->
                item {
                    val months = child.bookAgeMonths(today)
                    val subtitle = if (next.id == "month-$months") "${child.name} er ${monthsLabel(months!!)}" else next.chapter
                    OutlinedCard(onClick = { chapter = next.chapter; editingId = next.id }, modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                        Row(Modifier.padding(horizontal = BabySpacing.l, vertical = BabySpacing.m), horizontalArrangement = Arrangement.spacedBy(BabySpacing.m), verticalAlignment = Alignment.CenterVertically) {
                            IconCircle(chapterIcon(next.chapter))
                            Column(Modifier.weight(1f)) {
                                Text("Næste side", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(next.title, style = MaterialTheme.typography.titleMedium)
                                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            items(chapters.toList().chunked(2), key = { row -> row.first().first }) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
                    row.forEach { (title, templates) ->
                        val filled = templates.count { bookPageHasContent(pageOf(it)) }
                        val photo = templates.firstNotNullOfOrNull { pageOf(it)?.photos()?.firstOrNull() }
                        OutlinedCard(onClick = { chapter = title }, modifier = Modifier.weight(1f), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
                            Column(Modifier.padding(BabySpacing.s), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PhotoThumb(photoFile(photo), chapterIcon(title), 72.dp, MaterialTheme.shapes.small, MaterialTheme.colorScheme.primaryContainer, fillWidth = true)
                                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                                Text("$filled af ${templates.size} sider", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LinearProgressIndicator(progress = { filled / templates.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp), trackColor = MaterialTheme.colorScheme.primaryContainer, drawStopIndicator = {})
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item { Text("Skriv lidt ad gangen. Alle felter er frivillige, og kapitlerne kan udfyldes i den rækkefølge, du vil.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            val templates = chapters[open].orEmpty()
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { chapter = null }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Alle kapitler") }
                    Text(open, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                }
                val filled = templates.count { bookPageHasContent(pageOf(it)) }
                Row(Modifier.padding(start = BabySpacing.m), horizontalArrangement = Arrangement.spacedBy(BabySpacing.m), verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(progress = { filled / templates.size.toFloat() }, modifier = Modifier.weight(1f), trackColor = MaterialTheme.colorScheme.primaryContainer, drawStopIndicator = {})
                    Text("$filled af ${templates.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (open == "Måned for måned") item {
                GrowthPhotos(templates, ::pageOf, photoFile) { editingId = it }
            }
            item {
                val months = child.bookAgeMonths(today)
                Surface(shape = MaterialTheme.shapes.large, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)), color = MaterialTheme.colorScheme.surface) {
                    Column {
                        templates.forEachIndexed { index, template ->
                            val saved = pageOf(template)
                            val status = pageStatus(template, saved)
                            val current = template.id == "month-$months"
                            val available = bookPageAvailable(template, child, today)
                            val subtitle = when {
                                current && status == PageStatus.Empty -> "Nu · ${child.name} er ${monthsLabel(months!!)}"
                                status != PageStatus.Empty -> {
                                    val answered = saved!!.answers().values.count(String::isNotBlank)
                                    val fields = (template.prompts + saved.answers().keys).distinct().size
                                    val photos = saved.photos().size
                                    "$answered af $fields felter · $photos ${if (photos == 1) "billede" else "billeder"}"
                                }
                                !available && template.id.startsWith("month-") && child.birthDate != null -> {
                                    val days = ChronoUnit.DAYS.between(today, child.birthDate.plusMonths(template.id.removePrefix("month-").toLong()))
                                    if (days > 0) "Om $days ${if (days == 1L) "dag" else "dage"}" else "Ikke skrevet endnu"
                                }
                                !available -> "Kommer senere"
                                else -> "Ikke skrevet endnu"
                            }
                            Row(
                                Modifier.fillMaxWidth().clickable { editingId = template.id }
                                    .background(if (current) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = BabySpacing.m, vertical = BabySpacing.s)
                                    .alpha(if (available || status != PageStatus.Empty) 1f else 0.55f),
                                horizontalArrangement = Arrangement.spacedBy(BabySpacing.m), verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PhotoThumb(photoFile(saved?.photos()?.firstOrNull()), if (current) Icons.Outlined.StarOutline else chapterIcon(open), 40.dp, MaterialTheme.shapes.small, MaterialTheme.colorScheme.primaryContainer)
                                Column(Modifier.weight(1f)) {
                                    Text(template.title, style = MaterialTheme.typography.titleSmall)
                                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusDot(status)
                            }
                            if (index < templates.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(BabySpacing.l, Alignment.CenterHorizontally)) {
                    PageStatus.entries.forEach { status ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(status)
                            Text(when (status) { PageStatus.Done -> "Udfyldt"; PageStatus.Started -> "Påbegyndt"; PageStatus.Empty -> "Tom" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (exportOpen && child != null) AlertDialog(onDismissRequest = { exportOpen = false }, title = { Text("Eksportér Barnets bog") }, text = { Text("Du får en ZIP-fil med alle tekster i en redigerbar fil og billederne i en mappe. Billederne i fuld størrelse ligger desuden i Billeder › Barnets bog på telefonen.") }, confirmButton = { Button(onClick = {
        exportText = exportBabyBook(child.name, childPages); exportPhotos = childPages.flatMap { it.photos() }.distinct()
        exportOpen = false; export.launch("Barnets-bog.zip")
    }) { Text("Vælg placering") } }, dismissButton = { TextButton(onClick = { exportOpen = false }) { Text("Annuller") } })
    if (remindersOpen) reminderSettings?.invoke { remindersOpen = false }
    BackHandler(enabled = chapter != null && editingId == null) { chapter = null }
    val template = babyBookTemplates.firstOrNull { it.id == editingId }
    if (template != null && child != null) key(child.id, template.id) {
        BookPageEditor(child, template, childPages.firstOrNull { it.pageId == template.id }, onSave, photoFile, onPhotoSelected,
            onDismiss = { editingId = null }, onOpenPage = { id -> editingId = id; chapter = babyBookTemplates.first { it.id == id }.chapter })
    }
    message?.let { text -> AlertDialog(onDismissRequest = { message = null }, title = { Text("Eksport") }, text = { Text(text) }, confirmButton = { TextButton(onClick = { message = null }) { Text("OK") } }) }
}

private fun monthShort(template: BookPageTemplate) = when (val month = template.id.removePrefix("month-")) { "0" -> "Fødsel"; "1" -> "1 md."; else -> "$month mdr." }

/** "Se hvor meget du er vokset": the first and latest month photo side by side, then every month in a grid. */
@Composable
private fun GrowthPhotos(months: List<BookPageTemplate>, pageOf: (BookPageTemplate) -> BabyBookPage?, photoFile: (String?) -> File?, onOpen: (String) -> Unit) {
    val withPhoto = months.filter { pageOf(it)?.photos()?.isNotEmpty() == true }
    fun label(template: BookPageTemplate): String {
        val weight = pageOf(template)?.answers()?.get("Vægt (kg)")?.takeIf(String::isNotBlank)
        return listOfNotNull(monthShort(template), weight?.let { "$it kg" }).joinToString(" · ")
    }
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
        Column(Modifier.fillMaxWidth().padding(BabySpacing.m), verticalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
            Text("Se hvor meget du er vokset", style = MaterialTheme.typography.titleMedium)
            if (withPhoto.size >= 2) Row(horizontalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
                listOf(withPhoto.first(), withPhoto.last()).forEach { template ->
                    Column(Modifier.weight(1f).clip(MaterialTheme.shapes.medium).clickable { onOpen(template.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        PhotoThumb(photoFile(pageOf(template)?.photos()?.firstOrNull()), Icons.Outlined.CalendarMonth, 150.dp, MaterialTheme.shapes.medium, MaterialTheme.colorScheme.surface, fillWidth = true)
                        Text(label(template), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            } else Text("Tilføj et billede på hver månedsside, så kan I se forskellen her.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            months.chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { template ->
                        Column(Modifier.weight(1f).clip(MaterialTheme.shapes.small).clickable { onOpen(template.id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                            PhotoThumb(photoFile(pageOf(template)?.photos()?.firstOrNull()), Icons.Outlined.AddPhotoAlternate, 64.dp, MaterialTheme.shapes.small, MaterialTheme.colorScheme.surface, fillWidth = true)
                            Text(monthShort(template), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun StatusDot(status: PageStatus) {
    val color = MaterialTheme.colorScheme.primary
    val modifier = Modifier.size(10.dp).clip(CircleShape)
    when (status) {
        PageStatus.Done -> Box(modifier.background(color))
        PageStatus.Started -> Box(modifier.border(2.dp, color, CircleShape))
        PageStatus.Empty -> Box(modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape))
    }
}

@Composable
private fun IconCircle(icon: ImageVector) {
    Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
    }
}

/** Decodes a downscaled copy off the main thread, so long chapters don't load full-size photos. */
@Composable
private fun rememberPhoto(file: File?, maxPx: Int): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(null, file?.path, maxPx) {
        value = file?.let { withContext(Dispatchers.IO) { decodeScaled(it, maxPx) } }
    }
    return bitmap
}

private fun decodeScaled(file: File, maxPx: Int): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxPx && bounds.outHeight / (sample * 2) >= maxPx) sample *= 2
    BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
}.getOrNull()

@Composable
private fun PhotoThumb(file: File?, icon: ImageVector, size: Dp, shape: androidx.compose.ui.graphics.Shape, background: androidx.compose.ui.graphics.Color, fillWidth: Boolean = false) {
    val bitmap = rememberPhoto(file, 240)
    val modifier = (if (fillWidth) Modifier.fillMaxWidth().height(size) else Modifier.size(size)).clip(shape).background(background)
    Box(modifier, contentAlignment = Alignment.Center) {
        if (bitmap != null) Image(bitmap, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        else Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(if (size < 48.dp) 20.dp else 24.dp))
    }
}

@Composable
internal fun BookPageEditor(child: ChildProfile, template: BookPageTemplate, saved: BabyBookPage?,
    onSave: suspend (BabyBookPage) -> Unit, photoFile: (String?) -> File?, onPhotoSelected: suspend (Uri) -> String, onDismiss: () -> Unit, onOpenPage: (String) -> Unit = {}) {
    var answersJson by rememberSaveable { mutableStateOf(saved?.answersJson ?: "{}") }
    var photosJson by rememberSaveable { mutableStateOf(saved?.photosJson ?: "[]") }
    var saving by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val answers = Json.decodeFromString<Map<String, String>>(answersJson)
    val photos = Json.decodeFromString<List<String>>(photosJson)
    val scope = rememberCoroutineScope()
    val chapterPages = bookChapters[template.chapter].orEmpty().filter { !it.retired || it.id == template.id }
    val position = chapterPages.indexOfFirst { it.id == template.id }
    val changed = answersJson != (saved?.answersJson ?: "{}") || photosJson != (saved?.photosJson ?: "[]")
    val context = LocalContext.current
    var savedTo by remember { mutableStateOf<String?>(null) }
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            saving = true
            runCatching { onPhotoSelected(uri) }.onSuccess { name ->
                val number = Json.decodeFromString<List<String>>(photosJson).size + 1
                photosJson = Json.encodeToString(Json.decodeFromString<List<String>>(photosJson) + name)
                // The original, untouched, goes to the phone's Pictures folder named after the chapter and page.
                runCatching { withContext(Dispatchers.IO) { BookOriginals.save(context, uri, child, template, number) } }
                    .onSuccess { savedTo = it }.onFailure { error = "Billedet er tilføjet, men originalen kunne ikke gemmes i Billeder." }
            }.onFailure { error = "Billedet kunne ikke tilføjes." }
            saving = false
        }
    }
    // Saves only when something changed, then continues; an error keeps the page open.
    fun saveThen(next: () -> Unit) = scope.launch {
        if (!changed) { next(); return@launch }
        saving = true; error = null
        runCatching { onSave(BabyBookPage(child.id, template.id, answersJson, photosJson)) }.onSuccess { next() }.onFailure { error = "Kunne ikke gemme. Prøv igen." }
        saving = false
    }
    val dismiss = { if (!saving) { if (changed) discard = true else onDismiss() }; Unit }
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(start = BabySpacing.xs, end = BabySpacing.l, top = BabySpacing.s), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(enabled = !saving, onClick = dismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Tilbage") }
                    Column(Modifier.weight(1f)) {
                        Text(template.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${template.chapter} · side ${position + 1} af ${chapterPages.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(enabled = !saving, onClick = { saveThen(onDismiss) }) { Text(if (saving) "Gemmer…" else "Gem") }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = BabySpacing.l, vertical = BabySpacing.m), verticalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
                        photos.forEachIndexed { index, name ->
                            val bitmap = rememberPhoto(photoFile(name), 400)
                            Box(Modifier.size(104.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.primaryContainer).clickable { viewing = name }, contentAlignment = Alignment.Center) {
                                if (bitmap != null) Image(bitmap, "Billede ${index + 1}", contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
                                else Text("Billedet kunne ikke åbnes", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(BabySpacing.s))
                                FilledTonalIconButton(enabled = !saving, onClick = { photosJson = Json.encodeToString(photos.filterIndexed { i, _ -> i != index }) },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(32.dp)) { Icon(Icons.Outlined.Close, contentDescription = "Fjern billede ${index + 1}", modifier = Modifier.size(18.dp)) }
                            }
                        }
                        if (photos.size < 12) Surface(onClick = { pickPhoto.launch("image/*") }, enabled = !saving, shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(104.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Tilføj billede", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text(savedTo?.let { "Originalen er gemt i fuld størrelse i $it." }
                        ?: "Billeder gemmes i fuld størrelse i Billeder › ${BookOriginals.ROOT} › ${BookOriginals.folder(child, template).replace("/", " › ")}.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (template.id == "month-0") AssistChip(enabled = !saving, onClick = {
                        val birth = mapOf("Vægt (kg)" to child.birthWeightGrams?.let { java.math.BigDecimal(it).movePointLeft(3).stripTrailingZeros().toPlainString().replace('.', ',') }.orEmpty(), "Længde (cm)" to child.birthLengthCm?.toString()?.removeSuffix(".0")?.replace('.', ',').orEmpty())
                        answersJson = Json.encodeToString(birth.filterValues(String::isNotBlank) + answers.filterValues(String::isNotBlank))
                    }, label = { Text("Udfyld vægt og længde fra barnets profil") })
                    if (template.id == "birth-facts") AssistChip(enabled = !saving, onClick = {
                        val birth = mapOf("Dato" to child.birthDate?.toString().orEmpty(), "Klokkeslæt" to child.birthTime?.toString().orEmpty(), "Fødested" to child.hospital, "Graviditetsuge" to listOfNotNull(child.gestationalWeeks?.toString(), child.gestationalDays?.toString()).joinToString(" + "), "Vægt (g)" to child.birthWeightGrams?.toString().orEmpty(), "Længde (cm)" to child.birthLengthCm?.toString().orEmpty(), "Hovedomfang (cm)" to child.birthHeadCircumferenceCm?.toString().orEmpty())
                        answersJson = Json.encodeToString(birth + answers.filterValues(String::isNotBlank))
                    }, label = { Text("Udfyld tomme felter fra barnets profil") })
                    (template.prompts + answers.keys).distinct().forEach { prompt -> OutlinedTextField(value = answers[prompt].orEmpty(), onValueChange = { answersJson = Json.encodeToString(answers + (prompt to it)) }, label = { Text(prompt) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, minLines = if (isShortBookPrompt(prompt)) 1 else 3, enabled = !saving) }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Row(Modifier.fillMaxWidth().padding(horizontal = BabySpacing.s, vertical = BabySpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                    chapterPages.getOrNull(position - 1)?.let { previous ->
                        TextButton(enabled = !saving, onClick = { saveThen { onOpenPage(previous.id) } }, modifier = Modifier.weight(1f, fill = false)) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = null); Text(previous.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    chapterPages.getOrNull(position + 1)?.let { next ->
                        TextButton(enabled = !saving, onClick = { saveThen { onOpenPage(next.id) } }, modifier = Modifier.weight(1f, fill = false)) {
                            Text(next.title, maxLines = 1, overflow = TextOverflow.Ellipsis); Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
    viewing?.let { name ->
        Dialog(onDismissRequest = { viewing = null }) {
            val bitmap = rememberPhoto(photoFile(name), 1600)
            Surface(shape = MaterialTheme.shapes.large, onClick = { viewing = null }) {
                if (bitmap != null) Image(bitmap, contentDescription = "Billede", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth())
                else Text("Billedet kunne ikke åbnes", modifier = Modifier.padding(BabySpacing.xl))
            }
        }
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Kassér ændringer?") }, text = { Text("Dine seneste ændringer er ikke gemt.") }, confirmButton = { TextButton(onClick = onDismiss) { Text("Kassér") } }, dismissButton = { TextButton(onClick = { discard = false }) { Text("Fortsæt med at skrive") } })
}
