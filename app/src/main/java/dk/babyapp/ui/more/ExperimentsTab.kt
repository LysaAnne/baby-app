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
import dk.babyapp.domain.DevState
import dk.babyapp.domain.HomeExperiment
import dk.babyapp.domain.experimentIntro
import dk.babyapp.domain.experimentOrder
import dk.babyapp.domain.experimentTimeline
import dk.babyapp.domain.homeExperiments
import dk.babyapp.domain.psychAgeBand
import dk.babyapp.domain.state
import java.util.Locale


/**
 * Eksperimenter under Udvikling, in the same form as the other tabs: Om eksperimenterne, what fits the child's age
 * now (and earlier ones not yet done), and the later ones folded away. Tap the circle to tick one off.
 */
internal fun experimentItems(scope: LazyListScope, months: Double, nowHeading: String, done: Set<String>, accent: Color, onToggle: (String) -> Unit, folds: DevFolds) =
    categoryItems(scope, "exp", "Om eksperimenter", { ExperimentAbout(homeExperiments.count { it.key in done }) }, nowHeading,
        "Ingen eksperimenter passer til alderen lige nu.", "Fremtidige eksperimenter",
        experimentTimeline.map { item ->
            val state = item.state(months, done)
            DevEntry("${item.number}", state, psychAgeBand(item.fromMonths)) { ExperimentCard(item, state, item.key in done, accent) { onToggle(item.key) } }
        }, folds)

@Composable
private fun ExperimentAbout(done: Int) {
    Text("$done af ${homeExperiments.size} lavet", style = MaterialTheme.typography.titleSmall)
    experimentIntro.forEach { DetailBullet(AnnotatedString(it)) }
    Text(developmentCaveat, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** One experiment: the circle ticks it off, the arrow opens what you need, the steps and the research. */
@Composable
private fun ExperimentCard(item: HomeExperiment, state: DevState, done: Boolean, accent: Color, onToggle: () -> Unit) {
    val subtitle = listOfNotNull(item.ageLabel, stateLabel(state, "lavet")).joinToString(" · ")
    DevItemCard("${experimentOrder.getValue(item.number)}. ${item.title}", subtitle, accent, state, done, experimentTick, onToggle) {
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
    }
}

internal val experimentTick = TickLabels("Lavet", "Ikke lavet", "Markér som lavet")
