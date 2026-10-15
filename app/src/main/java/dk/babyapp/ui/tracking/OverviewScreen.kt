package dk.babyapp.ui.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.preferences.MeasurementChartSettings
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.domain.eventsForDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun OverviewScreen(
    childId: String?,
    events: List<CareEventEntity>,
    contentPadding: PaddingValues = PaddingValues(),
    chartSettings: MeasurementChartSettings = MeasurementChartSettings(),
    statisticsSettings: dk.babyapp.data.preferences.HeadSideStatisticsSettings = dk.babyapp.data.preferences.HeadSideStatisticsSettings(),
    onStatisticsSettingsChange: (dk.babyapp.data.preferences.HeadSideStatisticsSettings) -> Unit = {},
    onChartSettingsChange: (MeasurementChartSettings) -> Unit = {},
) {
    var statisticsOpen by rememberSaveable(childId) { mutableStateOf(false) }
    var chartsOpen by rememberSaveable(childId) { mutableStateOf(false) }
    var dayOpen by rememberSaveable(childId) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Overblik", style = MaterialTheme.typography.headlineSmall)
        if (childId == null) Text("Opret eller vælg et barn for at se overblik.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverviewTile("Grafer", Icons.Outlined.AutoGraph, childId != null, Modifier.weight(1f)) { chartsOpen = true }
            OverviewTile("Dagens overblik", Icons.Outlined.CalendarToday, childId != null, Modifier.weight(1f)) { dayOpen = true }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OverviewTile("Nøgletal", Icons.Outlined.AutoGraph, childId != null, Modifier.weight(1f)) { statisticsOpen = true }
            Spacer(Modifier.weight(1f))
        }
    }
    if (statisticsOpen && childId != null) HeadSideStatisticsDialog(childId, events, statisticsSettings, onStatisticsSettingsChange) { statisticsOpen = false }
    if (chartsOpen && childId != null) MeasurementChartsDialog(events, childId, chartSettings, onChartSettingsChange) { chartsOpen = false }
    if (dayOpen && childId != null) DailySummaryDialog(events.filter { it.childId == childId }) { dayOpen = false }
}

@Composable
private fun OverviewTile(label: String, icon: ImageVector, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, enabled = enabled, modifier = modifier.aspectRatio(1f)) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DailySummaryDialog(events: List<CareEventEntity>, onDismiss: () -> Unit) {
    var selectedDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val date = LocalDate.ofEpochDay(selectedDay)
    val today = LocalDate.now()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().systemBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dagens overblik", modifier = Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onDismiss) { Text("Luk") }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { selectedDay-- }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Forrige dag") }
                    Text(date.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.forLanguageTag("da"))), Modifier.weight(1f), textAlign = TextAlign.Center)
                    IconButton(onClick = { selectedDay++ }, enabled = date < today) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Næste dag") }
                    TextButton(onClick = { selectedDay = today.toEpochDay() }, enabled = date != today) { Text("I dag") }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    DaySummary(eventsForDay(events, date))
                }
            }
        }
    }
}
