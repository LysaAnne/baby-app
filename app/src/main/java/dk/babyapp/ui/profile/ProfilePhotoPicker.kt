package dk.babyapp.ui.profile

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dk.babyapp.data.profile.decodeProfileImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun rememberProfilePhotoPicker(onImport: suspend (Uri) -> String, onSelected: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentSelected by rememberUpdatedState(onSelected)
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            runCatching { withContext(Dispatchers.IO) { decodeProfileImage(context, uri) } }.onSuccess { bitmap = it }.onFailure { error = true }
            busy = false
        }
    }
    bitmap?.let { source ->
        var zoom by remember(source) { mutableFloatStateOf(1f) }
        var horizontal by remember(source) { mutableFloatStateOf(.5f) }
        var vertical by remember(source) { mutableFloatStateOf(.5f) }
        val edge = (minOf(source.width, source.height) / zoom).toInt().coerceAtLeast(1)
        val x = ((source.width - edge) * horizontal).toInt()
        val y = ((source.height - edge) * vertical).toInt()
        AlertDialog(onDismissRequest = { if (!busy) bitmap = null }, title = { Text("Tilpas profilbillede") }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Canvas(Modifier.fillMaxWidth().aspectRatio(1f)) { drawImage(source.asImageBitmap(), srcOffset = IntOffset(x, y), srcSize = IntSize(edge, edge), dstSize = IntSize(size.width.toInt(), size.height.toInt())) }
                Text("Zoom"); Slider(zoom, { zoom = it }, valueRange = 1f..4f, enabled = !busy)
                Text("Vandret placering"); Slider(horizontal, { horizontal = it }, enabled = !busy)
                Text("Lodret placering"); Slider(vertical, { vertical = it }, enabled = !busy)
                TextButton(enabled = !busy, onClick = { bitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, Matrix().apply { postRotate(90f) }, true) }) { Text("Drej 90°") }
            }
        }, confirmButton = { Button(enabled = !busy, onClick = {
            busy = true
            scope.launch {
                runCatching { withContext(Dispatchers.IO) {
                    val crop = Bitmap.createBitmap(source, x, y, edge, edge)
                    val file = File.createTempFile("profile-crop-", ".jpg", context.cacheDir)
                    try { file.outputStream().use { crop.compress(Bitmap.CompressFormat.JPEG, 92, it) }; onImport(Uri.fromFile(file)) }
                    finally { file.delete(); if (crop !== source) crop.recycle() }
                } }.onSuccess { currentSelected(it); bitmap = null }.onFailure { error = true }
                busy = false
            }
        }) { Text(if (busy) "Gemmer…" else "Brug billede") } }, dismissButton = { TextButton(enabled = !busy, onClick = { bitmap = null }) { Text("Annuller") } })
    }
    if (error) AlertDialog(onDismissRequest = { error = false }, title = { Text("Billedet kunne ikke bruges") }, text = { Text("Prøv et andet billede.") }, confirmButton = { TextButton(onClick = { error = false }) { Text("OK") } })
    return { if (!busy) launcher.launch("image/*") }
}
