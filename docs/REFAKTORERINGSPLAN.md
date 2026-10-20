# Refaktorerings- og designplan

Planen for at gøre appen flottere, enklere og hurtigere, uden at den mister noget, den kan i dag. Opdateres, når en fase eller et punkt er færdigt.

Sidst opdateret: 20. oktober 2026 · seneste commit `b67e66b`

## Næste skridt

**Fase 2 – Designsystem**, punkt 2.1: farver og tema.

Alt i fase 0 og 1, der handler om data, er færdigt. Punkt 1.5 og 1.6 laves sammen med de skærme, de hører til i fase 3.

Før du installerer en ny version på telefonen: tag en krypteret backup (Indstillinger → Eksport og backup), og gem filen uden for telefonen.

## Overblik

| Fase | Indhold | Status |
|---|---|---|
| 0 | Sikkerhedsnet for data | ✅ Færdig |
| 1 | Teknisk fundament | ✅ Data færdig · 1.5 og 1.6 følger fase 3 |
| 2 | Designsystem | ⏭️ Næste |
| 3 | Skærmene redesignes én ad gangen | Venter |
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

## Fase 2 – Designsystem ⏭️

Målet er et fælles, roligt udtryk, som alle skærme bygges af i fase 3. Det, der ses på skærmen, ændres kun lidt i denne fase. Det er især farver, skrift og mørk tilstand.

### 2.1 Farver og tema
- Farver samles i navngivne tokens: baggrund, flader, tekst, accent, advarsel osv.
- **Fejlfarven rettes.** Lige nu bruger advarsler barnets tredjefarve og er derfor ikke røde.
- Barnets farvetema bevares og bruges som accent.
- Fast farve og ikon pr. registreringstype (madning, søvn, ble, sundhed, mål, diverse), som går igen overalt.

### 2.2 Mørk tilstand og nattilstand
- **Mørk tilstand virker ikke i dag.** Valget i Indstillinger gemmes, men bruges aldrig. Det rettes, så System, Lys og Mørk virker.
- **Ny nattilstand:** dæmpede, varme farver og lav lysstyrke til natlig brug. Den kan slås til manuelt eller automatisk om aftenen.

### 2.3 Typografi og afstande
- Én afrundet skrifttype (forslag: Nunito) og færre tekststørrelser.
- Faste afstande og hjørner, så kort, knapper og dialoger står ens.
- Store trykflader: mindst 48 dp, og de vigtigste knapper 56 dp.

### 2.4 Fælles komponenter
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

### 3.1 I dag
- **Statuskort øverst:** "Sidst spist for 2 t 10 m (venstre)", "Sover siden 14.05", "Sidste ble for 1 t".
- **Hovedhandlinger:** et gitter med ca. 6 valgfrie hovedhandlinger og en "Mere"-knap i stedet for de lange kategorikort.
- **Kørende timere:** vises som en fast mini-afspiller over menulinjen og ligger ikke længere oven på indholdet.
- **Dagens tal** som kompakte chips og de seneste 3–5 registreringer.

### 3.2 Ét samlet "Registrer"-ark
- Erstatter både kategorikortene og den separate "+"-menu.
- Kategorier og søgning.
- Formularer som bundark med smarte standardværdier: "nu" som tidspunkt og sidst brugte værdier.

### 3.3 Journal
- Dagsoverskrifter, farvede ikoner og filterchips på én linje.
- Swipe for at redigere eller slette.
- **Fortryd** via snackbar efter sletning.
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
