package dk.babyapp.data.profile

import android.content.Context
import android.net.Uri
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

interface ProfileImageStorage {
    fun import(uri: Uri): String
    fun file(fileName: String): File
    fun delete(fileName: String)
}

class ProfilePhotoStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ProfileImageStorage {
    private val directory: File
        get() = File(context.filesDir, DIRECTORY).apply { mkdirs() }

    override fun import(uri: Uri): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val target = File(directory, fileName)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected image" }
            BitmapFactory.decodeStream(input, null, bounds)
        }
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_IMAGE_EDGE || bounds.outHeight / sampleSize > MAX_IMAGE_EDGE) {
            sampleSize *= 2
        }
        val bitmap = context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected image" }
            BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        }
        requireNotNull(bitmap) { "Unable to decode selected image" }
        target.outputStream().use { output ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        bitmap.recycle()
        return fileName
    }

    override fun file(fileName: String): File = File(directory, fileName)

    override fun delete(fileName: String) {
        file(fileName).delete()
    }

    private companion object {
        const val DIRECTORY = "profile_photos"
        const val MAX_IMAGE_EDGE = 1_024
        const val JPEG_QUALITY = 88
    }
}
