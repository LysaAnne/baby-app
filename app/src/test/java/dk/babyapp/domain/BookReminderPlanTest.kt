package dk.babyapp.domain

import dk.babyapp.data.book.*
import dk.babyapp.data.profile.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class BookReminderPlanTest {
    private val birth = LocalDate.of(2026, 1, 31)
    private val child = ChildProfile(id = "child", name = "Freja", birthDate = birth)
    private fun page(id: String) = babyBookTemplates.first { it.id == id }
    @Test fun monthlyAndBirthdayPagesWaitForCalendarDate() {
        assertFalse(bookPageAvailable(page("month-1"), child, LocalDate.of(2026, 2, 27)))
        assertTrue(bookPageAvailable(page("month-1"), child, LocalDate.of(2026, 2, 28)))
        assertFalse(bookPageAvailable(page("month-2"), child, LocalDate.of(2026, 3, 30)))
        assertTrue(bookPageAvailable(page("month-2"), child, LocalDate.of(2026, 3, 31)))
        val birthdayPages = babyBookTemplates.filter { it.chapter == "Din første fødselsdag" || it.title == "Første fødselsdag" || it.title == "Dit første år i billeder" }
        birthdayPages.forEach {
            assertFalse(bookPageAvailable(it, child, birth.plusYears(1).minusDays(1)))
            assertTrue(bookPageAvailable(it, child, birth.plusYears(1)))
        }
    }
    @Test fun holidaysAndPregnancyRespectDates() {
        assertFalse(bookPageAvailable(page("first-15"), child, LocalDate.of(2026, 12, 23)))
        assertTrue(bookPageAvailable(page("first-15"), child, LocalDate.of(2026, 12, 24)))
        assertFalse(bookPageAvailable(page("first-16"), child, LocalDate.of(2026, 12, 31)))
        assertTrue(bookPageAvailable(page("first-16"), child, LocalDate.of(2027, 1, 1)))
        assertEquals(LocalDate.of(2026, 4, 5), easterSunday(2026))
        assertFalse(bookPageAvailable(page("first-17"), child, LocalDate.of(2026, 4, 4)))
        assertTrue(bookPageAvailable(page("first-17"), child, LocalDate.of(2026, 4, 5)))
        val expected = child.copy(birthStatus = BirthStatus.Expected, dueDate = birth.plusYears(1))
        assertFalse(bookPageAvailable(page("birth-facts"), expected, birth))
        assertTrue(bookPageAvailable(page("before-0"), expected, birth))
        assertFalse(bookPageAvailable(page("before-12"), expected, birth))
    }
    @Test fun scheduleUsesThreeWeekdaysAndAdditionalMonthlyAnniversary() {
        val zone = ZoneId.of("Europe/Copenhagen")
        // Saturday February 28 is the one-month anniversary, before Monday's general prompt.
        val fridayEvening = LocalDate.of(2026, 2, 27).atTime(21, 0).atZone(zone)
        assertEquals(LocalDate.of(2026, 2, 28).atTime(20, 0).atZone(zone), nextBookReminderAt(listOf(child), fridayEvening))
        assertEquals(DayOfWeek.MONDAY, nextBookReminderAt(listOf(child), fridayEvening.plusDays(1))?.dayOfWeek)
        assertNull(nextBookReminderAt(emptyList(), fridayEvening))
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), bookReminderWeekdays)
    }
    @Test fun blankPagesAreEligibleButTextOrPhotosCountAsFilled() {
        assertFalse(bookPageHasContent(null))
        assertFalse(bookPageHasContent(BabyBookPage("child", "cover", "{\"Barnets navn\":\"  \"}")))
        assertTrue(bookPageHasContent(BabyBookPage("child", "cover", "{\"Barnets navn\":\"Freja\"}")))
        assertTrue(bookPageHasContent(BabyBookPage("child", "cover", photosJson = "[\"photo.jpg\"]")))
    }
}
