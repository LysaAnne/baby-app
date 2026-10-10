package dk.babyapp.domain

import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun ChildProfile.ageLabel(today: LocalDate = LocalDate.now()): String {
    if (birthStatus == BirthStatus.Expected) return "Ikke født endnu"
    val birth = birthDate ?: return "Alder ikke angivet"
    if (birth.isAfter(today)) return "Alder ikke angivet"
    val days = ChronoUnit.DAYS.between(birth, today)
    fun unit(value: Long, singular: String, plural: String) = "$value ${if (value == 1L) singular else plural}"
    if (days < 14) return unit(days, "dag", "dage")
    if (today.isBefore(birth.plusMonths(3))) return listOfNotNull(
        unit(days / 7, "uge", "uger"), (days % 7).takeIf { it > 0 }?.let { unit(it, "dag", "dage") },
    ).joinToString(" og ")
    var months = ChronoUnit.MONTHS.between(birth, today)
    if (!today.isBefore(birth.plusMonths(months + 1))) months++
    val remainder = ChronoUnit.DAYS.between(birth.plusMonths(months), today)
    return buildList {
        if (months >= 12) add("${months / 12} år")
        if (months % 12 > 0) add(unit(months % 12, "måned", "måneder"))
        if (months < 24 && remainder >= 7) add(unit(remainder / 7, "uge", "uger"))
        val rest = if (months < 24) remainder % 7 else remainder
        if (rest > 0) add(unit(rest, "dag", "dage"))
    }.joinToString(", ")
}
