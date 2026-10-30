package dk.babyapp.domain

enum class SongCategory(val label: String) { Bedtime("Godnat"), Actions("Fagter"), Rhymes("Rim og lege"), Christmas("Jul") }

/**
 * A song in the songbook. The app has no lyrics; the family can add the words themselves.
 * [origin] is the author or "Traditionel" where that is well known, otherwise empty; [actions] describes
 * the movements in the app's own words.
 */
data class Song(val id: String, val title: String, val category: SongCategory, val origin: String = "", val actions: String = "")

private const val TRAD = "Traditionel"

val songs = listOf(
    Song("mester-jakob", "Mester Jakob", SongCategory.Rhymes, TRAD, "Kan synges i kanon, når barnet bliver større."),
    Song("bjoernen-sover", "Bjørnen sover", SongCategory.Rhymes, TRAD, "En voksen er bjørnen og sover; når den vågner, løber alle væk."),
    Song("lille-peter-edderkop", "Lille Peter Edderkop", SongCategory.Actions, TRAD, "Fingrene kravler op, regnen falder ned, solen bliver en stor cirkel med armene."),
    Song("hjulene-paa-bussen", "Hjulene på bussen", SongCategory.Actions, TRAD, "Rul armene om hinanden, vinduesviskere med hænderne, og find på nye vers."),
    Song("se-min-kjole", "Se min kjole", SongCategory.Rhymes, TRAD, "Peg på tøj i farverne, der synges om."),
    Song("en-elefant", "En elefant kom marcherende", SongCategory.Actions, TRAD, "Gå tungt rundt med en arm som snabel; der kommer en elefant mere for hvert vers."),
    Song("op-lille-hans", "Op, lille Hans", SongCategory.Rhymes, TRAD, "En god vækkesang om morgenen."),
    Song("lille-lise", "Lille Lise let-på-tå", SongCategory.Actions, "", "Dans let på tæerne og drej rundt."),
    Song("mariehoenen", "Mariehønen Evigglad", SongCategory.Rhymes, "Kai Normann Andersen", ""),
    Song("to-katte", "Der sad to katte på et bord", SongCategory.Actions, TRAD, "Vis katte med to fingre, der vipper og falder ned."),
    Song("bro-bro-brille", "Bro, bro, brille", SongCategory.Rhymes, TRAD, "To laver en bro med armene, de andre går under; den sidste fanges."),
    Song("ride-ranke", "Ride, ride ranke", SongCategory.Actions, TRAD, "Barnet sidder på knæet og rides op og ned, og til sidst \"falder\" det forsigtigt."),
    Song("klappe-kage", "Klappe, klappe kage", SongCategory.Actions, TRAD, "Klap barnets hænder sammen i takt."),
    Song("i-en-skov", "I en skov en hytte lå", SongCategory.Actions, TRAD, "Tegn en hytte i luften, kig ud ad vinduet, og bank på døren."),
    Song("bondegaard", "Jens Hansen havde en bondegård", SongCategory.Actions, TRAD, "Lav dyrelydene, og lad barnet vælge næste dyr."),
    Song("tommelfinger", "Tommelfinger, tommelfinger, hvor er du?", SongCategory.Actions, TRAD, "Skjul hænderne bag ryggen og lad fingrene komme frem en ad gangen."),
    Song("hoved-skulder", "Hoved, skulder, knæ og tå", SongCategory.Actions, TRAD, "Rør ved kropsdelene, mens de nævnes, og syng hurtigere og hurtigere."),
    Song("over-soe-og-land", "Jeg gik mig over sø og land", SongCategory.Actions, TRAD, "Gå i ring og lav bevægelsen fra hvert vers."),
    Song("fem-smaa-aber", "Fem små aber sprang i sengen", SongCategory.Actions, TRAD, "Tæl ned på fingrene; én abe falder af for hvert vers."),
    Song("elefantens-vuggevise", "Elefantens vuggevise", SongCategory.Bedtime, "", "Vug langsomt fra side til side."),
    Song("solen-er-saa-roed", "Solen er så rød, mor", SongCategory.Bedtime, "", ""),
    Song("sov-soedt-barnlille", "Sov sødt, barnlille", SongCategory.Bedtime, "", ""),
    Song("lille-sky", "Lille sky gik morgentur", SongCategory.Rhymes, "", ""),
    Song("mors-lille-ole", "Mors lille Ole", SongCategory.Bedtime, "", ""),
    Song("bag-skaermen", "Hvem sidder der bag skærmen", SongCategory.Rhymes, "Christian Winther", ""),
    Song("hoejt-fra-traeets", "Højt fra træets grønne top", SongCategory.Christmas, "Peter Faber", "Gå om juletræet, mens I synger."),
    Song("tusind-julelys", "Nu tændes tusind julelys", SongCategory.Christmas, "Emmy Köhler", ""),
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
