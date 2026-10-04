package dk.babyapp.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dk.babyapp.core.di.CoreModule
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration14To15Test {
    private val databaseName = "migration-14-15-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrationPreservesProfileAndRemovesLegacyProviderColumns() {
        helper.createDatabase(databaseName, 14).apply {
            insertProfile(this)
            close()
        }

        helper.runMigrationsAndValidate(databaseName, 15, true, CoreModule.MIGRATION_14_15).use { database ->
            database.query("SELECT name, hospital FROM child_profiles WHERE id = 'child'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Freja", cursor.getString(0))
                assertEquals("Rigshospitalet", cursor.getString(1))
            }
            database.query("PRAGMA table_info(child_profiles)").use { cursor ->
                val names = buildSet { while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) }
                assertEquals(false, "gpContact" in names)
                assertEquals(false, "healthVisitorEmail" in names)
            }
        }
    }

    @Test
    fun migration15To16AddsDetailedTrackingFields() {
        val name = "migration-15-16-test"
        helper.createDatabase(name, 15).close()

        helper.runMigrationsAndValidate(name, 16, true, CoreModule.MIGRATION_15_16).use { database ->
            database.query("PRAGMA table_info(care_events)").use { cursor ->
                val names = buildSet { while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) }
                assertEquals(true, "breastfeedingIssue" in names)
                assertEquals(true, "diaperConsistency" in names)
                assertEquals(true, "measurementType" in names)
                assertEquals(true, "activityType" in names)
            }
        }
    }

    @Test
    fun migration16To17AddsOptionalTimeAndMedicationFields() {
        val name = "migration-16-17-test"
        helper.createDatabase(name, 16).close()
        helper.runMigrationsAndValidate(name, 17, true, CoreModule.MIGRATION_16_17).use { database ->
            database.query("PRAGMA table_info(care_events)").use { cursor ->
                val names = buildSet { while (cursor.moveToNext()) add(cursor.getString(cursor.getColumnIndexOrThrow("name"))) }
                assertEquals(true, "timeSpecified" in names)
                assertEquals(true, "medicationName" in names)
                assertEquals(true, "medicationDose" in names)
            }
        }
    }

    @Test
    fun migration17To18PreservesRecordsAndAddsDraftAndFoodFields() {
        val name = "migration-17-18-test"
        helper.createDatabase(name, 17).use { db ->
            val values = android.content.ContentValues()
            db.query("PRAGMA table_info(care_events)").use { cursor ->
                while (cursor.moveToNext()) {
                    if (cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1) {
                        val column = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                        if (cursor.getString(cursor.getColumnIndexOrThrow("type")) == "TEXT") values.put(column, "") else values.put(column, 0)
                    }
                }
            }
            values.put("id", "saved"); values.put("childId", "freja"); values.put("type", "Pumping"); values.put("notes", "Bevar min note")
            db.insert("care_events", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
        }
        helper.runMigrationsAndValidate(name, 18, true, CoreModule.MIGRATION_17_18).use { db ->
            db.query("SELECT notes, isDraft, pumpingMethod, foodName FROM care_events WHERE id = 'saved'").use { cursor ->
                org.junit.Assert.assertTrue(cursor.moveToFirst())
                assertEquals("Bevar min note", cursor.getString(0))
                assertEquals(0, cursor.getInt(1))
                assertEquals("", cursor.getString(2))
                assertEquals("", cursor.getString(3))
            }
        }
    }

    @Test
    fun migration18To19PreservesNursingAndAddsBook() {
        val name = "migration-18-19-test"
        helper.createDatabase(name, 18).use { db ->
            val child = android.content.ContentValues()
            db.query("PRAGMA table_info(child_profiles)").use { c ->
                while (c.moveToNext()) if (c.getInt(c.getColumnIndexOrThrow("notnull")) == 1) {
                    val column = c.getString(c.getColumnIndexOrThrow("name"))
                    if (c.getString(c.getColumnIndexOrThrow("type")) == "TEXT") child.put(column, "") else child.put(column, 0)
                }
            }
            child.put("id", "child"); child.put("name", "Freja")
            db.insert("child_profiles", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, child)
            val values = android.content.ContentValues()
            db.query("PRAGMA table_info(care_events)").use { cursor ->
                while (cursor.moveToNext()) if (cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1) {
                    val column = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    if (cursor.getString(cursor.getColumnIndexOrThrow("type")) == "TEXT") values.put(column, "") else values.put(column, 0)
                }
            }
            values.put("id", "nursing"); values.put("childId", "child"); values.put("type", "Breastfeeding")
            values.put("timerSegments", "1000-2000;3000-5000"); values.put("leftSeconds", 3); values.put("notes", "Bevar historik")
            db.insert("care_events", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
        }
        helper.runMigrationsAndValidate(name, 19, true, CoreModule.MIGRATION_18_19).use { db ->
            db.query("SELECT timerSegments, timerSegmentSides, leftSeconds, notes, nursingContinued FROM care_events WHERE id = 'nursing'").use { c ->
                org.junit.Assert.assertTrue(c.moveToFirst()); assertEquals("1000-2000;3000-5000", c.getString(0)); assertEquals("", c.getString(1)); assertEquals(3, c.getInt(2)); assertEquals("Bevar historik", c.getString(3)); assertEquals(0, c.getInt(4))
            }
            db.execSQL("PRAGMA foreign_keys = ON")
            db.execSQL("INSERT INTO baby_book_pages VALUES ('child', 'cover', '{}', '[]', 1)")
            db.execSQL("DELETE FROM child_profiles WHERE id = 'child'")
            db.query("SELECT COUNT(*) FROM baby_book_pages").use { c -> c.moveToFirst(); assertEquals(0, c.getInt(0)) }
        }
    }


    @Test fun migration19To20AddsOptionalNippleShieldWithoutDeletingBook() {
        val name = "migration-19-20-test"
        helper.createDatabase(name, 19).use { db ->
            val values = android.content.ContentValues()
            db.query("PRAGMA table_info(child_profiles)").use { c ->
                while (c.moveToNext()) if (c.getInt(c.getColumnIndexOrThrow("notnull")) == 1) {
                    val column = c.getString(c.getColumnIndexOrThrow("name"))
                    if (c.getString(c.getColumnIndexOrThrow("type")) == "TEXT") values.put(column, "") else values.put(column, 0)
                }
            }
            values.put("id", "child"); db.insert("child_profiles", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
            db.execSQL("INSERT INTO baby_book_pages VALUES ('child', 'cover', '{}', '[]', 1)")
        }
        helper.runMigrationsAndValidate(name, 20, true, CoreModule.MIGRATION_19_20).use { db ->
            db.query("SELECT COUNT(*) FROM baby_book_pages").use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
            db.query("PRAGMA table_info(care_events)").use { c ->
                var found = false
                while (c.moveToNext()) if (c.getString(c.getColumnIndexOrThrow("name")) == "nippleShield") { found = true; assertEquals("0", c.getString(c.getColumnIndexOrThrow("dflt_value"))) }
                org.junit.Assert.assertTrue(found)
            }
        }
    }


    @Test fun migration20To21PreservesLegacyNippleShield() {
        val name = "migration-20-21-test"
        helper.createDatabase(name, 20).use { db ->
            val values = android.content.ContentValues()
            db.query("PRAGMA table_info(care_events)").use { c ->
                while (c.moveToNext()) if (c.getInt(c.getColumnIndexOrThrow("notnull")) == 1) {
                    val column = c.getString(c.getColumnIndexOrThrow("name"))
                    if (c.getString(c.getColumnIndexOrThrow("type")) == "TEXT") values.put(column, "") else values.put(column, 0)
                }
            }
            values.put("id", "nursing"); values.put("childId", "child"); values.put("type", "Breastfeeding")
            values.put("nippleShield", 1); values.put("timerSegments", "1000-61000"); values.put("leftSeconds", 60)
            db.insert("care_events", android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values)
        }
        helper.runMigrationsAndValidate(name, 21, true, CoreModule.MIGRATION_20_21).use { db ->
            db.query("SELECT nippleShield, timerSegmentShields, timerSegments, leftSeconds FROM care_events WHERE id = 'nursing'").use { c ->
                org.junit.Assert.assertTrue(c.moveToFirst()); assertEquals(1, c.getInt(0)); assertEquals("", c.getString(1)); assertEquals("1000-61000", c.getString(2)); assertEquals(60, c.getInt(3))
            }
        }
    }

    private fun insertProfile(database: SupportSQLiteDatabase) {
        database.execSQL(
            """INSERT INTO child_profiles (
                id, name, nickname, birthDate, birthTime, dueDate, sex, birthWeightGrams,
                birthLengthCm, birthHeadCircumferenceCm, gestationalWeeks, gestationalDays,
                hospital, allergies, medicalNotes, photoFileName, avatar, createdAtEpochMillis,
                updatedAtEpochMillis, birthStatus, hospitalContact, gpContact, healthVisitorContact,
                hospitalEmail, hospitalAddress, hospitalNotes, gp, gpEmail, gpAddress, gpNotes,
                healthVisitor, healthVisitorEmail, healthVisitorAddress, healthVisitorNotes,
                midwife, midwifeContact, midwifeEmail, midwifeAddress, midwifeNotes, cprNumber,
                colorTheme, specialist, specialistContact, specialistEmail, specialistAddress,
                specialistNotes, otherProviderTitle, otherProvider, otherProviderContact,
                otherProviderEmail, otherProviderAddress, otherProviderNotes, fullName,
                registeredAddress, nationality, sortOrder
            ) VALUES (
                'child', 'Freja', '', '2026-04-01', NULL, NULL, 'Female', NULL,
                NULL, NULL, NULL, NULL, 'Rigshospitalet', '', '', NULL, 'Bunny', 1,
                1, 'Born', '', '', '', '', '', '', '', '', '', '', '', '', '', '',
                '', '', '', '', '', '', 'NeutralLight', '', '', '', '', '', '', '', '',
                '', '', '', '', '', '', 0
            )""".trimIndent(),
        )
    }
}
