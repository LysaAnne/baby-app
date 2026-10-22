package dk.babyapp.data.medicine

import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.util.UUID

@Serializable
data class MedicinePlan(
    val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val name: String,
    val dose: String = "",
    val instructions: String = "",
    val asNeeded: Boolean = false,
    val times: List<String> = emptyList(),
    val reminders: Boolean = false,
    val active: Boolean = true,
    /** Repeat every [repeatEvery] days, weeks or months ([repeatUnit]) from [repeatStart]; daily by default. */
    val repeatEvery: Int = 1,
    val repeatUnit: RepeatUnit = RepeatUnit.Day,
    val repeatStart: String? = null,
) {
    fun validTimes(): List<LocalTime> = times.mapNotNull { runCatching { LocalTime.parse(it) }.getOrNull() }.distinct().sorted()

    val isDaily get() = repeatEvery <= 1 && repeatUnit == RepeatUnit.Day

    /** Whether doses are due on [date]; months count from the start date's day (or the month's last day). */
    fun isDueOn(date: java.time.LocalDate): Boolean {
        if (isDaily) return true
        val start = repeatStart?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() } ?: return true
        if (date < start) return false
        val every = repeatEvery.coerceAtLeast(1)
        return when (repeatUnit) {
            RepeatUnit.Day -> java.time.temporal.ChronoUnit.DAYS.between(start, date) % every == 0L
            RepeatUnit.Week -> java.time.temporal.ChronoUnit.DAYS.between(start, date) % (7L * every) == 0L
            RepeatUnit.Month -> {
                val months = java.time.temporal.ChronoUnit.MONTHS.between(start.withDayOfMonth(1), date.withDayOfMonth(1))
                months % every == 0L && date.dayOfMonth == minOf(start.dayOfMonth, date.lengthOfMonth())
            }
        }
    }
}

@Serializable
enum class RepeatUnit(val one: String, val many: String) { Day("dag", "dage"), Week("uge", "uger"), Month("måned", "måneder") }
