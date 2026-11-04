package dk.babyapp.reminders

import dk.babyapp.data.preferences.CustomReminder
import dk.babyapp.domain.leaps
import dk.babyapp.domain.plusWeekFraction
import dk.babyapp.ui.settings.reminderSummary
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomRemindersTest {
    private val zone = ZoneId.of("Europe/Copenhagen")

    @Test fun nextTimeIsTheNextChosenWeekdayAtTheTime() {
        val weekdays = CustomReminder(title = "Mavetid", days = (1..5).toSet(), hour = 10, minute = 0)
        // Friday 30 October 2026 at 11: today's time has passed, so Monday 2 November.
        val friday = ZonedDateTime.of(2026, 10, 30, 11, 0, 0, 0, zone)
        assertEquals(ZonedDateTime.of(2026, 11, 2, 10, 0, 0, 0, zone), CustomReminderScheduler.nextTime(weekdays, friday))
        assertEquals(ZonedDateTime.of(2026, 10, 30, 10, 0, 0, 0, zone), CustomReminderScheduler.nextTime(weekdays, friday.withHour(9)))
        assertNull(CustomReminderScheduler.nextTime(weekdays.copy(days = emptySet()), friday))
    }

    @Test fun summariesReadNaturally() {
        assertEquals("Hverdage · 10.00", reminderSummary(CustomReminder(title = "x", days = (1..5).toSet(), hour = 10, minute = 0)))
        assertEquals("Man, ons · 8.05", reminderSummary(CustomReminder(title = "x", days = setOf(3, 1), hour = 8, minute = 5)))
    }

    @Test fun developmentNewsComesOnTheFirstDayOfASpurt() {
        val due = LocalDate.of(2026, 6, 1)
        val firstDay = due.plusWeekFraction(leaps[3].fussyFromWeek)
        assertTrue(DevelopmentReminderScheduler.newsFor(due, firstDay).any { it.startsWith("Spring 4") })
        assertTrue(DevelopmentReminderScheduler.newsFor(due, firstDay.plusDays(1)).none { it.startsWith("Spring") })
    }

    @Test fun registeringAReminderSavesTheMatchingActivity() {
        assertEquals(dk.babyapp.data.tracking.ActivityType.Bath to "", reminderActivity("Bad"))
        assertEquals(dk.babyapp.data.tracking.ActivityType.Bath to "", reminderActivity("Badedag for Freja"))
        assertEquals(dk.babyapp.data.tracking.ActivityType.TummyTime to "", reminderActivity("Husk mavetid"))
        assertEquals(dk.babyapp.data.tracking.ActivityType.Other to "Klip negle", reminderActivity(" Klip negle "))
    }
}
