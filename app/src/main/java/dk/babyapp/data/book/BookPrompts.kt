package dk.babyapp.data.book

// A few short, concrete writing starters per page, the way a printed baby book asks. Page IDs never change,
// and answers saved under older, longer questions are still shown and exported with the page.
private val pagePrompts = mapOf(
    // Før du kom
    "Om mor" to listOf("Sådan var mor, da du kom", "Det glæder mor sig til at dele med dig"),
    "Om far" to listOf("Sådan var far, da du kom", "Det glæder far sig til at dele med dig"),
    "Vores familie" to listOf("Hvem er vi?", "Det gør vi gerne sammen"),
    "Sådan mødtes vi" to listOf("Historien om, hvordan vi mødtes"),
    "Da vi fandt ud af, at du var på vej" to listOf("Dato", "Sådan fandt vi ud af det", "Sådan fortalte vi det til de andre"),
    "Graviditeten" to listOf("Sådan havde mor det", "Første gang vi mærkede dig sparke", "Det tænkte vi meget på"),
    "Første scanning" to listOf("Dato", "Det kunne vi se, og det følte vi"),
    "Vi tror, du bliver…" to listOf("Vores gæt", "Familiens gæt"),
    "Dit navn" to listOf("Det betyder dit navn for os", "Andre navne, vi overvejede"),
    "Babyshower" to listOf("Dato", "Hvem var med, og hvad skete der?"),
    "Dit værelse og dine første ting" to listOf("Sådan gjorde vi klar til dig", "Ting, der er arvet eller lavet til dig"),
    "Beskeder til dig før fødslen" to listOf("Kære lille du …"),
    "Den sidste tid i maven" to listOf("Sådan gik ventetiden", "Det pakkede vi i hospitalstasken"),
    // Fødslen
    "Sådan begyndte fødslen" to listOf("Dato og klokkeslæt", "Sådan startede det"),
    "Historien om din fødsel" to listOf("Fortællingen om din fødsel"),
    "Det første øjeblik med dig" to listOf("Det første, vi sagde og tænkte"),
    "Dit allerførste billede" to listOf("Hvem tog billedet, og hvad skete der lige da?"),
    "Din første dag" to listOf("Sådan gik din første dag"),
    "Hvem var der?" to listOf("Dem, der var med eller hjalp os"),
    "På hospitalet" to listOf("Hvor længe var vi der?", "Det husker vi fra tiden der"),
    "Da vi tog dig med hjem" to listOf("Dato", "Sådan var det at komme hjem med dig"),
    "Dit hospitalsarmbånd" to listOf("Det står på armbåndet"),
    "Din fødselsannonce" to listOf("Teksten vi sendte ud", "Hilsner vi fik"),
    // Den første tid hjemme
    "Dit hjem" to listOf("Her boede vi, da du kom"),
    "Din første nat hjemme" to listOf("Sådan gik den første nat"),
    "Det første bad" to listOf("Dato", "Sådan gik det"),
    "Den første gåtur" to listOf("Dato", "Hvor gik vi hen, og hvem var med?"),
    "Mennesker, der kom for at møde dig" to listOf("Hvem kom, og hvad sagde de?"),
    "Dine bedsteforældre" to listOf("Dine bedsteforældre og hvad du kalder dem", "Første gang de holdt dig"),
    "Dine første venner" to listOf("Mødregruppen og de første venner"),
    "Dig og kæledyrene" to listOf("Sådan tog dyrene imod dig"),
    "Sådan var du som nyfødt" to listOf("Sådan så du ud", "Sådan var du"),
    "Det kunne du lide" to listOf("Det gjorde dig glad"),
    "Det kunne du ikke lide" to listOf("Det brød du dig ikke om"),
    "Det fik dig til at falde til ro" to listOf("Det virkede, når du skulle falde til ro"),
    // Det særlige ved dig
    "Dine mange ansigtsudtryk" to listOf("Dine udtryk, og hvad de betød"),
    "Sådan ser du ud, når du sover" to listOf("Sådan sover du"),
    "Ting du gør, som får os til at grine" to listOf("Det får os til at grine"),
    "Dine kælenavne" to listOf("Dine kælenavne, og hvor de kommer fra"),
    "Sange vi synger for dig" to listOf("Sangene og hvornår vi synger dem"),
    "Dine yndlingsbøger" to listOf("Bøgerne du elsker"),
    "Dit yndlingslegetøj" to listOf("Dit yndlingslegetøj"),
    "Ting du er bange for" to listOf("Det gør dig utryg, og sådan trøster vi dig"),
    "De mærkeligste ting du elsker" to listOf("De sjoveste ting, du er vild med"),
    "Ord og lyde du siger" to listOf("Dine ord og lyde, og hvad de betyder"),
    "Din personlighed" to listOf("Dig med tre ord", "Sådan er du"),
    "Dig og mor" to listOf("Det elsker I at lave sammen"),
    "Dig og far" to listOf("Det elsker I at lave sammen"),
    "Dig og dine særlige mennesker" to listOf("Dine særlige mennesker"),
    "Familieligheder" to listOf("Det har du fra mor og far", "Det siger familien"),
    "Dit første år i billeder" to listOf("Billedtekster til året"),
    "Dine små hænder og fødder" to listOf("Dato", "Sådan lavede vi aftrykkene"),
    "En hårlok" to listOf("Dato", "Hvem klippede, og hvordan var dit hår?"),
    "Små ting vi aldrig vil glemme" to listOf("Små øjeblikke, vi vil huske"),
    // Din første fødselsdag
    "12 ting vi elsker ved dig" to listOf("Tolv ting, én pr. linje"),
    "Det har du lært os" to listOf("Det har du lært os"),
    "Et brev fra mor" to listOf("Kære …"),
    "Et brev fra far" to listOf("Kære …"),
    "Håb og ønsker for dit næste år" to listOf("Det ønsker vi for dig"),
)

