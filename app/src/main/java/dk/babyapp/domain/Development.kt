package dk.babyapp.domain

import dk.babyapp.data.profile.ChildProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A development spurt ("tigerspring"): a period when many babies are more unsettled for a while and then
 * show new abilities. Weeks are counted from the due date, as premature babies follow their due date.
 * The weeks are approximate; the texts are the app's own, written from general child development.
 */
data class Leap(
    val number: Int,
    val title: String,
    /** The unsettled period usually starts about here. */
    val fussyFromWeek: Double,
    /** Around here the new abilities usually show, and it calms down. */
    val calmFromWeek: Double,
    val seen: List<String>,
    val play: List<String>,
    val hard: List<String>,
    val skills: List<String>,
) {
    fun key(kind: String, index: Int) = "leap$number-$kind-$index"
}

val leaps = listOf(
    Leap(1, "Vågnere og mere opmærksom", 4.5, 5.5,
        seen = listOf("Kigger længere på ansigter og ting", "Reagerer tydeligere på lyd, lys og berøring", "Er vågen i længere perioder"),
        play = listOf("Tæt ansigt-til-ansigt-snak", "Bær barnet rundt og vis rummet", "Blid massage efter badet"),
        hard = listOf("Vil være meget tæt på", "Græder mere end før", "Vil gerne ammes eller have flaske oftere"),
        skills = listOf("Følger et ansigt med øjnene", "Første sociale smil", "Falder til ro ved en kendt stemme")),
    Leap(2, "Opdager hænder og lyde", 7.5, 9.5,
        seen = listOf("Kigger på sine egne hænder", "Laver små lyde og pludrer", "Drejer hovedet mod lyde"),
        play = listOf("Svar på barnets lyde, så I \"taler sammen\"", "Mavetid med noget spændende at se på", "Rangle lige foran barnet"),
        hard = listOf("Kortere lure", "Sutter mere på hænderne", "Mere usikker hos fremmede"),
        skills = listOf("Ser på og leger med egne hænder", "Pludrer og laver vokallyde", "Løfter hovedet på maven")),
    Leap(3, "Mere kontrol over kroppen", 11.5, 12.5,
        seen = listOf("Mere bløde og målrettede bevægelser", "Slår efter ting", "Griner højt"),
        play = listOf("Uro eller legebøjle at slå efter", "Lege med op og ned i armene", "Sang med fagter og stemmeleg"),
        hard = listOf("Vil kun være hos mor eller far", "Uroligere nætter", "Lettere at blive overstimuleret"),
        skills = listOf("Slår målrettet efter legetøj", "Griner højt", "Holder hovedet stabilt")),
    Leap(4, "Gribe, vende og undersøge", 14.5, 19.5,
        seen = listOf("Griber målrettet og putter ting i munden", "Vender og drejer ting i hænderne", "Begynder måske at vende sig"),
        play = listOf("Rangler og bløde ting at gribe", "Stof der knitrer", "Lad barnet vende sig fra ryg til side på gulvet"),
        hard = listOf("Klynger sig og vil bæres", "Kortere og flere lure", "Kan miste interessen for mad i perioder"),
        skills = listOf("Griber om legetøj", "Flytter ting fra hånd til hånd", "Vender sig fra ryg til mave eller omvendt")),
    Leap(5, "Afstand og hvor ting er", 22.5, 26.5,
        seen = listOf("Opdager at mor eller far kan gå væk", "Kigger efter ting der falder", "Interesseret i hvad der er inden i og bagved"),
        play = listOf("Borte-tit-tit", "Læg legetøj ned i og op af en kasse", "Kig efter ting der triller"),
        hard = listOf("Bliver ked af det, når I går ud af rummet", "Kan være genert over for andre", "Vågner oftere om natten"),
        skills = listOf("Leder efter legetøj der falder ned", "Siddet med lidt støtte", "Siger stavelser som ba-ba eller da-da")),
    Leap(6, "Ting hører sammen", 33.5, 37.5,
        seen = listOf("Sorterer og sammenligner ting", "Genkender dyr, billeder og personer", "Efterligner lyde og bevægelser"),
        play = listOf("Pegebøger med dyr og lyde", "Bægre der passer i hinanden", "Find den der er magen til"),
        hard = listOf("Mere vrede eller frustration", "Vil selv, men kan ikke helt", "Kan være urolig ved puslebordet"),
        skills = listOf("Peger på ting", "Vinker eller klapper", "Kravler eller flytter sig rundt")),
    Leap(7, "Gør ting i flere trin", 41.5, 46.5,
        seen = listOf("Prøver at gøre ting i rækkefølge", "Bygger og river ned", "Rækker ting frem og tager dem igen"),
        play = listOf("Stable klodser og vælte dem", "Hjælp med enkle gøremål, fx lægge i vasketøjskurven", "Puttekasse med former"),
        hard = listOf("Bliver frustreret, når noget ikke lykkes", "Protesterer mod søvn", "Vil ikke sidde stille ved måltider"),
        skills = listOf("Rejser sig op ad møbler", "Går langs møbler", "Bruger pincetgreb med to fingre")),
    Leap(8, "Vil selv og efterligner", 50.5, 54.5,
        seen = listOf("Efterligner hverdagens gøremål", "Vil selv spise og vælge", "Forstår mange flere ord end barnet kan sige"),
        play = listOf("Legekøkken eller gryder og skeer", "Ring i en legetelefon", "Lad barnet hjælpe med at tørre bordet af"),
        hard = listOf("Raserianfald når det ikke går som ønsket", "Vil kun det ene og så alligevel ikke", "Uro ved afsked i vuggestuen"),
        skills = listOf("Tager de første skridt", "Siger de første ord", "Drikker af en kop med hjælp")),
    Leap(9, "Prøver grænser og regler", 59.5, 64.5,
        seen = listOf("Afprøver hvad der sker, hvis …", "Gør det modsatte for at se reaktionen", "Begynder at forstå regler"),
        play = listOf("Gemme-lege", "Sortér legetøj efter farve", "Lege med vand og sand"),
        hard = listOf("Siger nej til meget", "Driller og tester grænser", "Svært ved at vente"),
        skills = listOf("Går sikkert alene", "Bruger flere ord", "Peger på kropsdele, når de nævnes")),
    Leap(10, "Mig og de andre", 70.5, 75.5,
        seen = listOf("Bliver bevidst om sig selv", "Vil gerne vise og dele", "Leger ved siden af andre børn"),
        play = listOf("Spejlleg og sjov med ansigter", "Rollelege med dukker og bamser", "Synge sange med navne i"),
        hard = listOf("Stærk vilje og jalousi", "Kan blive ked af det over små ting", "Søvnen kan blive forstyrret"),
        skills = listOf("Kender sig selv i spejlet", "Sætter to ord sammen", "Spiser selv med ske")),
)

