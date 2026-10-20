package dk.babyapp.data.backup

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.util.Base64
import org.json.JSONArray
import org.json.JSONObject

/**
 * Copies every key of a Preferences DataStore, so settings added later are included in backups
 * without having to be listed here. Each value keeps its type, because DataStore keys are typed.
 */
internal fun Preferences.toBackupJson(): JSONObject = JSONObject().also { json ->
    asMap().forEach { (key, value) ->
        val entry = when (value) {
            is Boolean -> JSONObject().put("type", "boolean").put("value", value)
            is Int -> JSONObject().put("type", "int").put("value", value)
            is Long -> JSONObject().put("type", "long").put("value", value)
            is Float -> JSONObject().put("type", "float").put("value", value.toDouble())
            is Double -> JSONObject().put("type", "double").put("value", value)
            is String -> JSONObject().put("type", "string").put("value", value)
            is Set<*> -> JSONObject().put("type", "stringSet").put("value", JSONArray(value.map { it.toString() }))
            is ByteArray -> JSONObject().put("type", "bytes").put("value", Base64.getEncoder().encodeToString(value))
            else -> null
        }
        if (entry != null) json.put(key.name, entry)
    }
}

/** Parses a backup section completely before anything is written, so a damaged file cannot leave half-restored settings. */
internal fun JSONObject.toPreferenceWrites(): List<(MutablePreferences) -> Unit> = keys().asSequence().map { name ->
    val entry = getJSONObject(name)
    when (val type = entry.getString("type")) {
        "boolean" -> entry.getBoolean("value").let { value -> { prefs: MutablePreferences -> prefs[booleanPreferencesKey(name)] = value } }
        "int" -> entry.getInt("value").let { value -> { prefs: MutablePreferences -> prefs[intPreferencesKey(name)] = value } }
        "long" -> entry.getLong("value").let { value -> { prefs: MutablePreferences -> prefs[longPreferencesKey(name)] = value } }
        "float" -> entry.getDouble("value").toFloat().let { value -> { prefs: MutablePreferences -> prefs[floatPreferencesKey(name)] = value } }
        "double" -> entry.getDouble("value").let { value -> { prefs: MutablePreferences -> prefs[doublePreferencesKey(name)] = value } }
        "string" -> entry.getString("value").let { value -> { prefs: MutablePreferences -> prefs[stringPreferencesKey(name)] = value } }
        "stringSet" -> entry.getJSONArray("value").let { array -> (0 until array.length()).map(array::getString).toSet() }
            .let { value -> { prefs: MutablePreferences -> prefs[stringSetPreferencesKey(name)] = value } }
        "bytes" -> Base64.getDecoder().decode(entry.getString("value"))
            .let { value -> { prefs: MutablePreferences -> prefs[byteArrayPreferencesKey(name)] = value } }
        else -> throw IllegalArgumentException("Ukendt indstillingstype $type")
    }
}.toList()
