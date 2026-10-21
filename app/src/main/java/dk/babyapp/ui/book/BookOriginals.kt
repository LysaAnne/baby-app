package dk.babyapp.ui.book

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dk.babyapp.data.book.BookPageTemplate
import dk.babyapp.data.book.babyBookTemplates
import dk.babyapp.data.profile.ChildProfile

/**
 * Copies a picked photo, byte for byte, to the phone's Pictures folder: Billeder › Barnets bog › child › chapter,
 * named so the chapter and page are clear, e.g. "03-05 Det første bad - 1.jpg". The app keeps its own smaller
 * copy for showing and backing up; the original is for printing.
 */
object BookOriginals {
    const val ROOT = "Barnets bog"

    private val chapters = babyBookTemplates.map { it.chapter }.distinct()

    private fun clean(text: String) = text.replace(Regex("[\\\\/:*?\"<>|…]"), "").replace(Regex("\\s+"), " ").trim()

    fun folder(child: ChildProfile, template: BookPageTemplate): String {
        val chapter = chapters.indexOf(template.chapter) + 1
        return "${clean(child.name).ifBlank { "Barn" }}/%02d ${clean(template.chapter)}".format(chapter)
    }

    fun fileName(template: BookPageTemplate, number: Int, extension: String): String {
        val chapter = chapters.indexOf(template.chapter) + 1
        val page = babyBookTemplates.filter { it.chapter == template.chapter }.indexOfFirst { it.id == template.id } + 1
        return "%02d-%02d ${clean(template.title)} - $number.$extension".format(chapter, page)
    }

    /** Returns where it was saved, or null on older Android versions without shared folders for apps. */
    fun save(context: Context, source: Uri, child: ChildProfile, template: BookPageTemplate, number: Int): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val resolver = context.contentResolver
        val mime = resolver.getType(source) ?: "image/jpeg"
        val extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
        val relative = "Pictures/$ROOT/${folder(child, template)}"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName(template, number, extension))
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(MediaStore.Images.Media.RELATIVE_PATH, relative)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val target = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values) ?: error("Kunne ikke oprette filen")
        try {
            resolver.openInputStream(source).use { input -> resolver.openOutputStream(target).use { output -> requireNotNull(input).copyTo(requireNotNull(output)) } }
            resolver.update(target, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        } catch (error: Exception) {
            resolver.delete(target, null, null)
            throw error
        }
        return "Billeder › $ROOT › ${folder(child, template).replace("/", " › ")}"
    }
}