/** For each first: one question that fits the milestone, after the date. */
private val milestoneIdeas = mapOf(
    "Første smil" to "Hvem smilede du til?",
    "Første rigtige grin" to "Hvad fik dig til at grine?",
    "Du holder hovedet" to "Sådan gik det",
    "Du ruller" to "Fra ryg til mave eller omvendt?",
    "Du sidder" to "Sådan gik det",
    "Du kravler" to "Hvad ville du hen til?",
    "Du rejser dig" to "Hvad støttede du dig til?",
    "Første skridt" to "Hvem gik du hen imod?",
    "Første tand" to "Hvilken tand, og hvordan opdagede vi den?",
    "Første ord" to "Hvad sagde du?",
    "Første faste mad" to "Hvad smagte du, og hvad syntes du?",
    "Du drikker af kop" to "Sådan gik det",
    "Første svømmetur" to "Hvor, og hvad syntes du om vandet?",
    "Første ferie" to "Hvor tog vi hen, og hvad oplevede vi?",
    "Første overnatning væk hjemmefra" to "Hvor sov du, og hvem passede dig?",
    "Første jul" to "Hvor holdt vi jul, og hvem var med?",
    "Første nytår" to "Sådan fejrede vi det",
    "Første påske" to "Sådan fejrede vi det",
)

/**
 * Pages that repeat another page. They are only shown when something is already written on them,
 * so nothing anyone wrote disappears.
 */
internal val retiredBookPageTitles = setOf(
    "Dit værelse", "Din første seng", "Første tur i barnevognen", "Første besøg",
    "De første ting, der fik dig til at smile", "Hvem synes familien, du ligner?", "Første fødselsdag",
)

/** Short facts get one line; the rest are for writing. */
fun isShortBookPrompt(prompt: String) = prompt.startsWith("Dato") || prompt.contains("(") ||
    prompt in setOf("Klokkeslæt", "Fødested", "Graviditetsuge", "Tøjstørrelse", "Barnets navn", "Vores gæt", "Familiens gæt", "Hvor længe var vi der?")

internal fun bookStoryPrompts(title: String, chapter: String): List<String> = pagePrompts[title] ?: when (chapter) {
    "De første gange" -> listOf("Dato", milestoneIdeas[title] ?: "Sådan gik det")
    else -> listOf("Det vil vi huske")
}
