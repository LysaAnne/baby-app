package dk.babyapp.ui.profile

import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.ProfileAvatar
import dk.babyapp.data.profile.ChildColorTheme
import dk.babyapp.data.profile.CareProvider
import dk.babyapp.data.preferences.MeasurementUnits
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.LocalTime

data class ProfileDraft(
    val id: String? = null,
    val name: String = "",
    val nickname: String = "",
    val birthStatus: BirthStatus = BirthStatus.Born,
    val birthDate: String = "",
    val birthTime: String = "",
    val dueDate: String = "",
    val sex: BiologicalSex = BiologicalSex.Unselected,
    val birthWeightGrams: String = "",
    val birthLengthCm: String = "",
    val birthHeadCircumferenceCm: String = "",
    val gestationalWeeks: String = "",
    val gestationalDays: String = "",
    val hospital: String = "",
    val hospitalContact: String = "",
    val hospitalEmail: String = "", val hospitalAddress: String = "", val hospitalNotes: String = "",
    val gp: String = "",
    val gpContact: String = "",
    val gpEmail: String = "", val gpAddress: String = "", val gpNotes: String = "",
    val healthVisitor: String = "",
    val healthVisitorContact: String = "",
    val healthVisitorEmail: String = "", val healthVisitorAddress: String = "", val healthVisitorNotes: String = "",
    val midwife: String = "", val midwifeContact: String = "", val midwifeEmail: String = "",
    val midwifeAddress: String = "", val midwifeNotes: String = "",
    val specialist: String = "", val specialistContact: String = "", val specialistEmail: String = "", val specialistAddress: String = "", val specialistNotes: String = "",
    val otherProviderTitle: String = "", val otherProvider: String = "", val otherProviderContact: String = "", val otherProviderEmail: String = "", val otherProviderAddress: String = "", val otherProviderNotes: String = "",
    val cprNumber: String = "",
    val fullName: String = "",
    val registeredAddress: String = "",
    val nationality: String = "",
    val allergies: String = "",
    val medicalNotes: String = "",
    val photoFileName: String? = null,
    val avatar: ProfileAvatar = ProfileAvatar.Bear,
    val colorTheme: ChildColorTheme = ChildColorTheme.Sage,
    val parentIds: Set<String> = emptySet(),
    val careProviders: List<CareProvider> = emptyList(),
)

enum class ProfileValidationError {
    NameRequired,
    InvalidBirthDate,
    DueDateRequired,
    BirthDateInFuture,
    InvalidBirthTime,
    InvalidDueDate,
    SexRequired,
    InvalidWeight,
    InvalidLength,
    InvalidHeadCircumference,
    InvalidGestation,
}

fun ProfileDraft.validate(today: LocalDate = LocalDate.now(), units: MeasurementUnits = MeasurementUnits.Metric): ProfileValidationError? {
    if (name.isBlank()) return ProfileValidationError.NameRequired
    if (birthStatus == BirthStatus.Born) {
        val parsedBirthDate = parseDate(birthDate) ?: return ProfileValidationError.InvalidBirthDate
        if (parsedBirthDate.isAfter(today)) return ProfileValidationError.BirthDateInFuture
        if (birthTime.isNotBlank() && parseTime(birthTime) == null) return ProfileValidationError.InvalidBirthTime
    } else if (dueDate.isBlank()) return ProfileValidationError.DueDateRequired
    if (dueDate.isNotBlank() && parseDate(dueDate) == null) return ProfileValidationError.InvalidDueDate
    if (sex == BiologicalSex.Unselected) return ProfileValidationError.SexRequired
    val weight = birthWeightGrams.toDoubleOrNull()?.let { if (units == MeasurementUnits.Imperial) it * 453.59237 else it }
    if (birthWeightGrams.isNotBlank() && weight?.takeIf { it in 100.0..10_000.0 } == null) {
        return ProfileValidationError.InvalidWeight
    }
    val length = birthLengthCm.toDoubleOrNull()?.let { if (units == MeasurementUnits.Imperial) it * 2.54 else it }
    if (birthLengthCm.isNotBlank() && length?.takeIf { it in 20.0..100.0 } == null) {
        return ProfileValidationError.InvalidLength
    }
    if (
        birthHeadCircumferenceCm.isNotBlank() &&
        birthHeadCircumferenceCm.toDoubleOrNull()?.let { if (units == MeasurementUnits.Imperial) it * 2.54 else it }?.takeIf { it in 15.0..60.0 } == null
    ) {
        return ProfileValidationError.InvalidHeadCircumference
    }
    val weeks = gestationalWeeks.toIntOrNull()
    val days = gestationalDays.toIntOrNull()
    if ((gestationalWeeks.isNotBlank() && weeks !in 20..45) || (gestationalDays.isNotBlank() && days !in 0..6)) {
        return ProfileValidationError.InvalidGestation
    }
    return null
}

