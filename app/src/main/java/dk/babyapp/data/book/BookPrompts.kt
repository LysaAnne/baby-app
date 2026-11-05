package dk.babyapp.data.book

// The questions on each page, chosen by Anne section by section in November 2026 (docs/book-prompts-choices.md).
// Page IDs never change. Answers are stored under the question's text: when a question is only reworded, the old
// text is listed in [bookPromptAliases] so the answer moves into the new field; any other answer saved under an
// older question is still shown and exported with the page, so nothing anyone wrote disappears.
private val pagePrompts = mapOf(
    // Før du kom
    "Om mor" to listOf("Mors fulde navn, og hvor hun er vokset op", "Mors job og hverdag, da du var på vej", "Tre ting, mor er rigtig god til",
        "Noget fra mors egen barndom, du skal kende", "Det sjoveste, mor har lavet i sit liv", "Det, mor er mest stolt af (før dig)",
        "Det glæder mor sig allermest til at dele med dig", "Det, mor var bange for og glad for ved at blive mor"),
    "Om far" to listOf("Fars fulde navn, og hvor han er vokset op", "Fars job og hverdag, da du var på vej", "Tre ting, far er rigtig god til",
        "Noget fra fars egen barndom, du skal kende", "Det sjoveste, far har lavet i sit liv", "Det, far er mest stolt af (før dig)",
        "Det glæder far sig allermest til at dele med dig", "Det, far var bange for og glad for ved at blive far"),
    "Vores familie" to listOf("Hvem bor sammen med dig, og hvad kalder vi hinanden?", "Hvem er i din nærmeste familie (søskende, bedsteforældre, onkler, tanter)?",
        "En tradition, vi gerne vil give videre til dig", "Familiens yndlingsret", "Det, vores familie altid griner af",
        "Hvor kommer familien fra – byer, lande, historier?", "Et familiemotto, vi lever efter (eller burde)", "Kæledyr og andre familiemedlemmer med pels eller fjer"),
    "Dine forældre" to listOf("Historien om, hvordan vi mødtes", "Hvor og hvornår så vi hinanden første gang?", "Vores første date – og hvad gik godt eller galt?",
        "Hvem tog det første skridt?", "Hvornår vidste vi, at det var noget særligt?", "Vores sang, vores sted eller vores ret",
        "Det, vi elsker mest ved hinanden", "Den største ting, vi har oplevet sammen før dig"),
    "Da vi fandt ud af, at du var på vej" to listOf("Dato", "Hvor var vi, og hvad var det første, vi sagde?", "Hvordan fandt vi ud af det (test, symptomer, mavefornemmelse)?",
        "Hvordan fortalte mor det til far (eller omvendt)?", "Sådan fortalte vi det til resten af familien", "Det, vi gjorde for at fejre det",
        "Hvor længe holdt vi på hemmeligheden?", "Det første, vi købte til dig", "Termin, vi fik at vide"),
    "Graviditeten" to listOf("Sådan havde mor det", "Hvilke cravings eller aversioner havde mor?", "Første gang vi mærkede dig sparke – hvor var vi?",
        "Det, du reagerede på i maven (musik, stemmer, mad)", "Hvor stor var maven, og hvad kaldte vi dig derinde?", "Sådan forberedte vi os (kurser, bøger, apps)",
        "Det, far gjorde for at hjælpe", "Sådan så verden ud omkring os, mens du voksede", "Vi troede, du ville blive en dreng/pige – og hvorfor?"),
    "Første scanning" to listOf("Dato", "Hvem var med?", "Hvad lavede du på skærmen?", "Det, vi følte, da vi så dig første gang",
        "Hvad sagde jordemoderen eller lægen?", "Senere scanninger, vi husker", "Vidste vi kønnet, og ville vi vide det?"),
    "Dit navn" to listOf("Det betyder dit navn for os", "Hvem fandt på navnet, og hvornår var vi sikre?", "Hvad betyder navnet, og hvem er du opkaldt efter?",
        "Navnene, der næsten blev dit", "Navne, vi var helt uenige om", "Dine mellemnavne, og hvor de kommer fra", "Hvad sagde familien, da de hørte navnet?",
        "Hvornår blev navnet afsløret?", "Kælenavne, vi allerede havde til dig i maven", "Navnedag, dåb eller navngivning – hvordan og hvornår?"),
    "Babyshower" to listOf("Dato", "Hvem holdt den, og hvem var med?", "Hvilke lege legede vi?", "Den sjoveste eller mest rørende gave", "Maden og kagen",
        "Gode råd, vi fik", "Var det en overraskelse?", "Pynt og tema", "Det bedste øjeblik den dag", "Hilsner til dig fra gæsterne"),
    "Dit værelse og dine første ting" to listOf("Sådan gjorde vi klar til dig", "Farven på væggene, og hvor din seng stod",
        "Ting, der er arvet eller lavet til dig – og af hvem", "Det første stykke tøj, vi købte til dig", "Din første bamse, og hvem gav dig den"),
    "Beskeder til dig før fødslen" to listOf("Kære lille du …", "Det glæder vi os allermest til at vise dig", "Det lover vi dig", "Det, vi håber, du arver fra os",
        "Det, vi håber, du ikke arver fra os", "En hilsen fra mor", "En hilsen fra far", "Hilsner fra familie og venner",
        "Det, vi gerne vil sige til dig på din 18-års fødselsdag", "Tre ting, vi drømmer om at opleve med dig"),
    "Den sidste tid i maven" to listOf("Sådan gik ventetiden", "Hvordan fik vi tiden til at gå de sidste uger?", "Det pakkede vi i hospitalstasken (og glemte)",
        "Hvad vejret og årstiden var lige før du kom", "Sådan havde mor det til sidst", "Det sidste, vi gjorde som \"bare os to\"",
        "Tricks, vi prøvede for at få fødslen i gang", "Hvem ringede hele tiden for at høre, om du var kommet?", "Det, vi var mest spændte på",
        "Blev du født før eller efter termin, og hvordan var det?"),
    // Fødslen
    "Sådan begyndte fødslen" to listOf("Dato og klokkeslæt", "Hvor var vi, og hvad lavede vi, da det gik i gang?", "Det første tegn på, at du var på vej ud",
        "Hvem ringede vi til først?", "Hvad sagde far, da han fik det at vide?", "Sådan kom vi frem til fødestedet (og hvor hurtigt kørte vi?)",
        "Var det planlagt, igangsat eller helt af sig selv?", "Det, vi nåede at gøre, inden vi tog af sted", "Vejret den dag", "Hvad tænkte vi lige da?"),
    "Historien om din fødsel" to listOf("Fortællingen om din fødsel", "Hvor lang tid tog det?", "Det, der hjalp mor mest undervejs", "Det sværeste øjeblik",
        "Det smukkeste øjeblik", "Det uventede, der skete", "Det, far gjorde undervejs"),
    "Det første øjeblik med dig" to listOf("Hvem holdt dig først, og hvad gjorde du?", "Græd du med det samme?", "Hvad tænkte mor?", "Hvad tænkte far?",
        "Det første, du gjorde (gabte, kiggede, greb en finger …)", "Hvordan duftede og føltes du?"),
    "Din første dag" to listOf("Sådan gik din første dag", "Hvordan sov og spiste du det første døgn?", "Dit første måltid – amning eller flaske, og hvordan gik det?",
        "Første besøgende og de første beskeder, vi sendte", "Første ble – og hvem skiftede den?", "Hvordan havde mor det?", "Det, far gjorde den første dag"),
    "Da vi tog dig med hjem" to listOf("Dato", "Hvordan kom vi hjem, og hvordan var turen?", "Det første, vi gjorde, da vi kom ind ad døren",
        "Hvem tog imod os derhjemme?", "Sådan var det at komme hjem med dig", "Hvordan føltes det at være alene med dig første gang?",
        "Hvordan reagerede kæledyrene?", "Hvordan gik den første aften?"),
    "Dit hospitalsarmbånd" to listOf("Det står på armbåndet", "Hvem tog det af, og hvornår?", "Andre små ting, vi gemte fra fødslen (navneskilt, huer, sut)"),
    // Den første tid hjemme
    "Dit hjem" to listOf("Her boede vi, da du kom", "Adressen, og hvordan der så ud", "Det, vi elskede ved vores hjem", "Hvor længe havde vi boet der?",
        "Planer om at flytte, eller blive boende?"),
    "Det første bad" to listOf("Dato", "Hvem badede dig?", "Hvad syntes du om vandet?", "Græd du, eller nød du det?", "Hvor blev du badet (balje, håndvask, badekar)?",
        "Dit første håndklæde eller badekåbe", "Badetid nu – din yndlingsdel"),
    "Den første gåtur" to listOf("Dato", "Hvor gik vi hen, og hvem var med?", "Hvad var vejret, og hvad havde du på?"),
    "Mennesker, der kom for at møde dig" to listOf("Hvem kom, og hvad sagde de?", "Hvem kom først, og hvad havde de med?", "Den sjoveste gave",
        "Hvem ville vi ønske, kunne have mødt dig?"),
    "Dine bedsteforældre" to listOf("Dine bedsteforældre, og hvad du kalder dem", "Første gang de holdt dig – hvad sagde de?",
        "Hvordan reagerede de, da de hørte, at du var på vej?", "Det, de gerne vil lære dig, når du bliver større", "Det, de gjorde for os den første tid",
        "Hvem ligner du fra deres side af familien?"),
    "Dine første venner" to listOf("Mødregruppen/fædregruppen og de første venner", "Børnene i gruppen og deres navne", "Venner af familien med børn på din alder",
        "Babysalmesang, babysvømning eller andre hold"),
    "Dig og kæledyrene" to listOf("Sådan tog dyrene imod dig", "Hvad hedder dyrene, og hvor gamle er de?", "Hvordan reagerede dyrene første gang?",
        "Det sjoveste, der er sket mellem jer", "Hvem passer på hvem?", "Det første, du gjorde, da du så dyret", "Hvor sover dyret, når du sover?"),
    "Sådan var du som nyfødt" to listOf("Sådan så du ud", "Hvem synes vi, du lignede de første uger?", "Sådan var du", "Det, der overraskede os ved dig"),
    "Det kunne du lide" to listOf("Det gjorde dig glad", "Lyde og sange, du kunne lide", "Den, du helst ville være hos"),
    "Det kunne du ikke lide" to listOf("Det brød du dig ikke om", "Det, der fik dig til at protestere højlydt"),
    // Det særlige ved dig: one box per page, except where there are a few named boxes.
    "Dine kælenavne" to listOf("Dine kælenavne, og hvor de kommer fra", "Det kælenavn, vi brugte i maven"),
    "Sange vi synger for dig" to listOf("Sange far synger", "Sange mor synger", "Sange, vi selv har fundet på"),
    "Dig og dine særlige mennesker" to listOf("Søskende, fætre og kusiner", "Bedsteforældre", "Anden familie", "Familiens venner",
        "Din dagplejer eller pædagog", "Andre særlige mennesker"),
    "Familieligheder" to listOf("Det har du fra mor", "Det har du fra far", "Hvem synes familien, du ligner mest?"),
)

