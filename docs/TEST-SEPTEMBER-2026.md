# Test på telefon — september 2026

Installér med Run i Android Studio oven på den eksisterende app. Afinstallation er ikke nødvendig. Brug gerne testfamilien i udviklerindstillingerne.

## Ble, noter og redigering

- Åbn Ble, vælg fx Afføring og Rød numse. Tryk Annuller: ingen ny registrering må vises i Journal eller dagens antal.
- Gentag og gem. Redigér typen til Våd og gem: journalen skal vise den nye type.
- Tilføj en flaske med tilbudt/spist ml, indhold og noter. Fold kortet ud: noterne skal vises. Redigér alle felterne og kontrollér resultatet.
- Start pumpning med Hånd og derefter med Maskine. Stop, indtast ml og noter, og gem. Metode, ml og noter skal kunne ændres bagefter.
- Kontrollér noter på amning, søvn, mål, aktiviteter, sundhedsbesøg, vaccination og fast føde.
- Ændr dato til et senere tidspunkt på en ble/flaske: Gem skal stadig fungere.

## Timere

- Start amning, lur, nattesøvn, pumpning og mavetid hver for sig. Tiden skal vises i topbaren og på timerkortet.
- Slå Hold skærmen tændt til. Skærmen skal forblive tændt, mens timeren kører og appen er åben; ved pause/stop eller når appen forlades skal den almindelige skærmtimeout gælde.
- Tillad notifikationer og visning på låseskærmen i telefonens indstillinger. Lås telefonen og fold notifikationen ud. Test Pause, Fortsæt, Stop og Skift side ved amning.
- Skift side, mens amningen er pauset: den må ikke starte af sig selv.
- Stop fra notifikationen. Vend tilbage til det relevante barn i I dag: redigeringsvinduet skal åbne. Annuller skal kassere registreringen; Gem skal lægge den i journalen.
- Annuller en aktiv timer direkte på kortet: både registreringen og notifikationen skal forsvinde.
- Lav en pause midt i en timer, stop og gem. Redigér kun noten: aktiv tid og pauseintervaller må ikke blive længere. Ændrer du start/slut, erstattes de gamle intervaller af det nye tidsrum.
- Luk og genåbn appen med en aktiv eller pauset timer. Den gemte tid skal bevares.

## Overblik og genveje

- Kontrollér, at amning og flaske samme dag viser både minutter og ml samt samlet antal madninger.
- Tilføj fx vægt, pumpning og medicin til Dagens overblik, så der er mere end fire felter. Genstart appen: valgene skal bevares.
- Fjern felter igen. Det sidste felt kan ikke fjernes.
- Åbn Tilpas hurtig registrering. Alle typer skal kunne slås fra og til i kategorikort; en tom kategori skal forsvinde.
- Scroll ned: den flydende +knap skal blive i bunden og åbne Manuel registrering.
- I Journal: find Filtre under udfold-knapperne. Test Madning · alle, Sundhed · alle og individuelle typer.

## Medicinkort

- Åbn I dag → Sundhed → Åbn medicinkort. Opret et præparat med eget navn, dosis og instruktioner.
- Tilføj to daglige klokkeslæt og slå påmindelser til. Appen skal tilbyde adgang til manglende notifikations-/alarmtilladelser.
- Læg et testtidspunkt et par minutter frem. Lås telefonen og kontrollér påmindelsen. Test gerne to præparater på samme tidspunkt.
- Redigér tidspunkterne eller afslut/slet præparatet: de tidligere påmindelser skal annulleres.
- Opret PN-medicin. Den skal kunne vælges ved registrering, men må ikke sende faste påmindelser.
- Registrér Medicin via hurtig registrering eller +. Vælg Fra medicinkort: navn og dosis skal udfyldes. Gem og kontrollér Journal.
- Skift til Hector: Frejas medicin må ikke stå på hans kort.
- Genstart appen: medicinkortet skal være bevaret. Test backup og gendannelse med testdata: medicinkort og registreringer skal følge med.

## Fast føde og billeder

- Åbn Fast føde. Registrér fx kartoffelmos, konsistens, mængde, reaktion og noter. Kontrollér kortet i Seneste og Journal, og test redigering.
- Upload et nyt telefonfoto til barn, forælder og familiemedlem. Kontroller orientering, zoom, vandret/lodret placering, rotation og det gemte udsnit.
- Annuller beskæringen: det tidligere profilbillede skal forblive uændret.
- Kontrollér det nye bjørneikon på telefonens startskærm/appoversigt.

## Verifikation

Verificeret 27. september 2026: debug-build, kodekontrol og Android Lint bestået; 40 unit tests og 14 Android-tests bestået på Pixel 8-emulator (API 37). Android-tests dækker bl.a. databaseopgradering 17→18, annullering/redigering, pauser bevaret ved ændring af pumpet ml, medicinkort i backup og EXIF-rotation. Manuel emulatorgennemgang bekræftede topbartimer, pause/fortsæt i notifikationspanelet, nyt ikon og en medicinpåmindelse leveret på det planlagte tidspunkt. Fysisk låseskærm, batterisparefunktioner og OEM-notifikationsindstillinger kræver ovenstående test på din egen telefon.

Platformgrundlag: [Android foreground-service types](https://developer.android.com/develop/background-work/services/fgs/service-types) og [Android alarm permissions](https://developer.android.com/develop/background-work/services/alarms).
