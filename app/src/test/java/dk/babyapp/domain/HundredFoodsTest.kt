package dk.babyapp.domain

import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HundredFoodsTest {
    @Test fun theListHasAHundredDifferentFoodsWithAllergensMarked() {
        assertEquals(100, hundredFoods.size)
        assertEquals(100, hundredFoods.map { it.name }.distinct().size)
        assertTrue(Allergen.entries.all { allergen -> hundredFoods.any { it.allergen == allergen } })
    }

    @Test fun registrationsCountByTheStartOfTheirWords() {
        fun meal(name: String, at: Long) = CareEventEntity(childId = "a", type = CareEventType.SolidFood, startedAt = at, endedAt = at, foodName = name)
        val events = listOf(meal("Gulerodsmos", 1), meal("gulerødder og æble", 2), meal("Hytteost", 3), meal("Æggekage", 4),
            meal("Broccoli", 5).copy(childId = "b"), meal("Laks", 6).copy(deletedAt = 7))
        val progress = hundredFoodsProgress(events, "a").mapKeys { it.key.name }
        assertEquals(FoodTasting(1, 2), progress["Gulerod"])
        assertEquals(1, progress.getValue("Æble").count)
        assertEquals(1, progress.getValue("Hytteost").count)
        assertEquals(1, progress.getValue("Æg").count)
        // "Hytteost" is not Ost, another child's meal and a deleted one do not count.
        assertFalse("Ost" in progress); assertFalse("Broccoli" in progress); assertFalse("Laks" in progress)
    }
}
