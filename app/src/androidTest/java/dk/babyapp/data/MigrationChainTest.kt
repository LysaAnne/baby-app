package dk.babyapp.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dk.babyapp.core.di.CoreModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Upgrades a database from every historical schema version to the current one and checks that
 * every row and every value that still has a column survives unchanged. New migrations are covered
 * automatically as long as they are added to [CoreModule.ALL_MIGRATIONS].
 */
@RunWith(AndroidJUnit4::class)
class MigrationChainTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    private val currentVersion = CoreModule.ALL_MIGRATIONS.last().endVersion

    @Test fun everyHistoricalVersionUpgradesWithoutLosingData() {
        assertEquals((1 until currentVersion).toList(), CoreModule.ALL_MIGRATIONS.map { it.startVersion })
        for (version in 1 until currentVersion) {
            val name = "migration-chain-$version"
            val before = helper.createDatabase(name, version).use { db ->
                userTables(db).forEach { table -> insertFilledRow(db, table, version) }
                userTables(db).associateWith { table -> rows(db, table) }
            }
            helper.runMigrationsAndValidate(name, currentVersion, true, *CoreModule.ALL_MIGRATIONS).use { db ->
                val tablesNow = userTables(db)
                before.forEach { (table, oldRows) ->
                    assertTrue("v$version: $table was removed", table in tablesNow)
                    val newRows = rows(db, table)
                    oldRows.forEach { old ->
                        val match = newRows.firstOrNull { new -> old.keys.filter { it in new }.all { old[it] == new[it] } }
                        assertTrue("v$version: a row in $table changed or disappeared: $old", match != null)
                    }
                }
            }
        }
    }

    private fun userTables(db: SupportSQLiteDatabase): List<String> =
        db.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' AND name NOT IN ('android_metadata', 'room_master_table')").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }

    /** Fills every column, including nullable ones, with a value that identifies the version and column. */
    private fun insertFilledRow(db: SupportSQLiteDatabase, table: String, version: Int) {
        val values = ContentValues()
        db.query("PRAGMA table_info($table)").use { cursor ->
            while (cursor.moveToNext()) {
                val column = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                when (cursor.getString(cursor.getColumnIndexOrThrow("type")).uppercase()) {
                    "INTEGER" -> values.put(column, 1_000L + version)
                    "REAL" -> values.put(column, version + 0.5)
                    else -> values.put(column, if (column in KEY_COLUMNS) "keep" else "v$version-$column")
                }
            }
        }
        db.insert(table, SQLiteDatabase.CONFLICT_ABORT, values)
    }

    private fun rows(db: SupportSQLiteDatabase, table: String): List<Map<String, Any?>> =
        db.query("SELECT * FROM $table").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.columnNames.indices.associate { index ->
                    cursor.getColumnName(index) to when (cursor.getType(index)) {
                        android.database.Cursor.FIELD_TYPE_NULL -> null
                        android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(index)
                        android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(index)
                        else -> cursor.getString(index)
                    }
                })
            }
        }

    private companion object {
        // Shared ids keep foreign keys between the filled tables consistent.
        val KEY_COLUMNS = setOf("id", "childId", "parentId")
    }
}
