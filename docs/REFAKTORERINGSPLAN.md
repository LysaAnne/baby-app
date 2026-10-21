# Refaktorerings- og designplan

Planen for at gøre appen flottere, enklere og hurtigere, uden at den mister noget, den kan i dag. Opdateres, når en fase eller et punkt er færdigt.

Sidst opdateret: 20. oktober 2026

## Næste skridt

**Fase 3 – Skærmene redesignes**, punkt 3.4: Overblik → Indsigt (aftales først med en skitse).

Alt i fase 0 og 1, der handler om data, er færdigt. Punkt 1.5 og 1.6 laves sammen med de skærme, de hører til i fase 3.

Før du installerer en ny version på telefonen: tag en krypteret backup (Indstillinger → Eksport og backup), og gem filen uden for telefonen.

## Overblik

| Fase | Indhold | Status |
|---|---|---|
| 0 | Sikkerhedsnet for data | ✅ Færdig |
| 1 | Teknisk fundament | ✅ Data færdig · 1.5 og 1.6 følger fase 3 |
| 2 | Designsystem | ✅ Færdig |
| 3 | Skærmene redesignes én ad gangen | 🔄 I gang · 3.1–3.3 færdige |
| 4 | Nye funktioner | Venter |

## Regler, der gælder for alle faser

- **Ingen data må slettes eller ændres.** Databaseændringer må kun tilføje noget. Data flyttes ved at blive kopieret, og det gamle bliver liggende, indtil kopien er bekræftet.
- **Gamle registreringer vises som før**, også typer, der ikke længere kan oprettes (fx pumpning med timer, ammebrik).
- **Gamle backupfiler kan altid gendannes.**
- **Hvert skridt testes** med automatiske tests og en rigtig opgradering fra forrige version på emulatoren, før det committes.
- [Funktionslisten](FEATURE-INVENTORY.md) er tjeklisten over alt, appen kan. Den skal stadig holde efter hvert skridt.

---

## Fase 0 – Sikkerhedsnet ✅

| Punkt | Hvad | Commit |
|---|---|---|
| Backup | Backuppen indeholder alle data, indstillinger og farvetemaer. Gendannelse tjekker hele filen, før den skriver noget. | `37b8630` |
| Gem nuværende arbejde | Codex-ændringerne fra 20. oktober | `2d42b4e` |
| Tests og funktionsliste | Opgraderingstest for alle databaseversioner, test der låser visningen af gamle registreringer, funktionsliste | `5d8d470` |

## Fase 1 – Teknisk fundament

| Punkt | Hvad | Status | Commit |
|---|---|---|---|
| 1.1 | Del `AppViewModel` op i registrering, familie, indstillinger, bog og udviklerværktøjer | ✅ | `1802f60` |
| 1.2 | Hent kun det, hver skærm bruger. I dag gik fra ~900 ms til ~20 ms med 3 års data. | ✅ | `b67e66b` |
| 1.3 | Medicinkort og tænder flyttet fra indstillingerne ind i databasen | ✅ | `9b7ddaa` |
| 1.4 | Faste ID'er i stedet for danske tekster som gemte nøgler (hurtigfiltre, kategorirækkefølge) | ✅ | `6a742da` |
| 1.5 | Domænemodel oven på den brede registreringstabel | Laves i fase 3 | – |
| 1.6 | Tekster til ressourcefiler; udviklerværktøjer kun i debug-versionen | Laves i fase 3 | – |

**Hvorfor 1.5 og 1.6 venter:** De rører de samme filer som redesignet. Hvis de laves nu, skal skærmene og deres tests omskrives to gange.

---

## Fase 2 – Designsystem ✅

Målet er et fælles, roligt udtryk, som alle skærme bygges af i fase 3. Det, der ses på skærmen, ændres kun lidt i denne fase. Det er især farver, skrift og mørk tilstand.

### 2.1 Farver og tema ✅

