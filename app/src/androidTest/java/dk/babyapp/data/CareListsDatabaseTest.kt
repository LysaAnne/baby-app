package dk.babyapp.data

import androidx.datastore.preferences.core.edit
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dk.babyapp.data.legacy.LegacyListMigrator
import dk.babyapp.data.medicine.MedicinePlan
import dk.babyapp.data.medicine.MedicineRepository
import dk.babyapp.data.preferences.appPreferencesDataStore
import dk.babyapp.data.teeth.TeethRepository
import dk.babyapp.data.teeth.ToothRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Medicine cards and teeth moved from preferences JSON into the database in version 24. */
@RunWith(AndroidJUnit4::class)
class CareListsDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    private val store = context.appPreferencesDataStore
    private lateinit var saved: androidx.datastore.preferences.core.Preferences

    private val plans = listOf(
        MedicinePlan(id = "b", childId = "child", name = "Panodil", dose = "2,5 ml", times = listOf("08:00", "20:00"), reminders = false),
        MedicinePlan(id = "a", childId = "other", name = "D-vitamin", asNeeded = true, active = false),
    )
    private val teeth = listOf(ToothRecord("child", 51, "2026-01-01"), ToothRecord("other", 81, "2026-02-01", "2026-09-01"))

    @Before fun saveStore() = runBlocking { saved = store.data.first() }

    @After fun restoreStore() = runBlocking {
        store.edit { it.clear(); it += saved }
        database.close()
    }

    private suspend fun storeLegacy(medicines: String, teeth: String) = store.edit {
        it.remove(LegacyListMigrator.MIGRATED)
        it[LegacyListMigrator.LEGACY_MEDICINES] = medicines
        it[LegacyListMigrator.LEGACY_TEETH] = teeth
    }

    @Test fun legacyListsAreCopiedOnceInOrderAndKeptAsSafetyCopy() = runBlocking {
        val medicinesJson = Json.encodeToString(plans)
        val teethJson = Json.encodeToString(teeth)
        storeLegacy(medicinesJson, teethJson)
        val medicines = MedicineRepository(database, LegacyListMigrator(context, database), context)
        val teethRepository = TeethRepository(database, LegacyListMigrator(context, database))

        assertEquals(plans, medicines.all())
        assertEquals(teeth, teethRepository.records.first())
        assertEquals(medicinesJson, store.data.first()[LegacyListMigrator.LEGACY_MEDICINES])
        assertEquals(teethJson, store.data.first()[LegacyListMigrator.LEGACY_TEETH])

        // A deletion after the copy must survive the next app start.
        medicines.replaceAll(plans.drop(1))
        val restarted = MedicineRepository(database, LegacyListMigrator(context, database), context)
        assertEquals(plans.drop(1), restarted.all())
    }

    @Test fun copyNeverOverwritesListsAlreadyInTheDatabase() = runBlocking {
        storeLegacy("[]", "[]")
        val medicines = MedicineRepository(database, LegacyListMigrator(context, database), context)
        medicines.replaceAll(plans)
        storeLegacy(Json.encodeToString(listOf(plans.first().copy(name = "Gammel"))), "[]")
        assertEquals(plans, MedicineRepository(database, LegacyListMigrator(context, database), context).all())
    }

    @Test fun unreadableLegacyListIsKeptAndRetriedInsteadOfLost() = runBlocking {
        storeLegacy("not json", Json.encodeToString(teeth))
        val migrator = LegacyListMigrator(context, database)
        assertEquals(emptyList<MedicinePlan>(), MedicineRepository(database, migrator, context).all())
        assertEquals(teeth, TeethRepository(database, migrator).records.first())
        assertNotEquals(true, store.data.first()[LegacyListMigrator.MIGRATED])
        assertEquals("not json", store.data.first()[LegacyListMigrator.LEGACY_MEDICINES])
    }

    @Test fun savingAToothKeepsOtherChildrenAndClearingRemovesIt() = runBlocking {
        storeLegacy("[]", Json.encodeToString(teeth))
        val repository = TeethRepository(database, LegacyListMigrator(context, database))
        repository.save(teeth.first().copy(lost = "2026-03-01"))
        assertEquals(teeth.last(), repository.records.first().single { it.childId == "other" })
        assertEquals("2026-03-01", repository.records.first().single { it.childId == "child" }.lost)
        repository.save(ToothRecord("child", 51))
        assertEquals(listOf(teeth.last()), repository.records.first())
    }
}
