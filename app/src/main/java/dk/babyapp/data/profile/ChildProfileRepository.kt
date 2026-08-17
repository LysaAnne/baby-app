package dk.babyapp.data.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ChildProfileRepository {
    val profiles: Flow<List<ChildProfile>>
    val careProviders: Flow<List<CareProvider>>
    suspend fun get(id: String): ChildProfile?
    suspend fun save(profile: ChildProfile)
    suspend fun delete(profile: ChildProfile)
    suspend fun setCareProviders(childId: String, providers: List<CareProvider>)
}

class DefaultChildProfileRepository(
    private val dao: ChildProfileDao,
    private val photoStore: ProfileImageStorage,
    private val careProviderDao: CareProviderDao,
) : ChildProfileRepository {
    override val profiles: Flow<List<ChildProfile>> =
        dao.observeAll().map { entities -> entities.map { it.toModel() } }
    override val careProviders = careProviderDao.observeAll().map { items -> items.map(CareProviderEntity::toModel) }

    override suspend fun get(id: String): ChildProfile? = dao.getById(id)?.toModel()

    override suspend fun save(profile: ChildProfile) {
        val previous = dao.getById(profile.id)?.toModel()
        dao.upsert(profile.toEntity())
        if (previous?.photoFileName != null && previous.photoFileName != profile.photoFileName) {
            photoStore.delete(previous.photoFileName)
        }
    }

    override suspend fun delete(profile: ChildProfile) {
        dao.delete(profile.toEntity())
        profile.photoFileName?.let(photoStore::delete)
    }
    override suspend fun setCareProviders(childId: String, providers: List<CareProvider>) {
        careProviderDao.deleteForChild(childId)
        if (providers.isNotEmpty()) careProviderDao.upsertAll(providers.map { it.toEntity(childId) })
    }
}
