package dk.babyapp.data.book

import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BabyBookTest {
    @Test fun templatesCoverTwelveMonthsAndHaveStableUniqueIds() {
        assertEquals(12, babyBookTemplates.count { it.id.startsWith("month-") })
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
        assertTrue(mother.prompts.size >= 5)
        assertNotEquals(mother.prompts, name.prompts)
        assertFalse("Fortæl historien" in mother.prompts)
    }

}
