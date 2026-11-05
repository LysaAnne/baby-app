package dk.babyapp.data.book

import androidx.room.*
import dk.babyapp.data.AppDatabase
import dk.babyapp.data.profile.ChildProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Entity(tableName = "baby_book_pages", primaryKeys = ["childId", "pageId"],
    foreignKeys = [ForeignKey(entity = ChildProfileEntity::class, parentColumns = ["id"], childColumns = ["childId"], onDelete = ForeignKey.CASCADE)])
data class BabyBookPage(
    val childId: String,
    val pageId: String,
    val answersJson: String = "{}",
    val photosJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis(),
    /** Questions left out of this child's book ("Spring over"); their text, if any, is kept but not exported. */
    @ColumnInfo(defaultValue = "[]") val skippedJson: String = "[]",
    /** Questions marked "Følg op på": something more is to be written later. */
    @ColumnInfo(defaultValue = "[]") val followUpJson: String = "[]",
    /** A page the family added themselves: its title, chapter and questions. Blank title for the book's own pages. */
    @ColumnInfo(defaultValue = "''") val customTitle: String = "",
    @ColumnInfo(defaultValue = "''") val customChapter: String = "",
    @ColumnInfo(defaultValue = "[]") val customPromptsJson: String = "[]",
) {
    fun answers(): Map<String, String> = Json.decodeFromString(answersJson)
    fun photos(): List<String> = Json.decodeFromString(photosJson)
    fun skipped(): Set<String> = Json.decodeFromString<List<String>>(skippedJson).toSet()
    fun followUps(): Set<String> = Json.decodeFromString<List<String>>(followUpJson).toSet()
    val isCustom get() = pageId.startsWith(CUSTOM_PAGE_PREFIX)

    /** The template of a page the family added; one box named after the page when no questions are given. */
    fun customTemplate(): BookPageTemplate = BookPageTemplate(pageId, customChapter.ifBlank { OWN_PAGES_CHAPTER }, customTitle.ifBlank { "Egen side" },
        Json.decodeFromString<List<String>>(customPromptsJson).filter(String::isNotBlank).ifEmpty { listOf(customTitle.ifBlank { "Egen side" }) }, custom = true)
}

const val CUSTOM_PAGE_PREFIX = "custom-"
const val OWN_PAGES_CHAPTER = "Egne sider"

@Dao
interface BabyBookDao {
    @Query("SELECT * FROM baby_book_pages") fun observeAll(): Flow<List<BabyBookPage>>
    @Upsert suspend fun save(page: BabyBookPage)
    @Query("DELETE FROM baby_book_pages WHERE childId = :childId AND pageId = :pageId") suspend fun delete(childId: String, pageId: String)
}

class BabyBookRepository @Inject constructor(database: AppDatabase) {
    private val dao = database.babyBookDao()
    val pages = dao.observeAll()
    suspend fun save(page: BabyBookPage) = dao.save(page.copy(updatedAt = System.currentTimeMillis()))
    /** Only for a page the family added themselves, after they confirmed it. */
    suspend fun deleteCustomPage(page: BabyBookPage) { require(page.isCustom); dao.delete(page.childId, page.pageId) }
}

data class BookPageTemplate(val id: String, val chapter: String, val title: String, val prompts: List<String>, val retired: Boolean = false, val custom: Boolean = false)

/** The book's pages followed by the pages this child's family added, each placed last in its chapter. */
fun bookTemplatesFor(childId: String?, pages: List<BabyBookPage>): List<BookPageTemplate> {
    val custom = pages.filter { it.childId == childId && it.isCustom }.sortedBy { it.pageId }.map { it.customTemplate() }
    val chapters = (babyBookTemplates.map { it.chapter } + custom.map { it.chapter }).distinct()
    return chapters.flatMap { chapter -> babyBookTemplates.filter { it.chapter == chapter } + custom.filter { it.chapter == chapter } }
}

/** Questions on [template] as they show for [page]: the template's, then any older question with text under it. */
fun BookPageTemplate.questionsFor(page: BabyBookPage?): List<String> =
    (prompts + normalizeBookAnswers(prompts, page?.answers().orEmpty()).filterValues(String::isNotBlank).keys).distinct()

private fun pages(chapter: String, prefix: String, titles: String) = titles.split('|').mapIndexed { i, title ->
    // A retired page only shows the questions something is already written under.
    val retired = title in retiredBookPageTitles
    BookPageTemplate("$prefix-$i", chapter, title, if (retired) emptyList() else bookStoryPrompts(title, chapter), retired = retired)
}

