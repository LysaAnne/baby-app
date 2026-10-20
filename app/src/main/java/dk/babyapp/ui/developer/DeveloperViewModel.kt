package dk.babyapp.ui.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.babyapp.data.color.ColorProfileRepository
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.profile.CareProviderType
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.FamilyMemberRole
import dk.babyapp.data.profile.ParentProfile
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.ProfileAvatar
import dk.babyapp.data.tracking.ActivityType
import dk.babyapp.data.tracking.BottleContent
import dk.babyapp.data.tracking.BreastSide
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.CareEventType
import dk.babyapp.data.tracking.DiaperColor
import dk.babyapp.data.tracking.DiaperConsistency
import dk.babyapp.data.tracking.DiaperType
import dk.babyapp.data.tracking.MeasurementType
import dk.babyapp.data.tracking.SleepQuality
import dk.babyapp.data.tracking.SleepType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Debug-only tools that fill the app with fictional test data. The buttons are only shown in debug builds. */
@HiltViewModel
class DeveloperViewModel @Inject constructor(
    private val profilesRepository: ChildProfileRepository,
    private val preferencesRepository: AppPreferencesRepository,
    private val parentRepository: ParentProfileRepository,
    private val careEventRepository: CareEventRepository,
    private val colorProfileRepository: ColorProfileRepository,
) : ViewModel() {
    fun createDeveloperTestFamily(onComplete: () -> Unit = {}) = viewModelScope.launch {
        val childId = "developer-test-child-freja"
        val hectorId = "developer-test-child-hector"
        val child = ChildProfile(
            id = childId,
            name = "Freja",
            nickname = "Fre",
            birthStatus = BirthStatus.Born,
            birthDate = LocalDate.now().minusMonths(4),
            birthTime = LocalTime.of(9, 42),
            dueDate = LocalDate.now().minusMonths(4).plusDays(5),
            sex = BiologicalSex.Female,
            birthWeightGrams = 3_480,
            birthLengthCm = 52.0,
            birthHeadCircumferenceCm = 35.0,
            gestationalWeeks = 39,
            gestationalDays = 2,
            cprNumber = "TEST-FREJA",
            fullName = "Freja Testfamilie Jensen",
            registeredAddress = "Testvej 12, 2100 København Ø",
            nationality = "Dansk",
            allergies = "Ingen kendte allergier",
            medicalNotes = "Dette er en testprofil og indeholder ikke rigtige helbredsoplysninger.",
            avatar = ProfileAvatar.Bunny,
            colorTheme = ChildColorTheme.GirlLight.name,
        )
        profilesRepository.save(child)
        profilesRepository.save(
            ChildProfile(
                id = hectorId,
                name = "Hector",
                nickname = "Hec",
                birthStatus = BirthStatus.Born,
                birthDate = LocalDate.now().minusYears(2),
                sex = BiologicalSex.Male,
                cprNumber = "TEST-HECTOR",
                fullName = "Hector Testfamilie Jensen",
                registeredAddress = "Testvej 12, 2100 København Ø",
                nationality = "Dansk",
                avatar = ProfileAvatar.Fox,
                colorTheme = ChildColorTheme.BoyLight.name,
            ),
        )
        profilesRepository.setCareProviders(
            childId,
            listOf(
                CareProvider(id = "developer-provider-hospital", childId = childId, type = CareProviderType.Hospital, name = "Rigshospitalet – test", phone = "+45 35 45 35 45", address = "Blegdamsvej 9, 2100 København Ø", notes = "Testfødested"),
                CareProvider(id = "developer-provider-gp", childId = childId, type = CareProviderType.Gp, name = "Lægehuset Solsikken", phone = "+45 12 34 56 78", email = "test@laegehuset.example", notes = "Fiktiv testkontakt"),
                CareProvider(id = "developer-provider-health", childId = childId, type = CareProviderType.HealthVisitor, name = "Mette Testsen", phone = "+45 22 33 44 55"),
                CareProvider(id = "developer-provider-midwife", childId = childId, type = CareProviderType.Midwife, name = "Anne Jordemoder", phone = "+45 33 44 55 66"),
            ),
        )
        val members = listOf(
            ParentProfile(id = "developer-parent-sofie", name = "Sofie", phone = "+45 20 11 22 33", email = "sofie@example.test", cprNumber = "TEST-SOFIE", avatar = ProfileAvatar.Butterfly, role = FamilyMemberRole.Mother, notes = "Freja og Hectors mor · testprofil"),
            ParentProfile(id = "developer-parent-jonas", name = "Jonas", phone = "+45 21 44 55 66", email = "jonas@example.test", cprNumber = "TEST-JONAS", avatar = ProfileAvatar.Fox, role = FamilyMemberRole.Father, notes = "Freja og Hectors far · testprofil"),
            ParentProfile(id = "developer-grandparent-karen", name = "Karen", phone = "+45 24 55 66 77", email = "karen@example.test", avatar = ProfileAvatar.Panda, role = FamilyMemberRole.Grandmother, notes = "Mormor · testprofil"),
            ParentProfile(id = "developer-grandparent-poul", name = "Poul", phone = "+45 26 77 88 99", email = "poul@example.test", avatar = ProfileAvatar.Lion, role = FamilyMemberRole.Grandfather, notes = "Morfar · testprofil"),
        )
        members.forEach { parentRepository.save(it) }
        parentRepository.setParents(childId, members.map { it.id }.toSet())
        parentRepository.setParents(hectorId, members.map { it.id }.toSet())
        val now = System.currentTimeMillis()
        listOf(
            CareEventEntity(id = "developer-event-breast", childId = childId, type = CareEventType.Breastfeeding, startedAt = now - 3 * 60 * 60 * 1_000, endedAt = now - 2 * 60 * 60 * 1_000 - 42 * 60 * 1_000, activeSide = BreastSide.Right, leftSeconds = 480, rightSeconds = 600, notes = "Rolig amning"),
            dk.babyapp.domain.headSideObservation(childId, dk.babyapp.data.tracking.HeadSide.Left, now - 60 * 60 * 1000).copy(id = "developer-head-left"),
            dk.babyapp.domain.headSideObservation(childId, dk.babyapp.data.tracking.HeadSide.Right, now - 30 * 60 * 1000).copy(id = "developer-head-right"),
            CareEventEntity(id = "developer-event-bottle", childId = childId, type = CareEventType.Bottle, startedAt = now - 90 * 60 * 1_000, endedAt = now - 90 * 60 * 1_000, bottleContent = BottleContent.BreastMilk, amountOfferedMl = 120, amountConsumedMl = 105, notes = "Testregistrering"),
            CareEventEntity(id = "developer-event-diaper", childId = childId, type = CareEventType.Diaper, startedAt = now - 35 * 60 * 1_000, endedAt = now - 35 * 60 * 1_000, diaperType = DiaperType.Both, observation = "Normal", notes = "Testregistrering"),
            CareEventEntity(id = "developer-event-pump", childId = childId, type = CareEventType.Pumping, startedAt = now - 5 * 60 * 60 * 1_000, endedAt = now - 5 * 60 * 60 * 1_000 + 15 * 60 * 1_000, leftSeconds = 900, pumpedAmountMl = 85),
        ).plus(developerFrejaHistory(childId)).forEach { careEventRepository.save(it) }
        preferencesRepository.setActiveChild(childId)
        onComplete()
    }

    private fun developerFrejaHistory(childId: String): List<CareEventEntity> {
        val zone = ZoneId.systemDefault()
        fun timestamp(date: LocalDate, hour: Int, minute: Int = 0): Long =
            date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

        return (1L..60L).flatMap { daysAgo ->
            val date = LocalDate.now().minusDays(daysAgo)
            val variation = (daysAgo % 5).toInt()
            val prefix = "developer-freja-day-$daysAgo"
            val breastMinutes = 14 + variation
            val napMinutes = 55 + variation * 8
            val nightMinutes = 500 + variation * 12
            val tummyMinutes = 8 + (daysAgo % 6).toInt() * 3
            val events = mutableListOf(
                CareEventEntity(id = "$prefix-breast-morning", childId = childId, type = CareEventType.Breastfeeding, startedAt = timestamp(date, 7, 10), endedAt = timestamp(date, 7, 10).plus(breastMinutes * 60_000L), activeSide = if (daysAgo % 2L == 0L) BreastSide.Left else BreastSide.Right, leftSeconds = if (daysAgo % 2L == 0L) breastMinutes * 60L else 0, rightSeconds = if (daysAgo % 2L == 0L) 0 else breastMinutes * 60L),
                CareEventEntity(id = "$prefix-bottle", childId = childId, type = CareEventType.Bottle, startedAt = timestamp(date, 11, 45), endedAt = timestamp(date, 11, 45), bottleContent = BottleContent.BreastMilk, amountOfferedMl = 130, amountConsumedMl = 95 + variation * 5),
                CareEventEntity(id = "$prefix-breast-evening", childId = childId, type = CareEventType.Breastfeeding, startedAt = timestamp(date, 18, 20), endedAt = timestamp(date, 18, 20).plus((breastMinutes + 3) * 60_000L), activeSide = BreastSide.Left, leftSeconds = (breastMinutes + 3) * 60L),
                CareEventEntity(id = "$prefix-nap", childId = childId, type = CareEventType.Sleep, startedAt = timestamp(date, 12, 50), endedAt = timestamp(date, 12, 50).plus(napMinutes * 60_000L), leftSeconds = napMinutes * 60L, sleepType = SleepType.Nap, sleepQuality = if (variation == 0) SleepQuality.Mixed else SleepQuality.Restful),
                CareEventEntity(id = "$prefix-night", childId = childId, type = CareEventType.Sleep, startedAt = timestamp(date, 20, 15), endedAt = timestamp(date, 20, 15).plus(nightMinutes * 60_000L), leftSeconds = nightMinutes * 60L, sleepType = SleepType.Night, awakenings = variation % 3, sleepQuality = if (variation == 4) SleepQuality.Mixed else SleepQuality.Restful),
                CareEventEntity(id = "$prefix-diaper-1", childId = childId, type = CareEventType.Diaper, startedAt = timestamp(date, 8, 5), endedAt = timestamp(date, 8, 5), diaperType = DiaperType.Wet),
                CareEventEntity(id = "$prefix-diaper-2", childId = childId, type = CareEventType.Diaper, startedAt = timestamp(date, 13, 20), endedAt = timestamp(date, 13, 20), diaperType = if (daysAgo % 3L == 0L) DiaperType.Both else DiaperType.Dirty, diaperColor = DiaperColor.Yellow, diaperConsistency = DiaperConsistency.Soft),
                CareEventEntity(id = "$prefix-diaper-3", childId = childId, type = CareEventType.Diaper, startedAt = timestamp(date, 17, 15), endedAt = timestamp(date, 17, 15), diaperType = DiaperType.Wet),
                CareEventEntity(id = "$prefix-diaper-4", childId = childId, type = CareEventType.Diaper, startedAt = timestamp(date, 20, 0), endedAt = timestamp(date, 20, 0), diaperType = DiaperType.Wet),
                CareEventEntity(id = "$prefix-tummy", childId = childId, type = CareEventType.Activity, startedAt = timestamp(date, 15, 10), endedAt = timestamp(date, 15, 10).plus(tummyMinutes * 60_000L), activityType = ActivityType.TummyTime, activityDurationSeconds = tummyMinutes * 60L),
            )
            if (daysAgo % 7L == 0L) {
                events += CareEventEntity(id = "$prefix-weight", childId = childId, type = CareEventType.Measurement, startedAt = timestamp(date, 10), endedAt = timestamp(date, 10), timeSpecified = false, measurementType = MeasurementType.Weight, measurementValue = 6.8 - daysAgo * 0.018, measurementUnit = "kg")
            }
            if (daysAgo % 14L == 0L) {
                events += CareEventEntity(id = "$prefix-height", childId = childId, type = CareEventType.Measurement, startedAt = timestamp(date, 10), endedAt = timestamp(date, 10), timeSpecified = false, measurementType = MeasurementType.Height, measurementValue = 64.0 - daysAgo * 0.055, measurementUnit = "cm")
                events += CareEventEntity(id = "$prefix-head", childId = childId, type = CareEventType.Measurement, startedAt = timestamp(date, 10), endedAt = timestamp(date, 10), timeSpecified = false, measurementType = MeasurementType.HeadCircumference, measurementValue = 41.5 - daysAgo * 0.025, measurementUnit = "cm")
            }
            if (daysAgo % 6L == 0L) {
                events += CareEventEntity(id = "$prefix-temperature", childId = childId, type = CareEventType.Measurement, startedAt = timestamp(date, 16, 30), endedAt = timestamp(date, 16, 30), measurementType = MeasurementType.Temperature, measurementValue = 36.6 + variation * 0.12, measurementUnit = "°C")
            }
            events
        }
    }

    fun createDeveloperPaletteChildren(onComplete: () -> Unit = {}) = viewModelScope.launch {
        val colorProfiles = colorProfileRepository.profiles.first()
        colorProfiles.forEach { profile ->
            profilesRepository.save(
                ChildProfile(
                    id = "developer-palette-${profile.id}",
                    name = profile.name,
                    birthDate = LocalDate.now().minusMonths(1),
                    sex = BiologicalSex.PreferNotToSay,
                    colorTheme = profile.id,
                ),
            )
        }
        colorProfiles.firstOrNull()?.let { preferencesRepository.setActiveChild("developer-palette-${it.id}") }
        onComplete()
    }
}
