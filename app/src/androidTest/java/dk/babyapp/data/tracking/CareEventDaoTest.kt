package dk.babyapp.data.tracking

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dk.babyapp.data.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CareEventDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: CareEventDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java).build()
        dao = database.careEventDao()
    }

    @After fun closeDatabase() = database.close()

    @Test
    fun eventCanBeUpdatedAndSoftDeleted() = runTest {
        val event = CareEventEntity(id = "event-1", childId = "child-1", type = CareEventType.Diaper, startedAt = 100, endedAt = 100, diaperType = DiaperType.Wet)
        dao.upsert(event)
        assertEquals(DiaperType.Wet, dao.observeAll().first().single().diaperType)

        dao.upsert(event.copy(diaperType = DiaperType.Both))
        assertEquals(DiaperType.Both, dao.get("event-1")?.diaperType)

        dao.upsert(event.copy(deletedAt = 200))
        assertEquals(emptyList<CareEventEntity>(), dao.observeAll().first())
    }

    @Test
    fun activeTimerNeverMovesBetweenChildren() = runTest {
        dao.upsert(CareEventEntity(id = "event-1", childId = "child-1", type = CareEventType.Pumping, startedAt = 100, runningSince = 100))
        assertEquals("event-1", dao.activeForChild("child-1")?.id)
        assertNull(dao.activeForChild("child-2"))
    }
}
