package dk.babyapp.data.teeth

import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ToothRecord(val childId: String, val tooth: Int, val emerged: String? = null, val lost: String? = null)

val upperMilkTeeth = listOf(55, 54, 53, 52, 51, 61, 62, 63, 64, 65)
val lowerMilkTeeth = listOf(85, 84, 83, 82, 81, 71, 72, 73, 74, 75)

fun ToothRecord.validate(today: LocalDate = LocalDate.now()) {
    require(childId.isNotBlank() && tooth in upperMilkTeeth + lowerMilkTeeth)
    val start = emerged?.let(LocalDate::parse)
    val end = lost?.let(LocalDate::parse)
    require(start == null || start <= today) { "Frembrud kan ikke ligge i fremtiden." }
    require(end == null || end <= today) { "Tab kan ikke ligge i fremtiden." }
    require(start == null || end == null || end >= start) { "Tab skal være på eller efter frembrud." }
}
