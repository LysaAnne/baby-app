package dk.babyapp.ui.tracking

import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.HealthRecordStatus
import dk.babyapp.data.tracking.HealthVisitType
import dk.babyapp.data.tracking.SleepQuality

internal fun CareEventType.displayLabel() = when (this) {
    CareEventType.Breastfeeding -> "Amning"
    CareEventType.Bottle -> "Flaske"
    CareEventType.Pumping -> "Pumpning"
    CareEventType.Diaper -> "Ble"
    CareEventType.Sleep -> "Søvn"
    CareEventType.HealthVisit -> "Sundhedsbesøg"
    CareEventType.Vaccination -> "Vaccination"
}

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