/** "De første gange": what fits each first, after the date and the questions every first has. */
private val milestoneIdeas = mapOf(
    "Første smil" to listOf("Hvem smilede du til?"),
    "Første rigtige grin" to listOf("Hvad fik dig til at grine?"),
    "Du holder hovedet" to listOf("Hvor lå du, og hvad kiggede du efter?"),
    "Du ruller" to listOf("Fra ryg til mave eller omvendt?", "Hvor rullede du hen?"),
    "Du sidder" to listOf("Hvor sad du, og hvad legede du med?"),
    "Du kravler" to listOf("Hvad ville du hen til?"),
    "Du rejser dig" to listOf("Hvad støttede du dig til?"),
    "Første skridt" to listOf("Hvem gik du hen imod?", "Hvor mange skridt nåede du?"),
    "Første tand" to listOf("Hvilken tand, og hvordan opdagede vi den?"),
    "Første ord" to listOf("Hvad sagde du?", "Hvem eller hvad sagde du det til?", "Det næste ord, der kom"),
    "Første faste mad" to listOf("Hvad smagte du?", "Hvem gav dig skeen?"),
    "Du drikker af kop" to listOf("Hvad var der i koppen, og hvor meget kom indenfor?"),
    "Første svømmetur" to listOf("Hvor var vi?", "Hvad syntes du om vandet?"),
    "Første ferie" to listOf("Hvor tog vi hen, og hvordan kom vi derhen?", "Det bedste og det mest kaotiske ved turen"),
    "Første overnatning væk hjemmefra" to listOf("Hvor sov du, og hvem passede dig?", "Hvordan sov du – og hvordan sov vi?", "Hvad lavede vi, mens du var væk?"),
    "Første jul" to listOf("Hvor holdt vi jul, og hvem var med?", "Din første julegave, og hvad du legede mest med (papiret?)"),
    "Første nytår" to listOf("Hvor var vi, og var du vågen ved midnat?", "Hvad syntes du om fyrværkeriet?"),
    "Første påske" to listOf("Hvor var vi, og hvad smagte du?"),
)

