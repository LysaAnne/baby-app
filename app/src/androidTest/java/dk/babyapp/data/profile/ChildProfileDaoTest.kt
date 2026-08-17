package dk.babyapp.data.profile

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dk.babyapp.data.AppDatabase
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChildProfileDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ChildProfileDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.childProfileDao()
    }

    @After
    fun closeDatabase() = database.close()

    @Test
    fun profileCanBeInsertedUpdatedAndDeleted() = runTest {
        val profile = ChildProfile(id = "child-1", name = "Alma", birthDate = LocalDate.of(2026, 1, 20))
        dao.upsert(profile.toEntity())

        assertEquals("Alma", dao.getById("child-1")?.name)
        dao.upsert(profile.copy(name = "Alma Marie").toEntity())
        assertEquals("Alma Marie", dao.observeAll().first().single().name)

        dao.delete(profile.copy(name = "Alma Marie").toEntity())
        assertNull(dao.getById("child-1"))
    }
}

