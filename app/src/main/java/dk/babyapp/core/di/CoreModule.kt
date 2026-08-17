package dk.babyapp.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.babyapp.core.logging.AndroidAppLogger
import dk.babyapp.core.logging.AppLogger
import dk.babyapp.data.AppDatabase
import dk.babyapp.data.preferences.AppPreferencesRepository
import dk.babyapp.data.preferences.DataStoreAppPreferencesRepository
import dk.babyapp.data.profile.ChildProfileDao
import dk.babyapp.data.profile.ChildProfileRepository
import dk.babyapp.data.profile.DefaultChildProfileRepository
import dk.babyapp.data.profile.ProfilePhotoStore
import dk.babyapp.data.profile.ProfileImageStorage
import dk.babyapp.data.profile.ParentProfileDao
import dk.babyapp.data.profile.ParentProfileRepository
import dk.babyapp.data.profile.DefaultParentProfileRepository
import dk.babyapp.data.profile.CareProviderDao
import dk.babyapp.data.tracking.CareEventDao
import dk.babyapp.data.tracking.CareEventRepository
import dk.babyapp.data.tracking.DefaultCareEventRepository
import dk.babyapp.tracking.AndroidTimerNotificationController
import dk.babyapp.tracking.TimerNotificationController
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {
    @Binds
    @Singleton
    abstract fun bindAppLogger(implementation: AndroidAppLogger): AppLogger

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        implementation: DataStoreAppPreferencesRepository,
    ): AppPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindProfileImageStorage(implementation: ProfilePhotoStore): ProfileImageStorage

    @Binds
    @Singleton
    abstract fun bindTimerNotificationController(implementation: AndroidTimerNotificationController): TimerNotificationController

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "baby_app.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                .build()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN birthStatus TEXT NOT NULL DEFAULT 'Born'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN hospitalContact TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN gpContact TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN healthVisitorContact TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "hospitalEmail", "hospitalAddress", "hospitalNotes",
                    "gpEmail", "gpAddress", "gpNotes",
                    "healthVisitorEmail", "healthVisitorAddress", "healthVisitorNotes",
                    "midwife", "midwifeContact", "midwifeEmail", "midwifeAddress", "midwifeNotes", "cprNumber",
                ).forEach { column ->
                    db.execSQL("ALTER TABLE child_profiles ADD COLUMN $column TEXT NOT NULL DEFAULT ''")
                }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS parent_profiles (id TEXT NOT NULL, name TEXT NOT NULL, phone TEXT NOT NULL, email TEXT NOT NULL, cprNumber TEXT NOT NULL, avatar TEXT NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE TABLE IF NOT EXISTS child_parent_links (childId TEXT NOT NULL, parentId TEXT NOT NULL, PRIMARY KEY(childId, parentId), FOREIGN KEY(childId) REFERENCES child_profiles(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(parentId) REFERENCES parent_profiles(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_child_parent_links_childId ON child_parent_links(childId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_child_parent_links_parentId ON child_parent_links(parentId)")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE parent_profiles ADD COLUMN role TEXT NOT NULL DEFAULT 'Parent'")
                db.execSQL("ALTER TABLE parent_profiles ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE parent_profiles ADD COLUMN photoFileName TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN colorTheme TEXT NOT NULL DEFAULT 'Sage'")
                listOf("specialist", "specialistContact", "specialistEmail", "specialistAddress", "specialistNotes", "otherProviderTitle", "otherProvider", "otherProviderContact", "otherProviderEmail", "otherProviderAddress", "otherProviderNotes").forEach { db.execSQL("ALTER TABLE child_profiles ADD COLUMN $it TEXT NOT NULL DEFAULT ''") }
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS care_providers (id TEXT NOT NULL, childId TEXT NOT NULL, type TEXT NOT NULL, customTitle TEXT NOT NULL, name TEXT NOT NULL, phone TEXT NOT NULL, email TEXT NOT NULL, address TEXT NOT NULL, notes TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(childId) REFERENCES child_profiles(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_care_providers_childId ON care_providers(childId)")
                listOf(
                    arrayOf("Hospital", "hospital", "hospitalContact", "hospitalEmail", "hospitalAddress", "hospitalNotes", "''"),
                    arrayOf("Gp", "gp", "gpContact", "gpEmail", "gpAddress", "gpNotes", "''"),
                    arrayOf("HealthVisitor", "healthVisitor", "healthVisitorContact", "healthVisitorEmail", "healthVisitorAddress", "healthVisitorNotes", "''"),
                    arrayOf("Midwife", "midwife", "midwifeContact", "midwifeEmail", "midwifeAddress", "midwifeNotes", "''"),
                    arrayOf("Specialist", "specialist", "specialistContact", "specialistEmail", "specialistAddress", "specialistNotes", "''"),
                    arrayOf("Other", "otherProvider", "otherProviderContact", "otherProviderEmail", "otherProviderAddress", "otherProviderNotes", "otherProviderTitle"),
                ).forEach { values -> db.execSQL("INSERT INTO care_providers SELECT id || '-${values[0]}', id, '${values[0]}', ${values[6]}, ${values[1]}, ${values[2]}, ${values[3]}, ${values[4]}, ${values[5]} FROM child_profiles WHERE ${values[1]} != '' OR ${values[2]} != '' OR ${values[6]} != ''") }
            }
        }
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS care_events (id TEXT NOT NULL, childId TEXT NOT NULL, type TEXT NOT NULL, startedAt INTEGER NOT NULL, endedAt INTEGER, runningSince INTEGER, activeSide TEXT, leftSeconds INTEGER NOT NULL, rightSeconds INTEGER NOT NULL, amountOfferedMl INTEGER, amountConsumedMl INTEGER, pumpedAmountMl INTEGER, bottleContent TEXT, diaperType TEXT, observation TEXT NOT NULL, notes TEXT NOT NULL, deletedAt INTEGER, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_care_events_childId ON care_events(childId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_care_events_startedAt ON care_events(startedAt)")
            }
        }
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN fullName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN registeredAddress TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE child_profiles ADD COLUMN nationality TEXT NOT NULL DEFAULT ''")
            }
        }

        @Provides
        fun provideChildProfileDao(database: AppDatabase): ChildProfileDao = database.childProfileDao()
        @Provides fun provideParentProfileDao(database: AppDatabase): ParentProfileDao = database.parentProfileDao()
        @Provides fun provideCareProviderDao(database: AppDatabase): CareProviderDao = database.careProviderDao()
        @Provides fun provideCareEventDao(database: AppDatabase): CareEventDao = database.careEventDao()
        @Provides @Singleton fun provideCareEventRepository(dao: CareEventDao): CareEventRepository = DefaultCareEventRepository(dao)
        @Provides @Singleton fun provideParentProfileRepository(dao: ParentProfileDao, photoStore: ProfileImageStorage): ParentProfileRepository = DefaultParentProfileRepository(dao, photoStore)

        @Provides
        @Singleton
        fun provideChildProfileRepository(
            dao: ChildProfileDao,
            photoStore: ProfileImageStorage,
            careProviderDao: CareProviderDao,
        ): ChildProfileRepository = DefaultChildProfileRepository(dao, photoStore, careProviderDao)
    }
}
