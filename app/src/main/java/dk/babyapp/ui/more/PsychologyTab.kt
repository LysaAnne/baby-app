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
 * Psykologi under Udvikling, in the same form as the other tabs: what developmental psychology describes at the
 * child's age right now, then every phenomenon on a timeline by age, filterable by area. Read ones are ticked off.
 */
internal fun psychologyItems(scope: LazyListScope, months: Double, checks: Map<String, DevelopmentCheck>, accent: Color, onToggle: (String) -> Unit) = with(scope) {
    item { PsychIntroCard() }
    val now = psychNow(months)
    if (now.isNotEmpty()) {
        item { TabHeading("Lige nu") }
        now.forEach { item(key = "now-${it.number}") { PsychCard(it, true, checks[it.key], accent) { onToggle(it.key) } } }
    }
    item { TabHeading("Tidslinje") }
    item { ChoiceChips(PsychArea.entries.map { it to it.label }, psychArea.value, allowNone = true) { psychArea.value = it } }
    psychTimeline.filter { psychArea.value == null || it.area == psychArea.value }.groupBy { psychAgeBand(it.fromMonths) }.forEach { (band, list) ->
        item(key = "psych-band-$band") { BandHeading(band, list.any { it.isActive(months) }, accent) }
        list.forEach { item(key = "psych-${it.number}") { PsychCard(it, it.isActive(months), checks[it.key], accent, past = it.toMonths?.let { end -> months > end } == true) { onToggle(it.key) } } }
    }
}

/** The area filter on the timeline, kept while the app is open. */
private val psychArea = mutableStateOf<PsychArea?>(null)

internal val psychTick = TickLabels("Læst", "Ikke læst")

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

/** One phenomenon: the circle marks it read, the arrow opens the theory, the studies and the reservations. */
@Composable
private fun PsychCard(item: PsychPhenomenon, now: Boolean, read: DevelopmentCheck?, accent: Color, past: Boolean = false, onToggleRead: () -> Unit) {
    val subtitle = listOfNotNull(item.ageLabel, "nu".takeIf { now }, "tidligere".takeIf { past && read == null }, read?.let { "læst ${developmentDate(it.date)}" }).joinToString(" · ")
    DevItemCard("psych-${item.number}", item.title, subtitle, accent, now, read != null, psychTick, onToggleRead) {
        Text("${item.english} · ${item.area.label}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val detail = psychDetails[item.number]
        if (detail != null) PsychDetailText(detail, accent) else {
            Text(item.definition, style = MaterialTheme.typography.bodyMedium)
            DetailHeading("Eksperiment: ${item.experiment}")
            Text(item.method, style = MaterialTheme.typography.bodyMedium)
            Text(item.finding, style = MaterialTheme.typography.bodyMedium)
            DetailHeading("Konklusion")
            Text(item.conclusion, style = MaterialTheme.typography.bodyMedium)
            if (item.nuance.isNotBlank()) {
                DetailHeading("Forbehold")
                Text(item.nuance, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** The full text under headings: summary, theory, key concepts, each study step by step, meaning, everyday life, caveats. */
@Composable
private fun PsychDetailText(detail: PsychDetail, accent: Color) {
    DetailHeading("Hvad er det?")
    Text(detail.summary, style = MaterialTheme.typography.bodyMedium)
    if (detail.theory.isNotEmpty()) {
        DetailHeading("Teorien bag")
        detail.theory.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
    if (detail.concepts.isNotEmpty()) {
        DetailHeading("Nøglebegreber")
        detail.concepts.forEach { DetailBullet(conceptText(it)) }
    }
    if (detail.studies.isNotEmpty()) {
        DetailHeading(if (detail.studies.size == 1) "Eksperimentet" else "Eksperimenterne")
        detail.studies.forEach { study ->
            Surface(shape = MaterialTheme.shapes.small, color = accent.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(study.name, style = MaterialTheme.typography.titleSmall)
                    Text(study.who, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Sådan foregik det", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 2.dp))
                    study.steps.forEachIndexed { i, step -> DetailBullet(AnnotatedString(step), marker = "${i + 1}.") }
                    Text("Det fandt de", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 2.dp))
                    study.findings.forEach { DetailBullet(AnnotatedString(it)) }
                }
            }
        }
    }
    if (detail.meaning.isNotEmpty()) {
        DetailHeading("Hvad betyder det?")
        detail.meaning.forEach { DetailBullet(AnnotatedString(it)) }
    }
    if (detail.atHome.isNotEmpty()) {
        DetailHeading("Det kan du se i hverdagen")
        detail.atHome.forEach { DetailBullet(AnnotatedString(it)) }
    }
    if (detail.caveats.isNotEmpty()) {
        DetailHeading("Forbehold")
        detail.caveats.forEach { DetailBullet(AnnotatedString(it)) }
    }
}

@Composable
internal fun DetailHeading(text: String) = Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))

@Composable
internal fun DetailBullet(text: AnnotatedString, marker: String = "•") {
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
