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