fun ProfileDraft.toProfile(existing: ChildProfile? = null, units: MeasurementUnits = MeasurementUnits.Metric): ChildProfile {
    val now = System.currentTimeMillis()
    return ChildProfile(
        id = existing?.id ?: id ?: java.util.UUID.randomUUID().toString(),
        name = name.trim(),
        nickname = nickname.trim(),
        birthStatus = birthStatus,
        birthDate = birthDate.takeIf { birthStatus == BirthStatus.Born && it.isNotBlank() }?.let(::parseDate),
        birthTime = birthTime.takeIf { birthStatus == BirthStatus.Born && it.isNotBlank() }?.let(::parseTime),
        dueDate = dueDate.takeIf(String::isNotBlank)?.let(::parseDate),
        sex = sex,
        birthWeightGrams = birthWeightGrams.toDoubleOrNull()?.let { if (units == MeasurementUnits.Imperial) (it * 453.59237).roundToInt() else it.roundToInt() },
        birthLengthCm = birthLengthCm.toDoubleOrNull()?.let { if (units == MeasurementUnits.Imperial) it * 2.54 else it },
        birthHeadCircumferenceCm = birthHeadCircumferenceCm.toDoubleOrNull()?.let { if (units == MeasurementUnits.Imperial) it * 2.54 else it },
        gestationalWeeks = gestationalWeeks.toIntOrNull(),
        gestationalDays = gestationalDays.toIntOrNull(),
        hospital = hospital.trim(),
        hospitalContact = hospitalContact.trim(),
        hospitalEmail = hospitalEmail.trim(), hospitalAddress = hospitalAddress.trim(), hospitalNotes = hospitalNotes.trim(),
        gp = gp.trim(),
        gpContact = gpContact.trim(),
        gpEmail = gpEmail.trim(), gpAddress = gpAddress.trim(), gpNotes = gpNotes.trim(),
        healthVisitor = healthVisitor.trim(),
        healthVisitorContact = healthVisitorContact.trim(),
        healthVisitorEmail = healthVisitorEmail.trim(), healthVisitorAddress = healthVisitorAddress.trim(), healthVisitorNotes = healthVisitorNotes.trim(),
        midwife = midwife.trim(), midwifeContact = midwifeContact.trim(), midwifeEmail = midwifeEmail.trim(),
        midwifeAddress = midwifeAddress.trim(), midwifeNotes = midwifeNotes.trim(), cprNumber = cprNumber.trim(),
        fullName = fullName.trim(), registeredAddress = registeredAddress.trim(), nationality = nationality.trim(),
        specialist = specialist.trim(), specialistContact = specialistContact.trim(), specialistEmail = specialistEmail.trim(), specialistAddress = specialistAddress.trim(), specialistNotes = specialistNotes.trim(),
        otherProviderTitle = otherProviderTitle.trim(), otherProvider = otherProvider.trim(), otherProviderContact = otherProviderContact.trim(), otherProviderEmail = otherProviderEmail.trim(), otherProviderAddress = otherProviderAddress.trim(), otherProviderNotes = otherProviderNotes.trim(),
        allergies = allergies.trim(),
        medicalNotes = medicalNotes.trim(),
        photoFileName = photoFileName,
        avatar = avatar,
        colorTheme = colorTheme,
        createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
        updatedAtEpochMillis = now,
    )
}

fun ChildProfile.toDraft(units: MeasurementUnits = MeasurementUnits.Metric, parentIds: Set<String> = emptySet(), careProviders: List<CareProvider> = emptyList()) = ProfileDraft(
    id = id,
    name = name,
    nickname = nickname,
    birthStatus = birthStatus,
    birthDate = birthDate?.toString().orEmpty(),
    birthTime = birthTime?.toString().orEmpty(),
    dueDate = dueDate?.toString().orEmpty(),
    sex = sex,
    birthWeightGrams = birthWeightGrams?.let { if (units == MeasurementUnits.Imperial) format(it / 453.59237) else it.toString() }.orEmpty(),
    birthLengthCm = birthLengthCm?.let { format(if (units == MeasurementUnits.Imperial) it / 2.54 else it) }.orEmpty(),
    birthHeadCircumferenceCm = birthHeadCircumferenceCm?.let { format(if (units == MeasurementUnits.Imperial) it / 2.54 else it) }.orEmpty(),
    gestationalWeeks = gestationalWeeks?.toString().orEmpty(),
    gestationalDays = gestationalDays?.toString().orEmpty(),
    hospital = hospital,
    hospitalContact = hospitalContact,
    hospitalEmail = hospitalEmail, hospitalAddress = hospitalAddress, hospitalNotes = hospitalNotes,
    gp = gp,
    gpContact = gpContact,
    gpEmail = gpEmail, gpAddress = gpAddress, gpNotes = gpNotes,
    healthVisitor = healthVisitor,
    healthVisitorContact = healthVisitorContact,
    healthVisitorEmail = healthVisitorEmail, healthVisitorAddress = healthVisitorAddress, healthVisitorNotes = healthVisitorNotes,
    midwife = midwife, midwifeContact = midwifeContact, midwifeEmail = midwifeEmail,
    midwifeAddress = midwifeAddress, midwifeNotes = midwifeNotes, cprNumber = cprNumber,
    fullName = fullName, registeredAddress = registeredAddress, nationality = nationality,
    specialist = specialist, specialistContact = specialistContact, specialistEmail = specialistEmail, specialistAddress = specialistAddress, specialistNotes = specialistNotes,
    otherProviderTitle = otherProviderTitle, otherProvider = otherProvider, otherProviderContact = otherProviderContact, otherProviderEmail = otherProviderEmail, otherProviderAddress = otherProviderAddress, otherProviderNotes = otherProviderNotes,
    allergies = allergies,
    medicalNotes = medicalNotes,
    photoFileName = photoFileName,
    avatar = avatar,
    colorTheme = colorTheme,
    parentIds = parentIds,
    careProviders = careProviders,
)

private fun parseDate(value: String): LocalDate? = runCatching { LocalDate.parse(value.trim()) }.getOrNull()
private fun parseTime(value: String): LocalTime? = runCatching { LocalTime.parse(value.trim()) }.getOrNull()
private fun format(value: Double) = "%.2f".format(java.util.Locale.US, value).trimEnd('0').trimEnd('.')
