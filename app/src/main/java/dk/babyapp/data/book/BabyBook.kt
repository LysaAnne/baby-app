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
) {
    fun answers(): Map<String, String> = Json.decodeFromString(answersJson)
    fun photos(): List<String> = Json.decodeFromString(photosJson)
}

@Dao
interface BabyBookDao {
    @Query("SELECT * FROM baby_book_pages") fun observeAll(): Flow<List<BabyBookPage>>
    @Upsert suspend fun save(page: BabyBookPage)
}

class BabyBookRepository @Inject constructor(database: AppDatabase) {
    private val dao = database.babyBookDao()
    val pages = dao.observeAll()
    suspend fun save(page: BabyBookPage) = dao.save(page.copy(updatedAt = System.currentTimeMillis()))
}

data class BookPageTemplate(val id: String, val chapter: String, val title: String, val prompts: List<String>, val retired: Boolean = false)

private fun pages(chapter: String, prefix: String, titles: String) = titles.split('|').mapIndexed { i, title ->
    BookPageTemplate("$prefix-$i", chapter, title, bookStoryPrompts(title, chapter), retired = title in retiredBookPageTitles)
}

val babyBookTemplates: List<BookPageTemplate> = listOf(
    BookPageTemplate("cover", "Før du kom", "Forside", listOf("Barnets navn", "En hilsen til dig")),
) + pages("Før du kom", "before", "Om mor|Om far|Vores familie|Sådan mødtes vi|Da vi fandt ud af, at du var på vej|Graviditeten|Første scanning|Vi tror, du bliver…|Dit navn|Babyshower|Dit værelse og dine første ting|Beskeder til dig før fødslen|Den sidste tid i maven") + listOf(
    BookPageTemplate("birth-facts", "Fødslen", "Dagen du blev født", listOf("Dato", "Klokkeslæt", "Fødested", "Graviditetsuge", "Vægt (g)", "Længde (cm)", "Hovedomfang (cm)")),
) + pages("Fødslen", "birth", "Sådan begyndte fødslen|Historien om din fødsel|Det første øjeblik med dig|Dit allerførste billede|Din første dag|Hvem var der?|På hospitalet|Da vi tog dig med hjem|Dit hospitalsarmbånd|Din fødselsannonce") +
    pages("Den første tid hjemme", "home", "Dit hjem|Dit værelse|Din første seng|Din første nat hjemme|Det første bad|Den første gåtur|Første tur i barnevognen|Første besøg|Mennesker, der kom for at møde dig|Dine bedsteforældre|Dine første venner|Dig og kæledyrene|Sådan var du som nyfødt|Det kunne du lide|Det kunne du ikke lide|Det fik dig til at falde til ro|De første ting, der fik dig til at smile") +
    (1..12).map { month -> BookPageTemplate("month-$month", "Måned for måned", "$month ${if (month == 1) "måned" else "måneder"}", listOf("Vægt (kg)", "Længde (cm)", "Det kan jeg nu", "Jeg elsker", "Et særligt minde")) } +
    pages("De første gange", "first", "Første smil|Første rigtige grin|Du holder hovedet|Du ruller|Du sidder|Du kravler|Du rejser dig|Første skridt|Første tand|Første ord|Første faste mad|Du drikker af kop|Første svømmetur|Første ferie|Første overnatning væk hjemmefra|Første jul|Første nytår|Første påske|Første fødselsdag") +
    pages("Det særlige ved dig", "personal", "Dine mange ansigtsudtryk|Sådan ser du ud, når du sover|Ting du gør, som får os til at grine|Dine kælenavne|Sange vi synger for dig|Dine yndlingsbøger|Dit yndlingslegetøj|Ting du er bange for|De mærkeligste ting du elsker|Ord og lyde du siger|Din personlighed|Dig og mor|Dig og far|Dig og dine særlige mennesker|Familieligheder|Hvem synes familien, du ligner?|Dit første år i billeder|Dine små hænder og fødder|En hårlok|Små ting vi aldrig vil glemme") + listOf(
    BookPageTemplate("world", "Verden omkring dig", "Verden da du blev født", listOf("Statsminister og årets store nyheder", "Musik, film og serier alle talte om", "Hvad ting kostede, fx mælk og benzin", "Sådan levede vi: hjem, arbejde og telefoner")),
    BookPageTemplate("birthday", "Din første fødselsdag", "Du er 1 år!", listOf("Vægt (kg)", "Højde (cm)", "Det kan du nu", "Fødselsdagsfesten", "Gaver og kage")),
) + pages("Din første fødselsdag", "year", "12 ting vi elsker ved dig|Det har du lært os|Et brev fra mor|Et brev fra far|Håb og ønsker for dit næste år")

fun exportBabyBook(name: String, pages: List<BabyBookPage>): String = buildString {
    appendLine("Barnets bog – $name"); appendLine("Mit første år"); appendLine()
    babyBookTemplates.groupBy { it.chapter }.forEach { (chapter, templates) ->
        appendLine("# $chapter")
        templates.forEach { template ->
            val page = pages.firstOrNull { it.pageId == template.id }
            if (template.retired && (page == null || (page.answers().values.none(String::isNotBlank) && page.photos().isEmpty()))) return@forEach
            appendLine(); appendLine("## ${template.title}")
            val answers = page?.answers().orEmpty()
            (template.prompts + answers.keys).distinct().forEach { prompt ->
                answers[prompt]?.takeIf(String::isNotBlank)?.let { appendLine("$prompt:"); appendLine(it); appendLine() }
            }
            if (answers.values.none(String::isNotBlank) && page?.photos().isNullOrEmpty()) appendLine("Ikke udfyldt endnu")
            page?.photos()?.forEach { appendLine("Billede: billeder/$it") }
        }
        appendLine()
    }
}