Færdig: farvetokens (`BabyColors`), røde fejl og slet-knapper, fast farve og ikon pr. kategori på I dag og i Journal, kontrast testet mod alle profilbaggrunde.

- Farver samles i navngivne tokens: baggrund, flader, tekst, accent, advarsel osv.
- **Fejlfarven rettes.** Lige nu bruger advarsler barnets tredjefarve og er derfor ikke røde.
- Barnets farvetema bevares og bruges som accent.
- Fast farve og ikon pr. registreringstype (madning, søvn, ble, sundhed, mål, diverse), som går igen overalt.

### 2.2 Mørk tilstand og nattilstand — fjernet

Blev bygget, men er fjernet igen efter ønske: appen er altid lys og farvet efter barnets valgte farveprofil. Rettelsen af mørke containerfarver er bevaret for egne mørke farveprofiler.

### 2.3 Typografi og afstande ✅

Færdig: Nunito i hele appen (lagt ind, virker offline), afstande (`BabySpacing`) og trykflader (`BabyTouch`). Hurtigknapperne blev gjort 56 dp høje, men er sat tilbage til den tidligere højde, fordi de fyldte for meget i højden; trykfladen er stadig mindst 48 dp. `TouchTargetTest` kræver mindst 48 dp for alt, der kan trykkes på, på I dag og i Journal. Skærmene tager afstandene i brug, efterhånden som de redesignes i fase 3.

- Én afrundet skrifttype (forslag: Nunito) og færre tekststørrelser.
- Faste afstande og hjørner, så kort, knapper og dialoger står ens.
- Trykflader på mindst 48 dp uden at gøre knapperne højere end nødvendigt; skærmen skal kunne rumme meget uden at scrolle.

### 2.4 Fælles komponenter ✅

Færdig: `ActionTile`, `EventRow`, `TimerBar`, `EntrySheet`, `ChipSelector`, `AmountStepper` og `BabyEmptyState` med handling i `ui/components`, med previews i lys, mørk og nat (`ComponentPreviews.kt`) og tests (`ComponentsTest`, `TouchTargetTest`). De tages i brug i fase 3.

Byggeklodser, som fase 3 bruger:
- `ActionTile` – stor knap til hurtig registrering
- `EventRow` – én registrering i en liste
- `TimerBar` – kørende timer som mini-afspiller
- `EntrySheet` – bundark til at registrere og redigere
- `ChipSelector` og `AmountStepper` – valg og mængder uden tastatur
- `EmptyState` – tom skærm med hjælpetekst

**Færdig, når:** Mørk tilstand og nattilstand virker. Fejlfarven er rød. Komponenterne findes med previews og tests. Eksisterende skærme ser ens ud med det nye tema i lys og mørk.

---

## Fase 3 – Skærmene redesignes én ad gangen

Én skærm ad gangen, hver i sin egen commit. Punkt 1.5 og 1.6 laves for den skærm, der redesignes.

### 3.1 I dag ✅

Aftalt ud fra skitse 2 (samme struktur, mere kompakt). Færdig:
- Dagens overblik: dagbladring i overskriften, ikoner i hvert felt, lavere felter. Felterne vælges og sorteres stadig selv.
- Kategorikort: ét kort pr. kategori som før (kan sorteres og skjules), mindre luft, to knapper pr. række under hinanden som før. Knapperne har den tidligere højde.
- Tællere: kompakte bjælker over menulinjen på alle faner, kun når en tæller er i gang, op til to; kan udvides til den fulde visning. Stop gemmes fra den fane, man står på. Tælleren i topbjælken er fjernet.
- +-knappen er bevaret og ligger altid over tællerne.
- Udfoldet tæller: nyt design med stor tid, felter for venstre/højre side, intervalliste, kontakt til "Hold skærmen tændt" og ensartede knapper.
- Registreringsvinduer: fælles design (`FormDialog`/`FormHeader`) med kategoriikon, afrundede felter i fuld bredde, dato/tid med ikoner og Annuller/Gem nederst.

