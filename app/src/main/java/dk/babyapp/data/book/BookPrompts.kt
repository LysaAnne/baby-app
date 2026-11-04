package dk.babyapp.data.book

// Two or three concrete, specific writing starters per page: small questions that call up a real memory
// rather than "tell about it". Page IDs never change, and answers saved under older questions are still
// shown and exported with the page, so changing a question never hides what anyone wrote.
private val pagePrompts = mapOf(
    // Før du kom
    "Om mor" to listOf("Mors job og hverdag, da du var på vej", "Mors yndlingsmad, -sang og -sted lige nu", "Noget fra mors egen barndom, du skal kende"),
    "Om far" to listOf("Fars job og hverdag, da du var på vej", "Fars yndlingsmad, -sang og -sted lige nu", "Noget fra fars egen barndom, du skal kende"),
    "Vores familie" to listOf("Hvem bor sammen med dig, og hvad kalder vi hinanden?", "En helt almindelig søndag hos os", "En tradition, vi gerne vil give videre til dig"),
    "Sådan mødtes vi" to listOf("Hvor og hvornår så vi hinanden første gang?", "Det første, vi lagde mærke til ved hinanden", "Vores første date – og hvad gik galt eller godt?"),
    "Da vi fandt ud af, at du var på vej" to listOf("Dato", "Hvor stod vi, og hvad var det første, vi sagde?", "Hvem fik det at vide først, og hvordan reagerede de?"),
    "Graviditeten" to listOf("Det, mor fik lyst til at spise (og ikke kunne tåle)", "Første gang vi mærkede dig sparke – hvor var vi?", "Det, vi gik og tænkte mest på om natten"),
    "Første scanning" to listOf("Dato", "Hvad lavede du på skærmen?", "Det første, vi sagde, da vi gik ud derfra"),
    "Vi tror, du bliver…" to listOf("Vores gæt (pige/dreng, dato, vægt, hårfarve)", "Familiens gæt", "Hvem gættede nærmest?"),
    "Dit navn" to listOf("Hvem fandt på navnet, og hvornår var vi sikre?", "Hvad betyder navnet, og hvem er du opkaldt efter?", "Navnene der næsten blev dit"),
    "Babyshower" to listOf("Dato", "Hvem var med, og hvilke lege legede vi?", "Den sjoveste eller mest rørende gave"),
    "Dit værelse og dine første ting" to listOf("Farven på væggene, og hvor din seng stod", "Ting, der er arvet eller lavet til dig – og af hvem", "Det første stykke tøj, vi købte til dig"),
    "Beskeder til dig før fødslen" to listOf("Kære lille du …", "Det glæder vi os allermest til at vise dig"),
    "Den sidste tid i maven" to listOf("Hvordan fik vi tiden til at gå de sidste uger?", "Det pakkede vi i hospitalstasken (og glemte)", "Hvad vejret og årstiden var lige før du kom"),
    // Fødslen
    "Sådan begyndte fødslen" to listOf("Dato og klokkeslæt", "Hvor var vi, og hvad lavede vi, da det gik i gang?", "Hvem ringede vi til først?"),
    "Historien om din fødsel" to listOf("Fortællingen om din fødsel", "Det, der hjalp mor mest undervejs", "Musikken, ordene eller hænderne, vi husker"),
    "Det første øjeblik med dig" to listOf("De første ord, der blev sagt, da du kom ud", "Hvem holdt dig først, og hvad gjorde du?", "Det første, vi lagde mærke til ved dit ansigt"),
    "Dit allerførste billede" to listOf("Hvem tog billedet, og hvad skete der lige da?"),
    "Din første dag" to listOf("Hvordan sov og spiste du det første døgn?", "Første besøgende og de første beskeder vi sendte", "Det, vi kiggede længst på ved dig"),
    "Hvem var der?" to listOf("Jordemoder, læger og andre, der hjalp os", "Hvem ventede derude eller i telefonen?"),
    "På hospitalet" to listOf("Hvor længe var vi der, og hvilken stue lå vi på?", "Maden, lydene og personalet vi husker", "Et øjeblik fra hospitalet, vi griner af nu"),
    "Da vi tog dig med hjem" to listOf("Dato", "Hvad havde du på, og hvem kørte?", "Det første, vi gjorde, da vi kom ind ad døren"),
    "Dit hospitalsarmbånd" to listOf("Det står på armbåndet"),
    "Din fødselsannonce" to listOf("Teksten vi sendte ud", "Den sødeste eller sjoveste hilsen, vi fik"),
    // Den første tid hjemme
    "Dit hjem" to listOf("Adressen, og hvordan der så ud", "Dit yndlingssted i huset", "Lydene du faldt i søvn til"),
    "Din første nat hjemme" to listOf("Hvor sov du, og hvor mange gange vågnede du?", "Hvem var mest nervøs – og hvorfor?"),
    "Det første bad" to listOf("Dato", "Hvem badede dig, og hvad syntes du om vandet?", "Græd du, eller nød du det?"),
    "Den første gåtur" to listOf("Dato", "Hvor gik vi hen, og hvem var med?", "Hvad var vejret, og hvad havde du på?"),
    "Mennesker, der kom for at møde dig" to listOf("Hvem kom først, og hvad havde de med?", "Det sødeste, nogen sagde om dig", "Hvem fik dig til at falde i søvn?"),
    "Dine bedsteforældre" to listOf("Hvad kalder du dine bedsteforældre?", "Første gang de holdt dig – hvad sagde de?", "Noget, de gerne vil lære dig, når du bliver større"),
    "Dine første venner" to listOf("Børnene i mødregruppen og deres navne", "Hvor mødtes vi, og hvad lavede vi sammen?"),
    "Dig og kæledyrene" to listOf("Hvordan reagerede dyrene første gang?", "Det sjoveste, der er sket mellem jer"),
    "Sådan var du som nyfødt" to listOf("Dit hår, dine øjne og dine små fingre", "Lyde og grimasser, du lavede", "Hvem synes vi, du lignede de første uger?"),
    "Det kunne du lide" to listOf("Det, der fik dig til at åbne øjnene helt op", "Din yndlingsstilling at ligge eller blive båret i"),
    "Det kunne du ikke lide" to listOf("Det, der fik dig til at protestere højlydt", "Hvordan vi fandt ud af det"),
    "Det fik dig til at falde til ro" to listOf("Den sang, lyd eller bevægelse, der virkede bedst", "Hvem var bedst til at få dig til at sove – og hvordan?"),
    // Det særlige ved dig
    "Dine mange ansigtsudtryk" to listOf("Dit 'jeg er sulten'-ansigt", "Dit 'det her er mærkeligt'-ansigt", "Det udtryk, vi elsker allermest"),
    "Sådan ser du ud, når du sover" to listOf("Hvor har du armene, når du sover?", "Lyde du laver i søvne"),
    "Ting du gør, som får os til at grine" to listOf("Det sjoveste, du har gjort indtil nu", "Noget, du gør igen og igen, fordi vi griner"),
    "Dine kælenavne" to listOf("Dine kælenavne, og hvem der fandt på dem", "Det navn, kun én person bruger"),
    "Sange vi synger for dig" to listOf("Godnatsangen", "Den sang, der altid får dig i godt humør", "En sang, vi selv har fundet på"),
    "Dine yndlingsbøger" to listOf("Bogen du vil have læst igen og igen", "Den side, du altid peger på"),
    "Dit yndlingslegetøj" to listOf("Hvad er det, og hvem gav dig det?", "Hvad kalder du det?"),
    "Ting du er bange for" to listOf("Det gør dig utryg", "Sådan trøster vi dig bedst"),
    "De mærkeligste ting du elsker" to listOf("Den mærkeligste ting, du er vild med", "Hvor fandt du den?"),
    "Ord og lyde du siger" to listOf("Din første lyd, der lød som et ord", "Dine egne ord, og hvad de betyder"),
    "Din personlighed" to listOf("Dig med tre ord", "Hvad gør du, når du møder nye mennesker?", "Er du en morgen- eller aftenbaby?"),
    "Dig og mor" to listOf("Jeres egen lille leg", "Det sted, I helst er sammen", "Det, mor gør, som kun virker for hende"),
    "Dig og far" to listOf("Jeres egen lille leg", "Det sted, I helst er sammen", "Det, far gør, som kun virker for ham"),
    "Dig og dine særlige mennesker" to listOf("Hvem lyser dit ansigt op, når de kommer ind?", "Det, I altid laver sammen"),
    "Familieligheder" to listOf("Det har du fra mor", "Det har du fra far", "Hvem synes familien, du ligner mest?"),
    "Dit første år i billeder" to listOf("Billedtekster til året"),
    "Dine små hænder og fødder" to listOf("Dato", "Hvor store var de? (fx en tommelfinger lang)", "Sådan lavede vi aftrykkene – og hvor meget du sprællede"),
    "En hårlok" to listOf("Dato", "Hvem klippede, og hvordan var dit hår?"),
    "Små ting vi aldrig vil glemme" to listOf("En helt almindelig dag, der blev særlig", "Et lille øjeblik, vi gerne ville fryse"),
    // Din første fødselsdag
    "12 ting vi elsker ved dig" to listOf("Tolv ting, én pr. linje"),
    "Det har du lært os" to listOf("Det har du lært os om os selv", "Det har vi lært om tålmodighed (og søvn)"),
    "Et brev fra mor" to listOf("Kære …"),
    "Et brev fra far" to listOf("Kære …"),
    "Håb og ønsker for dit næste år" to listOf("Ting, vi glæder os til at gøre med dig", "Det ønsker vi for dig"),
)

