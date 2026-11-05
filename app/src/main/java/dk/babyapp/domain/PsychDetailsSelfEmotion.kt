package dk.babyapp.domain

// Selvudvikling og emotionel og relationel udvikling (21–28).
internal val selfAndEmotionDetails: Map<Int, PsychDetail> = mapOf(
    21 to PsychDetail(
        summary = "Selvgenkendelse i spejlet er, når barnet forstår, at det er sig selv, det ser i spejlet. Det ses typisk mellem 18 og 24 måneder og regnes som et tegn på en begyndende bevidsthed om sig selv.",
        theory = listOf(
            "Spædbørn er længe interesserede i spejlbilleder, men behandler dem som en anden baby. Gordon Gallup udviklede i 1970 en test med chimpanser, hvor et mærke på ansigtet viser, om dyret forstår, at spejlbilledet er det selv.",
            "Michael Lewis og Jeanne Brooks-Gunn koblede selvgenkendelsen til fremkomsten af selvbevidste følelser som forlegenhed: for at blive forlegen må man kunne se sig selv udefra.",
        ),
        concepts = listOf(
            "Rouge-testen (mærketesten): et mærke sættes på barnets ansigt uden at barnet opdager det.",
            "Objektiv selvbevidsthed: at kunne se sig selv som en, andre kan se på.",
        ),
        studies = listOf(
            PsychStudy("Rouge-testen", "Beulah Amsterdam (1972); Lewis og Brooks-Gunn (1979)", listOf(
                "Mor tørrer diskret lidt rød farve på barnets næse.",
                "Barnet sættes foran et spejl.",
                "Man ser, om barnet rører sin egen næse (forstår at det er sig selv) eller spejlet.",
            ), listOf(
                "Få børn under 15 måneder rørte deres egen næse.",
                "Mellem 18 og 24 måneder gjorde de fleste det.",
            )),
            PsychStudy("På tværs af kulturer", "Broesch, Callaghan, Henrich, Murphy og Rochat (2011)", listOf(
                "Rouge-testen blev gennemført med børn i flere lande, bl.a. Kenya, Fiji, Peru og Canada.",
            ), listOf(
                "I nogle lande rørte meget få børn mærket, selvom de var lige så gamle.",
                "Det kan skyldes kulturelle forskelle i, hvordan man reagerer på noget mærkeligt, ikke manglende selvbevidsthed.",
            )),
        ),
        meaning = listOf(
            "Omkring 1½–2 år begynder barnet at se sig selv som en person, der kan ses af andre.",
            "Det baner vejen for ord som ‘mig’ og ‘min’ og for følelser som stolthed og flovhed.",
        ),
        atHome = listOf(
            "Barnet peger på sig selv i spejlet og siger sit navn eller ‘mig’.",
            "Barnet bliver interesseret i billeder af sig selv.",
            "Leg: sæt et klistermærke i panden og se, om barnet tager det af, når det ser sig i spejlet.",
        ),
        caveats = listOf(
            "Testen viser én slags selvforståelse; den siger ikke alt om barnets selvbevidsthed, og resultaterne varierer med kultur.",
        ),
    ),
    22 to PsychDetail(
        summary = "Selvbevidste følelser er følelser som forlegenhed, stolthed, skam og skyld. De kræver, at barnet kan se sig selv udefra og vurdere sig selv ud fra regler og forventninger.",
        theory = listOf(
            "Basale følelser som glæde, vrede og frygt ses fra det første leveår. Selvbevidste følelser kommer senere, fordi de bygger på selvgenkendelse og på at kende til standarder for godt og skidt.",
            "Michael Lewis beskriver to trin: forlegenhed ved at blive set (fra ca. 18–24 måneder) og senere stolthed, skam og skyld, når barnet kan vurdere sig selv ud fra regler (fra ca. 2½–3 år).",
            "Psykologien skelner mellem skam (‘jeg er dårlig’) og skyld (‘jeg gjorde noget dårligt’). Skyld hænger mere sammen med at ville reparere.",
        ),
        concepts = listOf(
            "Forlegenhed: ubehaget ved at være i centrum.",
            "Skam og skyld: følelser om sig selv eller om en handling.",
            "Stolthed: glæden ved selv at have klaret noget svært.",
        ),
        studies = listOf(
            PsychStudy("Svære og lette opgaver", "Lewis, Alessandri og Sullivan (1992)", listOf(
                "3-årige får opgaver, der enten er lette eller svære, fx puslespil.",
                "Nogle gange lykkes det, andre gange ikke.",
                "Forskerne registrerer ansigtsudtryk og kropsholdning.",
            ), listOf(
                "Børnene viste mest stolthed, når de klarede en svær opgave.",
                "De viste mest skam, når de ikke klarede en let opgave.",
            )),
            PsychStudy("Dukken, der går i stykker", "Grazyna Kochanska m.fl. (2002)", listOf(
                "Små børn får lov at lege med en voksens yndlingslegetøj, som er lavet til at gå i stykker.",
                "Den voksne bliver ked af det.",
            ), listOf(
                "Allerede omkring 2 år viste mange børn tegn på skyld, fx at kigge væk eller prøve at reparere.",
                "Børn, der viste mere skyld, overholdt senere i højere grad regler.",
            )),
        ),
        meaning = listOf(
            "Disse følelser er ikke et problem, men et tegn på, at barnet begynder at kende regler og forventninger.",
            "Hvordan voksne reagerer på fejl, kan påvirke, om barnet mest føler skam eller skyld.",
        ),
        atHome = listOf(
            "Barnet gemmer ansigtet, når alle synger fødselsdagssang.",
            "‘Se mig!’ og stolte blikke, når barnet har klaret noget selv.",
            "Barnet gemmer sig eller kigger væk, når noget er gået i stykker.",
        ),
        caveats = listOf(
            "Det er svært at måle følelser præcist ud fra adfærd; kultur påvirker, hvordan skam og stolthed vises.",
        ),
    ),
    23 to PsychDetail(
        summary = "Den selvbiografiske hukommelse er evnen til at huske egne oplevelser som en del af ens livshistorie. Den udvikles især fra 2 til 5 år.",
        theory = listOf(
            "De fleste voksne husker næsten intet fra før 3–4-års alderen. Det kaldes barndomsamnesi. Små børn kan godt huske, men erindringerne gemmes ikke på en måde, der holder hele livet.",
            "Katherine Nelson og Robyn Fivush beskriver, hvordan selvbiografisk hukommelse udvikles gennem sprog og samtaler: når voksne og børn snakker om det, de har oplevet, lærer barnet at organisere erindringer som historier.",
        ),
        concepts = listOf(
            "Barndomsamnesi: at man som voksen ikke husker de første leveår.",
            "Uddybende erindringssamtale: at snakke om oplevelser med mange spørgsmål og detaljer.",
        ),
        studies = listOf(
            PsychStudy("Mødres måde at mindes på", "Reese, Haden og Fivush (1993)", listOf(
                "Mødre og børn blev optaget, mens de snakkede om fælles oplevelser, fra barnet var ca. 3 år.",
                "Samtalerne blev kodet for, hvor uddybende moren var: mange åbne spørgsmål og detaljer, eller få og lukkede.",
            ), listOf(
                "Børn af mødre med en uddybende stil fortalte senere selv mere detaljerede erindringer.",
            )),
            PsychStudy("Første erindringer på tværs af kulturer", "Qi Wang (2001)", listOf(
                "Voksne fra USA og Kina blev bedt om at beskrive deres tidligste erindring.",
            ), listOf(
                "Amerikanernes første erindringer lå i gennemsnit omkring et halvt år tidligere end kinesernes og handlede mere om dem selv.",
                "Kultur og samtalestil påvirker, hvordan erindringer formes.",
            )),
        ),
        meaning = listOf(
            "Samtaler om fælles oplevelser hjælper barnet med at huske og forstå sig selv.",
        ),
        atHome = listOf(
            "Spørg barnet om dagen: ‘Hvad skete der? Hvem var med? Hvordan havde du det?’",
            "Kig i fotoalbum eller Barnets bog sammen og fortæl historierne igen.",
        ),
        caveats = listOf(
            "Erindringer kan forandres af det, man har hørt og set på billeder; tidlige erindringer er ikke altid præcise.",
        ),
    ),
    24 to PsychDetail(
        summary = "Identitetsudvikling handler om at finde ud af, hvem man er, hvad man står for, og hvor man hører til. Den foregår gennem hele barndommen, men især i ungdomsårene.",
        theory = listOf(
            "Erik Erikson beskrev ungdommen som tiden for krisen mellem identitet og rolleforvirring: den unge afprøver roller, værdier og fællesskaber for at finde sig selv.",
            "James Marcia operationaliserede Eriksons teori i fire identitetsstatusser, bestemt af om den unge har udforsket muligheder, og om den unge har forpligtet sig.",
        ),
        concepts = listOf(
            "Diffusion: hverken udforsket eller forpligtet.",
            "Overtaget identitet (foreclosure): forpligtet uden at have udforsket, fx forældrenes værdier.",
            "Moratorium: i gang med at udforske, endnu ikke forpligtet.",
            "Opnået identitet: har udforsket og forpligtet sig.",
        ),
        studies = listOf(
            PsychStudy("Identitetsstatusinterviewet", "James Marcia (1966)", listOf(
                "Unge voksne blev interviewet om job, religion og politik.",
                "Svarene blev vurderet ud fra udforskning og forpligtelse.",
            ), listOf(
                "De unge kunne placeres i fire statusser, og status hang sammen med fx selvværd og måden at håndtere pres på.",
                "Senere forskning viser, at mange bevæger sig frem og tilbage mellem statusserne.",
            )),
        ),
        meaning = listOf(
            "At afprøve forskellige stilarter, venner og holdninger er en normal del af at finde sig selv.",
        ),
        atHome = listOf(
            "Allerede små børn afprøver roller i leg: brandmand, mor, hund.",
            "I skolealderen bliver venner, interesser og tøj vigtige markører for, hvem man er.",
        ),
        caveats = listOf(
            "Identitet er ikke noget, man bliver færdig med; den udvikles gennem hele livet og afhænger af kultur og historiske forhold.",
        ),
    ),
    25 to PsychDetail(
        summary = "Tilknytning er det følelsesmæssige bånd mellem barnet og de nærmeste omsorgspersoner. Barnet søger tryghed hos dem, når det er bange, træt eller ked af det, og bruger dem som en sikker base til at udforske verden.",
        theory = listOf(
            "John Bowlby beskrev tilknytning som et medfødt system, der sikrer, at barnet holder sig tæt på omsorgspersoner, når det er utrygt. Han beskrev fire faser: forudsætninger (0–6 uger), begyndende tilknytning (6 uger–6/8 måneder), tydelig tilknytning (6/8–18/24 måneder) og et gensidigt forhold derefter.",
            "Mary Ainsworth viste, at forskelle i, hvor sensitivt forældre reagerer på barnets signaler, hænger sammen med forskelle i tilknytningens kvalitet.",
            "Barnet danner indre arbejdsmodeller: forventninger til, om andre er der, når man har brug for dem.",
        ),
        concepts = listOf(
            "Sikker base: barnet udforsker, fordi det ved, at den voksne er der.",
            "Sikker havn: barnet søger tilbage til den voksne for at blive trøstet.",
            "Sensitivitet: at opfatte, forstå og reagere passende og rettidigt på barnets signaler.",
            "Tilknytningsmønstre: tryg, utryg-undgående, utryg-ambivalent og desorganiseret.",
        ),
        studies = listOf(
            PsychStudy("Fremmedsituationen", "Ainsworth, Blehar, Waters og Wall (1978)", listOf(
                "Et barn på 12–18 måneder og dets forælder kommer ind i et legerum.",
                "En fremmed kommer ind, forælderen går kort ud og kommer tilbage; det gentages i otte korte episoder på ca. 3 minutter.",
                "Forskerne ser især på, hvordan barnet reagerer, når forælderen kommer tilbage.",
            ), listOf(
                "Trygt tilknyttede børn søger forælderen ved gensynet, bliver trøstet og leger videre.",
                "Undgående børn ignorerer forælderen; ambivalente søger trøst, men kan ikke falde til ro.",
                "Senere tilføjede Main og Solomon (1990) det desorganiserede mønster med modstridende adfærd.",
                "I mange studier er omkring 60–65 % af børnene trygt tilknyttede.",
            )),
            PsychStudy("Trøst eller mad?", "Harry Harlow (1958)", listOf(
                "Abeunger kunne vælge mellem en ‘mor’ af ståltråd med mælk og en ‘mor’ af blødt stof uden mælk.",
            ), listOf(
                "Ungerne tilbragte mest tid hos den bløde mor og søgte til hende, når de blev bange.",
                "Det viste, at tilknytning handler om tryghed og kontakt, ikke kun om mad.",
            )),
        ),
        meaning = listOf(
            "En tryg tilknytning kræver ikke perfekte forældre, men nok sensitivitet og reparation, når noget går galt.",
            "Børn kan knytte sig til flere personer: mor, far, bedsteforældre og pædagoger.",
        ),
        atHome = listOf(
            "Omkring 6–8 måneder foretrækker barnet tydeligt sine nærmeste og protesterer ved adskillelse.",
            "Barnet kravler væk for at udforske og kigger tilbage eller kommer tilbage til dig.",
            "Når barnet er træt eller bange, er det kun dig, der dur.",
        ),
        caveats = listOf(
            "Fremmedsituationen er et forskningsredskab, ikke en test forældre kan bruge til at vurdere deres eget barn.",
            "Fordelingen af mønstre varierer mellem kulturer, og temperament spiller også ind. Tilknytning er ikke skæbne; den kan ændres af nye relationer og forhold.",
        ),
    ),
    26 to PsychDetail(
        summary = "Separationsangst og fremmedangst er barnets protest og uro, når den nærmeste går, og forsigtighed over for fremmede. Det begynder ofte omkring 6–8 måneder og topper typisk mellem 10 og 18 måneder.",
        theory = listOf(
            "Separationsreaktioner er et tegn på tilknytning og på, at barnet nu kan huske og savne en, der er gået.",
            "Jerome Kagan og kolleger fandt, at separationsangsten topper omkring samme alder i meget forskellige kulturer, hvilket tyder på, at den hænger sammen med en almen modning af hukommelse og forventning.",
        ),
        concepts = listOf(
            "Separationsangst: uro, når tilknytningspersonen går.",
            "Fremmedangst: forsigtighed og tilbageholdenhed over for ukendte personer.",
        ),
        studies = listOf(
            PsychStudy("Separationsangst på tværs af kulturer", "Kagan, Kearsley og Zelazo (1978)", listOf(
                "Børn i bl.a. USA, Guatemala, Botswana og et israelsk kibbutzsamfund blev observeret, når moren gik ud.",
            ), listOf(
                "I alle grupper steg uroen fra ca. 7 måneder og toppede omkring 13–15 måneder, hvorefter den faldt.",
            )),
            PsychStudy("Mødet med den fremmede", "Alan Sroufe (1977) og andre", listOf(
                "En fremmed nærmer sig barnet langsomt, mens forælderen er til stede eller ikke er.",
            ), listOf(
                "Fremmedangsten var mindre, når forælderen var tæt på, og når den fremmede nærmede sig langsomt.",
            )),
        ),
        meaning = listOf(
            "Gråd ved afsked er et sundt tegn på tilknytning og ikke et tegn på, at noget er galt.",
        ),
        atHome = listOf(
            "Barnet græder, når du går ud af rummet, også selvom det kun er kort.",
            "Barnet gemmer sig bag dig, når bedstemor, det ikke har set længe, vil tage det.",
            "Korte, forudsigelige afskeder og en fast hilsen kan hjælpe.",
        ),
        caveats = listOf(
            "Der er store forskelle mellem børn; nogle reagerer næsten ikke, andre meget.",
        ),
    ),
    27 to PsychDetail(
        summary = "Empati og prosocial adfærd er at mærke andres følelser og handle for at hjælpe, dele eller trøste. Tidlige former ses allerede i 1–2-års alderen.",
        theory = listOf(
            "Martin Hoffman beskrev udviklingen fra smitsom gråd hos spædbørn over egocentrisk empati (at trøste andre med det, der trøster en selv) til at forstå, at andre har deres egne behov.",
            "Felix Warneken og Michael Tomasello har vist, at småbørn hjælper spontant, uden at nogen beder dem om det og uden belønning.",
        ),
        concepts = listOf(
            "Empati: at mærke og forstå andres følelser.",
            "Prosocial adfærd: at hjælpe, dele og trøste.",
            "Indre motivation: at gøre noget fordi det føles rigtigt, ikke for en belønning.",
        ),
        studies = listOf(
            PsychStudy("Den tabte klemme", "Warneken og Tomasello (2006)", listOf(
                "18 måneder gamle børn ser en voksen tabe en klemme eller ikke kunne åbne et skab med hænderne fulde.",
                "Den voksne beder ikke om hjælp.",
            ), listOf(
                "De fleste børn hjalp spontant mindst én gang, fx ved at samle klemmen op eller åbne skabet.",
                "De hjalp ikke, når den voksne tabte noget med vilje.",
            )),
            PsychStudy("Belønning, der underminerer", "Warneken og Tomasello (2008)", listOf(
                "20 måneder gamle børn hjalp en voksen. Nogle fik et lille stykke legetøj som belønning, andre fik ros eller intet.",
                "Senere fik børnene nye muligheder for at hjælpe.",
            ), listOf(
                "Børn, der havde fået materiel belønning, hjalp mindre bagefter end de andre.",
            )),
            PsychStudy("Mor slår sig", "Zahn-Waxler, Radke-Yarrow, Wagner og Chapman (1992)", listOf(
                "Mødre lod som om de slog sig eller var kede af det derhjemme.",
                "Børnenes reaktioner blev registreret fra 13 til 20 måneder.",
            ), listOf(
                "Med alderen viste børnene mere bekymring, og de begyndte at trøste aktivt.",
            )),
        ),
        meaning = listOf(
            "Små børn har en medfødt tilbøjelighed til at hjælpe; ros og fællesskab støtter den bedre end belønninger.",
        ),
        atHome = listOf(
            "Barnet giver dig sin sut eller bamse, når du er ked af det.",
            "Barnet vil hjælpe med at tømme opvaskemaskinen eller samle tøj op.",
        ),
        caveats = listOf(
            "At dele er sværere end at hjælpe og kommer senere; det er normalt, at 2–3-årige vil beholde deres ting.",
        ),
    ),
    28 to PsychDetail(
        summary = "Følelsesregulering er evnen til at håndtere og styre sine følelser, fx at falde til ro, vente eller skifte fokus. Små børn reguleres af voksne, og gradvist overtager barnet selv mere af arbejdet, især fra 2 til 7 år.",
        theory = listOf(
            "Claire Kopp beskrev udviklingen fra samregulering, hvor voksne regulerer barnet ved at trøste, vugge og tale, til selvregulering, hvor barnet selv bruger strategier.",
            "Hjernens områder for selvkontrol, især pandelappen, modnes langsomt. Derfor er det normalt, at små børn har raserianfald, og at de har brug for voksnes hjælp.",
            "Strategier udvikles fra enkle (at kigge væk, sutte på fingre) over afledning og trøsteobjekter til at tale om følelser og tænke anderledes om en situation.",
        ),
        concepts = listOf(
            "Samregulering: voksne hjælper barnet med at falde til ro.",
            "Selvregulering: barnet styrer selv sine følelser.",
            "Afledning: at flytte opmærksomheden væk fra det svære.",
        ),
        studies = listOf(
            PsychStudy("Still face", "Edward Tronick m.fl. (1978)", listOf(
                "En mor leger ansigt til ansigt med sit spædbarn på 2–5 måneder.",
                "Moren holder pludselig op med at reagere og ser udtryksløst på barnet i et par minutter.",
                "Derefter genoptager hun samspillet.",
            ), listOf(
                "Barnet prøver først at få kontakt med smil og lyde, bliver så uroligt, kigger væk eller trøster sig selv.",
                "Barnet falder til ro, når kontakten kommer igen. Det viser, hvor meget små børn reguleres gennem samspil.",
            )),
            PsychStudy("Vent på gaven", "Grolnick, Bridges og Connell (1996)", listOf(
                "2-årige skulle vente på en lækker godbid eller gave, mens deres mor var til stede.",
            ), listOf(
                "Børn, der aktivt ledte deres opmærksomhed væk, fx ved at lege med noget andet, var mindre kede af det.",
                "Mødre, der hjalp med afledning, hjalp børnene mest.",
            )),
        ),
        meaning = listOf(
            "Barnet lærer at regulere sig selv ved at blive reguleret af andre mange gange.",
            "Raserianfald er en normal del af udviklingen, ikke et tegn på dårlig opdragelse.",
        ),
        atHome = listOf(
            "En 2-årig kan gå fra glæde til raseri på et sekund og har brug for, at du bliver rolig.",
            "Sæt ord på følelser: ‘Du er vred, fordi du ikke må få kiksen’.",
            "Omkring 4–5 år kan barnet bedre vente og finde på at lege med noget andet imens.",
        ),
        caveats = listOf(
            "Temperament spiller en stor rolle; nogle børn er født med kraftigere reaktioner end andre.",
        ),
    ),
)
