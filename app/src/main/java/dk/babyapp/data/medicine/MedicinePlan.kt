package dk.babyapp.data.medicine

import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.util.UUID

@Serializable
data class MedicinePlan(
    val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val name: String,
    val dose: String = "",
    val instructions: String = "",
    val asNeeded: Boolean = false,
    val times: List<String> = emptyList(),
    val reminders: Boolean = false,
    val active: Boolean = true,
) {
    fun validTimes(): List<LocalTime> = times.mapNotNull { runCatching { LocalTime.parse(it) }.getOrNull() }.distinct().sorted()
}
