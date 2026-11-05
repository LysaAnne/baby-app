package dk.babyapp.domain

// Kognitiv udvikling (1–9).
internal val cognitionDetails: Map<Int, PsychDetail> = mapOf(
    1 to PsychDetail(
        summary = "Objektpermanens er forståelsen af, at ting og personer fortsætter med at eksistere, også når man ikke kan se, høre eller røre dem. Det er grundlaget for at lede efter ting, for borte-tit-tit og for at savne en, der er gået.",
        theory = listOf(
            "Jean Piaget beskrev objektpermanens som en del af det sensomotoriske stadie (0–2 år). Han mente, at barnet gradvist opbygger forståelsen gennem egne handlinger: først findes der kun det, barnet sanser lige nu, og først omkring 18–24 måneder har barnet en fuld, indre forestilling om ting, der er væk.",
            "Senere forskning med kiggetidsmetoder har udfordret Piagets tidsplan. Renée Baillargeon og andre har vist, at spædbørn allerede få måneder gamle reagerer, når noget umuligt sker med en skjult genstand. Mange forskere skelner derfor i dag mellem at have en forventning om, at ting findes, og at kunne bruge den forventning til at handle, altså lede, gribe og huske hvor.",
        ),
        concepts = listOf(
            "Sensomotorisk stadie: Piagets første stadie, hvor barnet forstår verden gennem sanser og bevægelser.",
            "Kiggetid (violation of expectation): spædbørn kigger længere på det overraskende; bruges til at undersøge, hvad de forventer.",
            "Synlige og usynlige flytninger: at kunne følge en ting, man ser blive flyttet, kommer før at kunne regne ud, hvor en skjult ting er blevet flyttet hen.",
        ),
        studies = listOf(
            PsychStudy("Det skjulte legetøj", "Jean Piaget (1954)", listOf(
                "Et legetøj, barnet er interesseret i, lægges foran barnet.",
                "Mens barnet ser på, dækkes legetøjet med et klæde eller en skærm.",
                "Man ser, om barnet fjerner klædet og leder efter legetøjet.",
            ), listOf(
                "Før ca. 8 måneder holder de fleste op med at interessere sig for legetøjet, så snart det er dækket.",
                "Omkring 8–12 måneder løfter barnet klædet og finder det.",
                "Først mellem ca. 18 og 24 måneder kan barnet regne ud, hvor en ting er, efter at den er flyttet i skjul.",
            )),
            PsychStudy("Vindebroen", "Baillargeon, Spelke og Wasserman (1985)", listOf(
                "5 måneder gamle børn vænnes til en plade, der vipper 180 grader frem og tilbage som en vindebro.",
                "En kasse stilles bag pladen, så den ikke kan ses, når pladen rejser sig.",
                "I den mulige version stopper pladen, hvor kassen står. I den umulige version vipper den hele vejen, som om kassen var forsvundet.",
                "Man måler, hvor længe barnet kigger på hver version.",
            ), listOf(
                "Børnene kiggede længere på den umulige version, selvom den lignede det, de var vænnet til.",
                "Det tolkes som, at de forventede, at den skjulte kasse stadig var der.",
            )),
        ),
        meaning = listOf(
            "Børn har sandsynligvis en tidlig forventning om, at ting findes, længe før de kan lede efter dem.",
            "Det, der udvikles mellem 4 og 24 måneder, er især evnen til at handle ud fra den viden og holde den i hukommelsen.",
        ),
        atHome = listOf(
            "Borte-tit-tit bliver sjovt, fordi barnet begynder at forvente, at du dukker op igen.",
            "Barnet kigger efter en ting, der falder ned fra højstolen, i stedet for at glemme den.",
            "Omkring 8–10 måneder begynder barnet at løfte tæpper og puder for at finde gemt legetøj.",
            "Protester, når du går ud af rummet, hænger delvist sammen med, at barnet nu ved, at du findes et andet sted.",
        ),
        caveats = listOf(
            "Længere kiggetid viser, at barnet opfatter en forskel, men ikke nødvendigvis hvorfor; nogle forskere mener, at simplere forklaringer (fx at noget er nyt eller bevæger sig mere) kan forklare dele af resultaterne.",
            "Aldrene er gennemsnit. Store forskelle mellem børn er helt almindelige.",
        ),
    ),
    2 to PsychDetail(
        summary = "A-not-B-fejlen er, når et barn leder efter en ting det sted, hvor det plejede at finde den (A), selvom det lige har set tingen blive gemt et nyt sted (B). Den ses typisk omkring 8–12 måneder.",
        theory = listOf(
            "Piaget så fejlen som et tegn på, at objektforståelsen endnu ikke var færdig: barnet forbinder tingen med sin egen handling (at lede ved A) frem for med tingens egentlige placering.",
            "I dag forklares fejlen oftere med udviklingen af eksekutive funktioner. Adele Diamond viste, at fejlen afhænger af, hvor længe barnet skal vente, og knyttede den til modningen af pandelappen: barnet skal både huske det nye sted og hæmme den indøvede bevægelse mod A. Esther Thelen og Linda Smith har vist, at også kroppens holdning og bevægelsesvaner spiller ind.",
        ),
        concepts = listOf(
            "Perseveration: at gentage en handling, der ikke længere passer til situationen.",
            "Hæmningskontrol: evnen til at stoppe en vane eller impuls.",
            "Arbejdshukommelse: at holde ny information aktiv i kort tid, fx hvor tingen nu er.",
        ),
        studies = listOf(
            PsychStudy("A-ikke-B", "Jean Piaget; videreudviklet af Adele Diamond (1985)", listOf(
                "Et legetøj gemmes flere gange under klæde A, og barnet finder det hver gang.",
                "Mens barnet ser på, gemmes legetøjet nu under klæde B.",
                "Barnet skal vente et kort øjeblik, fx et par sekunder, før det må lede.",
            ), listOf(
                "Mange børn på 8–10 måneder leder under A igen.",
                "Jo længere ventetid, jo oftere sker fejlen. Med alderen kan børnene klare længere ventetider.",
                "Fejlen forsvinder typisk omkring 12 måneder.",
            )),
        ),
        meaning = listOf(
            "Barnet ved ofte godt, hvor tingen er (nogle kigger mod B, mens de rækker mod A), men har endnu svært ved at styre handlingen.",
            "Fejlen er et tidligt vindue ind til udviklingen af selvkontrol og arbejdshukommelse.",
        ),
        atHome = listOf(
            "Barnet leder efter bamsen det sted, den plejer at ligge, selvom du lige har lagt den et andet sted.",
            "Gemmelege med to steder at gemme er sjove og udfordrende omkring 9–12 måneder.",
        ),
        caveats = listOf(
            "Fejlen afhænger meget af opstillingen: antal gemmesteder, ventetid, og om den voksne kigger på barnet og taler til det.",
        ),
    ),
    3 to PsychDetail(
        summary = "Årsagsforståelse er evnen til at forstå, at noget får noget andet til at ske. Den spirer allerede hos spædbørn, men udvikler sig tydeligt fra omkring 1 år, hvor barnet aktivt afprøver, hvad der sker, når det gør noget.",
        theory = listOf(
            "Allerede helt små børn lærer sammenhænge mellem egne handlinger og det, der sker bagefter. Det kaldes kontingenslæring og er den tidligste form for årsagsforståelse.",
            "Fra ca. 6 måneder ser spædbørn forskel på en bold, der støder til en anden og sætter den i gang, og en bold, der sætter i gang uden berøring. Fra 1–2 års alderen begynder børn at eksperimentere systematisk, og i førskolealderen kan de udlede skjulte årsager ud fra mønstre af hændelser. Alison Gopnik har beskrevet småbørn som små videnskabsfolk, der tester hypoteser gennem leg.",
        ),
        concepts = listOf(
            "Kontingenslæring: at lære, at en handling hænger sammen med en bestemt følge.",
            "Kausal slutning: at regne ud, hvad der er årsagen, ud fra hvad der sker hvornår.",
            "Eksperimenterende leg: at gentage og variere en handling for at se, hvad der sker.",
        ),
        studies = listOf(
            PsychStudy("Uroen og benet", "Carolyn Rovee-Collier (fra 1969 og frem)", listOf(
                "En blød snor bindes fra spædbarnets ankel til en uro over sengen.",
                "Når barnet sparker, bevæger uroen sig.",
                "Dage eller uger senere hænges uroen op igen, nu uden snor, og man tæller spark.",
            ), listOf(
                "Allerede 2–3 måneder gamle børn sparker meget mere, når sparkene får uroen til at bevæge sig.",
                "De kan huske sammenhængen i dage, og ældre spædbørn husker den i flere uger.",
            )),
            PsychStudy("Blicket-maskinen", "Alison Gopnik og David Sobel (2000)", listOf(
                "Børn på 2–4 år får vist en kasse, der lyser og spiller musik, når bestemte klodser (blickets) lægges på.",
                "Nogle klodser tænder maskinen, andre gør ikke.",
                "Børnene skal finde ud af, hvilke klodser der er blickets, eller selv få maskinen til at gå i gang.",
            ), listOf(
                "Allerede 2-årige kunne bruge mønsteret af hændelser til at finde årsagen.",
                "De valgte de klodser, der havde sat maskinen i gang, også i nye kombinationer.",
            )),
        ),
        meaning = listOf(
            "Årsagsforståelse bygges op gennem egen aktivitet: barnet lærer mest af selv at gøre noget og se, hvad der sker.",
            "At kunne forklare, hvordan noget virker, kommer meget senere end at kunne bruge sammenhængen.",
        ),
        atHome = listOf(
            "Barnet smider skeen på gulvet igen og igen og ser, hvad der sker, og hvad du gør.",
            "Det trykker på knapper, tænder og slukker lyset og åbner og lukker skabe.",
            "Legetøj med årsag og virkning (tryk her, og noget popper op) er populært fra ca. 9–18 måneder.",
        ),
        caveats = listOf(
            "Spædbørns kiggetid ved bolde, der støder sammen, viser, at de opfatter forskelle, men ikke nødvendigvis at de forstår årsag, som voksne gør.",
        ),
    ),
    4 to PsychDetail(
        summary = "Udsat imitation er at efterligne noget, man har set, efter en pause, hvor den, man efterligner, ikke længere er der. Det kræver, at barnet har gemt et billede af handlingen i hukommelsen.",
        theory = listOf(
            "Piaget mente, at udsat imitation først kom omkring 18 måneder, når barnet kunne danne indre forestillinger. Andrew Meltzoff og senere Rachel Barr og Harlene Hayne viste, at den findes meget tidligere, og at hukommelsen for handlinger varer længere og længere med alderen.",
            "Imitation er ikke bare kopiering. Fra ca. 14 måneder efterligner børn selektivt: de vurderer, om den voksnes måde at gøre noget på var et valg eller et nødvendigt kompromis.",
        ),
        concepts = listOf(
            "Repræsentation: et indre billede eller en erindring, der kan hentes frem senere.",
            "Rationel imitation: at efterligne det, der giver mening ud fra situationen, ikke bare alt.",
        ),
        studies = listOf(
            PsychStudy("Lampen og panden", "Andrew Meltzoff (1988)", listOf(
                "14 måneder gamle børn ser en voksen tænde en lampe ved at røre den med panden i stedet for med hånden.",
                "Børnene må ikke selv røre lampen den dag.",
                "En uge senere får de lampen.",
            ), listOf(
                "Mange af børnene lænede sig frem og rørte lampen med panden.",
                "Næsten ingen børn i kontrolgruppen, som ikke havde set det, fandt selv på det.",
            )),
            PsychStudy("Hånden, der var optaget", "Gergely, Bekkering og Király (2002)", listOf(
                "En voksen tænder lampen med panden. I den ene version har den voksne et tæppe om skuldrene og holder det med hænderne; i den anden er hænderne frie.",
                "Bagefter får 14 måneder gamle børn lampen.",
            ), listOf(
                "Når den voksne havde frie hænder, brugte flest børn panden.",
                "Når hænderne var optaget, brugte de fleste børn hånden; de forstod, at panden kun var et nødvendigt valg.",
            )),
            PsychStudy("Vanten på hånddukken", "Barr, Dowden og Hayne (1996)", listOf(
                "En voksen tager en vante af en hånddukke, ryster den, så en klokke ringer, og sætter den på igen.",
                "Børnene får dukken 24 timer senere.",
            ), listOf(
                "Allerede 6 måneder gamle børn gentog dele af handlingen et døgn senere.",
                "Med alderen husker børnene flere trin og i længere tid.",
            )),
        ),
        meaning = listOf(
            "Spædbørn lærer meget ved at se på andre og kan bruge det senere, uden at nogen viser det igen.",
            "Børn efterligner fornuftigt: de prøver at forstå, hvad den voksne ville opnå.",
        ),
        atHome = listOf(
            "Barnet ‘taler i telefon’, fejer gulvet eller rører i gryden dagen efter at have set dig gøre det.",
            "Barnet gentager en lyd, en fagte eller en sang fra vuggestuen derhjemme.",
        ),
        caveats = listOf(
            "Hvor meget og hvor længe børn husker, afhænger meget af, om omgivelserne og tingene ligner dem, de lærte i.",
        ),
    ),
    5 to PsychDetail(
        summary = "Symbolsk tænkning er at forstå, at én ting kan stå for noget andet: en klods kan være en telefon, et billede kan vise en rigtig hund, og et ord kan betyde en ting. Det er grundlaget for sprog, leg og senere tal og bogstaver.",
        theory = listOf(
            "Piaget så symbolfunktionen som overgangen fra det sensomotoriske til det præoperationelle stadie omkring 18–24 måneder: barnet kan nu tænke på ting, der ikke er til stede, og lade som om.",
            "Judy DeLoache har vist, at det særligt svære er dobbelt repræsentation: at en model eller et billede både er en ting i sig selv og et symbol for noget andet. Jo mere interessant tingen er i sig selv, jo sværere er det for småbørn at se den som et symbol.",
        ),
        concepts = listOf(
            "Dobbelt repræsentation: at holde to forståelser af samme ting i hovedet på én gang.",
            "Lad-som-om-leg: leg hvor ting får nye betydninger, fx en banan som telefon.",
            "Præoperationelt stadie: Piagets stadie fra ca. 2–7 år præget af symboler, sprog og fantasi.",
        ),
        studies = listOf(
            PsychStudy("Dukkehuset og stuen", "Judy DeLoache (1987)", listOf(
                "Barnet ser en lille model af et rum, hvor en lille legetøjshund gemmes, fx bag sofaen.",
                "Barnet får at vide, at en stor hund er gemt samme sted i det rigtige rum, som modellen forestiller.",
                "Barnet går ind i det rigtige rum og leder.",
            ), listOf(
                "Kun få 2½-årige gik direkte til det rigtige sted.",
                "De fleste 3-årige klarede det.",
            )),
            PsychStudy("Rummet, der skrumpede", "DeLoache, Miller og Rosengren (1997)", listOf(
                "2½-årige fik at vide, at en maskine kunne skrumpe det rigtige rum til et lille rum.",
                "En dukke gemtes i det store rum, og børnene skulle finde den i det ‘skrumpede’ rum.",
            ), listOf(
                "Nu klarede de fleste 2½-årige opgaven.",
                "Når barnet tror, at det er det samme rum, behøver det ikke forstå modellen som et symbol, og opgaven bliver let.",
            )),
        ),
        meaning = listOf(
            "Det er ikke hukommelsen, der gør opgaven svær for 2½-årige, men at forstå en ting som et symbol for noget andet.",
            "Symbolsk forståelse udvikler sig gradvist og afhænger af erfaring med billeder, bøger og leg.",
        ),
        atHome = listOf(
            "Fra ca. 18 måneder giver barnet bamsen mad eller lader som om, det drikker af en tom kop.",
            "Omkring 2–3 år bliver legen længere: en kasse bliver en bil, og en pind en tryllestav.",
            "Billedbøger bliver mere end ting at gribe: barnet peger og siger ‘vov’ til billedet af hunden.",
        ),
        caveats = listOf(
            "Alderen afhænger af opgaven; nogle former for symbolforståelse, som billeder, kommer tidligere end modeller.",
        ),
    ),
    6 to PsychDetail(
        summary = "Konservation er forståelsen af, at en mængde, et antal eller en vægt er den samme, selvom udseendet ændres, fx når vand hældes fra et bredt glas over i et højt og smalt.",
        theory = listOf(
            "Konservation er Piagets klassiske kendetegn for overgangen fra det præoperationelle til det konkret-operationelle stadie, ofte omkring 6–7 år.",
            "Ifølge Piaget lykkes opgaven, når barnet kan decentrere (tænke på højde og bredde på én gang), forstå reversibilitet (at man kan hælde vandet tilbage) og fokusere på forvandlingen frem for kun start og slut.",
            "Senere forskning har vist, at måden man spørger på, har stor betydning, og at børn kan vise konservation tidligere i enklere og mere meningsfulde opgaver.",
        ),
        concepts = listOf(
            "Centrering: at fokusere på én tydelig egenskab, fx vandets højde.",
            "Reversibilitet: forståelsen af, at en ændring kan gøres om.",
            "Konkret-operationelt stadie: Piagets stadie fra ca. 7–11 år, hvor logiske regler kan bruges på konkrete ting.",
        ),
        studies = listOf(
            PsychStudy("De to glas", "Jean Piaget", listOf(
                "Barnet ser to ens glas med lige meget vand og bekræfter, at der er lige meget.",
                "Mens barnet ser på, hældes det ene glas over i et højt, smalt glas.",
                "Barnet spørges: Er der lige meget at drikke, eller er der mest i et af dem?",
            ), listOf(
                "De fleste børn under ca. 6–7 år siger, at der er mest i det høje glas.",
                "Ældre børn siger, at der er lige meget, og forklarer, at man bare har hældt det om.",
            )),
            PsychStudy("Den frække bamse", "McGarrigle og Donaldson (1974)", listOf(
                "To lige lange rækker af knapper lægges op, og barnet bekræfter, at der er lige mange.",
                "En ‘fræk bamse’ kommer og skubber ved et uheld den ene række, så den bliver længere.",
                "Barnet spørges igen, om der er lige mange.",
            ), listOf(
                "Langt flere 4–6-årige svarede rigtigt, når ændringen skete ved et uheld, end når forskeren selv ændrede rækken med vilje.",
                "Når den voksne selv ændrer noget og spørger igen, tror mange børn, at der forventes et nyt svar.",
            )),
        ),
        meaning = listOf(
            "Konservation afspejler både logisk tænkning og barnets forståelse af, hvad den voksne mener med sit spørgsmål.",
        ),
        atHome = listOf(
            "Et barn på 4 år kan blive ked af det, hvis søskendes saft står i et højere glas, selvom der er lige meget.",
            "Pizzaen skåret i flere stykker kan virke som mere.",
        ),
        caveats = listOf(
            "Børn behersker forskellige typer konservation på forskellige tidspunkter; antal kommer typisk før volumen og vægt.",
            "Skolegang og kultur påvirker, hvornår børn klarer opgaverne.",
        ),
    ),
    7 to PsychDetail(
        summary = "Klasseinklusion er forståelsen af, at kategorier kan ligge inden i hinanden: alle roser er blomster, men ikke alle blomster er roser. Det er grundlaget for at sortere, sammenligne og forstå ord som ‘dyr’, ‘hund’ og ‘puddel’.",
        theory = listOf(
            "Piaget og Bärbel Inhelder beskrev evnen som en del af det konkret-operationelle stadie: barnet skal kunne tænke på helheden og delen på samme tid.",
            "Mindre børn sorterer tidligt efter én egenskab, fx farve eller form, og bruger både over- og underbegreber i sproget. Det svære er at sammenligne en del med helheden, den er en del af.",
        ),
        concepts = listOf(
            "Overkategori og underkategori: fx blomster og roser.",
            "Hierarkisk tænkning: at forstå, at noget kan høre til flere niveauer på én gang.",
        ),
        studies = listOf(
            PsychStudy("Roserne og blomsterne", "Inhelder og Piaget (1964)", listOf(
                "Barnet ser fx syv roser og tre tulipaner.",
                "Barnet spørges: Er der flest roser eller flest blomster?",
            ), listOf(
                "Yngre børn svarer ofte ‘flest roser’ og sammenligner roser med tulipaner.",
                "Fra ca. 7–8 år svarer flere børn ‘flest blomster’.",
            )),
            PsychStudy("De sovende køer", "McGarrigle, Grieve og Hughes (1978)", listOf(
                "Børn ser legetøjskøer, hvoraf de fleste er sorte. Alle køerne lægges ned, så de ‘sover’.",
                "Barnet spørges: Er der flest sorte køer eller flest sovende køer?",
            ), listOf(
                "Flere børn svarede rigtigt, når helheden blev gjort tydelig på denne måde.",
                "Formuleringen af spørgsmålet har stor betydning for, hvordan børn forstår det.",
            )),
        ),
        meaning = listOf(
            "Børn kan tidligt sortere og bruge kategorier; at sammenligne en del med helheden kræver mere.",
        ),
        atHome = listOf(
            "Et 2-årigt barn kalder alle firbenede dyr for ‘vovse’ og lærer så, at nogle af dem er katte.",
            "Sortering af legetøj i kasser efter dyr, biler og klodser er gode øvelser.",
        ),
        caveats = listOf(
            "Spørgsmålene er sproglige, så resultaterne afhænger også af barnets sprogforståelse.",
        ),
    ),
    8 to PsychDetail(
        summary = "Transitiv inferens er at kunne udlede en ny sammenhæng fra to kendte: hvis Anna er højere end Bo, og Bo er højere end Carl, er Anna højere end Carl.",
        theory = listOf(
            "Piaget mente, at børn først kan lave denne type slutning i det konkret-operationelle stadie omkring 7 år.",
            "Peter Bryant og Tom Trabasso viste, at mange yngre børn fejlede, fordi de ikke kunne huske de enkelte sammenligninger, ikke fordi de ikke kunne lave selve slutningen.",
        ),
        concepts = listOf(
            "Inferens: at slutte noget nyt ud fra det, man ved.",
            "Hukommelsesbelastning: hvor meget en opgave kræver, at man holder i hovedet samtidig.",
        ),
        studies = listOf(
            PsychStudy("De farvede pinde", "Bryant og Trabasso (1971)", listOf(
                "Børn på 4–6 år lærte, hvilken af to farvede pinde der var længst, i par: A > B, B > C, C > D, D > E.",
                "Træningen fortsatte, til børnene kunne huske alle parrene.",
                "Derefter blev de spurgt om par, de aldrig havde sammenlignet, fx B og D.",
            ), listOf(
                "Selv 4-årige svarede overvejende rigtigt, når de havde lært parrene godt.",
            )),
        ),
        meaning = listOf(
            "Logiske slutninger er mulige tidligere end Piaget troede, når hukommelseskravene er lave.",
        ),
        atHome = listOf(
            "Barnet kan regne ud, hvem der er ældst i familien ud fra sammenligninger: ‘Hvis mormor er ældre end mor…’.",
        ),
        caveats = listOf(
            "Der er diskussion om, hvorvidt børnene laver ægte logiske slutninger eller lærer en rækkefølge.",
        ),
    ),
    9 to PsychDetail(
        summary = "Metakognition er tænkning om tænkning: at vide, hvad man ved og ikke ved, og at kunne vælge en god måde at lære og huske på.",
        theory = listOf(
            "John Flavell indførte begrebet i 1970’erne. Han skelnede mellem metakognitiv viden (fx at lange lister er svære at huske) og metakognitiv styring (at planlægge, holde øje med og justere sin egen tænkning).",
            "Førskolebørn har en begyndende forståelse af tænkning, men overvurderer ofte, hvad de kan huske. Gennem skolealderen bliver vurderingerne mere realistiske, og børnene begynder at bruge strategier som at gentage, gruppere og skrive ned.",
        ),
        concepts = listOf(
            "Metahukommelse: viden om egen hukommelse.",
            "Produktionsmangel: at kunne bruge en strategi, men ikke selv finde på at gøre det.",
            "Selvmonitorering: at holde øje med, om man har forstået noget.",
        ),
        studies = listOf(
            PsychStudy("Hvor mange kan du huske?", "Flavell, Friedrichs og Hoyt (1970)", listOf(
                "Børn fra børnehave til 4. klasse skulle først gætte, hvor mange billeder de kunne huske.",
                "Derefter blev deres faktiske hukommelse målt.",
            ), listOf(
                "De yngste børn troede, at de kunne huske næsten det hele, men huskede langt mindre.",
                "De ældre børn gættede langt mere præcist.",
            )),
            PsychStudy("Gentagelse som strategi", "Keeney, Cannizzo og Flavell (1967)", listOf(
                "Børn skulle huske en række billeder i kort tid.",
                "Man observerede, om de mumlede navnene for sig selv.",
                "Børn, der ikke gentog, blev lært at gøre det.",
            ), listOf(
                "Børn, der gentog, huskede bedre.",
                "De, der ikke selv gentog, kunne godt bruge strategien, når de blev bedt om det, men stoppede bagefter igen.",
            )),
        ),
        meaning = listOf(
            "Børn skal både lære strategier og lære at bruge dem af sig selv.",
            "Voksne kan støtte ved at tale højt om, hvordan man husker og lærer.",
        ),
        atHome = listOf(
            "En 4-årig siger ‘jeg har altid vidst det’ om noget, den lige har lært.",
            "Et skolebarn begynder at sige ‘det skal jeg øve mig på’ eller ‘det kan jeg ikke huske, jeg skriver det ned’.",
        ),
        caveats = listOf(
            "Metakognition udvikles gennem hele barndommen og ungdommen og afhænger meget af skole og opdragelse.",
        ),
    ),
)
