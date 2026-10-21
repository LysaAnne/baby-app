package dk.babyapp.data.teeth

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dk.babyapp.data.AppDatabase
import dk.babyapp.data.legacy.LegacyListMigrator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// No foreign key to the child: a record must never be lost because its child row is missing.
@Entity(tableName = "tooth_records", primaryKeys = ["childId", "tooth"])
data class ToothRecordEntity(
    val childId: String,
    val tooth: Int,
    val emerged: String?,
    val lost: String?,
    val sortOrder: Int,
    @androidx.room.ColumnInfo(defaultValue = "''") val signs: String = "",
    @androidx.room.ColumnInfo(defaultValue = "''") val note: String = "",
    @androidx.room.ColumnInfo(defaultValue = "0") val dateUnknown: Boolean = false,
)

fun ToothRecord.toEntity(sortOrder: Int) = ToothRecordEntity(childId, tooth, emerged, lost, sortOrder, signs, note, dateUnknown)
fun ToothRecordEntity.toRecord() = ToothRecord(childId, tooth, emerged, lost, signs, note, dateUnknown)

@Dao
interface TeethDao {
    @Query("SELECT * FROM tooth_records ORDER BY sortOrder")
    fun observeAll(): Flow<List<ToothRecordEntity>>

    @Query("SELECT * FROM tooth_records ORDER BY sortOrder")
    suspend fun all(): List<ToothRecordEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM tooth_records")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<ToothRecordEntity>)

    /** Copies records without overwriting any record that is already stored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(records: List<ToothRecordEntity>)

    @Query("DELETE FROM tooth_records WHERE childId = :childId AND tooth = :tooth")
    suspend fun delete(childId: String, tooth: Int)

    @Query("DELETE FROM tooth_records WHERE childId = :childId")
    suspend fun deleteForChild(childId: String)

    @Query("DELETE FROM tooth_records")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(records: List<ToothRecordEntity>) {
        deleteAll()
        insertAll(records)
    }
}

/** Tooth eruption and loss dates for all children. */
@Singleton
class TeethRepository @Inject constructor(
    database: AppDatabase,
    private val migrator: LegacyListMigrator,
) {
    private val dao = database.teethDao()

    val records: Flow<List<ToothRecord>> = flow {
        migrator.ensureMigrated()
        emitAll(dao.observeAll().map { rows -> rows.map { it.toRecord() } })
    }

    /** Saves one tooth; a record without any dates (and not marked as come on an unknown date) is removed, as before. */
    suspend fun save(record: ToothRecord) {
        record.validate()
        migrator.ensureMigrated()
        if (record.emerged == null && record.lost == null && !record.dateUnknown) {
            dao.delete(record.childId, record.tooth)
        } else {
            val existing = dao.all().firstOrNull { it.childId == record.childId && it.tooth == record.tooth }
            dao.insertAll(listOf(record.toEntity(existing?.sortOrder ?: dao.nextSortOrder())))
        }
    }

    suspend fun deleteForChild(childId: String) {
        migrator.ensureMigrated()
        dao.deleteForChild(childId)
    }
}
