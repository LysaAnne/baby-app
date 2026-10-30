package dk.babyapp.domain

enum class SongCategory(val label: String) { Bedtime("Godnat"), Actions("Fagter"), Rhymes("Rim og lege"), Christmas("Jul") }

/**
 * A song in the songbook. The app has no lyrics; the family can add the words themselves.
 * [origin] is the author or "Traditionel" where that is well known, otherwise empty; [actions] describes
 * the movements in the app's own words; [link] is a page with the words, opened from the song.
 */
data class Song(val id: String, val title: String, val category: SongCategory, val origin: String = "", val actions: String = "", val link: String = "")

private const val TRAD = "Traditionel"

val songs = listOf(
    Song("mester-jakob", "Mester Jakob", SongCategory.Rhymes, TRAD, "Kan synges i kanon, når barnet bliver større."),
    Song("bjoernen-sover", "Bjørnen sover", SongCategory.Rhymes, TRAD, "En voksen er bjørnen og sover; når den vågner, løber alle væk."),
    Song("lille-peter-edderkop", "Lille Peter Edderkop", SongCategory.Actions, TRAD, "Fingrene kravler op, regnen falder ned, solen bliver en stor cirkel med armene.", link = "https://ugle.dk/lille_peter_edderkop.html"),
    Song("hjulene-paa-bussen", "Hjulene på bussen", SongCategory.Actions, TRAD, "Rul armene om hinanden, vinduesviskere med hænderne, og find på nye vers."),
    Song("se-min-kjole", "Se min kjole", SongCategory.Rhymes, TRAD, "Peg på tøj i farverne, der synges om."),
    Song("en-elefant", "En elefant kom marcherende", SongCategory.Actions, TRAD, "Gå tungt rundt med en arm som snabel; der kommer en elefant mere for hvert vers."),
    Song("op-lille-hans", "Op, lille Hans", SongCategory.Rhymes, TRAD, "En god vækkesang om morgenen.", link = "https://ugle.dk/op_lille_hans.html"),
    Song("lille-lise", "Lille Lise let-på-tå", SongCategory.Actions, "", "Dans let på tæerne og drej rundt.", link = "https://ugle.dk/lille_lise.html"),
    Song("mariehoenen", "Mariehønen Evigglad", SongCategory.Rhymes, "Kai Normann Andersen", ""),
    Song("to-katte", "Der sad to katte på et bord", SongCategory.Actions, TRAD, "Vis katte med to fingre, der vipper og falder ned.", link = "https://ugle.dk/der_sad_to_katte.html"),
    Song("bro-bro-brille", "Bro, bro, brille", SongCategory.Rhymes, TRAD, "To laver en bro med armene, de andre går under; den sidste fanges.", link = "https://ugle.dk/bro_bro_brille.html"),
    Song("ride-ranke", "Ride, ride ranke", SongCategory.Actions, TRAD, "Barnet sidder på knæet og rides op og ned, og til sidst \"falder\" det forsigtigt.", link = "https://ugle.dk/ride_ride_ranke.html"),
    Song("klappe-kage", "Klappe, klappe kage", SongCategory.Actions, TRAD, "Klap barnets hænder sammen i takt.", link = "https://ugle.dk/klappe_klappe_kage.html"),
    Song("i-en-skov", "I en skov en hytte lå", SongCategory.Actions, TRAD, "Tegn en hytte i luften, kig ud ad vinduet, og bank på døren.", link = "https://ugle.dk/i_en_skov_en_hytte_laa.html"),
    Song("bondegaard", "Jens Hansen havde en bondegård", SongCategory.Actions, TRAD, "Lav dyrelydene, og lad barnet vælge næste dyr."),
    Song("tommelfinger", "Tommelfinger, tommelfinger, hvor er du?", SongCategory.Actions, TRAD, "Skjul hænderne bag ryggen og lad fingrene komme frem en ad gangen."),
    Song("hoved-skulder", "Hoved, skulder, knæ og tå", SongCategory.Actions, TRAD, "Rør ved kropsdelene, mens de nævnes, og syng hurtigere og hurtigere."),
    Song("over-soe-og-land", "Jeg gik mig over sø og land", SongCategory.Actions, TRAD, "Gå i ring og lav bevægelsen fra hvert vers.", link = "https://ugle.dk/jeg_gik_mig_over_soe_og_land.html"),
    Song("fem-smaa-aber", "Fem små aber sprang i sengen", SongCategory.Actions, TRAD, "Tæl ned på fingrene; én abe falder af for hvert vers."),
    Song("elefantens-vuggevise", "Elefantens vuggevise", SongCategory.Bedtime, "", "Vug langsomt fra side til side."),
    Song("solen-er-saa-roed", "Solen er så rød, mor", SongCategory.Bedtime, "", ""),
    Song("sov-soedt-barnlille", "Sov sødt, barnlille", SongCategory.Bedtime, "", "", link = "https://ugle.dk/sov_soedt_barnlille.html"),
    Song("lille-sky", "Lille sky gik morgentur", SongCategory.Rhymes, "", ""),
    Song("mors-lille-ole", "Mors lille Ole", SongCategory.Bedtime, "", ""),
    Song("bag-skaermen", "Hvem sidder der bag skærmen", SongCategory.Rhymes, "Christian Winther", ""),
    Song("hoejt-fra-traeets", "Højt fra træets grønne top", SongCategory.Christmas, "Peter Faber", "Gå om juletræet, mens I synger."),
    Song("tusind-julelys", "Nu tændes tusind julelys", SongCategory.Christmas, "Emmy Köhler", ""),
    // Gamle danske børnesange from ugle.dk (Jørgen Ebert): titles only, each with a link to its page with the words.
    Song("ugle-abc", "ABC-visen", SongCategory.Rhymes, link = "https://ugle.dk/abc.html"),
    Song("ugle-den-lille-ole", "Den lille Ole med paraplyen", SongCategory.Bedtime, link = "https://ugle.dk/den_lille_ole.html"),
    Song("ugle-der-bor-en-bager", "Der bor en bager", SongCategory.Actions, link = "https://ugle.dk/der_bor_en_bager.html"),
    Song("ugle-der-er-ikke-paa-hele-den", "Der er ikke på hele den vide jord", SongCategory.Rhymes, link = "https://ugle.dk/der_er_ikke_paa_hele_den.html"),
    Song("ugle-der-kom-en-pige-gaaende", "Der kom en pige gående", SongCategory.Rhymes, link = "https://ugle.dk/der_kom_en_pige_gaaende.html"),
    Song("ugle-en-jaeger-gik-at-jage", "En jæger gik at jage", SongCategory.Rhymes, link = "https://ugle.dk/en_jaeger_gik_at_jage.html"),
    Song("ugle-en-lille-nisse-rejste", "En lille nisse rejste", SongCategory.Christmas, link = "https://ugle.dk/en_lille_nisse_rejste.html"),
    Song("ugle-en-pige-gik-i-enge", "En pige gik i enge", SongCategory.Rhymes, link = "https://ugle.dk/en_pige_gik_i_enge.html"),
    Song("ugle-fra-engeland-til-skotland", "Fra Engeland til Skotland", SongCategory.Rhymes, link = "https://ugle.dk/fra_engeland_til_skotland.html"),
    Song("ugle-her-er-soen-sikken-en", "Her er soen, sikken en", SongCategory.Actions, link = "https://ugle.dk/her_er_soen_sikken_en.html"),
    Song("ugle-husbonden-sender-lasse-ud", "Husbonden sender Lasse ud", SongCategory.Rhymes, link = "https://ugle.dk/husbonden_sender_lasse_ud.html"),
    Song("ugle-hvad-er-der-i-det-kloster", "Hvad er der i det kloster at se?", SongCategory.Rhymes, link = "https://ugle.dk/hvad_er_der_i_det_kloster.html"),
    Song("ugle-hvilket-postbud-en-due", "Hvilket postbud, en due", SongCategory.Rhymes, link = "https://ugle.dk/hvilket_postbud_en_due.html"),
    Song("ugle-hoejt-paa-en-gren-en-krage", "Højt på en gren en krage", SongCategory.Rhymes, link = "https://ugle.dk/hoejt_paa_en_gren_en_krage.html"),
    Song("ugle-hoene-og-hane", "Høne og hane", SongCategory.Rhymes, link = "https://ugle.dk/hoene_og_hane.html"),
    Song("ugle-i-skoven-skulle-vaere-gilde", "I skoven skulle være gilde", SongCategory.Rhymes, link = "https://ugle.dk/i_skoven_skulle_vaere_gilde.html"),
    Song("ugle-julekalender-paa-vers", "Julekalender på vers", SongCategory.Christmas, link = "https://ugle.dk/julekalender_paa_vers.html"),
    Song("ugle-katten-ligger-under-kakkelovn", "Katten ligger under kakkelovn", SongCategory.Rhymes, link = "https://ugle.dk/katten_ligger_under_kakkelovn.html"),
    Song("ugle-kom-mit-lille-pus-og-se", "Kom mit lille pus og se", SongCategory.Rhymes, link = "https://ugle.dk/kom_mit_lille_pus_og_se.html"),
    Song("ugle-kom-min-lille", "Kom, min lille, nu vi ville", SongCategory.Rhymes, link = "https://ugle.dk/kom_min_lille.html"),
    Song("ugle-langt-ud-i-skoven", "Langt ud' i skoven lå et lille bjerg", SongCategory.Rhymes, link = "https://ugle.dk/langt_ud_i_skoven.html"),
    Song("ugle-lille-foel", "Lille føl", SongCategory.Rhymes, link = "https://ugle.dk/lille_foel.html"),
    Song("ugle-lille-viggo-vil-du-ride-ranke", "Lille Viggo, vil du ride ranke?", SongCategory.Actions, link = "https://ugle.dk/lille_viggo_vil_du_ride_ranke.html"),
    Song("ugle-lotte-gik", "Lotte gik, Lotte gik", SongCategory.Actions, link = "https://ugle.dk/lotte_gik.html"),
    Song("ugle-laer-mig-nattens-stjerne", "Lær mig nattens stjerne", SongCategory.Bedtime, link = "https://ugle.dk/laer_mig_nattens_stjerne.html"),
    Song("ugle-moar-jeg-skal-tisse", "Moar, jeg skal tisse", SongCategory.Rhymes, link = "https://ugle.dk/moar_jeg_skal_tisse.html"),
    Song("ugle-munken-gaar-i-enge", "Munken går i enge", SongCategory.Actions, link = "https://ugle.dk/munken_gaar_i_enge.html"),
    Song("ugle-mae-sir-det-lille-lam", "Mæ, si'r det lille lam", SongCategory.Rhymes, link = "https://ugle.dk/mae_sir_det_lille_lam.html"),
    Song("ugle-nu-lukker-sig-mit-oeje", "Nu lukker sig mit øje", SongCategory.Bedtime, link = "https://ugle.dk/nu_lukker_sig_mit_oeje.html"),
    Song("ugle-naar-kommer-vaaren-vel", "Når kommer våren vel?", SongCategory.Rhymes, link = "https://ugle.dk/naar_kommer_vaaren_vel.html"),
    Song("ugle-offer-og-aedelig", "Offer og ædelig", SongCategory.Rhymes, link = "https://ugle.dk/offer_og_aedelig.html"),
    Song("ugle-og-hoer-lille-mor", "Og hør, lille mo'er", SongCategory.Rhymes, link = "https://ugle.dk/og_hoer_lille_mor.html"),
    Song("ugle-og-raeven-laa-under-birkerod", "Og ræven lå under birkerod", SongCategory.Rhymes, link = "https://ugle.dk/og_raeven_laa_under_birkerod.html"),
    Song("ugle-op-min-lille-ole", "Op, min lille Ole", SongCategory.Rhymes, link = "https://ugle.dk/op_min_lille_ole.html"),
    Song("ugle-pandeben", "Pandeben! Godt det gror!", SongCategory.Actions, link = "https://ugle.dk/pandeben.html"),
    Song("ugle-paa-en-groen-bakketop", "På en grøn bakketop", SongCategory.Rhymes, link = "https://ugle.dk/paa_en_groen_bakketop.html"),
    Song("ugle-rits-rats", "Rits Rats", SongCategory.Rhymes, link = "https://ugle.dk/rits_rats.html"),
    Song("ugle-skaere-skaere-havre", "Skære, skære havre", SongCategory.Actions, link = "https://ugle.dk/skaere_skaere_havre.html"),
    Song("ugle-skoenjomfru-hun-drager", "Skønjomfru hun drager det røde guldbånd", SongCategory.Rhymes, link = "https://ugle.dk/skoenjomfru_hun_drager.html"),
    Song("ugle-sov-soedelig-sov-bloedelig", "Sov sødelig, sov blødelig", SongCategory.Bedtime, link = "https://ugle.dk/sov_soedelig_sov_bloedelig.html"),
    Song("ugle-sov-mit-barn-sov-laenge", "Sov, mit barn, sov længe", SongCategory.Bedtime, link = "https://ugle.dk/sov_mit_barn_sov_laenge.html"),
    Song("ugle-stork-stork-langeben", "Stork, Stork, Langeben!", SongCategory.Rhymes, link = "https://ugle.dk/stork_stork_langeben.html"),
    Song("ugle-syv-arbejdsvante-dvaerge", "Syv arbejdsvante dværge", SongCategory.Rhymes, link = "https://ugle.dk/syv_arbejdsvante_dvaerge.html"),
    Song("ugle-syv-raske-piger", "Syv raske piger i en ring", SongCategory.Actions, link = "https://ugle.dk/syv_raske_piger.html"),
    Song("ugle-soeskende-jeg-kender-fem", "Søskende jeg kender fem", SongCategory.Rhymes, link = "https://ugle.dk/soeskende_jeg_kender_fem.html"),
    Song("ugle-saa-gaar-vi-rundt-om-en-enebaerbusk", "Så går vi rundt om en enebærbusk", SongCategory.Actions, link = "https://ugle.dk/saa_gaar_vi_rundt_om_en_enebaerbusk.html"),
    Song("ugle-tingelingelater", "Tinge-linge-later, tinsoldater", SongCategory.Rhymes, link = "https://ugle.dk/tingelingelater.html"),
    Song("ugle-tordenskjold", "Tordenskjold", SongCategory.Rhymes, link = "https://ugle.dk/tordenskjold.html"),
    Song("ugle-tornerose-var-et-vakkert-barn", "Tornerose var et vakkert barn", SongCategory.Rhymes, link = "https://ugle.dk/tornerose_var_et_vakkert_barn.html"),
    Song("ugle-tre-katte-kroeb-op", "Tre katte krøb op på taget engang", SongCategory.Rhymes, link = "https://ugle.dk/tre_katte_kroeb_op.html"),
    Song("ugle-visselul-mit-lille-barn", "Visselul, mit lille barn", SongCategory.Bedtime, link = "https://ugle.dk/visselul_mit_lille_barn.html"),
)

