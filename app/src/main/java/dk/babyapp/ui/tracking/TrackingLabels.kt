package dk.babyapp.ui.tracking

import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.HealthRecordStatus
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.BreastfeedingIssue
import dk.babyapp.data.tracking.DiaperColor
import dk.babyapp.data.tracking.DiaperConsistency
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.ActivityType

internal fun CareEventType.displayLabel() = when (this) {
    CareEventType.Breastfeeding -> "Amning"
    CareEventType.Bottle -> "Flaske"
    CareEventType.Pumping -> "Pumpning"
    CareEventType.Diaper -> "Ble"
    CareEventType.Sleep -> "Søvn"
    CareEventType.Measurement -> "Mål"
    CareEventType.Activity -> "Diverse"
    CareEventType.HealthVisit -> "Sundhedsbesøg"
    CareEventType.Vaccination -> "Vaccination"
}

internal fun BreastfeedingIssue.displayLabel() = when (this) {
    BreastfeedingIssue.PainfulLatch -> "Smerter ved sutteteknik"
    BreastfeedingIssue.SoreNipples -> "Ømme brystvorter"
    BreastfeedingIssue.CrackedNipples -> "Revner eller sår"
    BreastfeedingIssue.Engorgement -> "Brystspænding"
    BreastfeedingIssue.BlockedDuct -> "Tilstoppet mælkegang"
    BreastfeedingIssue.MastitisSymptoms -> "Tegn på brystbetændelse"
    BreastfeedingIssue.Other -> "Andet"
}

internal fun DiaperColor.displayLabel() = when (this) { DiaperColor.Yellow -> "Gul"; DiaperColor.Brown -> "Brun"; DiaperColor.Green -> "Grøn"; DiaperColor.Black -> "Sort"; DiaperColor.Red -> "Rød"; DiaperColor.White -> "Hvid eller grå"; DiaperColor.Other -> "Anden" }
internal fun DiaperConsistency.displayLabel() = when (this) { DiaperConsistency.Watery -> "Vandig"; DiaperConsistency.Loose -> "Løs"; DiaperConsistency.Soft -> "Blød"; DiaperConsistency.Formed -> "Formet"; DiaperConsistency.Hard -> "Hård"; DiaperConsistency.Mucous -> "Slimet" }
internal fun MeasurementType.displayLabel() = when (this) { MeasurementType.Weight -> "Vægt"; MeasurementType.Height -> "Højde/længde"; MeasurementType.HeadCircumference -> "Hovedomkreds"; MeasurementType.Temperature -> "Temperatur" }
internal fun MeasurementType.defaultUnit() = when (this) { MeasurementType.Weight -> "kg"; MeasurementType.Height, MeasurementType.HeadCircumference -> "cm"; MeasurementType.Temperature -> "°C" }
internal fun ActivityType.displayLabel() = when (this) { ActivityType.TummyTime -> "Mavetid"; ActivityType.Bath -> "Bad"; ActivityType.OutdoorTime -> "Udetid"; ActivityType.Play -> "Leg"; ActivityType.Medicine -> "Medicin"; ActivityType.Other -> "Andet" }

internal fun DiaperType?.displayLabel() = when (this) {
    DiaperType.Wet -> "Våd"
    DiaperType.Dirty -> "Afføring"
    DiaperType.Both -> "Begge"
    DiaperType.Dry -> "Tør"
    null -> "Ble"
}

internal fun BottleContent?.displayLabel() = when (this) {
    BottleContent.BreastMilk -> "Modermælk"
    BottleContent.Formula -> "Modermælkserstatning"
    BottleContent.Water -> "Vand"
    BottleContent.Other -> "Andet"
    null -> "Ikke angivet"
}

internal fun SleepQuality.displayLabel() = when (this) {
    SleepQuality.Restful -> "Rolig"
    SleepQuality.Mixed -> "Blandet"
    SleepQuality.Restless -> "Urolig"
}

internal fun HealthRecordStatus.displayLabel() = when (this) {
    HealthRecordStatus.Scheduled -> "Planlagt"
    HealthRecordStatus.Completed -> "Gennemført"
    HealthRecordStatus.Postponed -> "Udsat"
    HealthRecordStatus.Cancelled -> "Aflyst"
    HealthRecordStatus.Declined -> "Fravalgt"
}

internal fun HealthVisitType.displayLabel() = when (this) {
    HealthVisitType.PreventiveExam -> "Forebyggende børneundersøgelse"
    HealthVisitType.GpVisit -> "Egen læge"
    HealthVisitType.HealthVisitor -> "Sundhedsplejerske"
    HealthVisitType.Midwife -> "Jordemoder"
    HealthVisitType.Hospital -> "Hospital eller ambulatorium"
    HealthVisitType.Specialist -> "Speciallæge"
    HealthVisitType.Dental -> "Tandpleje"
    HealthVisitType.Other -> "Andet"
}
