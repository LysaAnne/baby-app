package dk.babyapp.data.profile

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import java.util.UUID
import kotlinx.coroutines.flow.Flow

enum class CareProviderType { Hospital, Gp, HealthVisitor, Midwife, Specialist, Other }

data class CareProvider(
    val id: String = UUID.randomUUID().toString(), val childId: String = "", val type: CareProviderType,
    val customTitle: String = "", val name: String = "", val phone: String = "", val email: String = "",
    val address: String = "", val notes: String = "",
)

@Entity(
    tableName = "care_providers",
    foreignKeys = [ForeignKey(entity = ChildProfileEntity::class, parentColumns = ["id"], childColumns = ["childId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("childId")],
)
data class CareProviderEntity(
    @PrimaryKey val id: String, val childId: String, val type: String, val customTitle: String, val name: String,
    val phone: String, val email: String, val address: String, val notes: String,
)

@Dao interface CareProviderDao {
    @Query("SELECT * FROM care_providers ORDER BY rowid") fun observeAll(): Flow<List<CareProviderEntity>>
    @Query("DELETE FROM care_providers WHERE childId = :childId") suspend fun deleteForChild(childId: String)
    @Upsert suspend fun upsertAll(items: List<CareProviderEntity>)
}

fun CareProviderEntity.toModel() = CareProvider(id, childId, enumValues<CareProviderType>().firstOrNull { it.name == type } ?: CareProviderType.Other, customTitle, name, phone, email, address, notes)
fun CareProvider.toEntity(child: String) = CareProviderEntity(id, child, type.name, customTitle, name, phone, email, address, notes)
