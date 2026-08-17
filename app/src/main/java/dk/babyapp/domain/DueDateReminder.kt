package dk.babyapp.domain

import dk.babyapp.data.profile.BirthStatus
import java.time.LocalDate

fun shouldShowDueDateReminder(
    birthStatus: BirthStatus,
    dueDate: LocalDate?,
    today: LocalDate = LocalDate.now(),
): Boolean = birthStatus == BirthStatus.Expected && dueDate != null && !dueDate.plusDays(14).isAfter(today)
