package dk.babyapp.domain

import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.MeasurementType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class GrowthSex(val file: String) { Girl("girls"), Boy("boys") }

/** The bundled WHO tables for a measurement, or null when there is none (temperature). */
fun MeasurementType.growthFile(sex: GrowthSex): String? = when (this) {
    MeasurementType.Weight -> "who-weight-${sex.file}.csv"
    MeasurementType.Height -> "who-length-${sex.file}.csv"
    MeasurementType.HeadCircumference -> "who-head-${sex.file}.csv"
    MeasurementType.Temperature -> null
}

/** L, M and S for one day of age; see docs/GROWTH-REFERENCE-PLAN.md for the formulas. */
data class Lms(val l: Double, val m: Double, val s: Double) {
    fun zScore(x: Double): Double? {
        if (!x.isFinite() || x <= 0 || m <= 0 || s <= 0) return null
        val z = if (l == 0.0) ln(x / m) / s else ((x / m).pow(l) - 1) / (l * s)
        return z.takeIf { it.isFinite() }
    }

    fun valueAt(z: Double): Double? {
        if (l == 0.0) return m * exp(s * z)
        val base = 1 + l * s * z
        if (base <= 0) return null
        return (m * base.pow(1 / l)).takeIf { it.isFinite() }
    }
}

/** A WHO table with one row per day of age, from birth. */
class GrowthReference(private val days: List<Lms>) {
    val lastDay get() = days.lastIndex
    fun at(day: Long): Lms? = if (day in 0..lastDay) days[day.toInt()] else null

    companion object {
        fun parse(text: String): GrowthReference {
            val rows = text.lineSequence().map(String::trim).filter { it.isNotEmpty() && !it.startsWith("#") }.map { line ->
                val (day, l, m, s) = line.split(',')
                day.toInt() to Lms(l.toDouble(), m.toDouble(), s.toDouble())
            }.toList()
            require(rows.isNotEmpty() && rows.withIndex().all { (i, row) -> row.first == i }) { "Growth table must list every day from 0" }
            return GrowthReference(rows.map { it.second })
        }
    }
}

/** The percentile lines drawn behind the child's curve. */
val growthPercentiles = listOf(3, 10, 25, 50, 75, 90, 97)

fun normalCdf(z: Double): Double {
    // Abramowitz and Stegun 7.1.26 via erf; accurate to about 1e-7, far finer than whole percentiles.
    val t = 1 / (1 + 0.3275911 * abs(z) / sqrt(2.0))
    val poly = t * (0.254829592 + t * (-0.284496736 + t * (1.421413741 + t * (-1.453152027 + t * 1.061405429))))
    val erf = 1 - poly * exp(-z * z / 2)
    return if (z >= 0) (1 + erf) / 2 else (1 - erf) / 2
}

/** The z-score for a percentile, by bisection on [normalCdf]. */
fun normalQuantile(percentile: Double): Double {
    require(percentile > 0 && percentile < 100)
    var low = -8.0; var high = 8.0
    repeat(80) { val mid = (low + high) / 2; if (normalCdf(mid) * 100 < percentile) low = mid else high = mid }
    return (low + high) / 2
}

/** Whole percentiles between 1 and 99; outside that only "under p1" or "over p99", to avoid false precision. */
fun percentileLabel(z: Double): String {
    val percentile = normalCdf(z) * 100
    return when {
        percentile < 1 -> "under p1"
        percentile > 99 -> "over p99"
        else -> "p${percentile.roundToInt().coerceIn(1, 99)}"
    }
}

sealed interface GrowthAvailability {
    data class Ready(val sex: GrowthSex, val birth: LocalDate) : GrowthAvailability
    data object MissingSex : GrowthAvailability
    data object MissingBirthDate : GrowthAvailability
    data class Premature(val weeks: Int) : GrowthAvailability
}

/** Never guesses sex from name or colours; premature children wait until corrected age is handled. */
fun ChildProfile.growthAvailability(): GrowthAvailability {
    val sex = when (sex) { BiologicalSex.Female -> GrowthSex.Girl; BiologicalSex.Male -> GrowthSex.Boy; else -> return GrowthAvailability.MissingSex }
    val birth = birthDate?.takeIf { birthStatus == BirthStatus.Born } ?: return GrowthAvailability.MissingBirthDate
    gestationalWeeks?.takeIf { it < 37 }?.let { return GrowthAvailability.Premature(it) }
    return GrowthAvailability.Ready(sex, birth)
}

/** Percentiles are not shown for the first two weeks, while babies regain their birth weight. */
const val GROWTH_FIRST_DAY = 14L

fun ageDays(birth: LocalDate, date: LocalDate) = ChronoUnit.DAYS.between(birth, date)

data class GrowthMeasurement(val date: LocalDate, val value: Double)

data class GrowthSummary(
    val latest: GrowthMeasurement,
    val latestZ: Double,
    /** Null when there is only one usable measurement: one point shows placement, not development. */
    val first: GrowthMeasurement?,
    val firstZ: Double?,
    val measuredGain: Double?,
    /** The gain that would have kept the first measurement's percentile. A comparison, not a target. */
    val sameCurveGain: Double?,
)

/**
 * Compares the first and last usable measurements on different dates. Measurements are usable from day 14
 * to the end of the table. Several on the same day: the last one counts.
 */
fun growthSummary(measurements: List<GrowthMeasurement>, reference: GrowthReference, birth: LocalDate): GrowthSummary? {
    val usable = measurements.filter { ageDays(birth, it.date) in GROWTH_FIRST_DAY..reference.lastDay.toLong() }
        .groupBy { it.date }.map { (_, sameDay) -> sameDay.last() }.sortedBy { it.date }
    val latest = usable.lastOrNull() ?: return null
    val latestLms = reference.at(ageDays(birth, latest.date)) ?: return null
    val latestZ = latestLms.zScore(latest.value) ?: return null
    val first = usable.firstOrNull()?.takeIf { it.date != latest.date }
    val firstZ = first?.let { reference.at(ageDays(birth, it.date))?.zScore(it.value) }
    if (first == null || firstZ == null) return GrowthSummary(latest, latestZ, null, null, null, null)
    val sameCurve = latestLms.valueAt(firstZ)
    return GrowthSummary(latest, latestZ, first, firstZ, latest.value - first.value, sameCurve?.minus(first.value))
}
