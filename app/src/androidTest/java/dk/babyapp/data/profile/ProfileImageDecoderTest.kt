package dk.babyapp.data.profile

import android.graphics.Bitmap
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileImageDecoderTest {
    @Test fun rotatedPhonePhotoIsNormalizedBeforeCropping() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File.createTempFile("orientation-test-", ".jpg", context.cacheDir)
        try {
            val source = Bitmap.createBitmap(120, 80, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
            file.outputStream().use { source.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            source.recycle()
            ExifInterface(file.path).apply { setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString()); saveAttributes() }
            val decoded = decodeProfileImage(context, Uri.fromFile(file))
            assertEquals(80, decoded.width)
            assertEquals(120, decoded.height)
            decoded.recycle()
        } finally { file.delete() }
    }
}
