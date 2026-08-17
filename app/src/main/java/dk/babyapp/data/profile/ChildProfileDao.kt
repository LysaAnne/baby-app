package dk.babyapp.data.profile

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildProfileDao {
    @Query("SELECT * FROM child_profiles ORDER BY sortOrder, name COLLATE NOCASE")
    fun observeAll(): Flow<List<ChildProfileEntity>>

    @Query("SELECT * FROM child_profiles WHERE id = :id")
    suspend fun getById(id: String): ChildProfileEntity?

    @Upsert
    suspend fun upsert(profile: ChildProfileEntity)

    @Delete
    suspend fun delete(profile: ChildProfileEntity)

    @Query("SELECT COUNT(*) FROM child_profiles")
    suspend fun count(): Int

    @Query("UPDATE child_profiles SET sortOrder = :position WHERE id = :id")
    suspend fun updateSortOrder(id: String, position: Int)
}
