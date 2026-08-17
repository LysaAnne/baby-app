package dk.babyapp.data.tracking

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CareEventDao {
    @Query("SELECT * FROM care_events WHERE deletedAt IS NULL ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<CareEventEntity>>

    @Query("SELECT * FROM care_events WHERE id = :id LIMIT 1")
    suspend fun get(id: String): CareEventEntity?

    @Query("SELECT * FROM care_events WHERE childId = :childId AND endedAt IS NULL AND deletedAt IS NULL LIMIT 1")
    suspend fun activeForChild(childId: String): CareEventEntity?

    @Upsert
    suspend fun upsert(event: CareEventEntity)
}
