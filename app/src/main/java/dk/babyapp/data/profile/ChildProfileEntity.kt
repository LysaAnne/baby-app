package dk.babyapp.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nickname: String,
    val birthStatus: String,
    val birthDate: String,
    val birthTime: String?,
    val dueDate: String?,
    val sex: String,
    val birthWeightGrams: Int?,
    val birthLengthCm: Double?,
    val birthHeadCircumferenceCm: Double?,
    val gestationalWeeks: Int?,
    val gestationalDays: Int?,
    val hospital: String,
    val cprNumber: String,
    val fullName: String,
    val registeredAddress: String,
    val nationality: String,
    val allergies: String,
    val medicalNotes: String,
    val photoFileName: String?,
    val avatar: String,
    val colorTheme: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val sortOrder: Int,
)

fun ChildProfile.toEntity() = ChildProfileEntity(
    id = id,
    name = name,
    nickname = nickname,
    birthStatus = birthStatus.name,
    birthDate = birthDate?.toString().orEmpty(),
    birthTime = birthTime?.toString(),
    dueDate = dueDate?.toString(),
    sex = sex.name,
    birthWeightGrams = birthWeightGrams,
    birthLengthCm = birthLengthCm,
    birthHeadCircumferenceCm = birthHeadCircumferenceCm,
    gestationalWeeks = gestationalWeeks,
    gestationalDays = gestationalDays,
    hospital = hospital,
    cprNumber = cprNumber,
    fullName = fullName,
    registeredAddress = registeredAddress,
    nationality = nationality,
    allergies = allergies,
    medicalNotes = medicalNotes,
    photoFileName = photoFileName,
    avatar = avatar.name,
    colorTheme = colorTheme,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    sortOrder = sortOrder,
)

fun ChildProfileEntity.toModel() = ChildProfile(
    id = id,
    name = name,
    nickname = nickname,
    birthStatus = enumValueOrDefault(birthStatus, BirthStatus.Born),
    birthDate = birthDate.takeIf(String::isNotBlank)?.let(LocalDate::parse),
    birthTime = birthTime?.let(LocalTime::parse),
    dueDate = dueDate?.let(LocalDate::parse),
    sex = enumValueOrDefault(sex, BiologicalSex.Unselected),
    birthWeightGrams = birthWeightGrams,
    birthLengthCm = birthLengthCm,
    birthHeadCircumferenceCm = birthHeadCircumferenceCm,
    gestationalWeeks = gestationalWeeks,
    gestationalDays = gestationalDays,
    hospital = hospital,
    cprNumber = cprNumber,
    fullName = fullName,
    registeredAddress = registeredAddress,
    nationality = nationality,
    allergies = allergies,
    medicalNotes = medicalNotes,
    photoFileName = photoFileName,
    avatar = enumValueOrDefault(avatar, ProfileAvatar.Bear),
    colorTheme = colorTheme,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    sortOrder = sortOrder,
)

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: default
