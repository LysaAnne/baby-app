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

    @Test
    fun narrowQueriesReturnExactlyTheirSlice() = runTest {
        val day = 86_400_000L
        val old = CareEventEntity(id = "old", childId = "child-1", type = CareEventType.Diaper, startedAt = 1 * day, endedAt = 1 * day)
        val oldTimer = CareEventEntity(id = "old-timer", childId = "child-1", type = CareEventType.Pumping, startedAt = 2 * day, runningSince = 2 * day)
        val oldDraft = CareEventEntity(id = "old-draft", childId = "child-1", type = CareEventType.Sleep, startedAt = 3 * day, endedAt = 3 * day + 1000, isDraft = true)
        val recent = CareEventEntity(id = "recent", childId = "child-1", type = CareEventType.Sleep, startedAt = 50 * day, endedAt = 50 * day + 3_600_000)
        val deleted = recent.copy(id = "deleted", deletedAt = 51 * day)
        val otherChild = CareEventEntity(id = "other", childId = "child-2", type = CareEventType.Sleep, startedAt = 50 * day, runningSince = 50 * day)
        listOf(old, oldTimer, oldDraft, recent, deleted, otherChild).forEach { dao.upsert(it) }

        assertEquals(listOf("recent", "old-draft", "old-timer", "old"), dao.observeForChild("child-1").first().map { it.id })
        assertEquals(listOf("recent", "old-draft", "old-timer"), dao.observeRecentForChild("child-1", 40 * day).first().map { it.id })
        assertEquals(setOf("old-timer", "other"), dao.unfinished().map { it.id }.toSet())
        assertEquals(listOf("other", "old-timer"), dao.observeUnfinished().first().map { it.id })
        assertEquals(listOf("recent"), dao.sleepsOverlapping("child-1", 50 * day + 1_800_000, 50 * day + 7_200_000).map { it.id })
        assertEquals(emptyList<String>(), dao.sleepsOverlapping("child-1", 50 * day + 3_600_000, 50 * day + 7_200_000).map { it.id })
        // An unfinished sleep counts as lasting until now.
        assertEquals(listOf("other"), dao.sleepsOverlapping("child-2", 60 * day, 61 * day).map { it.id })
    }
}
