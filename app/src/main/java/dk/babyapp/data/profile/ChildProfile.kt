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
    val cprNumber: String = "",
    val fullName: String = "",
    val registeredAddress: String = "",
    val nationality: String = "",
    val allergies: String = "",
    val medicalNotes: String = "",
    val photoFileName: String? = null,
    val avatar: ProfileAvatar = ProfileAvatar.Bear,
    val colorTheme: String = ChildColorTheme.NeutralLight.name,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),
    val sortOrder: Int = Int.MAX_VALUE,
)

enum class BirthStatus { Born, Expected }
enum class ChildThemeAppearance { Light, Dark }

enum class ChildColorTheme(val appearance: ChildThemeAppearance) {
    NeutralLight(ChildThemeAppearance.Light),
    NeutralDark(ChildThemeAppearance.Dark),
    BoyLight(ChildThemeAppearance.Light),
    BoyDark(ChildThemeAppearance.Dark),
    GirlLight(ChildThemeAppearance.Light),
    GirlDark(ChildThemeAppearance.Dark),
    PastelYellow(ChildThemeAppearance.Light),
    NeonNight(ChildThemeAppearance.Dark),
    ButtercupSky(ChildThemeAppearance.Light),
    SunsetCoast(ChildThemeAppearance.Light),
    NeonGrove(ChildThemeAppearance.Dark),
    PlumMist(ChildThemeAppearance.Dark),
    PeachTwilight(ChildThemeAppearance.Dark),
    SunlitMeadow(ChildThemeAppearance.Light),
    BerryPop(ChildThemeAppearance.Dark),
    OliveStone(ChildThemeAppearance.Dark),
    DesertBloom(ChildThemeAppearance.Light),
    GlacierBlue(ChildThemeAppearance.Light),
    NordicForest(ChildThemeAppearance.Light),
    MintBlush(ChildThemeAppearance.Light),
    BerryRose(ChildThemeAppearance.Dark),
    PeachCream(ChildThemeAppearance.Light),

    // Kept so profiles saved by earlier versions can still be opened.
    Sage(ChildThemeAppearance.Light),
    Rose(ChildThemeAppearance.Light),
    Sky(ChildThemeAppearance.Light),
    Lavender(ChildThemeAppearance.Light),
    Sunshine(ChildThemeAppearance.Light);

    companion object {
        val selectableEntries = entries.take(22)
    }
}

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
