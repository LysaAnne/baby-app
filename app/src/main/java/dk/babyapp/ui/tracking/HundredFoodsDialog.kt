package dk.babyapp.ui.tracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.domain.FirstFood
import dk.babyapp.domain.hundredFoods
import dk.babyapp.domain.hundredFoodsProgress
import dk.babyapp.ui.theme.CareCategory
import dk.babyapp.ui.theme.babyColors
import java.text.DateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

private enum class FoodFilter(val label: String) { All("Alle"), Missing("Mangler"), Tasted("Smagt"), Allergens("Allergener") }

/** Fødevarestyrelsens advice for children over 9 months, in short (foedevarestyrelsen.dk, "Barnet over 9 måneder"). */
private val foodAdvice = listOf(
    "Fra 8–9 måneder kan barnet få hele, bløde stykker af kartofler og grøntsager i stedet for mos. Familiens mad skal være blød og skåret i passende stykker.",
    "Hårde fødevarer frarådes før 3 år: hele nødder og mandler, popcorn, kerner, hele vindruer og rå gulerødder i stave. Blend, riv eller hak dem fint.",
    "Vindruer skæres på langs, og kernerne fjernes.",
    "Fra 9 måneder højst ¾ liter mælk om dagen i alt, også modermælk og modermælkserstatning. Højst 1 dl komælk i maden.",
    "Vand fra hanen er den bedste drik. Saft og juice kun som små smagsprøver, fortyndet.",
    "Ris indeholder arsen: begræns riskiks og risdrik. Kanel kun i små mængder. Højst ca. 50 g rosiner om ugen.",
    "Klemmeposer med frugt er ikke mad; giv indholdet på en tallerken med ske.",
    "Hold altid opsyn, mens barnet spiser.",
)

/**
 * 100 før 1: a hundred foods to taste before the first birthday. A food counts as tasted when a Fast føde
 * registration names it; tapping a food registers a tasting now. Allergens are marked, so they come early.
 */
@Composable
internal fun HundredFoodsDialog(childId: String, child: ChildProfile?, events: List<CareEventEntity>, onSave: (CareEventEntity) -> Unit, onDismiss: () -> Unit) {
    val progress = remember(events, childId) { hundredFoodsProgress(events, childId) }
    var filter by rememberSaveable { mutableStateOf(FoodFilter.All) }
    var adviceOpen by rememberSaveable { mutableStateOf(false) }
    var confirm by remember { mutableStateOf<FirstFood?>(null) }
    val colors = MaterialTheme.babyColors.category(CareCategory.SolidFood)
    val allergenFoods = hundredFoods.filter { it.allergen != null }
    val shown = hundredFoods.filter { food ->
        when (filter) {
            FoodFilter.All -> true
            FoodFilter.Missing -> food !in progress
            FoodFilter.Tasted -> food in progress
            FoodFilter.Allergens -> food.allergen != null
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        CategoryFormTheme(CareCategory.SolidFood) {
            Surface(Modifier.fillMaxSize(), color = formBody(CareCategory.SolidFood)) {
                Column(Modifier.fillMaxSize().systemBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Tilbage") }
                        Text("100 før 1", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    }
                    LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            WhiteCard {
                                Text("${progress.size} af ${hundredFoods.size} smagt", style = MaterialTheme.typography.titleLarge)
                                LinearProgressIndicator(progress = { progress.size / hundredFoods.size.toFloat() }, modifier = Modifier.fillMaxWidth(), color = colors.accent, trackColor = colors.container)
                                Text("Allergener: ${allergenFoods.count { it in progress }} af ${allergenFoods.size}", style = MaterialTheme.typography.bodyMedium)
                                val birthday = child?.birthDate?.plusYears(1)
                                if (birthday != null) {
                                    val days = ChronoUnit.DAYS.between(LocalDate.now(), birthday)
                                    Text(if (days > 0) "$days dage til 1-årsdagen" else "1-årsdagen er nået – listen kan bruges videre.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Tryk på en fødevare for at registrere en smagsprøve. Det, I skriver under Fast føde, tæller også med.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        item {
                            Surface(onClick = { adviceOpen = !adviceOpen }, shape = MaterialTheme.shapes.large, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Gode råd fra Fødevarestyrelsen", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                        Icon(Icons.Outlined.ExpandMore, if (adviceOpen) "Skjul råd" else "Vis råd", modifier = Modifier.rotate(if (adviceOpen) 180f else 0f))
                                    }
                                    if (adviceOpen) {
                                        foodAdvice.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                                        Text("Allergener (fx æg, jordnødder, mælk og gluten) er markeret, så I kan holde øje med, at de kommer med. Tal med sundhedsplejersken, hvis barnet har eksem eller der er fødevareallergi i familien.",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        item { PeriodBar(FoodFilter.entries.map { it.name to it.label }, filter.name) { filter = FoodFilter.valueOf(it) } }
                        shown.groupBy { it.group }.forEach { (group, foods) ->
                            item(key = group.name) {
                                val all = hundredFoods.filter { it.group == group }
                                Text("${group.label} · ${all.count { it in progress }}/${all.size}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
                            }
                            items(foods, key = { it.name }) { food ->
                                val tasting = progress[food]
                                Surface(shape = MaterialTheme.shapes.medium, color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.clickable(onClickLabel = "Registrér smagsprøve") { confirm = food }.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (tasting != null) Icon(Icons.Filled.CheckCircle, "Smagt", tint = colors.accent)
                                        else Icon(Icons.Outlined.RadioButtonUnchecked, "Ikke smagt endnu", tint = colors.accent.copy(alpha = 0.45f))
                                        Column(Modifier.weight(1f)) {
                                            Text(food.name, style = MaterialTheme.typography.bodyLarge)
                                            food.tip?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                        }
                                        food.allergen?.let { AllergenBadge(it.label) }
                                        if (tasting != null) Text("${tasting.count}×", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    confirm?.let { food ->
        val tasting = progress[food]
        FormDialog(
            category = CareCategory.SolidFood,
            onDismissRequest = { confirm = null },
            title = { Text(food.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tasting?.let { "Smagt ${it.count} ${if (it.count == 1) "gang" else "gange"}, første gang ${DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it.first))}." } ?: "Ikke smagt endnu.")
                    food.allergen?.let { Text("Allergen: ${it.label}. Giv det gerne igen jævnligt.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Annuller") } },
            confirmButton = {
                Button(onClick = {
                    val now = System.currentTimeMillis()
                    onSave(CareEventEntity(childId = childId, type = CareEventType.SolidFood, startedAt = now, endedAt = now, foodName = food.name))
                    confirm = null
                }) { Text("Registrér smagsprøve nu") }
            },
        )
    }
}

@Composable
private fun AllergenBadge(label: String) {
    val health = MaterialTheme.babyColors.category(CareCategory.Health)
    Box(Modifier.padding(start = 4.dp)) {
        Surface(shape = MaterialTheme.shapes.small, color = health.container) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = health.accent, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}
