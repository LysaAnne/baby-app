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
import kotlinx.coroutines.flow.map

data class ParentProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String = "",
    val email: String = "",
    val cprNumber: String = "",
    val avatar: ProfileAvatar = ProfileAvatar.Bear,
    val role: FamilyMemberRole = FamilyMemberRole.ParentNotSpecified,
    val notes: String = "",
    val photoFileName: String? = null,
)

enum class FamilyMemberRole {
    Mother, Father, CoMother, CoFather, Parent, ParentNotSpecified,
    Grandmother, Grandfather, Grandparent,
    BonusMother, BonusFather, BonusParent,
    Sister, Brother, Sibling, BonusSibling,
    Aunt, Uncle, MaternalAunt, PaternalAunt, MaternalUncle, PaternalUncle,
    Cousin, OtherNotSpecified;

    val isParent: Boolean get() = this in setOf(Mother, Father, CoMother, CoFather, Parent, ParentNotSpecified)
}

@Entity(tableName = "parent_profiles")
data class ParentProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val cprNumber: String,
    val avatar: String,
    val role: String,
    val notes: String,
    val photoFileName: String?,
)

@Entity(
    tableName = "child_parent_links",
    primaryKeys = ["childId", "parentId"],
    foreignKeys = [
        ForeignKey(entity = ChildProfileEntity::class, parentColumns = ["id"], childColumns = ["childId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ParentProfileEntity::class, parentColumns = ["id"], childColumns = ["parentId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("childId"), Index("parentId")],
)
data class ChildParentLink(val childId: String, val parentId: String)

@Dao
interface ParentProfileDao {
    @Query("SELECT * FROM parent_profiles ORDER BY name COLLATE NOCASE") fun observeParents(): Flow<List<ParentProfileEntity>>
    @Query("SELECT * FROM child_parent_links") fun observeLinks(): Flow<List<ChildParentLink>>
    @Upsert suspend fun upsert(parent: ParentProfileEntity)
    @Query("DELETE FROM parent_profiles WHERE id = :id") suspend fun delete(id: String)
    @Query("DELETE FROM child_parent_links WHERE childId = :childId") suspend fun clearLinks(childId: String)
    @Query("DELETE FROM child_parent_links WHERE parentId = :parentId") suspend fun clearMemberLinks(parentId: String)
    @Upsert suspend fun upsertLinks(links: List<ChildParentLink>)
}

fun ParentProfile.toEntity() = ParentProfileEntity(id, name, phone, email, cprNumber, avatar.name, role.name, notes, photoFileName)
fun ParentProfileEntity.toModel() = ParentProfile(id, name, phone, email, cprNumber, enumValues<ProfileAvatar>().firstOrNull { it.name == avatar } ?: ProfileAvatar.Bear, enumValues<FamilyMemberRole>().firstOrNull { it.name == role } ?: FamilyMemberRole.ParentNotSpecified, notes, photoFileName)

interface ParentProfileRepository {
    val parents: Flow<List<ParentProfile>>
    val links: Flow<List<ChildParentLink>>
    suspend fun save(parent: ParentProfile)
    suspend fun delete(parent: ParentProfile)
    suspend fun setParents(childId: String, parentIds: Set<String>)
    suspend fun setChildren(memberId: String, childIds: Set<String>)
}

class DefaultParentProfileRepository(private val dao: ParentProfileDao, private val photoStore: ProfileImageStorage) : ParentProfileRepository {
    override val parents = dao.observeParents().map { list -> list.map(ParentProfileEntity::toModel) }
    override val links = dao.observeLinks()
    override suspend fun save(parent: ParentProfile) = dao.upsert(parent.toEntity())
    override suspend fun delete(parent: ParentProfile) { dao.delete(parent.id); parent.photoFileName?.let(photoStore::delete) }
    override suspend fun setParents(childId: String, parentIds: Set<String>) {
        dao.clearLinks(childId)
        if (parentIds.isNotEmpty()) dao.upsertLinks(parentIds.map { ChildParentLink(childId, it) })
    }
    override suspend fun setChildren(memberId: String, childIds: Set<String>) {
        dao.clearMemberLinks(memberId)
        if (childIds.isNotEmpty()) dao.upsertLinks(childIds.map { ChildParentLink(it, memberId) })
    }
}
