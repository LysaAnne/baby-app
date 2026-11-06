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
import dk.babyapp.domain.DevState
import dk.babyapp.domain.PsychDetail
import dk.babyapp.domain.PsychPhenomenon
import dk.babyapp.domain.psychAgeBand
import dk.babyapp.domain.psychDetails
import dk.babyapp.domain.psychIntro
import dk.babyapp.domain.psychKnowledgeTypes
import dk.babyapp.domain.psychSources
import dk.babyapp.domain.psychTimeline
import dk.babyapp.domain.state

/**
 * Psykologi under Udvikling, in the same form as the other tabs: Om psykologi, what developmental psychology
 * describes at the child's age right now, and the later phenomena folded away. Read ones are ticked off.
 */
internal fun psychologyItems(scope: LazyListScope, months: Double, nowHeading: String, done: Set<String>, accent: Color, onToggle: (String) -> Unit, folds: DevFolds) =
    categoryItems(scope, "psych", "Om psykologi", { PsychAbout() }, nowHeading, "Intet at læse om lige nu.", "Fremtidige emner",
        psychTimeline.map { item ->
            val state = item.state(months, done)
            DevEntry("${item.number}", state, psychAgeBand(item.fromMonths)) { PsychCard(item, state, item.key in done, accent) { onToggle(item.key) } }
        }, folds)

internal val psychTick = TickLabels("Læst", "Ikke læst", "Markér som læst")

@Composable
private fun PsychAbout() {
    val context = LocalContext.current
    Text("Udviklingspsykologiens fænomener, eksperimenterne bag og deres forbehold, ordnet efter hvornår de typisk begynder. Kryds et emne af, når du har læst det.")
    psychIntro.forEach { DetailBullet(AnnotatedString(it)) }
    DetailHeading("Typer af viden")
    psychKnowledgeTypes.forEach { (type, text) -> Text("$type: $text", style = MaterialTheme.typography.bodySmall) }
    DetailHeading("Videre læsning")
    psychSources.forEach { (title, url) ->
        Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }.padding(vertical = 4.dp))
    }
    Text("Aldrene regnes fra terminsdatoen, ligesom resten af Udvikling. $developmentCaveat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** One phenomenon: the circle marks it read, the arrow opens the theory, the studies and the reservations. */
@Composable
private fun PsychCard(item: PsychPhenomenon, state: DevState, read: Boolean, accent: Color, onToggleRead: () -> Unit) {
    val subtitle = listOfNotNull(item.ageLabel, stateLabel(state, "læst")).joinToString(" · ")
    DevItemCard(item.title, subtitle, accent, state, read, psychTick, onToggleRead) {
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
