package dk.babyapp.data.color

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.ui.theme.defaultColorProfiles
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.colorProfileDataStore by preferencesDataStore(name = "color_profiles")

class DataStoreColorProfileRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ColorProfileRepository {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val bundledProfiles: List<ColorProfile> by lazy {
        runCatching {
            context.assets.open("child_color_profiles.json").bufferedReader().use { json.decodeFromString<List<ColorProfile>>(it.readText()) }
        }.getOrElse { defaultColorProfiles }
    }

    override val profiles: Flow<List<ColorProfile>> = context.colorProfileDataStore.data.map { values ->
        if (values[PROFILE_SET_VERSION] == CURRENT_PROFILE_SET_VERSION) values[PROFILES]?.let(::decodeProfiles) ?: bundledProfiles else bundledProfiles
    }

    override suspend fun save(profile: ColorProfile) {
        context.colorProfileDataStore.edit { values ->
            val current = currentProfiles(values[PROFILE_SET_VERSION], values[PROFILES])
            val index = current.indexOfFirst { it.id == profile.id }
            val updated = current.toMutableList().apply { if (index >= 0) set(index, profile) else add(profile) }
            values[PROFILES] = json.encodeToString(updated)
            values[PROFILE_SET_VERSION] = CURRENT_PROFILE_SET_VERSION
        }
    }

    override suspend fun delete(id: String) {
        context.colorProfileDataStore.edit { values ->
            val current = currentProfiles(values[PROFILE_SET_VERSION], values[PROFILES])
            values[PROFILES] = json.encodeToString(current.filterNot { it.id == id })
            values[PROFILE_SET_VERSION] = CURRENT_PROFILE_SET_VERSION
        }
    }

    override suspend fun replaceAll(profiles: List<ColorProfile>) {
        context.colorProfileDataStore.edit { values ->
            values[PROFILES] = json.encodeToString(profiles)
            values[PROFILE_SET_VERSION] = CURRENT_PROFILE_SET_VERSION
        }
    }

    override suspend fun move(id: String, direction: Int) {
        context.colorProfileDataStore.edit { values ->
            val current = currentProfiles(values[PROFILE_SET_VERSION], values[PROFILES]).toMutableList()
            val from = current.indexOfFirst { it.id == id }
            if (from < 0) return@edit
            val sameAppearance = current.indices.filter { current[it].isDark == current[from].isDark }
            val groupPosition = sameAppearance.indexOf(from)
            val targetGroupPosition = groupPosition + direction
            if (targetGroupPosition !in sameAppearance.indices) return@edit
            val to = sameAppearance[targetGroupPosition]
            val item = current[from]
            current[from] = current[to]
            current[to] = item
            values[PROFILES] = json.encodeToString(current)
            values[PROFILE_SET_VERSION] = CURRENT_PROFILE_SET_VERSION
        }
    }

    private fun decodeProfiles(value: String): List<ColorProfile> =
        runCatching { json.decodeFromString<List<ColorProfile>>(value) }.getOrElse { bundledProfiles }

    private fun currentProfiles(version: Int?, encoded: String?): List<ColorProfile> =
        if (version == CURRENT_PROFILE_SET_VERSION) encoded?.let(::decodeProfiles) ?: bundledProfiles else bundledProfiles

    private companion object {
        val PROFILES = stringPreferencesKey("profiles_json")
        val PROFILE_SET_VERSION = intPreferencesKey("profile_set_version")
        const val CURRENT_PROFILE_SET_VERSION = 2
    }
}
