package dk.babyapp.data.color

import kotlinx.coroutines.flow.Flow

interface ColorProfileRepository {
    val profiles: Flow<List<ColorProfile>>
    suspend fun save(profile: ColorProfile)
    suspend fun delete(id: String)
    suspend fun replaceAll(profiles: List<ColorProfile>)
    suspend fun move(id: String, direction: Int)
}
