# Funktionsliste og regressionstjekliste

Denne liste beskriver alt, appen kan pr. 20. oktober 2026 (commit `2d42b4e`). Den bruges før og efter hver refaktorering: alt nedenfor skal stadig virke, og alle eksisterende data skal vises som før.

## Regler for data

- Ingen opdatering må slette eller ændre gemte data. Databaseændringer må kun tilføje kolonner/tabeller; data flyttes ved kopiering, og det gamle fjernes først, når kopien er bekræftet.
- Registreringer, der ikke længere kan oprettes (pumpning med timer, ammebrik, søvn uden type, medicin uden medicinkort), skal stadig vises og kunne redigeres.
- Automatiske tjek:
  - `MigrationChainTest` opgraderer en database fra hver tidligere version (1–22) og kræver, at alle rækker og værdier er bevaret.
  - `LegacyEventDisplayTest` låser titel, resumé, detaljer og dagsoversigt for gamle registreringstyper fast (`app/src/test/resources/legacy-event-display.txt`).
  - `BackupRestoreTest` kræver, at backup gendanner alle data, indstillinger og farvetemaer, og at ældre backupfiler stadig kan gendannes.
- Manuelt: Tag en krypteret backup på telefonen før installation af en ny version.

## Opstart og generelt

- [ ] Velkomst/onboarding (dansk) med region, enheder og tema; intet barn oprettes i onboarding
- [ ] Tom-tilstand på I dag med "kom godt i gang"-dialog, der kun vises én gang; registrering slået fra uden barn
- [ ] Aktivt barn vælges i topbjælken (navn, avatar, alder); valget huskes
- [ ] Barnets farvetema farver hele appen
- [ ] Kørende timer vises i topbjælken og fører til I dag
- [ ] Indstillinger fra tandhjulet i topbjælken

## I dag

- [ ] Dagsoverblik med valgfrie nøgletal, bladring mellem dage og "Tilbage til i dag"
- [ ] Påmindelse "Er barnet blevet født?" 14 dage efter termin
- [ ] Hurtig registrering grupperet i kategorier (Madning, Søvn, Ble, Mål, Sundhed, Fast føde, Diverse); kategorier kan sorteres og knapper skjules
- [ ] Amning: start venstre/højre, pause/genoptag, skift side, stop → redigér før gem; fortsæt seneste amning
- [ ] Flaske: indhold (modermælk/erstatning/blanding med forhold/vand/andet), tilbudt og spist ml; tilbudt hæves til spist ved gem
- [ ] Pumpning: manuel formular med dato, tid, metode, ml og noter; seneste metode huskes; kan registreres mens anden timer kører
- [ ] Ble: våd/beskidt/begge/tør, farve, konsistens, observation, noter; gemmes først ved "Gem"
- [ ] Søvn: lur/nattesøvn som timer eller manuelt med sted, indsovning, opvågninger, kvalitet; overlap afvises
- [ ] Favoritside: hurtig venstre/højre med statistik-graf ved Søvn-overskriften
- [ ] Mål: vægt (gram/kg), højde, hovedomkreds, temperatur; dato med valgfrit tidspunkt; måle-grafer med husket periode
- [ ] Sundhed: sundhedsbesøg, vaccination, medicin (fra medicinkort), helbredsnotat, tænder, medicinkort
- [ ] Fast føde: mad, konsistens, mængde, reaktion, noter
- [ ] Diverse: mavetid, bad, leg, udetid, andet som timer eller manuelt
- [ ] Maks. to samtidige timere og én af hver type; fejlbesked ved konflikt
- [ ] Timerpanel nederst kan minimeres; "Hold skærmen tændt" mens timer kører
- [ ] "+"-knap med manuel registrering af alle typer

## Notifikationer og påmindelser

- [ ] Timer-notifikation med pause/genoptag, stop og sideskift
- [ ] Medicinpåmindelser: Taget (logger dosis), Snooze 10 min, Skip; overlever genstart og tidszoneskift; præcise alarmer med forklaret fallback
- [ ] Daglig påmindelse (tidspunkt kan vælges)
- [ ] Bogpåmindelser (dagligt/3× ugentligt/ugentligt/månedligt) der åbner den relevante side

## Journal

- [ ] Kronologisk liste grupperet pr. dag; dage kan foldes ind/ud; udvid/fold alle
- [ ] Filtre: grupperede kategorier og enkelte typer; hurtigfiltre kan tilpasses
- [ ] Redigér og slet (blød sletning) alle registreringstyper, også historiske former
- [ ] Tilføj registrering direkte fra Journal
- [ ] Genvej til måle-grafer fra målinger

## Overblik

- [ ] Grafer (måle-grafer)
- [ ] Dagens overblik med bladring mellem dage
- [ ] Favoritside-statistik med periode og korrektionsdatoer

## Barnets bog

- [ ] Kapitler og sider for det første år med fremdrift
- [ ] Sideeditor med tekstfelter og billeder
- [ ] Åbning direkte fra bogpåmindelse

## Familie

- [ ] Flere børn, også ufødte (termin), med sortering
- [ ] Barneprofil: grundoplysninger, fødsel, sundhed (CPR, allergier, noter), officielle oplysninger, avatar/foto, farvetema
- [ ] Sundhedspersoner som gentagelige kort (hospital, læge, sundhedsplejerske, jordemoder, speciallæge, andet)
- [ ] Forældre og øvrig familie med roller, foto og kontaktinfo; tovejs-kobling til børn; søskende udledes
- [ ] Profilfoto med beskæring, zoom, placering og rotation

## Indstillinger og data

- [ ] Region, enheder, tema, påmindelser
- [ ] Farveprofiler: opret, redigér, slet, sortér, eksport/import (JSON)
- [ ] CSV-eksport for periode og PDF-sundhedsoversigt
- [ ] Krypteret backup og gendannelse (data, fotos, alle indstillinger, farvetemaer)
- [ ] Debug-build: ryd alle data, opret testfamilie og palet-børn

## Kendte særheder i nuværende visning

Disse er fastlåst af `LegacyEventDisplayTest` og ændres kun bevidst:

- Ble uden type vises som "Ble – Ble".
- Aktivitet uden type har titlen "Diverse".
- Flaske uden mængder vises som "0 af 0 ml".
- Vægt i gram vises med decimal ("5200.0 g").