/** The family's own songbook data: favourites, the words they added, and their own songs. */
@kotlinx.serialization.Serializable
data class SongbookData(
    val favorites: Set<String> = emptySet(),
    val lyrics: Map<String, String> = emptyMap(),
    val ownSongs: List<OwnSong> = emptyList(),
)

@kotlinx.serialization.Serializable
data class OwnSong(val id: String = java.util.UUID.randomUUID().toString(), val title: String, val category: String = SongCategory.Rhymes.name)

/** One song found in an import: [songId] for a song already in the book, otherwise a new own song with [title]. */
data class ImportedSong(val title: String, val songId: String?, val words: String)

private fun normalizeTitle(value: String) = value.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

/**
 * Splits pasted text into songs. A song starts at a line that is the title of a song in the book (spelling and
 * punctuation may differ), or at a line starting with "#" for a new song. The lines below it are its words;
 * text before the first title is ignored. Songs without words are left out.
 */
fun parseLyricsImport(text: String, known: List<Song>): List<ImportedSong> {
    val byTitle = known.associateBy { normalizeTitle(it.title) }
    val result = mutableListOf<ImportedSong>()
    var title: String? = null
    var songId: String? = null
    val lines = mutableListOf<String>()
    fun finish() {
        val words = lines.joinToString("\n").trim('\n', ' ')
        title?.let { if (words.isNotBlank()) result += ImportedSong(it, songId, words) }
        lines.clear()
    }
    text.replace("\r\n", "\n").lines().forEach { line ->
        val trimmed = line.trim()
        val match = byTitle[normalizeTitle(trimmed.removePrefix("#"))]
        when {
            trimmed.isNotEmpty() && match != null -> { finish(); title = match.title; songId = match.id }
            trimmed.startsWith("#") && trimmed.removePrefix("#").isNotBlank() -> { finish(); title = trimmed.removePrefix("#").trim(); songId = null }
            title != null -> lines += line.trimEnd()
        }
    }
    finish()
    return result
}
