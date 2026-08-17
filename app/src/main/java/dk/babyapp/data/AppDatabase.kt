package dk.babyapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dk.babyapp.data.profile.ChildProfileDao
import dk.babyapp.data.profile.ChildProfileEntity
import dk.babyapp.data.profile.ParentProfileEntity
import dk.babyapp.data.profile.ChildParentLink
import dk.babyapp.data.profile.ParentProfileDao
import dk.babyapp.data.profile.CareProviderEntity
import dk.babyapp.data.profile.CareProviderDao
import dk.babyapp.data.tracking.CareEventDao
import dk.babyapp.data.tracking.CareEventEntity

@Database(
    entities = [ChildProfileEntity::class, ParentProfileEntity::class, ChildParentLink::class, CareProviderEntity::class, CareEventEntity::class],
    version = 13,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun childProfileDao(): ChildProfileDao
    abstract fun parentProfileDao(): ParentProfileDao
    abstract fun careProviderDao(): CareProviderDao
    abstract fun careEventDao(): CareEventDao
}