/** Every "første gang" page starts like this; "Hvor var vi?" is left out where the page asks where itself. */
private fun firstTimePrompts(specific: List<String>): List<String> {
    val asksWhere = specific.any { it.startsWith("Hvor var vi") || it.startsWith("Hvor holdt vi") || it.startsWith("Hvor tog vi") || it.startsWith("Hvor sov du") }
    return (listOf("Dato") + listOfNotNull("Hvor var vi?".takeIf { !asksWhere }) + listOf("Hvem var med eller så det?", "Sådan gik det") + specific).distinct()
}

/**
 * Pages that are no longer part of the book. They are only shown when something is already written on them,
 * so nothing anyone wrote disappears.
 */
internal val retiredBookPageTitles = setOf(
    "Dit værelse", "Din første seng", "Første tur i barnevognen", "Første besøg",
    "De første ting, der fik dig til at smile", "Hvem synes familien, du ligner?", "Første fødselsdag",
    // Chosen away in November 2026.
    "Vi tror, du bliver…", "Dit allerførste billede", "Hvem var der?", "På hospitalet", "Din fødselsannonce",
    "Din første nat hjemme", "Det fik dig til at falde til ro", "Dit første år i billeder", "Dine små hænder og fødder", "En hårlok",
    "12 ting vi elsker ved dig", "Det har du lært os", "Et brev fra mor", "Et brev fra far", "Håb og ønsker for dit næste år",
)

