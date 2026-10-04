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
        val bookPage = dk.babyapp.data.book.BabyBookPage("child", "cover", "{\"En hilsen til dig\":\"Kære Freja\"}")
        database.babyBookDao().save(bookPage)
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
        assertEquals(bookPage, database.babyBookDao().observeAll().first().single())
        assertEquals("Freja", database.childProfileDao().getById("child")?.name)
        assertEquals("child", database.careEventDao().get("event")?.childId)
        assertEquals(listOf(plan), preferences.preferences.first().medicines)
        preferences.updateMedicines(previousMedicines)
    }
    @Test fun olderBackupWithoutBookOrSideColumnsStillRestores() = runBlocking {
        database.childProfileDao().upsert(ChildProfile(id = "legacy-child", name = "Alma").toEntity())
        database.careEventDao().upsert(CareEventEntity(id = "legacy-event", childId = "legacy-child", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 2000, leftSeconds = 1, notes = "Gammel note"))
        val service = EncryptedBackupService(context, database)
        val password = "hemmelig123".toCharArray()
        val root = org.json.JSONObject(String(decrypt(service.create(password), password)))
        val tables = root.getJSONObject("tables")
        tables.remove("baby_book_pages")
        val event = tables.getJSONArray("care_events").getJSONObject(0)
        event.remove("timerSegmentSides"); event.remove("nursingContinued")
        service.restore(encrypt(root.toString().toByteArray(), password), password)
        assertEquals("Gammel note", database.careEventDao().get("legacy-event")?.notes)
        assertEquals("", database.careEventDao().get("legacy-event")?.timerSegmentSides)
        assertEquals(emptyList<dk.babyapp.data.book.BabyBookPage>(), database.babyBookDao().observeAll().first())
    }

}
