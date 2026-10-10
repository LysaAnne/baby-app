package dk.babyapp

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dk.babyapp.ui.BabyApp

@AndroidEntryPoint
class MainActivity : androidx.appcompat.app.AppCompatActivity() {
    private var bookTarget by androidx.compose.runtime.mutableStateOf<dk.babyapp.reminders.BookNotificationTarget?>(null)
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookTarget = dk.babyapp.reminders.BookNotificationTarget.from(intent)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            BabyApp(bookTarget = bookTarget, onBookTargetHandled = { bookTarget = null; intent.data = null })
        }
    }
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bookTarget = dk.babyapp.reminders.BookNotificationTarget.from(intent)
    }
}
