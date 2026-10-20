package dk.babyapp.data.tracking

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CareEventDao {
    @Query("SELECT * FROM care_events WHERE deletedAt IS NULL ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<CareEventEntity>>

    @Query("SELECT * FROM care_events WHERE childId = :childId AND deletedAt IS NULL ORDER BY startedAt DESC")
    fun observeForChild(childId: String): Flow<List<CareEventEntity>>

    /** Started timers of every child, paused or running; stopped drafts have an end time and are not included. */
    /** Everything Today needs: recent registrations plus unfinished timers and drafts of any age. */
    @Query("SELECT * FROM care_events WHERE childId = :childId AND deletedAt IS NULL AND (startedAt >= :from OR endedAt IS NULL OR isDraft = 1) ORDER BY startedAt DESC")
    fun observeRecentForChild(childId: String, from: Long): Flow<List<CareEventEntity>>

    @Query("SELECT * FROM care_events WHERE endedAt IS NULL AND deletedAt IS NULL ORDER BY startedAt DESC")
    fun observeUnfinished(): Flow<List<CareEventEntity>>

    @Query("SELECT * FROM care_events WHERE endedAt IS NULL AND deletedAt IS NULL")
    suspend fun unfinished(): List<CareEventEntity>

    /** Sleep of one child that may overlap [start, end]; an unfinished sleep counts as lasting until now. */
    @Query("SELECT * FROM care_events WHERE childId = :childId AND type = 'Sleep' AND deletedAt IS NULL AND startedAt < :end AND (endedAt IS NULL OR endedAt > :start)")
    suspend fun sleepsOverlapping(childId: String, start: Long, end: Long): List<CareEventEntity>

    @Query("SELECT * FROM care_events WHERE id = :id LIMIT 1")
    suspend fun get(id: String): CareEventEntity?

    @Query("SELECT * FROM care_events WHERE childId = :childId AND endedAt IS NULL AND deletedAt IS NULL LIMIT 1")
    suspend fun activeForChild(childId: String): CareEventEntity?

    @Upsert
    suspend fun upsert(event: CareEventEntity)
}
