package dk.babyapp.domain

// Social kognition og Theory of Mind (10–20).
internal val socialCognitionDetails: Map<Int, PsychDetail> = mapOf(
    10 to PsychDetail(
        summary = "Fælles opmærksomhed er, når barnet og en anden person kigger på det samme og begge ved det. Det er trekanten mellem barn, voksen og ting, og det er et af de vigtigste fundamenter for sprog og social læring.",
        theory = listOf(
            "Jerome Bruner så fælles opmærksomhed som stilladset for sprog: når barn og voksen ser på det samme, kan ord kobles til ting. Michael Tomasello beskriver det omkring 9 måneder som en ‘9-måneders-revolution’, hvor barnet begynder at forstå andre som personer med opmærksomhed og mål.",
            "Udviklingen sker i trin: først følger barnet den voksnes blik, så tjekker det, om den voksne kigger med, og til sidst peger barnet selv for at dele noget.",
        ),
        concepts = listOf(
            "Blikfølgning: at kigge derhen, hvor en anden kigger.",
            "Svarende fælles opmærksomhed: at følge en andens blik eller pegen.",
            "Initierende fælles opmærksomhed: selv at pege eller vise for at dele.",
            "Deklarativ pegen: at pege for at dele noget (‘se!’), ikke for at få noget.",
        ),
        studies = listOf(
            PsychStudy("Følg mit blik", "Scaife og Bruner (1975)", listOf(
                "En voksen sidder over for barnet og får øjenkontakt.",
                "Den voksne drejer hovedet og kigger mod den ene side.",
                "Man ser, om barnet vender sig og kigger samme vej.",
            ), listOf(
                "Nogle børn fulgte blikket allerede omkring 2–4 måneder, og flere gjorde det med alderen.",
                "Omkring 12 måneder fulgte de fleste blikket pålideligt.",
            )),
            PsychStudy("Åbne eller lukkede øjne", "Brooks og Meltzoff (2005)", listOf(
                "En voksen drejer hovedet mod en ting, enten med åbne eller lukkede øjne.",
                "Man ser, om børn på 9–11 måneder følger hovedet.",
            ), listOf(
                "Fra ca. 10 måneder fulgte børnene oftere hovedet, når øjnene var åbne.",
                "Børn, der tidligt fulgte blikket, havde senere et større ordforråd.",
            )),
            PsychStudy("Udviklingen fra 9 til 15 måneder", "Carpenter, Nagell og Tomasello (1998)", listOf(
                "24 børn blev fulgt hver måned fra 9 til 15 måneder i leg med deres mor.",
                "Forskerne registrerede blikfølgning, pegen og fælles leg.",
            ), listOf(
                "Færdighederne kom i en fast rækkefølge: først dele opmærksomhed, så følge den voksnes opmærksomhed, så selv styre den.",
                "Børn, der var tidligt ude med fælles opmærksomhed, lærte flere ord.",
            )),
        ),
        meaning = listOf(
            "Børn lærer ord lettest, når den voksne navngiver det, barnet allerede kigger på.",
            "Fælles opmærksomhed er et vigtigt tegn i sundhedsplejens og lægens vurdering af sproglig og social udvikling.",
        ),
        atHome = listOf(
            "Omkring 9–12 måneder peger barnet på en hund og kigger tilbage på dig for at se, om du også så den.",
            "Barnet rækker dig ting for at vise dem, ikke for at du skal tage dem.",
            "Når du siger ‘se!’ og peger, kigger barnet efter din finger.",
        ),
        caveats = listOf(
            "Alderen varierer meget. Hvis barnet sjældent peger eller følger blik efter 12–18 måneder, er det værd at nævne for sundhedsplejersken; det er ikke i sig selv en diagnose.",
        ),
    ),
    11 to PsychDetail(
        summary = "Forståelse af intentioner er at kunne se, hvad en anden prøver at opnå, og ikke kun hvad personen gør. Barnet begynder at se handlinger som rettet mod et mål.",
        theory = listOf(
            "Gennem det første år begynder børn at opfatte andres bevægelser som målrettede. Amanda Woodward har vist, at selv 6 måneder gamle børn reagerer på, hvilken ting en hånd rækker efter, ikke kun på bevægelsen.",
            "Omkring 9–18 måneder forstår børn også mislykkede forsøg og forskellen på ikke at ville og ikke at kunne. Det er et skridt mod at forstå andres indre verden.",
        ),
        concepts = listOf(
            "Målrettet handling: en handling, der forstås ud fra det, den skal opnå.",
            "Mislykket forsøg: når nogen prøver, men ikke lykkes; barnet må regne målet ud.",
        ),
        studies = listOf(
            PsychStudy("Det mislykkede forsøg", "Andrew Meltzoff (1995)", listOf(
                "18 måneder gamle børn ser en voksen prøve at trække en lille ‘håndvægt’ fra hinanden, men hænderne glider af.",
                "Den voksne lykkes aldrig.",
                "Barnet får derefter tingen.",
            ), listOf(
                "Børnene trak oftest tingen fra hinanden, som om de havde set det lykkes.",
                "Når en mekanisk tang lavede samme bevægelse, gjorde børnene det sjældent; de læste altså målet ind i en persons handling.",
            )),
            PsychStudy("Hvad rækker hånden efter?", "Amanda Woodward (1998)", listOf(
                "6 måneder gamle børn ser en hånd række efter en bold i stedet for en bamse, mange gange.",
                "Tingene byttes om, så bamsen står, hvor bolden stod.",
                "Hånden rækker enten efter samme ting på nyt sted eller ny ting på samme sted.",
            ), listOf(
                "Børnene kiggede længst, når hånden rakte efter den nye ting, selvom bevægelsen var den samme.",
                "Det tyder på, at de havde opfattet målet med handlingen.",
            )),
            PsychStudy("Vil ikke eller kan ikke?", "Behne, Carpenter, Call og Tomasello (2005)", listOf(
                "En voksen skal give barnet legetøj, men holder det enten tilbage med vilje eller taber det ved et uheld.",
            ), listOf(
                "Børn på 9 måneder og ældre blev mere utålmodige, når den voksne ikke ville, end når den voksne ikke kunne.",
            )),
        ),
        meaning = listOf(
            "Barnet begynder at forstå andres handlinger ud fra hensigt, hvilket er grundlaget for samarbejde, hjælp og sprog.",
        ),
        atHome = listOf(
            "Barnet hjælper med at åbne en dør, du har svært ved at åbne.",
            "Barnet bliver irriteret, når du driller med legetøjet, men ikke når du taber det.",
        ),
        caveats = listOf(
            "Kiggetidsstudier viser opmærksomhed, ikke nødvendigvis bevidst forståelse.",
        ),
    ),
    12 to PsychDetail(
        summary = "Social referencing er, når barnet i en usikker situation kigger på en voksen og bruger den voksnes reaktion til at vurdere, hvad det selv skal gøre.",
        theory = listOf(
            "Fra omkring 8–10 måneder begynder spædbørn at søge information i andres ansigter, stemmer og kropssprog, når noget er nyt eller uklart.",
            "Det bygger på fælles opmærksomhed og en begyndende forståelse af, at andres følelser handler om noget bestemt i omgivelserne.",
        ),
        concepts = listOf(
            "Emotionelt signal: et ansigtsudtryk eller tonefald, der fortæller noget om en situation.",
            "Det visuelle klippe: en glasplade over et tilsyneladende fald, brugt til at undersøge dybdeopfattelse og social referencing.",
        ),
        studies = listOf(
            PsychStudy("Det visuelle klippe", "Sorce, Emde, Campos og Klinnert (1985)", listOf(
                "1-årige kravler på en glasplade; under glasset ser det ud, som om der er et fald på ca. 30 cm.",
                "Moren står på den anden side med et spændende stykke legetøj.",
                "Moren viser enten et glad eller et bange ansigt.",
            ), listOf(
                "Når moren så glad ud, kravlede de fleste børn over.",
                "Når moren så bange ud, kravlede ingen af børnene over.",
            )),
        ),
        meaning = listOf(
            "Barnet lærer om verden gennem dine reaktioner. Din ro kan gøre det nye mindre farligt.",
        ),
        atHome = listOf(
            "Når en fremmed hilser, kigger barnet på dig, før det smiler eller græder.",
            "Når barnet falder, kigger det på dig: reagerer du roligt, græder det ofte mindre.",
        ),
        caveats = listOf(
            "Effekten afhænger af, hvor usikker situationen er; når noget tydeligt er sjovt eller farligt, kigger børn mindre på voksne.",
        ),
    ),
    13 to PsychDetail(
        summary = "Forståelsen af forskellige ønsker er at vide, at andre kan kunne lide noget andet end en selv. Det er et af de første trin i Theory of Mind.",
        theory = listOf(
            "Henry Wellman og David Liu har vist, at børn lærer om andres sind i en fast rækkefølge: først at ønsker kan være forskellige, så at meninger kan være forskellige, så at det man har set, påvirker hvad man ved, og så falske overbevisninger.",
            "Forståelsen af ønsker kommer før forståelsen af overbevisninger, fordi ønsker er tættere på handlinger og følelser, som barnet kan observere.",
        ),
        concepts = listOf(
            "Theory of Mind: forståelsen af, at andre har tanker, ønsker og følelser, som kan være forskellige fra ens egne.",
            "Egocentrisme: at tage udgangspunkt i sit eget perspektiv.",
        ),
        studies = listOf(
            PsychStudy("Broccoli eller kiks", "Repacholi og Gopnik (1997)", listOf(
                "Børn på 14 og 18 måneder smager på broccoli og kiks; næsten alle foretrækker kiks.",
                "En voksen smager begge og viser tydeligt, at hun elsker broccoli og ikke kan lide kiks.",
                "Den voksne rækker hånden frem og beder barnet om noget.",
            ), listOf(
                "De 18 måneder gamle gav oftest broccoli, altså det den voksne kunne lide.",
                "De 14 måneder gamle gav oftest kiks, altså det de selv kunne lide.",
            )),
        ),
        meaning = listOf(
            "Omkring 1½ år begynder barnet at forstå, at andre har deres egen smag og deres egne ønsker.",
        ),
        atHome = listOf(
            "Barnet tilbyder dig din kaffe i stedet for sin egen mad.",
            "Barnet siger ‘far kan lide…’ eller ‘mor vil ikke have…’.",
        ),
        caveats = listOf(
            "Rækkefølgen er fundet i mange lande, men tempoet varierer med kultur og sprog.",
        ),
    ),
    14 to PsychDetail(
        summary = "Forståelsen af forskellige overbevisninger er at vide, at andre kan tro noget andet om verden end en selv, fx at katten er i haven, mens man selv tror, den er i garagen.",
        theory = listOf(
            "Det er andet trin i Wellman og Lius Theory of Mind-skala. Barnet forstår, at to personer kan mene forskelligt, selv om det endnu ikke ved, hvem der har ret.",
            "Tæt forbundet er forståelsen af, at det at se fører til at vide: den der har kigget i kassen, ved hvad der er i den.",
        ),
        concepts = listOf(
            "Overbevisning: det, en person tror er sandt.",
            "Videnskilde: hvordan man ved noget, fx ved at have set eller fået det fortalt.",
        ),
        studies = listOf(
            PsychStudy("Theory of Mind-skalaen", "Wellman og Liu (2004)", listOf(
                "Børn på 3–5 år får en række korte opgaver med dukker og billeder.",
                "I opgaven om forskellige overbevisninger hører barnet, at en dukke leder efter sin kat og tror, den er i haven, mens barnet selv tror, den er i garagen.",
                "Barnet spørges: Hvor vil dukken lede?",
            ), listOf(
                "De fleste 3–4-årige svarede ‘i haven’, altså ud fra dukkens tro.",
                "Opgaven var lettere end opgaverne om falsk overbevisning, som de samme børn ofte fejlede.",
            )),
            PsychStudy("At se er at vide", "Pratt og Bryant (1990)", listOf(
                "Børn ser én dukke kigge ned i en kasse og en anden løfte den uden at kigge.",
                "Barnet spørges: Hvem ved, hvad der er i kassen?",
            ), listOf(
                "Mange 3-årige forstod, at den der kiggede, også vidste det.",
            )),
        ),
        meaning = listOf(
            "Barnet begynder at forstå, at uenighed ikke betyder, at den anden er forkert, men at man ser verden forskelligt.",
        ),
        atHome = listOf(
            "Barnet siger ‘du tror…’ eller ‘jeg tror…’.",
            "Barnet forklarer dig noget, du ikke har set: ‘du var der ikke, så du ved det ikke’.",
        ),
        caveats = listOf(
            "Opgaverne er sproglige; børn med mindre sprog kan forstå mere, end de kan vise.",
        ),
    ),
    15 to PsychDetail(
        summary = "At forstå falske overbevisninger er at forstå, at en anden kan tro noget, som ikke er sandt, og vil handle ud fra det. Det regnes som et vigtigt skridt i Theory of Mind og ses typisk omkring 4–5 år.",
        theory = listOf(
            "Ideen kommer fra filosofien: for at vise, at man forstår, at andre har tanker, må man kunne forudsige, hvad de gør, når deres tanke er forkert.",
            "En stor metaanalyse af 178 studier (Wellman, Cross og Watson, 2001) viste, at de fleste 3-årige fejler opgaven, mens de fleste 5-årige klarer den, og at udviklingen følger samme mønster i mange lande.",
            "Sprog, søskende og samtaler om følelser og tanker hænger sammen med, at børn klarer opgaven tidligere.",
        ),
        concepts = listOf(
            "Falsk overbevisning: en tro, der ikke passer med virkeligheden.",
            "Uventet placering og uventet indhold: de to klassiske typer opgaver.",
        ),
        studies = listOf(
            PsychStudy("Sally og Anne", "Baron-Cohen, Leslie og Frith (1985)", listOf(
                "To dukker: Sally lægger en kugle i sin kurv og går ud.",
                "Mens Sally er væk, flytter Anne kuglen over i sin kasse.",
                "Sally kommer tilbage. Barnet spørges: Hvor vil Sally lede efter kuglen?",
            ), listOf(
                "De fleste 4-årige og børn med Downs syndrom svarede ‘i kurven’.",
                "De fleste autistiske børn i studiet svarede ‘i kassen’.",
            )),
            PsychStudy("Smarties-æsken", "Perner, Leekam og Wimmer (1987)", listOf(
                "Barnet ser en kendt slikæske og gætter, at der er slik i.",
                "Æsken åbnes, og der er blyanter i.",
                "Barnet spørges: Hvad vil en ven tro er i æsken? Og hvad troede du selv først?",
            ), listOf(
                "Mange 3-årige siger ‘blyanter’ til begge spørgsmål, også om hvad de selv troede.",
                "Det tyder på, at de også har svært ved at huske deres egen tidligere forkerte tro.",
            )),
        ),
        meaning = listOf(
            "Omkring 4–5 år forstår barnet, at tanker er forestillinger om verden, som kan være forkerte.",
            "Det ændrer leg, løgne, overraskelser og forståelsen af andres følelser.",
        ),
        atHome = listOf(
            "Barnet begynder at lave overraskelser og gemmelege, hvor du skal tro noget forkert.",
            "Barnet kan begynde at lyve, hvilket er et tegn på, at det forstår, at du kan tro noget, der ikke er sandt.",
        ),
        caveats = listOf(
            "Opgaverne kræver sprog, hukommelse og hæmningskontrol, så de kan undervurdere, hvad yngre børn forstår.",
            "Om autisme: resultaterne gælder grupper, ikke enkeltpersoner, og mange autistiske børn klarer opgaverne senere.",
        ),
    ),
    16 to PsychDetail(
        summary = "Implicit Theory of Mind er spørgsmålet om, hvorvidt meget små børn forventer, at andre handler ud fra deres tro, før de kan sige det med ord. Det er et af de mest omdiskuterede emner i feltet.",
        theory = listOf(
            "I 2005 viste Kristine Onishi og Renée Baillargeon, at 15 måneder gamle børn kiggede længere, når en person ledte det rigtige sted efter en ting, personen ikke kunne vide var flyttet. Det blev tolket som en tidlig, ikke-sproglig forståelse af falske overbevisninger.",
            "Nogle forskere mener, at børn har et tidligt, ubevidst system for at følge andres perspektiv, og at det senere, sproglige system bygger ovenpå. Andre mener, at resultaterne kan forklares med enklere mekanismer.",
        ),
        concepts = listOf(
            "Implicit: vist gennem blik eller handling, ikke gennem svar.",
            "Forventningsblik: at kigge derhen, hvor man forventer, en person vil gå.",
            "Replikation: at gentage et studie for at se, om resultatet holder.",
        ),
        studies = listOf(
            PsychStudy("Den grønne og den gule kasse", "Onishi og Baillargeon (2005)", listOf(
                "15 måneder gamle børn ser en skuespiller gemme et stykke legetøj i en grøn kasse.",
                "Mens skuespilleren ikke kan se det, flytter legetøjet sig over i en gul kasse.",
                "Skuespilleren rækker ind i enten den grønne eller den gule kasse.",
            ), listOf(
                "Børnene kiggede længere, når skuespilleren rakte ind i den gule kasse, hvor legetøjet faktisk var.",
                "Det blev tolket som, at de havde forventet, at hun ville lede, hvor hun troede, det var.",
            )),
            PsychStudy("Store replikationsforsøg", "Bl.a. Kulke m.fl. (2018) og ManyBabies 2", listOf(
                "Flere laboratorier gentog de tidlige forsøg med større grupper og samme metoder.",
            ), listOf(
                "Resultaterne har været blandede, og mange forsøg har ikke kunnet gentage de oprindelige fund.",
            )),
        ),
        meaning = listOf(
            "Det er stadig uafklaret, hvor tidligt børn forstår andres tro. Den sikre viden er, at forståelsen bliver tydelig og stabil omkring 4–5 år.",
        ),
        atHome = listOf(
            "Allerede i 1–2-års alderen kan børn hjælpe en voksen, der leder efter noget, ved at pege på det rigtige sted.",
        ),
        caveats = listOf(
            "Fundene er omdiskuterede og har haft svært ved at blive gentaget; de bør tolkes med forsigtighed.",
        ),
    ),
    17 to PsychDetail(
        summary = "Andenordens Theory of Mind er at forstå tanker om tanker: ‘Peter tror, at Marie tror…’. Det udvikles typisk omkring 6–9 år og er vigtigt for at forstå ironi, hemmeligheder og komplekse sociale situationer.",
        theory = listOf(
            "Første orden er at forstå, hvad en person tror om verden. Anden orden er at forstå, hvad en person tror om en andens tro.",
            "Det kræver at holde flere perspektiver i hovedet på én gang og er derfor tæt forbundet med arbejdshukommelse og sprog.",
        ),
        concepts = listOf(
            "Rekursiv tænkning: tanker indlejret i tanker.",
            "Første og anden orden: hvor mange lag af ‘tror at’ der er.",
        ),
        studies = listOf(
            PsychStudy("Isbilen", "Perner og Wimmer (1985)", listOf(
                "Barnet hører en historie: John og Mary ser isbilen i parken. Mary går hjem for at hente penge.",
                "Isbilen kører til kirken. John ser det, og Mary hører det fra ismanden på vejen, uden at John ved det.",
                "Barnet spørges: Hvor tror John, at Mary vil gå hen for at købe is?",
            ), listOf(
                "De fleste børn under 6 år svarede ‘kirken’.",
                "Fra ca. 7–9 år svarede de fleste ‘parken’, fordi John ikke ved, at Mary ved det.",
            )),
        ),
        meaning = listOf(
            "Barnet kan nu forstå mere komplekse sociale situationer, fx at nogen prøver at få andre til at tro noget.",
        ),
        atHome = listOf(
            "Barnet begynder at forstå drillerier, hemmeligheder og ‘han tror bare, at hun synes…’.",
        ),
        caveats = listOf(
            "Med enklere historier kan nogle 5–6-årige klare opgaven; alderen afhænger meget af opgaven.",
        ),
    ),
    18 to PsychDetail(
        summary = "Forståelsen af skjulte følelser er at vide, at man kan føle én ting og vise noget andet, fx smile over en gave, man ikke kan lide.",
        theory = listOf(
            "Børn lærer gradvist kulturens visningsregler: hvornår det er passende at vise og skjule følelser. Det kræver både at forstå andres perspektiv og at kunne styre sit eget udtryk.",
            "Omkring 4 år har børn svært ved at skelne mellem det, man føler, og det, man viser. Omkring 6 år forstår de fleste forskellen.",
        ),
        concepts = listOf(
            "Visningsregler: kulturelle normer for, hvilke følelser man viser hvornår.",
            "Tilsyneladende og virkelig følelse: det man viser, og det man føler.",
        ),
        studies = listOf(
            PsychStudy("Virkelig og tilsyneladende følelse", "Harris, Donnelly, Guz og Pitt-Watson (1986)", listOf(
                "Børn på 4 og 6 år hører historier, hvor en person har en grund til at skjule en følelse.",
                "De spørges: Hvordan har personen det indeni? Og hvordan ser personen ud udenpå?",
            ), listOf(
                "6-årige skelnede tydeligt mellem det indre og det ydre.",
                "4-årige gav ofte samme svar på begge spørgsmål.",
            )),
            PsychStudy("Den skuffende gave", "Carolyn Saarni (1984)", listOf(
                "Børn løser en opgave og får en flot gave. Senere løser de en ny og får en skuffende gave, fx et babylegetøj.",
                "Man observerer deres ansigtsudtryk, når de åbner den.",
            ), listOf(
                "Med alderen skjulte børnene i stigende grad deres skuffelse; pigerne gjorde det lidt oftere end drengene.",
            )),
        ),
        meaning = listOf(
            "Barnet lærer, at følelser og udtryk ikke altid er det samme, og at man nogle gange skjuler følelser for at tage hensyn.",
        ),
        atHome = listOf(
            "Barnet siger tak for en gave med et lidt stift smil.",
            "Barnet siger ‘jeg er ikke ked af det’ med bævrende læber og opdager, at du alligevel kan se det.",
        ),
        caveats = listOf(
            "Visningsregler er forskellige fra kultur til kultur og familie til familie.",
        ),
    ),
    19 to PsychDetail(
        summary = "Faux pas-forståelse er at kunne se, når nogen siger noget sårende uden at mene det, fordi de ikke vidste bedre. Det kræver både at forstå, hvad den anden vidste, og hvordan det føles for den, der hører det.",
        theory = listOf(
            "En faux pas indeholder to perspektiver på én gang: den talende vidste ikke, at det var sårende, og den lyttende blev såret. Børn skal forstå både viden og følelser og koble dem.",
            "Simon Baron-Cohen og kolleger udviklede en test, der viser en gradvis udvikling fra ca. 7 til 11 år.",
        ),
        concepts = listOf(
            "Faux pas: en social bommert, sagt uden ond hensigt.",
            "Utilsigtet krænkelse: at såre nogen uden at vide det.",
        ),
        studies = listOf(
            PsychStudy("Faux pas-testen", "Baron-Cohen, O’Riordan, Stone, Jones og Plaisted (1999)", listOf(
                "Børn hører korte historier, fx: Ida har fået nye gardiner. Hendes veninde siger: ‘De gardiner er grimme, jeg håber, du får nogle nye’.",
                "De spørges: Sagde nogen noget, de ikke burde have sagt? Hvorfor? Vidste hun, at det var Idas nye gardiner?",
            ), listOf(
                "Evnen til at opdage og forklare faux pas udviklede sig tydeligt fra 7 til 11 år.",
                "Piger klarede opgaven lidt tidligere end drenge.",
            )),
        ),
        meaning = listOf(
            "Barnet kan nu forstå sociale situationer, hvor gode hensigter og dårlige følger går hånd i hånd.",
        ),
        atHome = listOf(
            "Et skolebarn kan forklare: ‘Han vidste jo ikke, at hun var ked af det’.",
        ),
        caveats = listOf(
            "Testen kræver meget sprog og opmærksomhed og måler ikke kun social forståelse.",
        ),
    ),
    20 to PsychDetail(
        summary = "Forståelsen af ironi og ikke-bogstaveligt sprog er at forstå, at man kan sige én ting og mene noget andet, fx ‘dejligt vejr’ i silende regn.",
        theory = listOf(
            "Ironi kræver, at barnet forstår talerens hensigt og ofte andenordens Theory of Mind: hvad taleren vil have, at lytteren skal forstå.",
            "Forståelsen kommer gradvist fra ca. 5–6 år og forfines langt ind i ungdommen. Tonefald og ansigtsudtryk hjælper yngre børn.",
        ),
        concepts = listOf(
            "Ironi og sarkasme: at mene det modsatte af det sagte, ofte for at være sjov eller kritisk.",
            "Metafor: et billedligt udtryk, fx ‘han er en sten’.",
        ),
        studies = listOf(
            PsychStudy("Løgn eller ironi?", "Winner og Leekam (1991)", listOf(
                "Børn på 5–7 år hører historier, hvor en person siger noget forkert, enten for at snyde (løgn) eller for at være ironisk.",
                "De spørges, om personen ville have, at den anden skulle tro på det.",
            ), listOf(
                "Børn, der forstod andenordens tanker, kunne bedre skelne ironi fra løgn.",
            )),
        ),
        meaning = listOf(
            "Ironi og humor bliver gradvist en del af barnets sprog i skolealderen.",
        ),
        atHome = listOf(
            "Et 5-årigt barn kan tage ‘sikke en flot oprydning’ helt bogstaveligt.",
            "Et 8–10-årigt barn begynder selv at bruge ironi og forstår ordspil og drillerier.",
        ),
        caveats = listOf(
            "Forståelsen afhænger af tonefald og situation; uden dem kan selv voksne misforstå ironi.",
        ),
    ),
)
