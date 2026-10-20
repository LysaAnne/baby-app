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
import dk.babyapp.data.medicine.toEntity
import dk.babyapp.data.medicine.toPlan
import dk.babyapp.data.teeth.toEntity
import dk.babyapp.data.teeth.toRecord
import kotlinx.serialization.encodeToString
import org.junit.After
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dk.babyapp.data.color.DataStoreColorProfileRepository
import dk.babyapp.data.color.colorProfileDataStore
import dk.babyapp.data.preferences.appPreferencesDataStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupRestoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()

    private lateinit var savedPreferences: androidx.datastore.preferences.core.Preferences

    // Backup and restore touch the real app preferences; put them back afterwards.
    @org.junit.Before fun saveStore() = runBlocking {
        savedPreferences = context.appPreferencesDataStore.data.first()
        context.appPreferencesDataStore.edit { it[dk.babyapp.data.legacy.LegacyListMigrator.MIGRATED] = true }
        Unit
    }

    @After fun close() = runBlocking {
        context.appPreferencesDataStore.edit { it.clear(); it += savedPreferences }
        database.close()
    }

    @Test fun encryptedBackupRestoresProfilesAndEvents() = runBlocking {
        database.childProfileDao().upsert(ChildProfile(id = "child", name = "Freja").toEntity())
        database.careEventDao().upsert(CareEventEntity(id = "event", childId = "child", type = CareEventType.Diaper, startedAt = 123, endedAt = 123))
        val bookPage = dk.babyapp.data.book.BabyBookPage("child", "cover", "{\"En hilsen til dig\":\"Kære Freja\"}")
        database.babyBookDao().save(bookPage)
        val preferences = DataStoreAppPreferencesRepository(context)
        val previousStatistics = preferences.preferences.first().headSideStatistics
        val statistics = dk.babyapp.data.preferences.HeadSideStatisticsSettings("Week", correctionDates = mapOf("child" to "2026-10-01"))
        preferences.updateHeadSideStatistics(statistics)
        val head = dk.babyapp.domain.headSideObservation("child", dk.babyapp.data.tracking.HeadSide.Left, 123).copy(id = "head")
        database.careEventDao().upsert(head)
        val tooth = dk.babyapp.data.teeth.ToothRecord("child", 51, "2026-01-01", "2026-10-01")
        database.teethDao().insertAll(listOf(tooth.toEntity(0)))
        val plan = MedicinePlan(id = "test-medicine", childId = "child", name = "Testmedicin", dose = "Testdosis", asNeeded = true)
        database.medicineDao().insertAll(listOf(plan.toEntity(0)))
        val service = EncryptedBackupService(context, database)
        val bytes = service.create("hemmelig123".toCharArray())
        database.openHelper.writableDatabase.execSQL("DELETE FROM care_events")
        database.openHelper.writableDatabase.execSQL("DELETE FROM child_profiles")
        database.medicineDao().deleteAll()
        database.teethDao().deleteAll()
        preferences.updateHeadSideStatistics(dk.babyapp.data.preferences.HeadSideStatisticsSettings())
        service.restore(bytes, "hemmelig123".toCharArray())
        assertEquals(bookPage, database.babyBookDao().observeAll().first().single())
        assertEquals("Freja", database.childProfileDao().getById("child")?.name)
        assertEquals("child", database.careEventDao().get("event")?.childId)
        assertEquals(listOf(plan), database.medicineDao().all().map { it.toPlan() })
        assertEquals(listOf(tooth), database.teethDao().all().map { it.toRecord() })
        assertEquals(dk.babyapp.data.tracking.HeadSide.Left, database.careEventDao().get("head")?.headSide)
        assertEquals(statistics, preferences.preferences.first().headSideStatistics)
        preferences.updateHeadSideStatistics(previousStatistics)
    }
    @Test fun olderBackupWithoutBookOrSideColumnsStillRestores() = runBlocking {
        database.childProfileDao().upsert(ChildProfile(id = "legacy-child", name = "Alma").toEntity())
        database.careEventDao().upsert(CareEventEntity(id = "legacy-event", childId = "legacy-child", type = CareEventType.Breastfeeding, startedAt = 1000, endedAt = 2000, leftSeconds = 1, notes = "Gammel note"))
        val service = EncryptedBackupService(context, database)
        val password = "hemmelig123".toCharArray()
        val root = org.json.JSONObject(String(decrypt(service.create(password), password)))
        val tables = root.getJSONObject("tables")
        tables.remove("baby_book_pages")
        tables.remove("medicine_plans"); tables.remove("tooth_records")
        root.remove("teeth")
        val legacyPlan = MedicinePlan(id = "legacy-medicine", childId = "legacy-child", name = "Gammel medicin", times = listOf("08:00"))
        root.put("medicines", kotlinx.serialization.json.Json.encodeToString(listOf(legacyPlan)))
        val event = tables.getJSONArray("care_events").getJSONObject(0)
        event.remove("headSide"); root.remove("headSideStatistics")
        event.remove("timerSegmentSides"); event.remove("nursingContinued")
        service.restore(encrypt(root.toString().toByteArray(), password), password)
        assertEquals("Gammel note", database.careEventDao().get("legacy-event")?.notes)
        assertEquals("", database.careEventDao().get("legacy-event")?.timerSegmentSides)
        assertEquals(emptyList<dk.babyapp.data.book.BabyBookPage>(), database.babyBookDao().observeAll().first())
        assertEquals(listOf(legacyPlan), database.medicineDao().all().map { it.toPlan() })
        assertEquals(emptyList<dk.babyapp.data.teeth.ToothRecord>(), database.teethDao().all().map { it.toRecord() })
    }

    @Test fun backupRestoresEverySettingAndColorProfile() = runBlocking {
        val appStore = context.appPreferencesDataStore
        val colorStore = context.colorProfileDataStore
        val savedApp = appStore.data.first()
        val savedColors = colorStore.data.first()
        try {
            appStore.edit { prefs ->
                prefs[booleanPreferencesKey("backup_test_boolean")] = true
                prefs[intPreferencesKey("backup_test_int")] = 7
                prefs[longPreferencesKey("backup_test_long")] = 1_760_000_000_000
                prefs[floatPreferencesKey("backup_test_float")] = 1.5f
                prefs[doublePreferencesKey("backup_test_double")] = 2.25
                prefs[stringPreferencesKey("backup_test_string")] = "Madning · alle"
                prefs[stringSetPreferencesKey("backup_test_set")] = setOf("Bottle", "Diaper")
            }
            val colors = DataStoreColorProfileRepository(context)
            colors.move(colors.profiles.first().first().id, 1)
            val expectedApp = appStore.data.first()
            val expectedColors = colorStore.data.first()
            val service = EncryptedBackupService(context, database)
            val bytes = service.create("hemmelig123".toCharArray())
            appStore.edit { it.clear() }
            colorStore.edit { it.clear() }
            service.restore(bytes, "hemmelig123".toCharArray())
            assertEquals(expectedApp, appStore.data.first())
            assertEquals(expectedColors, colorStore.data.first())
        } finally {
            appStore.edit { it.clear(); it += savedApp }
            colorStore.edit { it.clear(); it += savedColors }
        }
    }

    @Test fun backupFromNewerAppIsRejectedWithoutTouchingData() = runBlocking {
        database.childProfileDao().upsert(ChildProfile(id = "kept-child", name = "Ida").toEntity())
        val service = EncryptedBackupService(context, database)
        val password = "hemmelig123".toCharArray()
        val root = org.json.JSONObject(String(decrypt(service.create(password), password)))
        root.put("databaseVersion", database.openHelper.writableDatabase.version + 1)
        root.getJSONObject("tables").put("child_profiles", org.json.JSONArray())
        val result = runCatching { service.restore(encrypt(root.toString().toByteArray(), password), password) }
        assertTrue(result.isFailure)
        assertEquals("Ida", database.childProfileDao().getById("kept-child")?.name)
    }
}
