package dk.babyapp.data.tracking

data class DanishHealthTemplate(
    val key: String,
    val title: String,
    val vaccineName: String = "",
    val dose: String = "",
)

val danishPreventiveExaminationTemplates = listOf(
    DanishHealthTemplate("exam-5-weeks", "5-ugers børneundersøgelse"),
    DanishHealthTemplate("exam-5-months", "5-måneders børneundersøgelse"),
    DanishHealthTemplate("exam-12-months", "12-måneders børneundersøgelse"),
    DanishHealthTemplate("exam-2-years", "2-års børneundersøgelse"),
    DanishHealthTemplate("exam-3-years", "3-års børneundersøgelse"),
    DanishHealthTemplate("exam-4-years", "4-års børneundersøgelse"),
    DanishHealthTemplate("exam-5-years", "5-års børneundersøgelse"),
)

/**
 * The health visitor's (sundhedsplejerske) home visits in the first year, as recommended by Sundhedsstyrelsen.
 * The exact weeks vary between municipalities; the visits are offered by the municipality.
 */
val danishHealthVisitorTemplates = listOf(
    DanishHealthTemplate("visitor-postnatal", "Sundhedsplejerske · barselsbesøg"),
    DanishHealthTemplate("visitor-establishing", "Sundhedsplejerske · etableringsbesøg"),
    DanishHealthTemplate("visitor-2-months", "Sundhedsplejerske · 2 måneder"),
    DanishHealthTemplate("visitor-4-6-months", "Sundhedsplejerske · 4–6 måneder"),
    DanishHealthTemplate("visitor-8-10-months", "Sundhedsplejerske · 8–10 måneder"),
)

/** Examinations and health visitor visits: the visits that can be booked. */
val danishVisitTemplates get() = danishPreventiveExaminationTemplates + danishHealthVisitorTemplates

val danishVaccinationTemplates = listOf(
    DanishHealthTemplate("vaccine-dtkphib-1", "3 måneder · DiTeKiPol-Hib", "DiTeKiPol-Hib", "Dosis 1 af 3"),
    DanishHealthTemplate("vaccine-pneumococcal-1", "3 måneder · Pneumokok", "Pneumokok", "Dosis 1 af 3"),
    DanishHealthTemplate("vaccine-dtkphib-2", "5 måneder · DiTeKiPol-Hib", "DiTeKiPol-Hib", "Dosis 2 af 3"),
    DanishHealthTemplate("vaccine-pneumococcal-2", "5 måneder · Pneumokok", "Pneumokok", "Dosis 2 af 3"),
    DanishHealthTemplate("vaccine-dtkphib-3", "12 måneder · DiTeKiPol-Hib", "DiTeKiPol-Hib", "Dosis 3 af 3"),
    DanishHealthTemplate("vaccine-pneumococcal-3", "12 måneder · Pneumokok", "Pneumokok", "Dosis 3 af 3"),
    DanishHealthTemplate("vaccine-mmr-1", "15 måneder · MFR", "MFR", "Dosis 1 af 2"),
    DanishHealthTemplate("vaccine-mmr-2", "4 år · MFR", "MFR", "Dosis 2 af 2"),
    DanishHealthTemplate("vaccine-dtkp-booster", "5 år · DiTeKiPol revaccination", "DiTeKiPol revaccination", "Revaccination"),
    DanishHealthTemplate("vaccine-hpv-1", "12 år · HPV dosis 1", "HPV", "Dosis 1 af 2"),
    DanishHealthTemplate("vaccine-hpv-2", "12 år · HPV dosis 2", "HPV", "Dosis 2 af 2"),
)
