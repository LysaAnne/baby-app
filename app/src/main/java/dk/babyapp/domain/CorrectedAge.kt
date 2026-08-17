package dk.babyapp.domain

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

data class ChildAge(
    val chronological: Period,
    val corrected: Period?,
    val prematurityDays: Long,
)

fun calculateChildAge(
    birthDate: LocalDate,
    dueDate: LocalDate?,
    onDate: LocalDate = LocalDate.now(),
): ChildAge {
    val chronological = Period.between(birthDate, onDate.coerceAtLeast(birthDate))
    val prematurityDays = if (dueDate != null && dueDate.isAfter(birthDate)) {
        ChronoUnit.DAYS.between(birthDate, dueDate)
    } else {
        0
    }
    val correctedStart = birthDate.plusDays(prematurityDays)
    val corrected = when {
        prematurityDays <= 0 -> null
        onDate.isBefore(correctedStart) -> Period.ZERO
        !onDate.isBefore(correctedStart.plusYears(2)) -> null
        else -> Period.between(correctedStart, onDate)
    }
    return ChildAge(chronological, corrected, prematurityDays)
}

val Period.totalMonths: Int
    get() = years * 12 + months
