package dk.babyapp.data.legacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.data.AppDatabase
import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.medicine.toEntity
import dk.babyapp.data.preferences.appPreferencesDataStore
import dk.babyapp.data.teeth.ToothRecord
import dk.babyapp.data.teeth.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Medicine cards and teeth were stored as JSON in app preferences until database version 24.
 * This copies them into the database once. The JSON is left in place as a safety copy and is
 * never read again after the copy is marked done, so later edits and deletions stay authoritative.
 */
@Singleton
class LegacyListMigrator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {
    private val lock = Mutex()
    @Volatile private var done = false

    suspend fun ensureMigrated() {
        if (done) return
        lock.withLock {
            if (done) return
            val store = context.appPreferencesDataStore
            val values = store.data.first()
            if (values[MIGRATED] != true) {
                val medicines = runCatching { values[LEGACY_MEDICINES]?.let { Json.decodeFromString<List<MedicinePlan>>(it) }.orEmpty() }
                val teeth = runCatching { values[LEGACY_TEETH]?.let { Json.decodeFromString<List<ToothRecord>>(it) }.orEmpty() }
                medicines.getOrNull()?.let { plans -> database.medicineDao().insertMissing(plans.mapIndexed { index, plan -> plan.toEntity(index) }) }
                teeth.getOrNull()?.let { records -> database.teethDao().insertMissing(records.mapIndexed { index, record -> record.toEntity(index) }) }
                // A list that cannot be decoded stays in preferences and is retried on the next start instead of being marked done.
                if (medicines.isSuccess && teeth.isSuccess) store.edit { it[MIGRATED] = true }
            }
            done = true
        }
    }

    /** Used after a backup restore has written the database lists directly. */
    suspend fun markMigrated() = lock.withLock {
        context.appPreferencesDataStore.edit { it[MIGRATED] = true }
        done = true
    }

    companion object {
        val LEGACY_MEDICINES = stringPreferencesKey("medicines")
        val LEGACY_TEETH = stringPreferencesKey("teeth")
        val MIGRATED = booleanPreferencesKey("legacy_lists_in_database")
    }
}
