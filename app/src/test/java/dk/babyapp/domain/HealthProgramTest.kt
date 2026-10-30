package dk.babyapp.domain

import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.HealthRecordStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthProgramTest {
    private val birth = LocalDate.of(2026, 6, 21)
    private val child = ChildProfile(id = "c", name = "Freja", birthDate = birth)
    private fun record(key: String, status: HealthRecordStatus?) = CareEventEntity(childId = "c", type = if (key.startsWith("vaccine")) CareEventType.Vaccination else CareEventType.HealthVisit,
        startedAt = 0, endedAt = 0, officialScheduleKey = key, healthStatus = status)

    @Test fun upcomingListsDueItemsAndLeavesOutDoneOnes() {
        val today = LocalDate.of(2026, 9, 15) // 3 months is 21 Sep, 5 weeks was 26 Jul
        val keys = upcomingProgramme(child, emptyList(), today).map { it.template.key }
        assertEquals(listOf("exam-5-weeks", "visitor-2-months", "vaccine-dtkphib-1", "vaccine-pneumococcal-1"), keys)
        val done = upcomingProgramme(child, listOf(record("exam-5-weeks", HealthRecordStatus.Completed), record("vaccine-dtkphib-1", HealthRecordStatus.Declined)), today)
        assertEquals(listOf("visitor-2-months", "vaccine-pneumococcal-1"), done.map { it.template.key })
        val planned = upcomingProgramme(child, listOf(record("vaccine-pneumococcal-1", HealthRecordStatus.Scheduled)), today)
        assertTrue(planned.first { it.template.key == "vaccine-pneumococcal-1" }.scheduled != null)
    }

    @Test fun remindersComeAWeekBeforeAndOnTheDay() {
        assertEquals(2, programmeRemindersFor(child, emptyList(), LocalDate.of(2026, 9, 14)).size)
        assertEquals(2, programmeRemindersFor(child, emptyList(), LocalDate.of(2026, 9, 21)).size)
        assertEquals(0, programmeRemindersFor(child, emptyList(), LocalDate.of(2026, 9, 16)).size)
        assertEquals(0, programmeRemindersFor(child.copy(birthStatus = BirthStatus.Expected), emptyList(), LocalDate.of(2026, 9, 14)).size)
    }

    @Test fun nextVisitIsShownHoweverFarAheadAndSkipsVaccines() {
        val today = LocalDate.of(2026, 9, 15)
        // An overdue visit that is not done comes first.
        assertEquals("exam-5-weeks", nextVisit(child, emptyList(), today)?.template?.key)
        val visits = listOf("exam-5-weeks", "visitor-postnatal", "visitor-establishing", "visitor-2-months").map { record(it, HealthRecordStatus.Completed) }
        // The 5-month examination is more than 30 days away but is still the next one.
        assertEquals("exam-5-months", nextVisit(child, visits, today)?.template?.key)
    }

    @Test fun dueLabelsReadNaturally() {
        val today = LocalDate.of(2026, 9, 15)
        assertEquals("i dag", dueLabel(today, today)); assertEquals("om 9 dage", dueLabel(today.plusDays(9), today)); assertEquals("for 3 dage siden", dueLabel(today.minusDays(3), today))
    }
}
