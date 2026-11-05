package dk.babyapp.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.domain.HomeExperiment
import dk.babyapp.domain.bookRef
import dk.babyapp.domain.experimentIntro
import dk.babyapp.domain.experimentTimeline
import dk.babyapp.domain.experimentsNow
import dk.babyapp.domain.homeExperiments
import dk.babyapp.domain.psychAgeBand
import dk.babyapp.ui.tracking.ChoiceChips
import dk.babyapp.ui.tracking.WhiteCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val doneFormat = DateTimeFormatter.ofPattern("d. MMM yyyy", Locale.forLanguageTag("da"))

/**
 * Eksperimenter under Udvikling: what fits the child's age now, then every experiment on a timeline by age.
 * Tap the circle to tick one off (the date is kept); tap the card to read how to do it.
 */
internal fun experimentItems(scope: LazyListScope, months: Double, checks: Map<String, DevelopmentCheck>, accent: Color, onToggle: (String) -> Unit) = with(scope) {
    item { ExperimentIntroCard(homeExperiments.count { checks[it.key] != null }) }
    val now = experimentsNow(months)
    if (now.isNotEmpty()) {
        item { Text("Lige nu", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
        now.forEach { item(key = "exp-now-${it.number}") { ExperimentCard(it, true, checks[it.key], accent) { onToggle(it.key) } } }
    }
    item { Text("Tidslinje", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
    item { BookFilter() }
    experimentTimeline.filter { !bookOnly.value || it.bookRef != null }.groupBy { psychAgeBand(it.fromMonths) }.forEach { (band, list) ->
        item(key = "exp-band-$band") {
            Text(band, style = MaterialTheme.typography.titleSmall, color = if (list.any { it.isActive(months) }) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp))
        }
        list.forEach { item(key = "exp-${it.number}") { ExperimentCard(it, it.isActive(months), checks[it.key], accent, past = months > it.toMonths) { onToggle(it.key) } } }
    }
}

/** "Fra bogen" shows only the 50 from Experimenting with Babies; kept for the session like the area filter in Psykologi. */
private val bookOnly = mutableStateOf(false)

@Composable
private fun BookFilter() {
    ChoiceChips(listOf(false to "Alle", true to "Fra bogen (50)"), bookOnly.value) { bookOnly.value = it ?: false }
}

@Composable
private fun ExperimentIntroCard(done: Int) {
    var open by rememberSaveable { mutableStateOf(false) }
    WhiteCard {
        Row(Modifier.fillMaxWidth().clickable { open = !open }, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Om eksperimenterne", style = MaterialTheme.typography.titleSmall)
                Text("$done af ${homeExperiments.size} lavet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ExpandMore, if (open) "Skjul" else "Vis", modifier = Modifier.rotate(if (open) 180f else 0f))
        }
        if (open) experimentIntro.forEach { DetailBullet(AnnotatedString(it)) }
    }
}

/** One experiment: the circle ticks it off, the rest of the row opens what you need, the steps and the research. */
@Composable
private fun ExperimentCard(item: HomeExperiment, now: Boolean, done: DevelopmentCheck?, accent: Color, past: Boolean = false, onToggle: () -> Unit) {
    var open by rememberSaveable(item.number) { mutableStateOf(false) }
    Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggle) {
                    if (done != null) Icon(Icons.Filled.CheckCircle, "Lavet: ${item.title}", tint = accent)
                    else Icon(Icons.Outlined.RadioButtonUnchecked, "Ikke lavet: ${item.title}", tint = accent.copy(alpha = 0.45f))
                }
                Row(Modifier.weight(1f).clickable { open = !open }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                        Text(listOfNotNull(item.ageLabel, item.bookRef?.let { "bog nr. ${it.first}" }, "nu".takeIf { now }, "tidligere".takeIf { past && done == null },
                            done?.let { "lavet ${LocalDate.parse(it.date).format(doneFormat)}" }).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall, color = if (now) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.ExpandMore, if (open) "Skjul ${item.title}" else "Læs om ${item.title}", modifier = Modifier.rotate(if (open) 180f else 0f))
                }
            }
            if (open) Column(Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item.bookRef?.let { (nr, title) ->
                    Text("Experimenting with Babies nr. $nr: ‘$title’", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DetailHeading("Hvad undersøger det?")
                Text(item.idea, style = MaterialTheme.typography.bodyMedium)
                DetailHeading("Du skal bruge")
                item.materials.forEach { DetailBullet(AnnotatedString(it)) }
                DetailHeading("Sådan gør du")
                item.steps.forEachIndexed { i, step -> DetailBullet(AnnotatedString(step), marker = "${i + 1}.") }
                DetailHeading("Det kan du kigge efter")
                item.lookFor.forEach { DetailBullet(AnnotatedString(it)) }
                if (item.safety.isNotBlank()) {
                    Surface(shape = MaterialTheme.shapes.small, color = accent.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Pas på", style = MaterialTheme.typography.labelLarge)
                            Text(item.safety, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                DetailHeading("Forskningen bag")
                Text(item.research, style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (done != null) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = accent)
                    Text(if (done != null) " Lavet" else " Markér som lavet", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
