package dk.babyapp.domain

import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.MeasurementType
import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GrowthReferenceTest {
    private fun table(type: String, sex: String) = GrowthReference.parse(File("src/main/assets/growth/who-$type-$sex.csv").readText())

    @Test fun lmsReproducesThePublishedWhoPercentiles() {
        val rows = javaClass.classLoader!!.getResource("who-published-percentiles.csv")!!.readText().lines().filter { it.isNotBlank() && !it.startsWith("#") }.drop(1)
        assertEquals(60, rows.size)
        val tables = mutableMapOf<String, GrowthReference>()
        rows.forEach { row ->
            val cells = row.split(',')
            val (type, sex, day) = cells
            val (p3, p50, p97) = cells.drop(3)
            val lms = tables.getOrPut("$type-$sex") { table(type, sex) }.at(day.toLong())!!
            listOf(3 to p3, 50 to p50, 97 to p97).forEach { (percentile, published) ->
                assertEquals("$type $sex day $day p$percentile", published.toDouble(), lms.valueAt(normalQuantile(percentile.toDouble()))!!, 0.005)
            }
        }
    }

    @Test fun everyBundledTableCoversBirthToTwoYears() {
        listOf("weight", "length", "head").forEach { type -> listOf("girls", "boys").forEach { sex -> assertEquals(730, table(type, sex).lastDay) } }
        assertEquals("who-length-girls.csv", MeasurementType.Height.growthFile(GrowthSex.Girl))
        assertNull(MeasurementType.Temperature.growthFile(GrowthSex.Boy))
    }

    @Test fun zScoreAndCurveValueAreInverseAndRejectInvalidInput() {
        val lms = Lms(0.3809, 3.2322, 0.14171)
        assertEquals(0.0, lms.zScore(3.2322)!!, 1e-9)
        listOf(-2.5, -1.0, 0.7, 2.2).forEach { z -> assertEquals(z, lms.zScore(lms.valueAt(z)!!)!!, 1e-9) }
        val logNormal = Lms(0.0, 10.0, 0.1)
        assertEquals(1.0, logNormal.zScore(10.0 * kotlin.math.exp(0.1))!!, 1e-9)
        assertNull(lms.zScore(0.0)); assertNull(lms.zScore(-1.0)); assertNull(lms.zScore(Double.NaN))
        assertNull(Lms(1.0, 5.0, 0.1).valueAt(-11.0))
    }

    @Test fun percentilesAreRoundedAndExtremesAreNotGivenFalsePrecision() {
        assertEquals("p50", percentileLabel(0.0))
        assertEquals("p97", percentileLabel(normalQuantile(97.0)))
        assertEquals("under p1", percentileLabel(-2.6))
        assertEquals("over p99", percentileLabel(2.6))
        assertEquals(0.975, normalCdf(1.959964), 1e-6)
    }

    @Test fun sameCurveComparisonMatchesThePlansSyntheticCheck() {
        val reference = GrowthReference((0..40).map { Lms(1.0, if (it < 30) 5.0 else 6.0, 0.1) })
        val birth = LocalDate.of(2026, 1, 1)
        val summary = growthSummary(listOf(GrowthMeasurement(birth.plusDays(14), 5.0), GrowthMeasurement(birth.plusDays(30), 5.7)), reference, birth)!!
        assertEquals(0.0, summary.firstZ!!, 1e-9)
        assertEquals(-0.5, summary.latestZ - summary.firstZ!!, 1e-9)
        assertEquals(1.0, summary.sameCurveGain!!, 1e-9)
        assertEquals(0.7, summary.measuredGain!!, 1e-9)
    }

    @Test fun firstTwoWeeksAreLeftOutOnePointGivesNoTrendAndTheLastOfADayCounts() {
        val reference = GrowthReference((0..40).map { Lms(1.0, 5.0, 0.1) })
        val birth = LocalDate.of(2026, 1, 1)
        val one = growthSummary(listOf(GrowthMeasurement(birth, 3.0), GrowthMeasurement(birth.plusDays(20), 5.0)), reference, birth)!!
        assertEquals(birth.plusDays(20), one.latest.date)
        assertNull(one.first); assertNull(one.measuredGain)
        val sameDay = growthSummary(listOf(GrowthMeasurement(birth.plusDays(20), 4.0), GrowthMeasurement(birth.plusDays(20), 5.5)), reference, birth)!!
        assertEquals(5.5, sameDay.latest.value, 1e-9)
        assertNull(growthSummary(listOf(GrowthMeasurement(birth.plusDays(41), 5.0)), reference, birth))
    }

    @Test fun curvesNeedSexAndBirthDateAndWaitForPrematureChildren() {
        val child = ChildProfile(name = "Freja", birthDate = LocalDate.of(2026, 6, 21), sex = BiologicalSex.Female)
        assertEquals(GrowthAvailability.Ready(GrowthSex.Girl, LocalDate.of(2026, 6, 21)), child.growthAvailability())
        assertEquals(GrowthAvailability.MissingSex, child.copy(sex = BiologicalSex.Unselected).growthAvailability())
        assertEquals(GrowthAvailability.MissingSex, child.copy(sex = BiologicalSex.Other).growthAvailability())
        assertEquals(GrowthAvailability.MissingBirthDate, child.copy(birthStatus = BirthStatus.Expected).growthAvailability())
        assertEquals(GrowthAvailability.Premature(34), child.copy(gestationalWeeks = 34).growthAvailability())
        assertEquals(GrowthAvailability.Ready(GrowthSex.Girl, LocalDate.of(2026, 6, 21)), child.copy(gestationalWeeks = 38).growthAvailability())
    }
}