val babyBookTemplates: List<BookPageTemplate> = listOf(
    BookPageTemplate("cover", "Før du kom", "Forside", listOf("Barnets fulde navn", "En hilsen til dig", "Hvem har lavet denne bog til dig?")),
) + pages("Før du kom", "before", "Om mor|Om far|Vores familie|Dine forældre|Da vi fandt ud af, at du var på vej|Graviditeten|Første scanning|Vi tror, du bliver…|Dit navn|Babyshower|Dit værelse og dine første ting|Beskeder til dig før fødslen|Den sidste tid i maven") + listOf(
    BookPageTemplate("birth-facts", "Fødslen", "Dagen du blev født", listOf("Dato", "Klokkeslæt", "Fødested", "Graviditetsuge", "Vægt (g)", "Længde (cm)", "Hovedomfang (cm)")),
) + pages("Fødslen", "birth", "Sådan begyndte fødslen|Historien om din fødsel|Det første øjeblik med dig|Dit allerførste billede|Din første dag|Hvem var der?|På hospitalet|Da vi tog dig med hjem|Dit hospitalsarmbånd|Din fødselsannonce") +
    pages("Den første tid hjemme", "home", "Dit hjem|Dit værelse|Din første seng|Din første nat hjemme|Det første bad|Den første gåtur|Første tur i barnevognen|Første besøg|Mennesker, der kom for at møde dig|Dine bedsteforældre|Dine første venner|Dig og kæledyrene|Sådan var du som nyfødt|Det kunne du lide|Det kunne du ikke lide|Det fik dig til at falde til ro|De første ting, der fik dig til at smile") +
    // Month 0 is the birth itself, so the series starts where the child did.
    listOf(BookPageTemplate("month-0", "Måned for måned", "Ved fødslen", monthBookPrompts(0))) +
    (1..12).map { month -> BookPageTemplate("month-$month", "Måned for måned", "$month ${if (month == 1) "måned" else "måneder"}", monthBookPrompts(month)) } +
    pages("De første gange", "first", "Første smil|Første rigtige grin|Du holder hovedet|Du ruller|Du sidder|Du kravler|Du rejser dig|Første skridt|Første tand|Første ord|Første faste mad|Du drikker af kop|Første svømmetur|Første ferie|Første overnatning væk hjemmefra|Første jul|Første nytår|Første påske|Første fødselsdag") +
    pages("Det særlige ved dig", "personal", "Hvilke sjove ansigtsudtryk lavede du?|Sådan sover du|Ting du gør, som får os til at grine|Dine kælenavne|Sange vi synger for dig|Dine yndlingsbøger|Dit yndlingslegetøj|Ting du er bange for|Ting du elsker|Ord og lyde du siger|Din personlighed|Det elsker du at lave med mor|Det elsker du at lave med far|Dig og dine særlige mennesker|Familieligheder|Hvem synes familien, du ligner?|Dit første år i billeder|Dine små hænder og fødder|En hårlok|Små ting vi aldrig vil glemme") + listOf(
    BookPageTemplate("world", "Verden omkring dig", "Verden da du blev født", listOf("Statsminister og årets store nyheder", "Musik, film og serier, alle talte om",
        "Hvad ting kostede, fx mælk, benzin og et hus", "Sådan levede vi: hjem, arbejde og telefoner", "Årets hit, der kørte i radioen", "Den app eller det gadget, alle brugte",
        "Hvad gik folk med (mode og frisurer)?", "Årets mest populære babynavne", "Sportsbegivenheder det år", "Vejret den dag, du blev født",
        "Det, folk snakkede om ved middagsbordet", "Ting, der findes nu, som måske er væk, når du læser det her", "Hvad vi tror, verden ser ud som, når du er 18",
        "Det, vi var bekymrede for i verden", "Det, vi var glade for og håbede på")),
    BookPageTemplate("birthday", "Din første fødselsdag", "Du er 1 år!", listOf("Vægt (kg)", "Højde (cm)", "Det kan du nu", "Fødselsdagsfesten", "Gaver og kage",
        "Det, du siger og forstår nu", "Tøj- og skostørrelse", "Dit yndlingsmåltid lige nu")),
) + pages("Din første fødselsdag", "year", "12 ting vi elsker ved dig|Det har du lært os|Et brev fra mor|Et brev fra far|Håb og ønsker for dit næste år")

fun exportBabyBook(name: String, pages: List<BabyBookPage>): String = buildString {
    appendLine("Barnets bog – $name"); appendLine("Mit første år"); appendLine()
    bookTemplatesFor(pages.firstOrNull()?.childId, pages).groupBy { it.chapter }.forEach { (chapter, templates) ->
        appendLine("# $chapter")
        templates.forEach { template ->
            val page = pages.firstOrNull { it.pageId == template.id }
            if (template.retired && (page == null || (page.answers().values.none(String::isNotBlank) && page.photos().isEmpty()))) return@forEach
            appendLine(); appendLine("## ${template.title}")
            val answers = normalizeBookAnswers(template.prompts, page?.answers().orEmpty())
            val skipped = page?.skipped().orEmpty()
            (template.prompts + answers.keys).distinct().filter { it !in skipped }.forEach { prompt ->
                answers[prompt]?.takeIf(String::isNotBlank)?.let { appendLine("$prompt:"); appendLine(it); appendLine() }
            }
            if (answers.values.none(String::isNotBlank) && page?.photos().isNullOrEmpty()) appendLine("Ikke udfyldt endnu")
            page?.photos()?.forEach { appendLine("Billede: billeder/$it") }
        }
        appendLine()
    }
}
