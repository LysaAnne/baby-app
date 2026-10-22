package dk.babyapp.data.medicine

import android.content.Context
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.data.AppDatabase
import dk.babyapp.data.legacy.LegacyListMigrator
import dk.babyapp.reminders.MedicineReminderScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// No foreign key to the child: a plan must never be lost because its child row is missing.
@Entity(tableName = "medicine_plans", indices = [Index("childId")])
data class MedicinePlanEntity(
    @PrimaryKey val id: String,
    val childId: String,
    val name: String,
    val dose: String,
    val instructions: String,
    val asNeeded: Boolean,
    val timesJson: String,
    val reminders: Boolean,
    val active: Boolean,
    val sortOrder: Int,
    @androidx.room.ColumnInfo(defaultValue = "1") val repeatEvery: Int = 1,
    @androidx.room.ColumnInfo(defaultValue = "'Day'") val repeatUnit: String = "Day",
    val repeatStart: String? = null,
)

fun MedicinePlan.toEntity(sortOrder: Int) = MedicinePlanEntity(id, childId, name, dose, instructions, asNeeded, Json.encodeToString(times), reminders, active, sortOrder, repeatEvery, repeatUnit.name, repeatStart)

fun MedicinePlanEntity.toPlan() = MedicinePlan(
    id = id, childId = childId, name = name, dose = dose, instructions = instructions, asNeeded = asNeeded,
    times = runCatching { Json.decodeFromString<List<String>>(timesJson) }.getOrDefault(emptyList()), reminders = reminders, active = active,
    repeatEvery = repeatEvery.coerceAtLeast(1), repeatUnit = RepeatUnit.entries.firstOrNull { it.name == repeatUnit } ?: RepeatUnit.Day, repeatStart = repeatStart,
)

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicine_plans ORDER BY sortOrder")
    fun observeAll(): Flow<List<MedicinePlanEntity>>

    @Query("SELECT * FROM medicine_plans ORDER BY sortOrder")
    suspend fun all(): List<MedicinePlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<MedicinePlanEntity>)

    /** Copies plans without overwriting any plan that is already stored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(plans: List<MedicinePlanEntity>)

    @Query("DELETE FROM medicine_plans")
    suspend fun deleteAll()

    @Query("DELETE FROM medicine_plans WHERE childId = :childId")
    suspend fun deleteForChild(childId: String)

    @Transaction
    suspend fun replaceAll(plans: List<MedicinePlanEntity>) {
        deleteAll()
        insertAll(plans)
    }
}

/** Medicine cards for all children. Every change reschedules the medicine reminders. */
@Singleton
class MedicineRepository @Inject constructor(
    database: AppDatabase,
    private val migrator: LegacyListMigrator,
    @param:ApplicationContext private val context: Context,
) {
    private val dao = database.medicineDao()

    val plans: Flow<List<MedicinePlan>> = flow {
        migrator.ensureMigrated()
        emitAll(dao.observeAll().map { rows -> rows.map { it.toPlan() } })
    }

    suspend fun all(): List<MedicinePlan> {
        migrator.ensureMigrated()
        return dao.all().map { it.toPlan() }
    }

    suspend fun replaceAll(plans: List<MedicinePlan>) {
        migrator.ensureMigrated()
        dao.replaceAll(plans.mapIndexed { index, plan -> plan.toEntity(index) })
        MedicineReminderScheduler(context).sync(plans)
    }

    suspend fun deleteForChild(childId: String) {
        migrator.ensureMigrated()
        dao.deleteForChild(childId)
        MedicineReminderScheduler(context).sync(all())
    }
}
