package dk.babyapp.ui.tracking

import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.BreastfeedingIssue
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperColor
import dk.babyapp.data.tracking.DiaperConsistency
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.HeadSide
import dk.babyapp.data.tracking.HealthRecordStatus
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks down how stored registrations are shown, including shapes that can no longer be created
 * (timed pumping, nipple shield, sleep without type, medicine without a medicine card). A refactor
 * that changes any of these texts fails here. Deliberate changes: run with UPDATE_GOLDEN=1 and
 * review the diff of legacy-event-display.txt.
 */
class LegacyEventDisplayTest {
    @Test fun storedRegistrationsAreShownAsBefore() {
        val actual = buildString {
            events.forEach { (name, event) ->
                appendLine("## $name")
                appendLine("title: ${eventTitle(event)}")
                appendLine("summary: ${event.summary()}")
                appendLine("details: ${event.details()}")
            }
            appendLine("## day summary")
            daySummaryRows(events.map { it.second }).forEach { row -> appendLine("${if (row.subordinate) "  " else ""}${row.label} | ${row.count} | ${row.details}") }
        }
        val golden = File("src/test/resources/legacy-event-display.txt")
        if (System.getenv("UPDATE_GOLDEN") == "1") golden.apply { parentFile.mkdirs() }.writeText(actual)
        assertEquals(golden.readText(), actual)
    }

    private val base = CareEventEntity(id = "id", childId = "child", type = CareEventType.Diaper, startedAt = 1_760_000_000_000, endedAt = 1_760_000_000_000)
    private val start = base.startedAt

    private val events: List<Pair<String, CareEventEntity>> = listOf(
        "nursing with sides, shield and issue" to base.copy(
            type = CareEventType.Breastfeeding, endedAt = start + 900_000, leftSeconds = 540, rightSeconds = 360, activeSide = BreastSide.Right,
            nippleShield = true, breastfeedingIssue = BreastfeedingIssue.SoreNipples, notes = "Rolig",
            timerSegments = "$start-${start + 540_000};${start + 540_000}-${start + 900_000}", timerSegmentSides = "Left;Right", timerSegmentShields = "true;false",
        ),
        "nursing before side segments existed" to base.copy(type = CareEventType.Breastfeeding, endedAt = start + 600_000, leftSeconds = 600),
        "bottle mixed with ratio" to base.copy(type = CareEventType.Bottle, amountOfferedMl = 120, amountConsumedMl = 90, bottleContent = BottleContent.Mixed, bottleMixRatio = "1:1", notes = "Bøvs"),
        "bottle without amounts" to base.copy(type = CareEventType.Bottle),
        "pumping with old timer" to base.copy(type = CareEventType.Pumping, endedAt = start + 1_200_000, leftSeconds = 1_200, pumpedAmountMl = 80, pumpingMethod = "Maskine"),
        "pumping without amount" to base.copy(type = CareEventType.Pumping),
        "diaper full" to base.copy(diaperType = DiaperType.Both, diaperColor = DiaperColor.Yellow, diaperConsistency = DiaperConsistency.Soft, observation = "Udslæt", notes = "Creme"),
        "diaper without type" to base,
        "night sleep full" to base.copy(type = CareEventType.Sleep, endedAt = start + 3_600_000, leftSeconds = 3_600, sleepType = SleepType.Night, sleepLocation = "Vugge", settlingMethod = "Vugget", awakenings = 2, sleepQuality = SleepQuality.Restful),
        "sleep before sleep types" to base.copy(type = CareEventType.Sleep, endedAt = start + 1_800_000, leftSeconds = 1_800),
        "weight in kg" to base.copy(type = CareEventType.Measurement, measurementType = MeasurementType.Weight, measurementValue = 5.2, measurementUnit = "kg"),
        "weight in gram" to base.copy(type = CareEventType.Measurement, measurementType = MeasurementType.Weight, measurementValue = 5200.0, measurementUnit = "g"),
        "temperature without time" to base.copy(type = CareEventType.Measurement, measurementType = MeasurementType.Temperature, measurementValue = 37.4, measurementUnit = "°C", timeSpecified = false),
        "tummy time" to base.copy(type = CareEventType.Activity, activityType = ActivityType.TummyTime, activityDurationSeconds = 300),
        "activity before activity types" to base.copy(type = CareEventType.Activity),
        "medicine without card" to base.copy(type = CareEventType.Activity, activityType = ActivityType.Medicine, medicationName = "Panodil", medicationDose = "2,5 ml"),
        "medicine blank" to base.copy(type = CareEventType.Activity, activityType = ActivityType.Medicine),
        "health visit" to base.copy(type = CareEventType.HealthVisit, healthVisitType = HealthVisitType.HealthVisitor, healthStatus = HealthRecordStatus.Completed, providerDisplayName = "Sundhedsplejerske", healthTitle = "4-månedersbesøg", healthReason = "Rutine", timeSpecified = false),
        "vaccination" to base.copy(type = CareEventType.Vaccination, healthStatus = HealthRecordStatus.Scheduled, vaccineName = "DiTeKiPol/Hib", vaccineDose = "1. dosis", officialScheduleKey = "vaccine-3m", timeSpecified = false),
        "solid food" to base.copy(type = CareEventType.SolidFood, foodName = "Gulerod", foodTexture = "Mos", foodAmount = "2 skefulde", foodReaction = "Glad"),
        "favourite side" to base.copy(type = CareEventType.HeadSide, headSide = HeadSide.Left),
        "favourite side unknown" to base.copy(type = CareEventType.HeadSide),
    )
}
