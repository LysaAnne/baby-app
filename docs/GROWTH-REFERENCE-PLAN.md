# Valgfrie vækstreferencer i målegrafer

Status: undersøgt og planlagt 11. oktober 2026. Ikke implementeret i appen.

## Kildegrundlag

Sundhedsstyrelsens [Monitorering af vækst hos 0-5-årige børn](https://www.sst.dk/media/hzwj0akv/monitorering-af-vaekst-0-5-aar.pdf) anbefaler WHO for vægt og længde/højde, men danske referencer for hovedomfang. Det er alders- og kønsspecifikke kurver, ikke én ugentlig minimumstilvækst. Relevante afsnit: 2.2, 3, 4, 6, 7 og 8.

Anbefalede percentiler er p3, p10, p25, p50, p75, p90, p97, suppleret med p1 for længde og p1/p99 for hovedomfang. Percentilplacering alene afgør ikke trivsel. Vægt under p3 eller fald svarende til to percentilintervaller giver anledning til opfølgning. Hovedomfang over p97 eller tydelige kurveskift kræver også faglig vurdering. Disse forhold kan ikke oversættes til én fælles bestået/ikke-bestået-regel.

De første to uger og præmaturitet kræver særskilt håndtering. Liggende længde og stående højde er forskellige mål; overgangen omkring to år svarer omtrent til 0,7 cm.

Datakilder til implementering:

- [WHO vægt-for-alder](https://www.who.int/tools/child-growth-standards/standards/weight-for-age): officielle udvidede tabeller for piger og drenge.
- [WHO længde/højde-for-alder](https://www.who.int/tools/child-growth-standards/standards/length-height-for-age): separate længde- og højdetabeller.
- [Danske LMS-tabeller](https://www.vaekstkurver.dk/sds_tabeller.html) og [hovedomfang 0–5 år](https://www.vaekstkurver.dk/files/hovedomfang.pdf): publicerede numeriske parametre. Verificér referencens version imod Sundhedsstyrelsens bilag før lancering; kald den ikke WHO-hovedomfang.
- [CDC: LMS-formler for WHO-data](https://www.cdc.gov/growthcharts/who-data-files.htm): matematisk dokumentation. Hent selve WHO-data hos WHO.

## Beregningsmodel

For hver måling bruges måledato, fødselsdato, referencekøn, måletype og værdi i kg/cm. Alder beregnes fra lokale kalenderdatoer, ikke millisekunder divideret med 24 timer. Anvend tabeludgiverens aldersenhed og grænser. WHO-tabeller med daglig opløsning foretrækkes.

L er transformationens parameter, M medianen og S variationskoefficienten. For værdi x:

```text
z = ((x / M)^L - 1) / (L * S)   når L != 0
z = ln(x / M) / S               når L == 0
percentil = 100 * Φ(z)

kurveværdi(alder, z) = M * (1 + L*S*z)^(1/L)
kurveværdi(alder, z) = M * exp(S*z)          når L == 0
```

Φ er standardnormalfordelingens fordelingsfunktion. Afvis ugyldige parametre, ikke-positive mål, ikke-endelige tal og ugyldig potensbase. Vis ikke falsk præcision ved ekstreme værdier: uden for det validerede område vises eksempelvis »under p1«/»over p99« frem for eksakte percentiler. WHO's særlige behandling af ekstreme z-scores skal valideres før eventuel numerisk visning derude.

Udvikling mellem to målinger beskrives med `Δz = z2 - z1`. En stiplet sammenligningskurve kan fastholde første målings z-score:

```text
referenceVedSlut = kurveværdi(alder2, z1)
tilvækstLangsSammeKurve = referenceVedSlut - x1
måltTilvækst = x2 - x1
forskelFraSammeKurve = x2 - referenceVedSlut
```

Dette er en matematisk sammenligning med uændret percentil, ikke en prognose eller et krav til barnets vækst. Syntetisk kontrol: L=1, S=0,1, M1=5, M2=6, x1=5 giver z1=0 og referenceVedSlut=6. Er x2=5,7, er Δz=-0,5 og forskellen -0,3. Tallene er ikke en klinisk anbefaling.

Referencelinjer beregnes med z=Φ⁻¹(p/100). Ved danske tabelaldre mellem knudepunkter kan lineær interpolation af L/M/S bruges som eksplicit tilnærmelse. Den grove opløsning, især 0–3 måneder, skal sammenholdes med udgiverens kurver; uvaliderede interpolerede værdier må ikke udløse automatiske helbredsmarkeringer. Ingen ekstrapolation eller interpolation over skiftet mellem længde og højde.

## Foreslået visning og afgrænsning

- Én kontakt: **Vis vækstreferencer**, som huskes efter genstart. Standard fra. Gælder vægt, længde/højde og hovedomfang; temperaturen er uændret.
- Tynde, mærkede percentillinjer bag barnets tydelige målekurve. Ingen grøn baggrund, der lover normal trivsel. p50 betyder median, ikke et mål barnet skal nå.
- Under grafen: seneste percentil, ændring i z-score og målt tilvækst sammenholdt med tilvækst langs samme percentil. Vis start/slutdatoer for sammenligningen. Én måling giver kun placering, ikke udvikling.
- Brug beskrivende tekst såsom »Fra p25 til p10 i den viste periode«. Undlad »følger anbefalingen« som automatisk sundhedsstempel. Et barn kan ligge stabilt lavt eller have et relevant fald inden for p3–p97.
- Eventuelle senere opfølgningsmarkeringer kræver særskilte regler pr. mål og klinisk gennemgang. Tæl ikke blot krydsede linjer som hele percentilintervaller, og indfør ikke en opdigtet universel Δz-tærskel.
- Manglende fødselsdato eller referencekøn giver en forklaring og mulighed for at udfylde profil. Udled aldrig referencekøn fra navn eller farvetema. Vis stadig barnets rå målinger.
- Første 14 dage: ingen automatisk udviklingsvurdering. For præmature: vis rå data, indtil alderkorrektion er særskilt valideret. Modellen er kronologisk alder minus `(280 - gestationsalderVedFødselIDage)` ved dokumenteret præmaturitet, ikke ved enhver fødsel før terminsdato. Stopalder for korrektion skal fastlægges og vises eksplicit; sammenlign ikke ukritisk over skiftet.
- Registrér måleposition for længde/højde før automatisk sammenligning. Ældre målinger med ukendt metode skal markeres, ikke ændres. Uden for tabellernes alder vises ingen reference.

## Integration i nuværende app

1. Pak verificerede referencefiler lokalt med kilde, version, dato, aldersenhed og checksum. Kontrollér brugs-/distributionsvilkår. Ingen data sendes ud af appen.
2. Tilføj en ren domæneberegning ved siden af `MeasurementCharts.kt`: tabelopslag, LMS, percentiler og beskrivende sammenligning. Hold kilde og status for tilnærmelse i resultatet.
3. Udvid `MeasurementChartSettings` med bagudkompatibel standardværdi `showGrowthReferences=false`; eksisterende periode/datoer bevares.
4. Giv `MeasurementChartsDialog` barnets profil fra både Today og Journal. Indtegn referencer på samme tidsakse og med fælles y-skala. Bevar alle målepunkter, også uden for referencernes gyldighed.
5. Vælg første og sidste gyldige måling på forskellige datoer i den viste periode som tydeligt mærket sammenligning. Ved flere målinger samme dato: vis alle, men kræv entydigt valg til beregningen. Ændret periode må ikke ændre percentilen for et bestemt punkt.
6. Gem kun præferencen; udled resultater fra aktuelle data. Profilændringer, rettede målinger og skift af barn skal genberegne referencer. Fødselsmål må ikke duplikeres som hændelser.

## Verifikation før færdigmelding

- Sammenhold beregninger ved flere aldre for begge køn og alle tre mål med publicerede tabelværdier; median giver z=0, og frem/tilbage-beregning skal stemme.
- Test L=0, ugyldige input, aldergrænser, skuddag/sommertid, enheder, manglende profil, præmaturitet, ukendt måleposition og dubletter samme dato.
- Test fastholdt percentil og faldende percentil trods stigende rå vægt; én måling må ikke give trend. Kontroller særskilt interpolationsfejl og toårsovergang.
- UI: til/fra, genstart, begge indgange, børneskift, perioder, tomme data, tilgængelige tekstforklaringer og målinger uden for kurveområdet.
- Kør målrettede unit-/UI-tests, build, lint og projektets kodestilkontrol ved implementering. Fysisk telefonkontrol rapporteres separat.

Ingen appkode, referencepakker eller automatiske kliniske vurderinger er leveret med denne plan.
