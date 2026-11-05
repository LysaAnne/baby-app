package dk.babyapp.data.book

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BabyBookTest {
    @Test fun templatesCoverBirthAndTwelveMonthsAndHaveStableUniqueIds() {
        assertEquals((0..12).map { "month-$it" }, babyBookTemplates.filter { it.id.startsWith("month-") }.map { it.id })
        assertEquals(babyBookTemplates.size, babyBookTemplates.map { it.id }.distinct().size)
    }
    @Test fun exportPreservesMultilineDanishTextAndPhotoReferences() {
        val text = "Kære dig\nÆble, øjne og håb ❤️"
        val page = BabyBookPage("a", "cover", Json.encodeToString(mapOf("En hilsen til dig" to text)), Json.encodeToString(listOf("foto.jpg")))
        val output = exportBabyBook("Freja", listOf(page))
        assertTrue(output.contains(text))
        assertTrue(output.contains("billeder/foto.jpg"))
        assertTrue(output.contains("12 måneder"))
        assertTrue(output.contains("Din første fødselsdag"))
    }
    @Test fun oldStoryTextRemainsInExportAfterAddingSpecificPrompts() {
        val page = BabyBookPage("a", "before-0", Json.encodeToString(mapOf("Fortæl historien" to "Mors gamle historie")))
        assertTrue(exportBabyBook("Freja", listOf(page)).contains("Mors gamle historie"))
        val mother = babyBookTemplates.first { it.id == "before-0" }
        val name = babyBookTemplates.first { it.title == "Dit navn" }
        // The questions Anne chose for the page.
        assertEquals("Mors fulde navn, og hvor hun er vokset op", mother.prompts.first())
        assertNotEquals(mother.prompts, name.prompts)
        assertFalse("Fortæl historien" in mother.prompts)
    }

    @Test fun rewordedQuestionsKeepTheirAnswersAndRetiredPagesShowOnlyWhatIsWritten() {
        val cover = babyBookTemplates.first()
        assertEquals(mapOf("Barnets fulde navn" to "Freja"), normalizeBookAnswers(cover.prompts, mapOf("Barnets navn" to "Freja")))
        // A new answer wins; the old one is kept as it is.
        assertEquals(mapOf("Barnets navn" to "Gammel", "Barnets fulde navn" to "Ny"), normalizeBookAnswers(cover.prompts, mapOf("Barnets navn" to "Gammel", "Barnets fulde navn" to "Ny")))
        val retired = babyBookTemplates.first { it.title == "En hårlok" }
        assertTrue(retired.retired)
        assertEquals(emptyList<String>(), retired.prompts)
        assertEquals(listOf("Hvem klippede?"), retired.questionsFor(BabyBookPage("a", retired.id, Json.encodeToString(mapOf("Hvem klippede?" to "Mormor")))))
        assertEquals("Dine forældre", babyBookTemplates.first { it.id == "before-3" }.title)
    }

    @Test fun ownPagesJoinTheirChapterAndExportWithoutSkippedQuestions() {
        val own = BabyBookPage("a", "custom-1", Json.encodeToString(mapOf("Hvem var med?" to "Mormor", "Hvad spiste vi?" to "Kage")),
            skippedJson = Json.encodeToString(listOf("Hvad spiste vi?")), customTitle = "Første tur til Skagen", customChapter = "De første gange",
            customPromptsJson = Json.encodeToString(listOf("Hvem var med?", "Hvad spiste vi?")))
        val templates = bookTemplatesFor("a", listOf(own))
        val firsts = templates.filter { it.chapter == "De første gange" }
        assertEquals("custom-1", firsts.last().id)
        assertEquals(babyBookTemplates.size + 1, templates.size)
        val export = exportBabyBook("Freja", listOf(own))
        assertTrue(export.contains("## Første tur til Skagen"))
        assertTrue(export.contains("Mormor"))
        assertFalse(export.contains("Kage"))
        assertEquals(listOf("Min side"), own.copy(customTitle = "Min side", customPromptsJson = "[]").customTemplate().prompts)
    }
}