/**
 * Reworded questions: the new text and the old texts it replaces. An answer under an old text is shown in, and
 * saved under, the new question, as long as the new question has no answer of its own.
 */
val bookPromptAliases: Map<String, List<String>> = mapOf(
    "Barnets fulde navn" to listOf("Barnets navn"),
    "Hvor var vi, og hvad var det første, vi sagde?" to listOf("Hvor stod vi, og hvad var det første, vi sagde?"),
    "Hvilke cravings eller aversioner havde mor?" to listOf("Det, mor fik lyst til at spise (og ikke kunne tåle)"),
    "Navnene, der næsten blev dit" to listOf("Navnene der næsten blev dit", "Andre navne, vi overvejede"),
    "Første gang vi mærkede dig sparke – hvor var vi?" to listOf("Første gang vi mærkede dig sparke"),
    "Det kan du nu" to listOf("Det kan jeg nu"),
    "Det elsker du lige nu" to listOf("Jeg elsker"),
    "Musik, film og serier, alle talte om" to listOf("Musik, film og serier alle talte om"),
    "Hvad ting kostede, fx mælk, benzin og et hus" to listOf("Hvad ting kostede, fx mælk og benzin"),
    "Hvilke sjove ansigtsudtryk lavede du?" to listOf("Dine udtryk, og hvad de betød"),
    "Ting du gør, som får os til at grine" to listOf("Det får os til at grine"),
    "Dine yndlingsbøger" to listOf("Bøgerne du elsker"),
    "Ting du er bange for" to listOf("Det gør dig utryg"),
    "Ting du elsker" to listOf("De sjoveste ting, du er vild med"),
    "Ord og lyde du siger" to listOf("Dine ord og lyde, og hvad de betyder"),
    "Din personlighed" to listOf("Sådan er du"),
    "Det elsker du at lave med mor" to listOf("Det elsker I at lave sammen"),
    "Det elsker du at lave med far" to listOf("Det elsker I at lave sammen"),
    "Små ting vi aldrig vil glemme" to listOf("Små øjeblikke, vi vil huske"),
    "Dine kælenavne, og hvor de kommer fra" to listOf("Dine kælenavne og hvor de kommer fra"),
)

