package dk.babyapp.domain

// Eksekutive funktioner og moralsk udvikling (29–36).
internal val executiveAndMoralDetails: Map<Int, PsychDetail> = mapOf(
    29 to PsychDetail(
        summary = "Hæmningskontrol er evnen til at stoppe en automatisk eller impulsiv reaktion og gøre noget andet i stedet. Den udvikles markant mellem 3 og 7 år og fortsætter med at modnes ind i ungdommen.",
        theory = listOf(
            "Hæmningskontrol er en af de tre centrale eksekutive funktioner sammen med arbejdshukommelse og kognitiv fleksibilitet. Adele Diamond beskriver dem som hjernens ‘styresystem’, der især afhænger af pandelappen.",
            "Hæmning gør det muligt at vente på tur, lade være med at tage en anden persons legetøj og fokusere trods forstyrrelser. Hos små børn er den svag, fordi pandelappen modnes langsomt.",
        ),
        concepts = listOf(
            "Responshæmning: at stoppe en handling, man er på vej til at gøre.",
            "Interferenskontrol: at holde fokus trods forstyrrelser.",
            "Eksekutive funktioner: de mentale processer, der styrer tanker og handlinger mod et mål.",
        ),
        studies = listOf(
            PsychStudy("Dag-nat-opgaven", "Gerstadt, Hong og Diamond (1994)", listOf(
                "Barnet ser kort med enten en sol eller en måne og stjerner.",
                "Barnet skal sige ‘nat’ til solen og ‘dag’ til månen, altså det modsatte af det naturlige.",
            ), listOf(
                "3½–4-årige havde meget svært ved opgaven og sagde ofte det automatiske ord.",
                "Fra ca. 6–7 år klarede de fleste opgaven næsten fejlfrit.",
            )),
            PsychStudy("Hoved-tæer-knæ-skuldre", "Ponitz, McClelland m.fl. (2008)", listOf(
                "Barnet skal gøre det modsatte af det, den voksne siger: når man siger ‘rør dit hoved’, skal barnet røre tæerne.",
                "Senere tilføjes flere regler.",
            ), listOf(
                "Resultaterne hos børnehavebørn forudsagde senere skolefærdigheder i matematik og læsning.",
            )),
            PsychStudy("Selvkontrol gennem livet", "Moffitt m.fl. (2011), Dunedin-studiet", listOf(
                "Omkring 1.000 børn fra New Zealand blev fulgt fra fødslen til 32 år.",
                "Selvkontrol i barndommen blev vurderet af forældre, lærere og observatører.",
            ), listOf(
                "Bedre selvkontrol i barndommen hang sammen med bedre helbred, økonomi og færre straffesager som voksen, også når der blev taget højde for intelligens og social baggrund.",
            )),
        ),
        meaning = listOf(
            "Små børn ved ofte godt, hvad de ikke må, men kan endnu ikke stoppe sig selv. Det er modning, ikke ulydighed.",
            "Lege med regler og skift (fx ‘Simon siger’ og stopdans) træner hæmningskontrol.",
        ),
        atHome = listOf(
            "Barnet siger ‘nej, nej’ og rører alligevel ved det forbudte.",
            "Lege som ‘rødt lys, grønt lys’ og stopdans bliver sjovere og lettere med alderen.",
        ),
        caveats = listOf(
            "Der er store forskelle mellem børn, og træning af én opgave overføres ikke altid til hverdagen.",
        ),
    ),
    30 to PsychDetail(
        summary = "Kognitiv fleksibilitet er evnen til at skifte mellem regler, perspektiver eller måder at løse noget på. Den udvikler sig især omkring 3–5 år og forbedres gennem hele skolealderen.",
        theory = listOf(
            "Philip Zelazo forklarer udviklingen med, at børn bliver bedre til at tænke over regler på et højere niveau: ikke bare ‘hvis rød, så her’, men ‘i farvelegen gælder det, at …’.",
            "Fleksibilitet bygger på både hæmningskontrol (at slippe den gamle regel) og arbejdshukommelse (at holde den nye regel i tankerne).",
        ),
        concepts = listOf(
            "Regelskift: at skifte fra én regel til en anden.",
            "Perseveration: at fortsætte med den gamle regel, selvom man ved den er skiftet.",
        ),
        studies = listOf(
            PsychStudy("Kortsorteringsopgaven (DCCS)", "Zelazo, Frye og Rapus (1996)", listOf(
                "Barnet sorterer kort med fx røde biler og blå blomster i to bakker efter farve.",
                "Efter nogle runder får barnet besked på at sortere efter form i stedet.",
                "Man ser, om barnet skifter regel.",
            ), listOf(
                "De fleste 3-årige fortsatte med at sortere efter farve, selvom de kunne fortælle den nye regel.",
                "De fleste 5-årige skiftede uden problemer.",
            )),
        ),
        meaning = listOf(
            "Børn kan vide, hvad de skal gøre, uden at kunne gøre det. Viden og handling hænger først sammen senere.",
        ),
        atHome = listOf(
            "En 3-årig har svært ved at skifte fra én leg til en anden eller fra én måde at gøre noget på.",
            "Varsling hjælper: ‘Om lidt skal vi stoppe og tage tøj på’.",
        ),
        caveats = listOf(
            "Resultaterne afhænger af, hvordan opgaven er bygget; små ændringer kan gøre det lettere for selv 3-årige.",
        ),
    ),
    31 to PsychDetail(
        summary = "Arbejdshukommelse er evnen til at holde information i tankerne og arbejde med den i kort tid, fx at huske en besked eller regne i hovedet. Den vokser støt fra 2 til 12 år og videre ind i ungdommen.",
        theory = listOf(
            "Alan Baddeley og Graham Hitch beskrev arbejdshukommelsen som et system med en central styring og to hjælpesystemer: et for lyd og sprog (den fonologiske løkke) og et for billeder og rum (det visuospatiale skitseblok).",
            "Små børn kan holde 2–3 ting i tankerne ad gangen; voksne omkring 4–7. Kapaciteten vokser med hjernens modning og med strategier som at gentage for sig selv.",
        ),
        concepts = listOf(
            "Fonologisk løkke: holder ord og lyde i tankerne.",
            "Visuospatial skitseblok: holder billeder og placeringer i tankerne.",
            "Talspænd: hvor mange tal man kan gentage i rækkefølge.",
        ),
        studies = listOf(
            PsychStudy("Arbejdshukommelsens model", "Baddeley og Hitch (1974)", listOf(
                "Voksne løste opgaver, mens de samtidig skulle huske tal.",
            ), listOf(
                "Folk kunne løse sproglige og rumlige opgaver samtidigt bedre end to af samme slags, hvilket tyder på adskilte delsystemer.",
            )),
            PsychStudy("Udvikling gennem barndommen", "Gathercole, Pickering, Ambridge og Wearing (2004)", listOf(
                "Børn fra 4 til 15 år løste en række arbejdshukommelsesopgaver.",
            ), listOf(
                "Kapaciteten voksede jævnt fra 4 år til de tidlige teenageår, og strukturen med delsystemer fandtes allerede fra 6 år.",
                "Svag arbejdshukommelse hang sammen med vanskeligheder i skolen.",
            )),
        ),
        meaning = listOf(
            "Lange beskeder med mange trin er svære for små børn; ét eller to trin ad gangen virker bedre.",
        ),
        atHome = listOf(
            "‘Tag skoene af, hæng jakken op og vask hænder’ kan være for meget for en 3-årig.",
            "Lege som ‘Jeg pakker min kuffert’ og huskespil træner arbejdshukommelsen.",
        ),
        caveats = listOf(
            "Computerbaseret træning forbedrer selve opgaverne, men viser sjældent effekt på skolepræstationer.",
        ),
    ),
    32 to PsychDetail(
        summary = "Udsat behovstilfredsstillelse er evnen til at vente på en større belønning frem for at tage en mindre med det samme. Den udvikles især fra 3 til 6 år.",
        theory = listOf(
            "Walter Mischel undersøgte, hvordan børn klarer at vente, og fandt, at strategier som at aflede sig selv eller tænke på godbidden som et billede gør ventetiden lettere.",
            "Nyere forskning viser, at ventetid ikke kun handler om selvkontrol, men også om tillid til, at belønningen faktisk kommer, og om social baggrund.",
        ),
        concepts = listOf(
            "Varm og kold tænkning: at fokusere på det lækre (varmt) eller på noget neutralt (koldt).",
            "Afledning: at kigge væk, synge eller lege for at klare ventetiden.",
        ),
        studies = listOf(
            PsychStudy("Skumfidustesten", "Walter Mischel m.fl. (1970’erne); Shoda, Mischel og Peake (1990)", listOf(
                "En 4–5-årig sidder ved et bord med en godbid, fx en skumfidus.",
                "Den voksne siger: Hvis du venter, til jeg kommer tilbage, får du to. Du kan ringe med klokken, men så får du kun én.",
                "Den voksne går i op til 15–20 minutter.",
            ), listOf(
                "Børn, der kiggede væk, sang eller legede, ventede længere.",
                "I opfølgningsstudier hang lang ventetid sammen med bedre skoleresultater i ungdommen.",
            )),
            PsychStudy("Genundersøgelsen", "Watts, Duncan og Quan (2018)", listOf(
                "Forskerne gentog studiet med over 900 børn med mere varieret social baggrund.",
            ), listOf(
                "Sammenhængen med senere skoleresultater var meget mindre, og den forsvandt næsten, når der blev taget højde for familiens baggrund.",
            )),
            PsychStudy("Den upålidelige voksne", "Kidd, Palmeri og Aslin (2013)", listOf(
                "Inden testen oplevede børnene en voksen, der enten holdt eller brød sine løfter.",
            ), listOf(
                "Børn, der havde mødt en pålidelig voksen, ventede i gennemsnit ca. 12 minutter; de andre kun ca. 3.",
                "At vente er derfor også en rationel vurdering af, om det kan betale sig.",
            )),
        ),
        meaning = listOf(
            "At kunne vente hænger sammen med både modning, strategier og tillid. Børn, der oplever, at løfter holdes, har god grund til at vente.",
        ),
        atHome = listOf(
            "Hold det, du lover: ‘Vi skal på legepladsen efter frokost’.",
            "Giv barnet noget at lave imens, det venter, fx at synge en sang.",
        ),
        caveats = listOf(
            "Skumfidustesten er ikke en test af barnets fremtid; resultaterne er langt mindre forudsigende, end man først troede.",
        ),
    ),
    33 to PsychDetail(
        summary = "Præference for hjælpsomme er tanken om, at selv spædbørn foretrækker figurer, der hjælper andre, frem for figurer, der hindrer. Det er et omdiskuteret forskningsfelt.",
        theory = listOf(
            "Paul Bloom, Kiley Hamlin og Karen Wynn har argumenteret for, at mennesker fødes med et rudimentært moralsk sans, som gør det muligt at vurdere andres handlinger som gode eller dårlige.",
            "Kritikere mener, at resultaterne kan forklares af enklere ting som bevægelsens form eller farve, og flere replikationer har ikke fundet effekten.",
        ),
        concepts = listOf(
            "Sociale vurderinger: at bedømme andre ud fra deres handlinger.",
            "Replikation: at gentage et studie for at se, om resultatet holder.",
        ),
        studies = listOf(
            PsychStudy("Bakken", "Hamlin, Wynn og Bloom (2007)", listOf(
                "6 og 10 måneder gamle spædbørn ser en lille figur med øjne prøve at kravle op ad en bakke.",
                "En anden figur hjælper den op; en tredje skubber den ned.",
                "Bagefter får barnet lov at vælge mellem hjælperen og hindreren.",
            ), listOf(
                "Langt de fleste spædbørn rakte ud efter hjælperen.",
            )),
            PsychStudy("Stor fælles gentagelse", "ManyBabies 4 (Lucca m.fl., 2024–2025)", listOf(
                "Mange laboratorier verden over gentog bakkestudiet med samme procedure.",
            ), listOf(
                "Samlet set valgte spædbørnene ikke hjælperen oftere end tilfældigt.",
                "Det sår tvivl om, hvor robust den oprindelige effekt er.",
            )),
        ),
        meaning = listOf(
            "Det er uvist, om spædbørn har en medfødt moralsk sans. Men allerede småbørn reagerer tydeligt på, om andre hjælper eller skader.",
        ),
        atHome = listOf(
            "Omkring 1½–2 år kan barnet tydeligt blive ked af det, når en figur i en bog bliver drillet.",
        ),
        caveats = listOf(
            "Et af de mest omdiskuterede områder i udviklingspsykologi; tag de tidlige resultater med forbehold.",
        ),
    ),
    34 to PsychDetail(
        summary = "Retfærdighedssans er forventninger om, at goder fordeles fair. Spædbørn ser ud til at forvente lige fordeling, og fra 3–8 år udvikler børn en mere aktiv sans for retfærdighed.",
        theory = listOf(
            "Børns retfærdighedssans udvikles fra at reagere på uretfærdighed mod en selv til at reagere på uretfærdighed mod andre, også når det koster noget.",
            "Kultur påvirker, hvornår og hvordan børn fordeler fair.",
        ),
        concepts = listOf(
            "Ulighedsaversion: ubehag ved ulige fordeling.",
            "Fordel til mig selv (fordelagtig ulighed) og fordel til andre (ufordelagtig ulighed).",
        ),
        studies = listOf(
            PsychStudy("Forventning om lige fordeling", "Sloane, Baillargeon og Premack (2012); Schmidt og Sommerville (2011)", listOf(
                "15–21 måneder gamle børn ser en voksen dele kiks eller mælk ud til to personer.",
                "Nogle gange lige, andre gange ulige.",
                "Man måler, hvor længe barnet kigger.",
            ), listOf(
                "Børnene kiggede længere på ulige fordelinger, som om de var overraskede.",
            )),
            PsychStudy("Del eller lad være", "Fehr, Bernhard og Rockenbach (2008)", listOf(
                "Børn på 3–8 år valgte mellem fordelinger af slik mellem sig selv og et andet barn.",
            ), listOf(
                "3–4-årige var mest selviske; fra 7–8 år valgte de fleste lige fordeling.",
            )),
            PsychStudy("Afvis den uretfærdige", "Blake og McAuliffe (2011)", listOf(
                "To børn kan acceptere eller afvise en fordeling af slik. Afviser man, får ingen noget.",
            ), listOf(
                "Selv 4-årige afviste, når de selv fik mindst.",
                "Fra ca. 8 år afviste de også, når de selv fik mest.",
            )),
        ),
        meaning = listOf(
            "Børn har tidligt forventninger om fairness, men at dele selv kommer gradvist.",
        ),
        atHome = listOf(
            "‘Det er ikke fair!’ når søskende får mere.",
            "Lad barnet dele kagen og lade den anden vælge.",
        ),
        caveats = listOf(
            "Kigge-tidsstudier med spædbørn er svære at tolke, og resultaterne varierer.",
        ),
    ),
    35 to PsychDetail(
        summary = "Moralsk ræsonnement handler om, hvordan børn begrunder, hvad der er rigtigt og forkert. Det udvikler sig fra at handle om straf og regler til at handle om hensyn, fairness og principper.",
        theory = listOf(
            "Jean Piaget beskrev overgangen fra heteronom moral (regler kommer fra voksne og kan ikke ændres) til autonom moral (regler er aftaler, der kan ændres), omkring 7–10 år.",
            "Lawrence Kohlberg beskrev tre niveauer: præ-konventionel (straf og belønning), konventionel (normer og forventninger) og post-konventionel (universelle principper).",
            "Elliot Turiel viste, at selv små børn skelner mellem moralske regler (om at skade andre) og konventioner (som at række hånden op).",
            "Carol Gilligan kritiserede Kohlberg for at overse en omsorgsetik baseret på relationer.",
        ),
        concepts = listOf(
            "Moralske regler: handler om skade, retfærdighed og rettigheder.",
            "Konventioner: sociale aftaler, der kan ændres.",
            "Omsorgsetik: moral baseret på omsorg og relationer.",
        ),
        studies = listOf(
            PsychStudy("Heinz-dilemmaet", "Lawrence Kohlberg (1958 og senere)", listOf(
                "Børn og unge hører om Heinz, hvis kone er døende. En apoteker tager overpris for medicinen.",
                "Heinz stjæler medicinen. Var det rigtigt eller forkert, og hvorfor?",
            ), listOf(
                "Kohlberg fokuserede på begrundelsen. Yngre børn begrundede med straf; ældre med normer og principper.",
            )),
            PsychStudy("Moral og konvention", "Judith Smetana (1981); Elliot Turiel", listOf(
                "Små børn hørte om forskellige regelbrud, fx at slå et andet barn eller ikke at rydde op.",
                "De blev spurgt: Ville det være okay, hvis der ikke var en regel?",
            ), listOf(
                "Allerede 2½–4-årige sagde, at det at slå var forkert, selvom der ikke var en regel, men at oprydning afhang af reglen.",
            )),
        ),
        meaning = listOf(
            "Børns moralske tænkning bliver mere nuanceret, men allerede små børn ved, at det er forkert at skade andre.",
        ),
        atHome = listOf(
            "‘Du må ikke, fordi du bliver skældt ud’ hos små børn; ‘det er ikke fair’ hos større.",
            "Snak om dilemmaer i historier og bøger.",
        ),
        caveats = listOf(
            "Kohlbergs teori er kritiseret for at være vestlig og for at fokusere på tænkning frem for handling.",
        ),
    ),
    36 to PsychDetail(
        summary = "Intention versus konsekvens handler om, hvorvidt børn bedømmer en handling ud fra, hvad personen ville, eller ud fra, hvad der skete. Små børn fokuserer på konsekvensen; fra omkring 5–7 år vægter intentionen mere.",
        theory = listOf(
            "Piaget mente, at små børn bedømmer ud fra skadens størrelse, og at de først senere tager hensyn til intention.",
            "Nyere forskning viser, at børn tidligere tager hensyn til intention, når historien er enkel, men at det at kombinere intention og konsekvens er svært.",
        ),
        concepts = listOf(
            "Intention: hvad personen ville.",
            "Konsekvens: hvad der faktisk skete.",
            "Uheld: en skadelig konsekvens uden ond intention.",
        ),
        studies = listOf(
            PsychStudy("Koppehistorierne", "Jean Piaget (1932)", listOf(
                "Børn hørte om en dreng, der ved et uheld smadrede 15 kopper, og en dreng, der smadrede én kop, mens han prøvede at snuppe syltetøj.",
                "Hvem var værst?",
            ), listOf(
                "Yngre børn sagde drengen med de 15 kopper; ældre sagde drengen med syltetøjet.",
            )),
            PsychStudy("Bolden", "Sharon Nelson (1980)", listOf(
                "3-årige hørte historier, hvor en dreng kaster en bold med god eller dårlig intention, og bolden rammer eller ikke rammer.",
                "Historierne blev vist med billeder.",
            ), listOf(
                "Selv 3-årige tog hensyn til intentionen, når den blev gjort tydelig.",
            )),
            PsychStudy("Straf eller skyld?", "Cushman, Sheketoff, Wharton og Carey (2013)", listOf(
                "Børn på 4–8 år vurderede både, om en person var slem, og om personen skulle straffes.",
            ), listOf(
                "Intention påvirkede tidligt, hvor slem en person var; men for straf vægtede konsekvensen længere.",
            )),
        ),
        meaning = listOf(
            "Det kan være svært for små børn at forstå, at et uheld ikke er det samme som at gøre noget med vilje.",
        ),
        atHome = listOf(
            "‘Han skubbede mig!’ – også når det var et uheld.",
            "Hjælp med ord: ‘Han ville ikke gøre det, det var et uheld’.",
        ),
        caveats = listOf(
            "Resultater afhænger meget af, hvordan historierne er bygget og fortalt.",
        ),
    ),
)
