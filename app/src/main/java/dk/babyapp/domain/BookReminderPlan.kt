package dk.babyapp.domain

import dk.babyapp.data.book.*
import dk.babyapp.data.profile.*
import java.time.*

val bookReminderWeekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)

fun bookPageHasContent(page: BabyBookPage?) = page != null &&
    (page.answers().values.any(String::isNotBlank) || page.photos().isNotEmpty())

fun bookPageAvailable(template: BookPageTemplate, child: ChildProfile, today: LocalDate): Boolean {
    if (template.chapter == "Før du kom") {
        return template.id != "before-12" || child.birthStatus == BirthStatus.Born || child.dueDate?.minusWeeks(4)?.let { today >= it } == true
    }
    val birth = child.birthDate?.takeIf { child.birthStatus == BirthStatus.Born && it <= today } ?: return false
    template.id.removePrefix("month-").toIntOrNull()?.takeIf { template.id.startsWith("month-") }?.let { return today >= birth.plusMonths(it.toLong()) }
    if (template.chapter == "Din første fødselsdag" || template.title == "Første fødselsdag" || template.title == "Dit første år i billeder") return today >= birth.plusYears(1)
    val holiday = when (template.title) {
        "Første jul" -> MonthDay.of(12, 24)
        "Første nytår" -> MonthDay.of(1, 1)
        else -> null
    }
    if (holiday != null) {
        val first = holiday.atYear(birth.year).let { if (it < birth) it.plusYears(1) else it }
        return today >= first
    }
    if (template.title == "Første påske") {
        val first = easterSunday(birth.year).let { if (it < birth) easterSunday(birth.year + 1) else it }
        return today >= first
    }
    return true
}

// Gregorian Easter, used only to avoid suggesting the first Easter before it occurs.
internal fun easterSunday(year: Int): LocalDate {
    val a = year % 19; val b = year / 100; val c = year % 100
    val d = b / 4; val e = b % 4; val f = (b + 8) / 25; val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30; val i = c / 4; val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7; val m = (a + 11 * h + 22 * l) / 451
    return LocalDate.of(year, (h + l - 7 * m + 114) / 31, (h + l - 7 * m + 114) % 31 + 1)
}

fun isBookReminderDay(date: LocalDate, frequency: dk.babyapp.data.preferences.BookReminderFrequency): Boolean = when (frequency) {
    dk.babyapp.data.preferences.BookReminderFrequency.Daily -> true
    dk.babyapp.data.preferences.BookReminderFrequency.ThreeWeekly -> date.dayOfWeek in bookReminderWeekdays
    dk.babyapp.data.preferences.BookReminderFrequency.Weekly -> date.dayOfWeek == DayOfWeek.MONDAY
    dk.babyapp.data.preferences.BookReminderFrequency.Monthly -> date.dayOfMonth == 1
}

fun nextBookReminderAt(children: List<ChildProfile>, now: ZonedDateTime, frequency: dk.babyapp.data.preferences.BookReminderFrequency = dk.babyapp.data.preferences.BookReminderFrequency.ThreeWeekly): ZonedDateTime? {
    if (children.isEmpty()) return null
    return (0L..32L).map { now.toLocalDate().plusDays(it).atTime(20, 0).atZone(now.zone) }
        .first { it > now && isBookReminderDay(it.toLocalDate(), frequency) }
}
