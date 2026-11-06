package dk.babyapp.domain

import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Where one item under Udvikling stands for the child's age and what has been ticked off. Every tab sorts its
 * items the same way: [Now], [Overdue] and [DoneNow] under "Lige nu", [Future] under "Fremtidige", and [DonePast]
 * folded away under "Afsluttede".
 */
enum class DevState { Now, Overdue, DoneNow, DonePast, Future }

/** The four areas under Udvikling that have something to tick off. */
enum class DevArea { Leaps, Milestones, Psychology, Experiments }

/** How much of what belongs to the child's age right now has been done. */
data class DevProgress(val done: Int, val total: Int) {
    val complete get() = done >= total
}

private fun state(started: Boolean, ended: Boolean, complete: Boolean) = when {
    !started -> DevState.Future
    !ended -> if (complete) DevState.DoneNow else DevState.Now
    else -> if (complete) DevState.DonePast else DevState.Overdue
}

/** Lige nu: under way or left over from earlier, done or not. */
val DevState.isCurrent get() = this == DevState.Now || this == DevState.Overdue || this == DevState.DoneNow

fun monthsFromWeeks(weeks: Double) = weeks * 7 / 30.44

/** The keys of a spurt's games and skills, which can be ticked off. */
fun Leap.tickKeys() = play.indices.map { key("play", it) } + skills.indices.map { key("skill", it) }

fun Leap.state(weeks: Double, done: Set<String>) = state(weeks >= fussyFromWeek, weeks >= calmFromWeek, tickKeys().all { it in done })

val Milestone.tickKey get() = "milestone-$key"
fun Milestone.state(months: Double, done: Set<String>) = state(months >= fromMonths, months > toMonths, tickKey in done)

fun PsychPhenomenon.state(months: Double, done: Set<String>) = state(months >= fromMonths, toMonths != null && months > toMonths, key in done)

fun HomeExperiment.state(months: Double, done: Set<String>) = state(months >= fromMonths, months > toMonths, key in done)

/**
 * Done out of all for what is under "Lige nu" in [area]: the games and skills of the current and left-over spurts,
 * or the milestones, phenomena or experiments that are current or left over from earlier.
 */
fun developmentProgress(area: DevArea, weeks: Double, done: Set<String>): DevProgress {
    val months = monthsFromWeeks(weeks)
    return when (area) {
        DevArea.Leaps -> leaps.filter { it.state(weeks, done).isCurrent }.flatMap { it.tickKeys() }.let { keys -> DevProgress(keys.count { it in done }, keys.size) }
        DevArea.Milestones -> milestones.filter { it.state(months, done).isCurrent }.let { list -> DevProgress(list.count { it.tickKey in done }, list.size) }
        DevArea.Psychology -> psychTimeline.filter { it.state(months, done).isCurrent }.let { list -> DevProgress(list.count { it.key in done }, list.size) }
        DevArea.Experiments -> experimentTimeline.filter { it.state(months, done).isCurrent }.let { list -> DevProgress(list.count { it.key in done }, list.size) }
    }
}

/** A milestone's window in whole months, "4–9 mdr". */
fun Milestone.wholeMonthWindow() = "${fromMonths.roundToInt()}–${toMonths.roundToInt()} mdr"

/** An age in months spelt out: "3 måneder og 3 uger", "9 måneder", "1 uge". */
fun monthsAndWeeks(months: Double): String {
    var whole = floor(months).toInt()
    var weeks = ((months - whole) * 30.44 / 7).roundToInt()
    if (weeks >= 4) { whole += 1; weeks = 0 }
    val parts = listOfNotNull(
        whole.takeIf { it > 0 }?.let { if (it == 1) "1 måned" else "$it måneder" },
        weeks.takeIf { it > 0 }?.let { if (it == 1) "1 uge" else "$it uger" },
    )
    return parts.joinToString(" og ").ifEmpty { "0 måneder" }
}
