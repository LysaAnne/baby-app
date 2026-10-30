package dk.babyapp.ui.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.domain.OwnSong
import dk.babyapp.domain.Song
import dk.babyapp.domain.SongCategory
import dk.babyapp.domain.SongbookData
import dk.babyapp.domain.songs
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import dk.babyapp.ui.tracking.CategoryFormTheme
import dk.babyapp.ui.tracking.ChoiceChips
import dk.babyapp.ui.tracking.FormDialog
import dk.babyapp.ui.tracking.LocalTimerBar
import dk.babyapp.ui.tracking.RegistrationTextField
import dk.babyapp.ui.tracking.formBody

private const val FAVORITES = "favorites"

/**
 * Sangbog: Danish children's songs with where they come from and the movements in our own words. The app holds no
 * lyrics; the family can write or paste the words for each song, mark favourites and add their own songs.
 */
@Composable
internal fun SongbookDialog(data: SongbookData, onUpdate: (SongbookData) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var open by remember { mutableStateOf<Song?>(null) }
    var adding by remember { mutableStateOf(false) }
    val accent = MaterialTheme.babyColors.category(CareCategory.Sleep).accent
    // Own songs are shown like the others; their id starts with "own-".
    val all = songs + data.ownSongs.map { Song("own-${it.id}", it.title, runCatching { SongCategory.valueOf(it.category) }.getOrDefault(SongCategory.Rhymes), "Egen sang") }
    val shown = all.filter { song ->
        (query.isBlank() || song.title.contains(query.trim(), ignoreCase = true)) &&
            when (filter) { null -> true; FAVORITES -> song.id in data.favorites; else -> song.category.name == filter }
    }.sortedBy { it.title }
    fun toggleFavorite(id: String) = onUpdate(data.copy(favorites = if (id in data.favorites) data.favorites - id else data.favorites + id))
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CategoryFormTheme(CareCategory.Sleep) {
            Surface(Modifier.fillMaxSize(), color = formBody(CareCategory.Sleep)) {
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Tilbage") }
                        Text("Sangbog", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { adding = true }) { Icon(Icons.Outlined.Add, "Tilføj egen sang") }
                    }
                    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            RegistrationTextField(query, { query = it }, label = { Text("Søg sang") }, singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) })
                        }
                        item {
                            ChoiceChips(listOf(FAVORITES to "Favoritter") + SongCategory.entries.map { it.name to it.label }, filter, allowNone = true) { filter = it }
                        }
                        if (shown.isEmpty()) item { Text(if (filter == FAVORITES) "Tryk på hjertet ved en sang for at gøre den til favorit." else "Ingen sange matcher.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(4.dp)) }
                        items(shown, key = { it.id }) { song ->
                            Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Row(Modifier.clickable { open = song }.padding(start = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(song.title, style = MaterialTheme.typography.bodyLarge)
                                        Text(listOf(song.origin, song.category.label, "tekst tilføjet".takeIf { !data.lyrics[song.id].isNullOrBlank() }).filter { !it.isNullOrBlank() }.joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { toggleFavorite(song.id) }) {
                                        if (song.id in data.favorites) Icon(Icons.Filled.Favorite, "Fjern ${song.title} fra favoritter", tint = accent)
                                        else Icon(Icons.Outlined.FavoriteBorder, "Gør ${song.title} til favorit", tint = accent)
                                    }
                                }
                            }
                        }
                    }
                    LocalTimerBar.current()
                }
            }
        }
    }
    open?.let { song ->
        SongDialog(song, data.lyrics[song.id].orEmpty(), onDismiss = { open = null },
            onDelete = if (song.id.startsWith("own-")) ({ val id = song.id.removePrefix("own-"); onUpdate(data.copy(ownSongs = data.ownSongs.filterNot { it.id == id }, lyrics = data.lyrics - song.id, favorites = data.favorites - song.id)); open = null }) else null,
        ) { lyrics -> onUpdate(data.copy(lyrics = if (lyrics.isBlank()) data.lyrics - song.id else data.lyrics + (song.id to lyrics))); open = null }
    }
    if (adding) OwnSongDialog({ adding = false }) { own, words -> onUpdate(data.copy(ownSongs = data.ownSongs + own, lyrics = if (words.isBlank()) data.lyrics else data.lyrics + ("own-${own.id}" to words))); adding = false }
}

/** One song: where it is from, the movements, and the family's own words for it. */
@Composable
private fun SongDialog(song: Song, lyrics: String, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(lyrics) }
    val context = LocalContext.current
    FormDialog(category = CareCategory.Sleep, onDismissRequest = onDismiss, title = { Text(song.title) }, subtitle = listOf(song.origin, song.category.label).filter(String::isNotBlank).joinToString(" · "), text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (song.actions.isNotBlank()) Text(song.actions, style = MaterialTheme.typography.bodyMedium)
            RegistrationTextField(text, { text = it }, label = { Text("Sangtekst") }, minLines = 4,
                placeholder = { Text("Skriv eller indsæt teksten, som I synger den") })
            FilledTonalButton(onClick = {
                // A web search for the words; the app itself has no lyrics.
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode("${song.title} sangtekst")))) }
            }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Search, null); Text(" Find teksten") }
        }
    }, dismissButton = if (onDelete != null) ({ IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Slet sangen", tint = MaterialTheme.colorScheme.error) }; Spacer(Modifier.weight(1f)) }) else null,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Annuller") }
            Button(onClick = { onSave(text.trim()) }) { Text("Gem") }
        })
}

@Composable
private fun OwnSongDialog(onDismiss: () -> Unit, onSave: (OwnSong, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var words by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SongCategory.Rhymes) }
    FormDialog(category = CareCategory.Sleep, onDismissRequest = onDismiss, title = { Text("Egen sang") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RegistrationTextField(title, { title = it }, label = { Text("Titel") }, singleLine = true)
            ChoiceChips(SongCategory.entries.map { it to it.label }, category, label = "Slags") { it?.let { category = it } }
            RegistrationTextField(words, { words = it }, label = { Text("Sangtekst") }, minLines = 4)
        }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Annuller") } },
        confirmButton = { Button(enabled = title.isNotBlank(), onClick = { onSave(OwnSong(title = title.trim(), category = category.name), words.trim()) }) { Text("Tilføj") } })
}
