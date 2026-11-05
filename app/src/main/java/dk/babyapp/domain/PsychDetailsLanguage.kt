package dk.babyapp.domain

// Sprog, perspektiv og social læring og ungdom (37–46).
internal val languageAndPerspectiveDetails: Map<Int, PsychDetail> = mapOf(
    37 to PsychDetail(
        summary = "Fonemisk kategorisering er evnen til at høre forskel på sproglyde, fx ‘ba’ og ‘pa’. Spædbørn kan høre forskel på lyde fra alle verdens sprog, men mod slutningen af det første år specialiserer de sig i deres eget sprog.",
        theory = listOf(
            "Et fonem er den mindste lyd, der kan skelne mellem ord. Sprog bruger forskellige fonemer: dansk skelner fx mellem mange vokaler, som andre sprog ikke gør.",
            "Patricia Kuhl beskriver, hvordan spædbørn ‘låser sig fast’ på lydene i det sprog, de hører mest. Det kaldes perceptuel indsnævring.",
        ),
        concepts = listOf(
            "Fonem: den mindste lyd, der ændrer et ords betydning.",
            "Kategorisk perception: at høre glidende lyde som enten den ene eller den anden kategori.",
            "Perceptuel indsnævring: at miste evnen til at høre forskelle, man ikke bruger.",
        ),
        studies = listOf(
            PsychStudy("Suttemetoden", "Eimas, Siqueland, Jusczyk og Vigorito (1971)", listOf(
                "1–4 måneder gamle spædbørn sutter på en sut, der registrerer, hvor hurtigt de sutter.",
                "Hver gang de sutter, hører de en lyd, fx ‘ba’. Efterhånden keder de sig og sutter langsommere.",
                "Så skifter lyden til ‘pa’.",
            ), listOf(
                "Spædbørnene sutter hurtigere igen, når lyden skifter. De hører altså forskellen.",
            )),
            PsychStudy("Hindi og salish", "Werker og Tees (1984)", listOf(
                "Engelsktalende spædbørn hørte lydforskelle fra hindi og salish, som ikke findes på engelsk.",
                "Hvis barnet drejede hovedet ved lydskift, fik det et legetøj at se.",
            ), listOf(
                "6–8 måneder gamle spædbørn hørte forskellene; 10–12 måneder gamle gjorde det næsten ikke.",
            )),
            PsychStudy("Mandarin fra et menneske eller en skærm", "Kuhl, Tsao og Liu (2003)", listOf(
                "9 måneder gamle amerikanske spædbørn fik 12 sessioner med mandarin, enten fra et menneske, en video eller en lydfil.",
            ), listOf(
                "Kun børnene, der havde mødt et levende menneske, bevarede evnen til at høre mandarinlyde.",
            )),
        ),
        meaning = listOf(
            "Spædbørn lærer sprog bedst i samspil med mennesker, ikke fra skærme.",
        ),
        atHome = listOf(
            "Snak, syng og pludre med barnet. Det er menneskelig kontakt, der tæller.",
            "Tosprogede familier kan tale hvert deres sprog til barnet.",
        ),
        caveats = listOf(
            "Evnen til at høre fremmede lyde forsvinder ikke helt; voksne kan stadig lære, det kræver blot mere.",
        ),
    ),
    38 to PsychDetail(
        summary = "Statistisk sproglæring er spædbørns evne til at finde mønstre i sproget, fx hvor et ord begynder og slutter, ved at registrere hvor ofte stavelser følger hinanden.",
        theory = listOf(
            "Talesprog er en lang strøm af lyde uden pauser mellem ordene. Spædbørn må derfor finde ord på anden vis.",
            "Stavelser inden for et ord følger ofte hinanden (fx ‘ba-by’), mens stavelser på tværs af ordgrænser er mere tilfældige. Spædbørn ser ud til at bruge denne statistik.",
        ),
        concepts = listOf(
            "Overgangssandsynlighed: hvor ofte en stavelse følger en anden.",
            "Segmentering: at dele talestrømmen op i ord.",
        ),
        studies = listOf(
            PsychStudy("Det kunstige sprog", "Saffran, Aslin og Newport (1996)", listOf(
                "8 måneder gamle spædbørn hørte to minutter af en monoton strøm af stavelser uden pauser, fx ‘bidakupadotigolabubidaku…’.",
                "Nogle stavelser fulgte altid hinanden (‘ord’), andre kun tilfældigt.",
                "Bagefter hørte de ‘ord’ og ‘ikke-ord’ og man målte, hvor længe de lyttede.",
            ), listOf(
                "Spædbørnene lyttede forskelligt til ord og ikke-ord efter kun to minutter.",
                "Det viste, at spædbørn kan registrere statistiske mønstre i sproget.",
            )),
        ),
        meaning = listOf(
            "Spædbørn er aktive lærende, der finder mønstre i det, de hører hver dag.",
        ),
        atHome = listOf(
            "Gentag ord og sætninger, fx i rim, remser og sange.",
            "Barnet begynder at genkende ord, længe før det selv siger dem.",
        ),
        caveats = listOf(
            "Statistisk læring er kun én af flere strategier; tryk, rytme og sociale signaler hjælper også.",
        ),
    ),
    39 to PsychDetail(
        summary = "Fast mapping er evnen til at koble et nyt ord til en betydning efter meget få møder. Det hjælper børn med at lære mange ord hurtigt, især fra 18 måneder til 6 år.",
        theory = listOf(
            "Børn lærer i gennemsnit flere nye ord om dagen i førskolealderen. Det er muligt, fordi de bruger antagelser om, hvad ord betyder.",
            "Ellen Markman foreslog bl.a., at børn antager, at ting kun har ét navn (gensidig eksklusivitet), og at et nyt ord sandsynligvis betyder hele tingen.",
            "Senere forskning viser, at den første kobling er skrøbelig; at huske ordet længe kræver gentagelse.",
        ),
        concepts = listOf(
            "Fast mapping: hurtig første kobling mellem ord og betydning.",
            "Gensidig eksklusivitet: en ting har kun ét navn.",
            "Helhedsantagelsen: et nyt ord betyder hele tingen, ikke en del af den.",
        ),
        studies = listOf(
            PsychStudy("Chromium", "Carey og Bartlett (1978)", listOf(
                "3–4-årige blev bedt om at hente ‘den chromium-farvede bakke, ikke den røde’. Chromium var et opdigtet farveord.",
            ), listOf(
                "Mange børn havde efter én gang lært noget om ordet og kunne huske det uger senere.",
            )),
            PsychStudy("Glemmer de igen?", "Horst og Samuelson (2008)", listOf(
                "2-årige hørte nye ord for nye ting og valgte rigtigt i øjeblikket.",
                "Fem minutter senere blev de testet igen.",
            ), listOf(
                "Børnene kunne vælge rigtigt i øjeblikket, men huskede sjældent ordet bagefter.",
            )),
        ),
        meaning = listOf(
            "Børn gætter godt, men for at huske et ord skal de høre det mange gange i forskellige situationer.",
        ),
        atHome = listOf(
            "Navngiv ting, når barnet er interesseret i dem, og gentag ordet senere.",
            "Læs de samme bøger igen og igen.",
        ),
        caveats = listOf(
            "Ordforråd varierer meget mellem børn; det er normalt.",
        ),
    ),
    40 to PsychDetail(
        summary = "Pragmatik er evnen til at bruge og forstå sprog i en social sammenhæng: at forstå, hvad folk mener, ikke kun hvad de siger. Det udvikles fra førskolealder gennem skolealder.",
        theory = listOf(
            "Filosoffen Paul Grice beskrev, at samtaler bygger på samarbejde: vi forventer, at andre siger det relevante, sandt og tilstrækkeligt.",
            "Når nogen siger ‘nogle af kagerne er spist’, forstår voksne, at ikke alle er spist. Børn tager ofte ordene mere bogstaveligt.",
        ),
        concepts = listOf(
            "Implikatur: det, der menes, men ikke siges direkte.",
            "Bogstavelig forståelse: at forstå ordene helt præcist.",
            "Referentiel kommunikation: at beskrive så andre kan forstå, hvad man mener.",
        ),
        studies = listOf(
            PsychStudy("Nogle giraffer har lange halse", "Ira Noveck (2001)", listOf(
                "Børn og voksne vurderede udsagn som ‘nogle giraffer har lange halse’.",
            ), listOf(
                "Børn på 7–10 år sagde ofte ‘sandt’; voksne sagde oftere ‘forkert’, fordi alle giraffer har det.",
            )),
            PsychStudy("Bogstaveligt, men ikke dumt", "Katsos og Bishop (2011)", listOf(
                "5–6-årige bedømte en dukke, der sagde ‘nogle’, når ‘alle’ var korrekt, på en skala.",
            ), listOf(
                "Børnene gav dukken lavere score, så de lagde mærke til det, men de ville ikke kalde det forkert.",
            )),
            PsychStudy("Beskriv figuren", "Glucksberg og Krauss (1966–1967)", listOf(
                "To børn sidder med en skærm imellem sig og skal beskrive mærkelige figurer for hinanden.",
            ), listOf(
                "Førskolebørn gav ofte beskrivelser, der kun gav mening for dem selv, fx ‘mors hat’.",
                "Evnen forbedredes gennem skolealderen.",
            )),
        ),
        meaning = listOf(
            "Små børn kan tage ord meget bogstaveligt; ironi og antydninger forstås først senere.",
        ),
        atHome = listOf(
            "Barnet siger ‘du sagde jo, at …’ når noget ikke passer præcist.",
            "Ironi som ‘det var sørme godt’ når noget går galt, forstås ofte først omkring 6–8 år.",
        ),
        caveats = listOf(
            "Børn forstår ofte mere, end testene viser, fordi testopgaverne kan være kunstige.",
        ),
    ),
    41 to PsychDetail(
        summary = "Visuel perspektivtagning er forståelsen af, hvad andre kan se, og hvordan noget ser ud fra deres side. Den udvikles i to trin: først hvad andre kan se, senere hvordan det ser ud for dem.",
        theory = listOf(
            "Piaget mente, at førskolebørn er egocentriske: de tror, at andre ser verden, som de selv gør.",
            "John Flavell beskrev to niveauer: niveau 1 (omkring 2–3 år), at forstå om andre kan se noget; niveau 2 (omkring 4–5 år), at forstå hvordan det ser ud fra deres side.",
        ),
        concepts = listOf(
            "Egocentrisme: at have svært ved at se tingene fra andres side.",
            "Niveau 1 og niveau 2 perspektivtagning: om andre kan se noget, og hvordan det ser ud for dem.",
        ),
        studies = listOf(
            PsychStudy("De tre bjerge", "Piaget og Inhelder (1956)", listOf(
                "Barnet ser en model med tre bjerge.",
                "En dukke placeres på den anden side, og barnet skal vælge et billede, der viser, hvad dukken ser.",
            ), listOf(
                "Børn under ca. 7 år valgte ofte deres eget perspektiv.",
            )),
            PsychStudy("Gem drengen fra politimanden", "Martin Hughes (1975)", listOf(
                "Barnet skulle gemme en dukkedreng, så to dukkepolitimænd ikke kunne se ham.",
            ), listOf(
                "Selv 3½–5-årige løste opgaven meget godt, fordi den gav mening for dem.",
            )),
            PsychStudy("Skildpadden", "Flavell, Everett, Croft og Flavell (1981)", listOf(
                "Et billede af en skildpadde lægges på bordet mellem barnet og den voksne.",
                "Barnet skal sige, om skildpadden står på benene eller ligger på ryggen for den voksne.",
            ), listOf(
                "3-årige kunne sige, om den voksne kunne se billedet, men ikke hvordan det så ud. 4–5-årige kunne begge.",
            )),
        ),
        meaning = listOf(
            "Små børn er ikke ‘egoistiske’; de har bare svært ved at forestille sig et andet synspunkt.",
        ),
        atHome = listOf(
            "Barnet viser dig en bog med billedsiden mod sig selv.",
            "Barnet ‘gemmer sig’ ved at dække sine egne øjne.",
        ),
        caveats = listOf(
            "Piaget undervurderede små børn; opgavens udformning betyder meget.",
        ),
    ),
    42 to PsychDetail(
        summary = "Social kategorisering er at dele mennesker ind i grupper, fx efter sprog, køn eller udseende. Det begynder tidligt og kan føre til præferencer for ens egen gruppe, især fra 3 til 8 år.",
        theory = listOf(
            "Mennesker inddeler verden i kategorier for at forstå den. Det gælder også andre mennesker.",
            "Spædbørn foretrækker velkendte ansigter og stemmer; senere udvikler børn ideer om grupper og kan begynde at foretrække deres egen gruppe.",
            "Frances Aboud viste, at fordomme hos børn ofte topper omkring 5–7 år og derefter falder, når de kan tænke mere fleksibelt.",
        ),
        concepts = listOf(
            "Ind-gruppe og ud-gruppe: ‘os’ og ‘dem’.",
            "Ind-gruppe-præference: at foretrække sin egen gruppe.",
        ),
        studies = listOf(
            PsychStudy("Ansigter", "Kelly m.fl. (2005, 2007)", listOf(
                "Spædbørn så ansigter fra forskellige etniske grupper, og man målte, hvor længe de kiggede.",
            ), listOf(
                "Nyfødte havde ingen præference; 3 måneder gamle foretrak ansigter fra deres egen gruppe.",
                "Omkring 9 måneder blev de dårligere til at skelne ansigter fra andre grupper.",
            )),
            PsychStudy("Sprog og accent", "Kinzler, Dupoux og Spelke (2007)", listOf(
                "Spædbørn hørte to personer tale, én på deres modersmål og én på et fremmed sprog.",
                "Bagefter tilbød begge et stykke legetøj.",
            ), listOf(
                "10 måneder gamle tog oftere imod fra den, der talte deres eget sprog. 5-årige valgte venner med deres egen accent.",
            )),
            PsychStudy("Minimale grupper", "Dunham, Baron og Carey (2011)", listOf(
                "5-årige blev tilfældigt delt i en rød og en grøn gruppe med t-shirts.",
            ), listOf(
                "Selv efter få minutter foretrak børnene deres egen gruppe og huskede positive ting om den.",
            )),
        ),
        meaning = listOf(
            "At inddele i grupper er naturligt, men hvordan voksne taler om forskelle, har betydning.",
        ),
        atHome = listOf(
            "‘Drenge kan ikke lege med dukker’ og lignende regler omkring 4–6 år.",
            "Mangfoldighed i bøger, legetøj og venner kan hjælpe.",
        ),
        caveats = listOf(
            "Præferencer varierer meget med miljø; studierne viser tendenser, ikke skæbne.",
        ),
    ),
    43 to PsychDetail(
        summary = "Social læring er at lære ved at se og efterligne andre. Børn efterligner fra spædbarnsalderen og lærer også at vælge, hvem de stoler på.",
        theory = listOf(
            "Albert Bandura viste, at børn lærer adfærd ved at observere andre, også uden selv at blive belønnet.",
            "Børn efterligner ofte nøjagtigt, også unødvendige trin (overimitation). Det kan hjælpe dem med at lære kultur og ritualer.",
            "Børn vælger fra omkring 3–4 år, hvem de lærer af, ud fra hvem der tidligere har haft ret.",
        ),
        concepts = listOf(
            "Observationslæring: at lære ved at se andre.",
            "Overimitation: at efterligne også unødvendige handlinger.",
            "Selektiv tillid: at vælge, hvem man lærer af.",
        ),
        studies = listOf(
            PsychStudy("Bobo-dukken", "Bandura, Ross og Ross (1961)", listOf(
                "3–6-årige så en voksen enten lege fredeligt eller slå og sparke en oppustelig dukke.",
                "Bagefter fik børnene lov at lege i rummet med dukken.",
            ), listOf(
                "Børn, der havde set den aggressive voksne, var mere aggressive og efterlignede de præcise handlinger.",
                "I et senere studie (1965) var det vigtigt, om den voksne blev belønnet eller straffet.",
            )),
            PsychStudy("Overimitation", "Horner og Whiten (2005); Lyons, Young og Keil (2007)", listOf(
                "Børn og chimpanser så en voksen åbne en kasse med unødvendige trin, fx at banke på toppen.",
            ), listOf(
                "Børnene efterlignede alle trin, selvom kassen var gennemsigtig; chimpanserne sprang de unødvendige over.",
            )),
            PsychStudy("Hvem har ret?", "Koenig og Harris (2005)", listOf(
                "3–4-årige så to voksne navngive kendte ting. Den ene navngav dem rigtigt, den anden forkert.",
                "Bagefter navngav de nye ting med forskellige ord.",
            ), listOf(
                "4-årige valgte oftere at stole på den, der før havde haft ret.",
            )),
        ),
        meaning = listOf(
            "Børn ser og efterligner meget. Hvad de ser hos voksne, har betydning.",
        ),
        atHome = listOf(
            "Barnet ‘taler i telefon’, ‘laver mad’ eller ‘støvsuger’ som dig.",
            "Barnet gentager ord og bandeord, du helst ikke ville have, det hørte.",
        ),
        caveats = listOf(
            "Bobo-studiet er kritiseret for at måle leg og ikke rigtig aggression.",
        ),
    ),
    44 to PsychDetail(
        summary = "Hypotetisk ræsonnement er evnen til at tænke over muligheder, ‘hvad nu hvis’, og teste hypoteser systematisk. Ifølge Piaget udvikles det i ungdommen, men tidlige former ses hos yngre børn.",
        theory = listOf(
            "Piaget beskrev det formelle operationelle stadie (fra ca. 11–12 år), hvor unge kan tænke abstrakt og systematisk om muligheder.",
            "Deanna Kuhn viste, at mange unge og voksne har svært ved at koordinere teori og bevis, og at videnskabelig tænkning kræver øvelse.",
            "Yngre børn kan ræsonnere hypotetisk, når opgaven er knyttet til leg eller fantasi.",
        ),
        concepts = listOf(
            "Hypotetisk-deduktiv tænkning: at lave en hypotese og teste den systematisk.",
            "Variabelkontrol: at ændre én ting ad gangen.",
        ),
        studies = listOf(
            PsychStudy("Pendulopgaven", "Inhelder og Piaget (1958)", listOf(
                "Børn og unge skulle finde ud af, hvad der bestemmer, hvor hurtigt et pendul svinger: længden af snoren, vægten eller kraften.",
            ), listOf(
                "Yngre børn ændrede flere ting på én gang. Unge testede én ting ad gangen og fandt, at det er længden.",
            )),
            PsychStudy("Fantasiens logik", "Dias og Harris (1988)", listOf(
                "4–6-årige hørte syllogismer som ‘alle fisk lever i træer; Tot er en fisk; bor Tot i et træ?’.",
                "Nogle hørte det fortalt i en fantasistemme.",
            ), listOf(
                "Børnene ræsonnerede logisk, når det blev præsenteret som fantasi.",
            )),
        ),
        meaning = listOf(
            "Unge kan tænke over abstrakte muligheder, men det kræver øvelse at tænke videnskabeligt.",
        ),
        atHome = listOf(
            "‘Hvad nu hvis …’-spørgsmål og fantasileg hos små børn.",
            "Diskussioner om samfund, politik og retfærdighed hos større børn.",
        ),
        caveats = listOf(
            "Ikke alle voksne bruger formel tænkning; det afhænger af uddannelse og erfaring.",
        ),
    ),
    45 to PsychDetail(
        summary = "Jævnaldrendes indflydelse bliver særligt stærk i ungdommen. Unge tager flere chancer, når venner ser på, fordi belønning og social accept vægtes højt.",
        theory = listOf(
            "Laurence Steinberg beskriver en ubalance i ungdomshjernen: belønningssystemet er meget følsomt, mens kontrolsystemet i pandelappen stadig modnes.",
            "Social accept er særlig vigtig i ungdommen, og afvisning føles stærkt.",
        ),
        concepts = listOf(
            "Belønningsfølsomhed: hjernens reaktion på belønning.",
            "Social accept: at høre til blandt jævnaldrende.",
        ),
        studies = listOf(
            PsychStudy("Kørespillet", "Gardner og Steinberg (2005)", listOf(
                "Unge, unge voksne og voksne spillede et computerspil om at køre over for gult, alene eller med venner til stede.",
            ), listOf(
                "Unge tog langt flere chancer, når vennerne så på. Voksne blev ikke påvirket.",
            )),
            PsychStudy("Hjernen i scanneren", "Chein, Albert, O’Brien, Uckert og Steinberg (2011)", listOf(
                "Spillet blev gentaget i en hjernescanner.",
            ), listOf(
                "Hos unge aktiveredes belønningsområder mere, når venner så på.",
            )),
            PsychStudy("Social påvirkning gennem livet", "Knoll, Magis-Weinberg, Speekenbrink og Blakemore (2015)", listOf(
                "Personer fra 8 til 59 år vurderede risici og så derefter andres vurderinger.",
            ), listOf(
                "Unge (12–14 år) blev mest påvirket af andre teenageres vurderinger.",
            )),
        ),
        meaning = listOf(
            "Unge er ikke dumme; de vejer social accept højt. Det er en normal del af at løsrive sig.",
        ),
        atHome = listOf(
            "Venner bliver vigtigere end familien for mange beslutninger.",
            "Gode venskaber og fællesskaber kan påvirke positivt.",
        ),
        caveats = listOf(
            "Indflydelse kan også være positiv: unge hjælper mere, når venner ser på.",
        ),
    ),
    46 to PsychDetail(
        summary = "Eksekutiv modning handler om, at hjernens kontrolsystemer fortsætter med at udvikles gennem ungdommen og ind i 20’erne.",
        theory = listOf(
            "Hjernen modnes bagfra og frem: pandelappen, der styrer planlægning og impulskontrol, er den sidste til at modnes.",
            "Under ungdommen sker en ‘oprydning’ af synapser og en isolering af nervebaner (myelinisering), der gør hjernen mere effektiv.",
            "B.J. Casey beskriver en ubalancemodel: følelsesmæssige systemer modnes tidligere end kontrolsystemer.",
        ),
        concepts = listOf(
            "Synaptisk beskæring: at fjerne forbindelser, der ikke bruges.",
            "Myelinisering: at isolere nervebaner, så signaler går hurtigere.",
        ),
        studies = listOf(
            PsychStudy("Hjernescanninger gennem barndom", "Giedd m.fl. (1999)", listOf(
                "Børn og unge fik MR-scanninger gentagne gange gennem flere år.",
            ), listOf(
                "Den grå substans i pandelappen toppede omkring puberteten og blev så tyndere; den hvide substans voksede.",
            )),
            PsychStudy("Øjenbevægelser", "Luna m.fl. (2004)", listOf(
                "Personer fra 8 til 30 år skulle kigge væk fra et lys, der dukkede op (anti-saccade).",
            ), listOf(
                "Evnen forbedredes til ca. 14–15 år, hvorefter fejl og hastighed nåede voksenniveau.",
            )),
            PsychStudy("Ubalancemodellen", "Casey, Jones og Hare (2008)", listOf(
                "Forskerne sammenstillede hjerneforskning om belønning og kontrol.",
            ), listOf(
                "Belønningssystemet er særligt aktivt i ungdommen, mens kontrolsystemerne stadig modnes.",
            )),
        ),
        meaning = listOf(
            "Unge har brug for støtte til planlægning, søvn og struktur, selvom de virker voksne.",
        ),
        atHome = listOf(
            "Teenageren glemmer aftaler og træffer impulsive beslutninger.",
            "Hjælp med kalender, rutiner og at tale beslutninger igennem.",
        ),
        caveats = listOf(
            "Hjernen modnes forskelligt; ‘færdig ved 25’ er en forenkling.",
        ),
    ),
)
