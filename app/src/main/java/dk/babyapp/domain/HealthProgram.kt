package dk.babyapp.domain

import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DanishHealthTemplate
import dk.babyapp.data.tracking.HealthRecordStatus
import dk.babyapp.data.tracking.danishPreventiveExaminationTemplates
import dk.babyapp.data.tracking.danishVaccinationTemplates
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/** When each item in the Danish child health programme is due, counted from birth. */
private val programmeAges: Map<String, Period> = mapOf(
    "exam-5-weeks" to Period.ofWeeks(5), "exam-5-months" to Period.ofMonths(5), "exam-12-months" to Period.ofMonths(12),
    "exam-2-years" to Period.ofYears(2), "exam-3-years" to Period.ofYears(3), "exam-4-years" to Period.ofYears(4), "exam-5-years" to Period.ofYears(5),
    "vaccine-dtkphib-1" to Period.ofMonths(3), "vaccine-pneumococcal-1" to Period.ofMonths(3),
    "vaccine-dtkphib-2" to Period.ofMonths(5), "vaccine-pneumococcal-2" to Period.ofMonths(5),
    "vaccine-dtkphib-3" to Period.ofMonths(12), "vaccine-pneumococcal-3" to Period.ofMonths(12),
    "vaccine-mmr-1" to Period.ofMonths(15), "vaccine-mmr-2" to Period.ofYears(4),
    "vaccine-dtkp-booster" to Period.ofYears(5), "vaccine-hpv-1" to Period.ofYears(12), "vaccine-hpv-2" to Period.of(12, 6, 0),
)

data class ProgrammeItem(val template: DanishHealthTemplate, val vaccination: Boolean, val due: LocalDate, val scheduled: CareEventEntity?)

/**
 * Programme items that are not done yet, from those overdue by up to [overdueDays] to those due within [aheadDays].
 * Done means a registration for that item that is completed or declined; a planned one is returned with it.
 */
fun upcomingProgramme(child: ChildProfile, events: List<CareEventEntity>, today: LocalDate, aheadDays: Long = 30, overdueDays: Long = 60): List<ProgrammeItem> {
    val birth = child.birthDate?.takeIf { child.birthStatus == BirthStatus.Born } ?: return emptyList()
    val records = events.filter { it.childId == child.id && it.deletedAt == null && !it.isDraft && it.officialScheduleKey != null &&
        (it.type == CareEventType.HealthVisit || it.type == CareEventType.Vaccination) }
    val all = danishPreventiveExaminationTemplates.map { it to false } + danishVaccinationTemplates.map { it to true }
    return all.mapNotNull { (template, vaccination) ->
        val due = programmeAges[template.key]?.let { birth.plus(it) } ?: return@mapNotNull null
        val mine = records.filter { it.officialScheduleKey == template.key }
        if (mine.any { it.healthStatus == HealthRecordStatus.Completed || it.healthStatus == HealthRecordStatus.Declined || it.healthStatus == null }) return@mapNotNull null
        if (ChronoUnit.DAYS.between(today, due) !in -overdueDays..aheadDays) return@mapNotNull null
        ProgrammeItem(template, vaccination, due, mine.filter { it.healthStatus == HealthRecordStatus.Scheduled }.maxByOrNull { it.startedAt })
    }.sortedBy { it.due }
}

/** Items to remind about today: due in a week, or due today, and not done. */
fun programmeRemindersFor(child: ChildProfile, events: List<CareEventEntity>, today: LocalDate): List<ProgrammeItem> =
    upcomingProgramme(child, events, today, aheadDays = 7, overdueDays = 0).filter { it.due == today || it.due == today.plusDays(7) }

fun dueLabel(due: LocalDate, today: LocalDate): String {
    val days = ChronoUnit.DAYS.between(today, due)
    return when {
        days == 0L -> "i dag"
        days == 1L -> "i morgen"
        days > 1 -> "om $days dage"
        days == -1L -> "i går"
        else -> "for ${-days} dage siden"
    }
}
