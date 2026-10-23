package dk.babyapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dk.babyapp.ui.theme.BabyAppTheme
import dk.babyapp.ui.theme.BabySpacing
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.defaultColorProfiles

/** Every shared component with example content, so the gallery can be shown in light, dark and night. */
@Composable
fun ComponentGallery() {
    Column(Modifier.padding(BabySpacing.l), verticalArrangement = Arrangement.spacedBy(BabySpacing.m)) {
        Row(horizontalArrangement = Arrangement.spacedBy(BabySpacing.s)) {
            ActionTile("Amning", CareCategory.Feeding, onClick = {}, modifier = Modifier.weight(1f), detail = "for 2 t 10 m")
            ActionTile("Ble", CareCategory.Diaper, onClick = {}, modifier = Modifier.weight(1f), detail = "for 1 t")
            ActionTile("Søvn", CareCategory.Sleep, onClick = {}, modifier = Modifier.weight(1f), enabled = false)
        }
        TimerBar("Amning · venstre", CareCategory.Feeding, running = true, elapsedSeconds = { 754 }, onToggle = {}, onStop = {}, subtitle = "Startet 14.05")
        EventRow("Flaske", "14.20", CareCategory.Feeding, summary = "90 ml modermælk", onClick = {})
        EventRow("Søvn", "12.50", CareCategory.Sleep, summary = "1 t 5 min", onClick = {})
        var content by remember { mutableStateOf<String?>("Modermælk") }
        ChipSelector(listOf("Modermælk", "Erstatning", "Blanding", "Vand"), content, { content = it }, { it }, title = "Indhold")
        var amount by remember { mutableStateOf<Int?>(90) }
        AmountStepper(amount, { amount = it }, unit = "ml", label = "Spist")
        BabyEmptyState(Icons.Outlined.History, "Ingen registreringer endnu", "Det, du registrerer, vises her.", actionLabel = "Registrer", onAction = {})
    }
}

@Preview(name = "Lys", showBackground = true, widthDp = 380)
@Composable
private fun ComponentGalleryLight() = BabyAppTheme(defaultColorProfiles.first()) { Surface { ComponentGallery() } }
