package dk.babyapp.data.color

import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class ColorProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isDark: Boolean,
    val background: String,
    val primary: String,
    val primaryContainer: String,
    val secondary: String,
    val tertiary: String,
    val builtIn: Boolean = false,
)

fun ColorProfile.duplicate() = copy(
    id = UUID.randomUUID().toString(),
    name = "$name – kopi",
    builtIn = false,
)

fun String.normalizedHex(): String? {
    val value = trim().uppercase().let { if (it.startsWith("#")) it else "#$it" }
    return value.takeIf { it.matches(Regex("#[0-9A-F]{6}")) }
}
