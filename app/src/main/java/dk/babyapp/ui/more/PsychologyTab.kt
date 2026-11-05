package dk.babyapp.ui.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dk.babyapp.data.preferences.DevelopmentCheck
import dk.babyapp.domain.PsychArea
import dk.babyapp.domain.PsychDetail
import dk.babyapp.domain.PsychPhenomenon
import dk.babyapp.domain.psychIntro
import dk.babyapp.domain.psychKnowledgeTypes
import dk.babyapp.domain.psychNow
import dk.babyapp.domain.psychAgeBand
import dk.babyapp.domain.psychDetails
import dk.babyapp.domain.psychTimeline
import dk.babyapp.domain.psychSoon
import dk.babyapp.domain.psychSources
import dk.babyapp.ui.tracking.ChoiceChips
import dk.babyapp.ui.tracking.WhiteCard

/**
 * Psykologi under Udvikling: what developmental psychology describes at the child's age right now, what comes
 * soon, and every phenomenon by area with its experiment and reservations. Read ones can be ticked off.
 */
internal fun psychologyItems(scope: LazyListScope, months: Double, checks: Map<String, DevelopmentCheck>, accent: Color, onToggle: (String) -> Unit) = with(scope) {
    item { PsychIntroCard() }
    val now = psychNow(months)
    val soon = psychSoon(months)
    if (now.isNotEmpty()) {
        item { Text("Lige nu", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
        now.forEach { item(key = "now-${it.number}") { PsychCard(it, true, checks[it.key] != null, accent) { onToggle(it.key) } } }
    }
    if (soon.isNotEmpty()) {
        item { Text("Kommer snart", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
        soon.forEach { item(key = "soon-${it.number}") { PsychCard(it, false, checks[it.key] != null, accent) { onToggle(it.key) } } }
    }
    item { Text("Tidslinje", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp, top = 4.dp)) }
    item { Timeline(months, checks, accent, onToggle) }
}

/** Every phenomenon in the order it begins, grouped by age like the leaps and milestones; filter by area. */
@Composable
private fun Timeline(months: Double, checks: Map<String, DevelopmentCheck>, accent: Color, onToggle: (String) -> Unit) {
    var area by rememberSaveable { mutableStateOf<PsychArea?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceChips(PsychArea.entries.map { it to it.label }, area, allowNone = true) { area = it }
        psychTimeline.filter { area == null || it.area == area }.groupBy { psychAgeBand(it.fromMonths) }.forEach { (band, items) ->
            val bandNow = items.any { it.isActive(months) }
            Text(band, style = MaterialTheme.typography.titleSmall, color = if (bandNow) accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 6.dp))
            items.forEach { PsychCard(it, it.isActive(months), checks[it.key] != null, accent, past = it.toMonths?.let { end -> months > end } == true) { onToggle(it.key) } }
        }
    }
}

@Composable
private fun PsychIntroCard() {
    var open by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    WhiteCard {
        Row(Modifier.fillMaxWidth().clickable { open = !open }, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Om psykologisk udvikling", style = MaterialTheme.typography.titleSmall)
                Text("Udviklingspsykologiens fænomener, eksperimenterne bag og deres forbehold", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ExpandMore, if (open) "Skjul" else "Vis", modifier = Modifier.rotate(if (open) 180f else 0f))
        }
        if (open) {
            psychIntro.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            Text("Typer af viden", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
            psychKnowledgeTypes.forEach { (type, text) -> Text("$type: $text", style = MaterialTheme.typography.bodySmall) }
            Text("Videre læsning", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
            psychSources.forEach { (title, url) ->
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }.padding(vertical = 4.dp))
            }
            Text("Aldrene regnes fra terminsdatoen, ligesom resten af Udvikling.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** One phenomenon: tap to read the definition, the experiment, the finding and the reservations. */
@Composable
private fun PsychCard(item: PsychPhenomenon, now: Boolean, read: Boolean, accent: Color, past: Boolean = false, onToggleRead: () -> Unit) {
    var open by rememberSaveable(item.number) { mutableStateOf(false) }
    Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth().clickable { open = !open }, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall)
                    Text(listOfNotNull(item.ageLabel, "nu".takeIf { now }, "tidligere".takeIf { past }, "læst".takeIf { read }).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = if (now) accent else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Outlined.ExpandMore, if (open) "Skjul ${item.title}" else "Læs om ${item.title}", modifier = Modifier.rotate(if (open) 180f else 0f))
            }
            if (open) {
                Text("${item.english} · ${item.area.label}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val detail = psychDetails[item.number]
                if (detail != null) PsychDetailText(detail, accent) else {
                    Text(item.definition, style = MaterialTheme.typography.bodyMedium)
                    Heading("Eksperiment: ${item.experiment}")
                    Text(item.method, style = MaterialTheme.typography.bodyMedium)
                    Text(item.finding, style = MaterialTheme.typography.bodyMedium)
                    Heading("Konklusion")
                    Text(item.conclusion, style = MaterialTheme.typography.bodyMedium)
                    if (item.nuance.isNotBlank()) {
                        Heading("Forbehold")
                        Text(item.nuance, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(Modifier.fillMaxWidth().clickable(onClick = onToggleRead).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (read) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = accent)
                    Text(if (read) " Læst" else " Markér som læst", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/** The full text under headings: summary, theory, key concepts, each study step by step, meaning, everyday life, caveats. */
@Composable
private fun PsychDetailText(detail: PsychDetail, accent: Color) {
    Heading("Hvad er det?")
    Text(detail.summary, style = MaterialTheme.typography.bodyMedium)
    if (detail.theory.isNotEmpty()) {
        Heading("Teorien bag")
        detail.theory.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
    if (detail.concepts.isNotEmpty()) {
        Heading("Nøglebegreber")
        detail.concepts.forEach { Bullet(conceptText(it)) }
    }
    if (detail.studies.isNotEmpty()) {
        Heading(if (detail.studies.size == 1) "Eksperimentet" else "Eksperimenterne")
        detail.studies.forEach { study ->
            Surface(shape = MaterialTheme.shapes.small, color = accent.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(study.name, style = MaterialTheme.typography.titleSmall)
                    Text(study.who, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Sådan foregik det", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 2.dp))
                    study.steps.forEachIndexed { i, step -> Bullet(AnnotatedString(step), marker = "${i + 1}.") }
                    Text("Det fandt de", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 2.dp))
                    study.findings.forEach { Bullet(AnnotatedString(it)) }
                }
            }
        }
    }
    if (detail.meaning.isNotEmpty()) {
        Heading("Hvad betyder det?")
        detail.meaning.forEach { Bullet(AnnotatedString(it)) }
    }
    if (detail.atHome.isNotEmpty()) {
        Heading("Det kan du se i hverdagen")
        detail.atHome.forEach { Bullet(AnnotatedString(it)) }
    }
    if (detail.caveats.isNotEmpty()) {
        Heading("Forbehold")
        detail.caveats.forEach { Bullet(AnnotatedString(it)) }
    }
}

@Composable
private fun Heading(text: String) = Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))

@Composable
private fun Bullet(text: AnnotatedString, marker: String = "•") {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(marker, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

/** "Begreb: forklaring" with the term in bold. */
internal fun conceptText(concept: String): AnnotatedString {
    val colon = concept.indexOf(':')
    if (colon <= 0) return AnnotatedString(concept)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(concept.substring(0, colon)) }
        append(concept.substring(colon))
    }
}
