package dk.babyapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dk.babyapp.ui.navigation.BabyAppNavigation
import dk.babyapp.ui.theme.BabyAppTheme

@Composable
fun BabyApp() {
    BabyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BabyAppNavigation()
        }
    }
}