/** Moves answers under a reworded question's old text to the [prompts] that replace it. Nothing is dropped. */
fun normalizeBookAnswers(prompts: List<String>, answers: Map<String, String>): Map<String, String> {
    var result = answers
    prompts.forEach { prompt ->
        if (result[prompt]?.isNotBlank() == true) return@forEach
        val old = bookPromptAliases[prompt].orEmpty().firstOrNull { it != prompt && it !in prompts && result[it]?.isNotBlank() == true } ?: return@forEach
        result = (result - old) + (prompt to result.getValue(old))
    }
    return result
}

private val shortBookPrompts = setOf(
    "Klokkeslæt", "Fødested", "Graviditetsuge", "Tøjstørrelse", "Tøj- og skostørrelse", "Barnets navn", "Barnets fulde navn",
    "Vores gæt", "Familiens gæt", "Hvem gættede nærmest?", "Hvor længe var vi der?", "Termin, vi fik at vide", "Hvem var med?",
    "Hvor var vi?", "Hvem badede dig?", "Hvem tog det første skridt?", "Hvor mange skridt nåede du?", "Hvad sagde du?",
)

/** Short facts get one line; the rest are for writing. */
fun isShortBookPrompt(prompt: String) = prompt.startsWith("Dato") || prompt.endsWith("(kg)") || prompt.endsWith("(cm)") || prompt.endsWith("(g)") || prompt in shortBookPrompts

/** The single box on a page that only has its title as question. */
internal fun bookStoryPrompts(title: String, chapter: String): List<String> = pagePrompts[title] ?: when (chapter) {
    "De første gange" -> firstTimePrompts(milestoneIdeas[title].orEmpty())
    else -> listOf(title)
}

/** Asked on every month page, after weight and length. */
private val everyMonth = listOf(
    "Det kan du nu", "Det elsker du lige nu", "Det brød du dig ikke om denne måned", "Et særligt minde", "Sådan sover du for tiden",
    "Sådan spiser du for tiden", "Din yndlingsleg eller -legetøj", "Lyde eller ord, du siger", "Hvem du så mest denne måned", "Tøjstørrelse",
    "Det, vi var mest trætte af", "Det, vi ikke vil glemme fra denne måned", "Et kælenavn, vi brugte meget",
)

/** Month by month: weight and length, the questions every month has, then what is usually new around that age. */
internal fun monthBookPrompts(month: Int): List<String> = (listOf("Vægt (kg)", "Længde (cm)") + everyMonth + when (month) {
    0 -> listOf("Sådan var du", "Det første, vi lagde mærke til ved dig", "Det, vi ikke kunne lade være med at kigge på", "Din første uge med tre ord")
    1 -> listOf("Hvordan sov du – og hvordan sov vi?", "Det fik dig til at falde til ro", "Dit første rigtige smil?", "Det, vi lærte om dig")
    2 -> listOf("Hvem fik dine første smil?", "Lyde, du er begyndt at lave", "Det, du kigger efter")
    3 -> listOf("Hvad kigger du længst på?", "Har du opdaget dine hænder?", "Første gang, du grinede højt")
    4 -> listOf("Hvad griber du efter?", "Hvad får dig til at grine højt?", "Vender du dig – og hvilken vej?")
    5 -> listOf("Hvordan kommer du rundt på gulvet?", "Hvem kender du nu, og hvem er du genert over for?", "Det, du putter i munden")
    6 -> listOf("Hvad har du smagt – og hvad var et klart nej?", "Den leg, du elsker lige nu", "Sidder du selv?")
    7 -> listOf("Hvad siger du (ba-ba, da-da …)?", "Hvor vil du helst sidde og kigge?", "Borte-tit-tit – hvad siger du til det?")
    8 -> listOf("Hvordan kommer du hen til det, du vil have?", "Hvad leder du efter, når det bliver gemt?", "Hvem bliver du ked af at sige farvel til?")
    9 -> listOf("Hvad peger du på?", "Hvem vinker du til?", "Hvad kan du spise selv?")
    10 -> listOf("Hvad rejser du dig op ad?", "Hvad har du lært at gøre efter os?", "Det mest drilske, du har gjort")
    11 -> listOf("Hvilke ord forstår du?", "Hvad vil du helst gøre selv?", "Skridt langs møblerne – hvor går du hen?")
    else -> listOf("Det, vi glæder os til at se dig lære", "Det største, der er sket i dit første år", "Hvordan har du ændret dig siden fødslen?")
}).distinct()
