package dk.babyapp.data.profile

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class ChildProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val nickname: String = "",
    val birthStatus: BirthStatus = BirthStatus.Born,
    val birthDate: LocalDate? = null,
    val birthTime: LocalTime? = null,
    val dueDate: LocalDate? = null,
    val sex: BiologicalSex = BiologicalSex.Unselected,
    val birthWeightGrams: Int? = null,
    val birthLengthCm: Double? = null,
    val birthHeadCircumferenceCm: Double? = null,
    val gestationalWeeks: Int? = null,
    val gestationalDays: Int? = null,
    val hospital: String = "",
    val hospitalContact: String = "",
    val hospitalEmail: String = "",
    val hospitalAddress: String = "",
    val hospitalNotes: String = "",
    val gp: String = "",
    val gpContact: String = "",
    val gpEmail: String = "",
    val gpAddress: String = "",
    val gpNotes: String = "",
    val healthVisitor: String = "",
    val healthVisitorContact: String = "",
    val healthVisitorEmail: String = "",
    val healthVisitorAddress: String = "",
    val healthVisitorNotes: String = "",
    val midwife: String = "",
    val midwifeContact: String = "",
    val midwifeEmail: String = "",
    val midwifeAddress: String = "",
    val midwifeNotes: String = "",
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
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
)

enum class BirthStatus { Born, Expected }
enum class ChildColorTheme { Sage, Rose, Sky, Lavender, Sunshine }

enum class BiologicalSex {
    Unselected,
    PreferNotToSay,
    Female,
    Male,
    Other,
}

enum class ProfileAvatar(val symbol: String) {
    Bear("🐻"),
    Bunny("🐰"),
    Star("⭐"),
    Sprout("🌱"),
    Fox("🦊"),
    Koala("🐨"),
    Panda("🐼"),
    Lion("🦁"),
    Whale("🐳"),
    Butterfly("🦋"),
    Rainbow("🌈"),
    Moon("🌙"),
}