Backup af udseendet før 3.1: git-mærket `for-redesign-i-dag` og `backups/babyapp-for-redesign-i-dag-d585cba.apk`.

### 3.2 +-menuen ✅

Færdig: bundark med kategorier, farvede ikoner og to knapper pr. række; kategorier med ét valg har ingen overskrift, men ikonet i knappen.

- Samme indhold og funktion som i dag; kategorikortene på I dag bevares.
- Menuen bag + får det nye design: bundark, kategorier med farvede ikoner, knapper to pr. række.

### 3.3 Journal ✅

Færdig: ikoner i overskriften, filterchips på én linje, dagsoverskrift med dagens tal (samme felter som Dagens overblik), kompakte registreringer, hold inde for Rediger/Slet (ingen swipe), sletning uden bekræftelse men med **Fortryd**, og gradvis indlæsning (30 dage, derefter 60 dage ad gangen).

- Samme struktur, mere kompakt: registreringer i samme stil som I dag.
- **Gradvis indlæsning** mens man scroller, i stedet for hele historikken på én gang (i dag ca. 600 ms med 3 års data).

### 3.4 Overblik → Indsigt
- Rigtigt dashboard med 7- og 30-dages tendenser for søvn, madning og bleer.
- Døgnrytmegraf og favoritside.
- Vækstkurver med WHO-referencer (se [GROWTH-REFERENCE-PLAN.md](GROWTH-REFERENCE-PLAN.md)).
- Grafer bliver rigtige skærme, ikke dialoger.

### 3.5 Familie og Indstillinger
- Enklere profilvisning.
- Indstillinger får deres egen skærm.
- Udviklerværktøjer findes kun i debug-versionen.

### 3.6 Barnets bog
- Mere visuelt kapiteloverblik med fremdrift og billeder.
- Svar, der er gemt under gamle spørgsmålstekster, vises fortsat.

**For hver skærm:** Funktionslisten holder. Gamle registreringer vises korrekt. UI-tests er opdateret. Skærmen er afprøvet i lys, mørk og nat.

---

## Fase 4 – Nye funktioner

Prioriteret forslag. Rækkefølgen aftales, før hver funktion påbegyndes.

1. **Fortryd** ved sletning og ændring, hvis den ikke allerede er kommet med i 3.3.
2. **Hjemmeskærm-widget og genveje** (langt tryk på app-ikonet): start amning eller registrér ble uden at åbne appen.
3. **"Tid siden sidst"-tællere** på I dag og i widgeten.
4. **Rapport til sundhedsplejersken:** PDF med de seneste 1–4 uger, vækstkurve og spørgsmål.
5. **Spørgsmål til sundhedsplejersken eller lægen,** som kobles til næste besøg.
6. **Medicinoverblik:** "Sidste dosis kl. 14.30 (for 3 t siden)". Kun information; appen beregner aldrig doser.
7. **Påmindelser om vaccinationer og børneundersøgelser** ud fra det danske program.
8. **Lager af modermælk:** pumpet ind, brugt ud, holdbarhed.
9. **Deling mellem forældre og synkronisering.** Den største opgave, og den kræver konto og server.
10. **Månedlige fotos i Barnets bog** med en "se hvor meget du er vokset"-sammenligning.

---

## Kendte forhold, der bevidst ikke er ændret

- **Fire særheder i visningen:** "Ble – Ble" for en ble uden type, titlen "Diverse" for en gammel aktivitet uden type, "0 af 0 ml" for en flaske uden mængde, og "5200.0 g". De er låst af en test og rettes kun bevidst, fx i fase 3.
- **Barnets bog:** Svar er gemt under spørgsmålets tekst. Editoren viser også svar, hvis spørgsmål har skiftet navn, så de ikke kan forsvinde.
- **Registreringernes indhold:** Fx pumpemetoden "Manuel pumpe" er data og vises, som det blev gemt.
- **Nedgradering:** Efter en opdatering kan en ældre app-version ikke installeres ovenpå, fordi Android ikke tillader det. Gendan i stedet fra backup.