/** For each first: two questions that fit the milestone, after the date. */
private val milestoneIdeas = mapOf(
    "Første smil" to listOf("Hvem smilede du til?", "Hvad lavede vi lige da?"),
    "Første rigtige grin" to listOf("Hvad fik dig til at grine?", "Hvem hørte det, og prøvede vi at få dig til det igen?"),
    "Du holder hovedet" to listOf("Hvor lå du, og hvad kiggede du efter?"),
    "Du ruller" to listOf("Fra ryg til mave eller omvendt?", "Så du selv overrasket ud?"),
    "Du sidder" to listOf("Hvor sad du, og hvad legede du med?", "Hvor længe holdt du balancen?"),
    "Du kravler" to listOf("Hvad ville du hen til?", "Kravlede du fremad, baglæns eller på din egen måde?"),
    "Du rejser dig" to listOf("Hvad støttede du dig til?", "Hvordan kom du ned igen?"),
    "Første skridt" to listOf("Hvem gik du hen imod?", "Hvor mange skridt nåede du?"),
    "Første tand" to listOf("Hvilken tand, og hvordan opdagede vi den?", "Hvordan havde du det de dage?"),
    "Første ord" to listOf("Hvad sagde du?", "Hvem eller hvad sagde du det til?"),
    "Første faste mad" to listOf("Hvad smagte du?", "Dit ansigt, da du smagte (og hvor meget endte på hagen)"),
    "Du drikker af kop" to listOf("Hvad var der i koppen, og hvor meget kom indenfor?"),
    "Første svømmetur" to listOf("Hvor var vi?", "Hvad syntes du om vandet?"),
    "Første ferie" to listOf("Hvor tog vi hen, og hvordan kom vi derhen?", "Det bedste og det mest kaotiske ved turen"),
    "Første overnatning væk hjemmefra" to listOf("Hvor sov du, og hvem passede dig?", "Hvordan sov du – og hvordan sov vi?"),
    "Første jul" to listOf("Hvor holdt vi jul, og hvem var med?", "Din første julegave, og hvad du legede mest med (papiret?)"),
    "Første nytår" to listOf("Hvor var vi, og var du vågen ved midnat?"),
    "Første påske" to listOf("Hvor var vi, og hvad smagte du?"),
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
    prompt in setOf("Klokkeslæt", "Fødested", "Graviditetsuge", "Tøjstørrelse", "Barnets navn", "Vores gæt", "Familiens gæt", "Hvem gættede nærmest?", "Hvor længe var vi der?", "Godnatsangen", "Hvad kalder du det?")

internal fun bookStoryPrompts(title: String, chapter: String): List<String> = pagePrompts[title] ?: when (chapter) {
    "De første gange" -> listOf("Dato") + (milestoneIdeas[title] ?: listOf("Hvor var vi, og hvem så det?"))
    else -> listOf("Hvad skete der, og hvem var med?", "Det vil vi huske")
}

/** Month by month: what is usually new around that age, asked concretely. Weight and length come first. */
internal fun monthBookPrompts(month: Int): List<String> = listOf("Vægt (kg)", "Længde (cm)") + when (month) {
    0 -> listOf("Sådan var du", "Det første, vi lagde mærke til ved dig", "Et særligt minde")
    1 -> listOf("Hvordan sov du – og hvordan sov vi?", "Det fik dig til at falde til ro", "Et særligt minde")
    2 -> listOf("Hvem fik dine første smil?", "Lyde du er begyndt at lave", "Et særligt minde")
    3 -> listOf("Hvad kigger du længst på?", "Har du opdaget dine hænder?", "Et særligt minde")
    4 -> listOf("Hvad griber du efter?", "Hvad får dig til at grine højt?", "Et særligt minde")
    5 -> listOf("Hvordan kommer du rundt på gulvet?", "Hvem kender du nu, og hvem er du genert over for?", "Et særligt minde")
    6 -> listOf("Hvad har du smagt – og hvad var et klart nej?", "Den leg, du elsker lige nu", "Et særligt minde")
    7 -> listOf("Hvad siger du (ba-ba, da-da …)?", "Hvor vil du helst sidde og kigge?", "Et særligt minde")
    8 -> listOf("Hvordan kommer du hen til det, du vil have?", "Hvad leder du efter, når det bliver gemt?", "Et særligt minde")
    9 -> listOf("Hvad peger du på?", "Hvem vinker du til?", "Et særligt minde")
    10 -> listOf("Hvad rejser du dig op ad?", "Hvad har du lært at gøre efter os?", "Et særligt minde")
    11 -> listOf("Hvilke ord forstår du?", "Hvad vil du helst gøre selv?", "Et særligt minde")
    else -> listOf("Det kan du nu", "Det, vi glæder os til at se dig lære", "Et særligt minde")
}
