package dk.babyapp.data.tracking

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface CareEventRepository {
    val events: Flow<List<CareEventEntity>>
    suspend fun save(event: CareEventEntity)
    suspend fun get(id: String): CareEventEntity?
    suspend fun activeForChild(childId: String): CareEventEntity?
    suspend fun softDelete(event: CareEventEntity)
}

class DefaultCareEventRepository @Inject constructor(
    private val dao: CareEventDao,
) : CareEventRepository {
    override val events = dao.observeAll()
    override suspend fun save(event: CareEventEntity) = dao.upsert(event.copy(updatedAt = System.currentTimeMillis()))
    override suspend fun get(id: String) = dao.get(id)
    override suspend fun activeForChild(childId: String) = dao.activeForChild(childId)
    override suspend fun softDelete(event: CareEventEntity) =
        dao.upsert(event.copy(deletedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()))
}
