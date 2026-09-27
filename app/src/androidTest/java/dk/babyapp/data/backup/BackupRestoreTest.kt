package dk.babyapp.data.backup

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dk.babyapp.data.AppDatabase
import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.profile.toEntity
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import dk.babyapp.data.medicine.MedicinePlan
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()

    @After fun close() = database.close()

    @Test fun encryptedBackupRestoresProfilesAndEvents() = runBlocking {
        database.childProfileDao().upsert(ChildProfile(id = "child", name = "Freja").toEntity())
        database.careEventDao().upsert(CareEventEntity(id = "event", childId = "child", type = CareEventType.Diaper, startedAt = 123, endedAt = 123))
        val preferences = DataStoreAppPreferencesRepository(context)
        val previousMedicines = preferences.preferences.first().medicines
        val plan = MedicinePlan(id = "test-medicine", childId = "child", name = "Testmedicin", dose = "Testdosis", asNeeded = true)
        preferences.updateMedicines(listOf(plan))
        val service = EncryptedBackupService(context, database)
        val bytes = service.create("hemmelig123".toCharArray())
        database.openHelper.writableDatabase.execSQL("DELETE FROM care_events")
        database.openHelper.writableDatabase.execSQL("DELETE FROM child_profiles")
        preferences.updateMedicines(emptyList())
        service.restore(bytes, "hemmelig123".toCharArray())
        assertEquals("Freja", database.childProfileDao().getById("child")?.name)
        assertEquals("child", database.careEventDao().get("event")?.childId)
        assertEquals(listOf(plan), preferences.preferences.first().medicines)
        preferences.updateMedicines(previousMedicines)
    }
}
