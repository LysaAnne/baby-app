package dk.babyapp.domain

/**
 * The full text for one psychology topic, shown under headings when it is opened. Written in November 2026 to
 * go deeper than the first overview: the theory, key concepts, the classic studies step by step, what they mean,
 * what parents may notice, and the reservations. Researchers and years name the studies so they can be looked up.
 */
data class PsychDetail(
    /** Two or three sentences: what the phenomenon is. */
    val summary: String,
    /** "Teorien bag": short paragraphs. */
    val theory: List<String>,
    /** "Nøglebegreber": "Begreb: forklaring"; the part before the colon is shown in bold. */
    val concepts: List<String>,
    val studies: List<PsychStudy>,
    /** "Hvad betyder det?" */
    val meaning: List<String>,
    /** "Det kan du se i hverdagen" */
    val atHome: List<String>,
    /** "Forbehold" */
    val caveats: List<String>,
)

/** One study: its name, who and when, what was done step by step, and what was found. */
data class PsychStudy(val name: String, val who: String, val steps: List<String>, val findings: List<String>)

/** Every topic's full text, by its number. */
val psychDetails: Map<Int, PsychDetail> by lazy {
    cognitionDetails + socialCognitionDetails + selfAndEmotionDetails + executiveAndMoralDetails + languageAndPerspectiveDetails
}