/** Where the child is in relation to the spurts right now. */
sealed interface LeapPhase {
    /** Before the first one, between two, or after the last. */
    data class Calm(val next: Leap?, val weeksToNext: Double?) : LeapPhase
    data class InLeap(val leap: Leap) : LeapPhase
}

/** The date the weeks are counted from: the due date, or the birth date when no due date is given. */
fun developmentStart(child: ChildProfile): LocalDate? = child.dueDate ?: child.birthDate

fun weeksFromDue(child: ChildProfile, today: LocalDate = LocalDate.now()): Double? =
    developmentStart(child)?.let { ChronoUnit.DAYS.between(it, today) / 7.0 }

fun leapPhase(weeks: Double): LeapPhase =
    leaps.firstOrNull { weeks >= it.fussyFromWeek && weeks < it.calmFromWeek }?.let { LeapPhase.InLeap(it) }
        ?: leaps.firstOrNull { it.fussyFromWeek > weeks }.let { next -> LeapPhase.Calm(next, next?.let { it.fussyFromWeek - weeks }) }

fun LocalDate.plusWeekFraction(weeks: Double): LocalDate = plusDays(Math.round(weeks * 7))

/**
 * A milestone and its usual window in months. The motor milestones use the WHO windows (the ages by which
 * 1 % to 99 % of healthy children reach them); the others are typical ages.
 */
data class Milestone(val key: String, val title: String, val fromMonths: Double, val toMonths: Double, val who: Boolean = false, val about: String = "")

val milestones = listOf(
    Milestone("smile", "Smiler socialt", 1.0, 3.0, about = "Barnet smiler tilbage til jer, når I taler og smiler – ikke kun i søvne. Det er et tidligt tegn på kontakt."),
    Milestone("head-up", "Løfter hovedet på maven", 1.5, 4.0, about = "På maven løfter barnet hovedet, først kort og siden med støtte på underarmene. Mavetid hver dag hjælper."),
    Milestone("roll", "Vender sig", 3.0, 7.0, about = "Barnet ruller fra ryg til side eller mave, eller omvendt. Mange vender først den ene vej."),
    Milestone("sit", "Sidder uden støtte", 3.8, 9.2, who = true, about = "Barnet sidder selv med ret ryg i mindst 10 sekunder uden at holde sig og uden at falde."),
    Milestone("stand-support", "Står med støtte", 4.8, 11.4, who = true, about = "Barnet står oprejst med fødderne på gulvet og holder fast i et møbel eller en hånd i mindst 10 sekunder."),
    Milestone("crawl", "Kravler på hænder og knæ", 5.2, 13.5, who = true, about = "Barnet kravler fremad på hænder og knæ med maven fri af gulvet. Nogle børn springer kravlen over eller rumpeskubber i stedet."),
    Milestone("walk-support", "Går med støtte", 5.9, 13.7, who = true, about = "Barnet går sidelæns eller fremad langs møbler eller med en hånd at holde i."),
    Milestone("stand-alone", "Står alene", 6.9, 16.9, who = true, about = "Barnet står selv uden at holde fast i mindst 10 sekunder."),
    Milestone("walk-alone", "Går alene", 8.2, 17.6, who = true, about = "Barnet tager mindst fem skridt alene med ret ryg."),
    Milestone("first-word", "Siger det første ord", 9.0, 15.0, about = "Et ord, barnet bruger om det samme hver gang, fx \"mor\", \"far\" eller et ord for en ting eller et dyr."),
    Milestone("pincer", "Pincetgreb med to fingre", 8.0, 12.0, about = "Barnet samler små ting op mellem tommelfinger og pegefinger, fx en rosin eller en ært."),
    Milestone("two-words", "Sætter to ord sammen", 18.0, 24.0, about = "Barnet sætter to ord sammen til en lille sætning, fx \"mere mad\" eller \"far væk\"."),
)

fun Milestone.window(): String {
    fun months(value: Double) = if (value % 1.0 == 0.0) value.toInt().toString() else String.format(java.util.Locale.forLanguageTag("da"), "%.1f", value)
    return "${months(fromMonths)}–${months(toMonths)} mdr" + if (who) " (WHO)" else ""
}
