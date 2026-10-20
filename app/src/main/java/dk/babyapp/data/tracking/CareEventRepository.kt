package dk.babyapp.data.tracking

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Registrations are read in the smallest slice a screen needs: loading years of history takes
 * around a second and is repeated after every change while it is observed.
 */
interface CareEventRepository {
    /** The whole history of one child. Observe only while a screen shows history. */
    fun eventsForChild(childId: String): Flow<List<CareEventEntity>>
    /** Registrations started at or after [from] plus unfinished timers and drafts. */
    fun recentEventsForChild(childId: String, from: Long): Flow<List<CareEventEntity>>
    /** Started timers of every child, newest first. */
    val unfinishedEvents: Flow<List<CareEventEntity>>
    suspend fun unfinished(): List<CareEventEntity>
    suspend fun sleepsOverlapping(childId: String, start: Long, end: Long): List<CareEventEntity>
    suspend fun save(event: CareEventEntity)
    suspend fun get(id: String): CareEventEntity?
    suspend fun activeForChild(childId: String): CareEventEntity?
    suspend fun softDelete(event: CareEventEntity)
}

class DefaultCareEventRepository @Inject constructor(
    private val dao: CareEventDao,
) : CareEventRepository {
    override fun eventsForChild(childId: String) = dao.observeForChild(childId)
    override fun recentEventsForChild(childId: String, from: Long) = dao.observeRecentForChild(childId, from)
    override val unfinishedEvents = dao.observeUnfinished()
    override suspend fun unfinished() = dao.unfinished()
    override suspend fun sleepsOverlapping(childId: String, start: Long, end: Long) = dao.sleepsOverlapping(childId, start, end)
    override suspend fun save(event: CareEventEntity) = dao.upsert(event.copy(updatedAt = System.currentTimeMillis()))
    override suspend fun get(id: String) = dao.get(id)
    override suspend fun activeForChild(childId: String) = dao.activeForChild(childId)
    override suspend fun softDelete(event: CareEventEntity) =
        dao.upsert(event.copy(deletedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
}
