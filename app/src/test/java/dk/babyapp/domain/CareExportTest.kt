package dk.babyapp.domain

import dk.babyapp.data.profile.ChildProfile
import dk.babyapp.data.tracking.CareEventEntity
import dk.babyapp.data.tracking.CareEventType
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareExportTest {
    @Test fun `csv identifies child range units timezone and excludes other children`() {
        val zone = ZoneId.of("Europe/Copenhagen")
        val day = LocalDate.of(2026, 8, 18)
        val time = day.atStartOfDay(zone).toInstant().toEpochMilli()
        val csv = createCareCsv(
            ChildProfile(id = "a", name = "Freja"),
            listOf(
                CareEventEntity(id = "included", childId = "a", type = CareEventType.Diaper, startedAt = time, endedAt = time),
                CareEventEntity(id = "excluded", childId = "b", type = CareEventType.Diaper, startedAt = time, endedAt = time),
            ),
            day, day, "Metrisk", zone,
        )
        assertTrue(csv.contains("Freja")); assertTrue(csv.contains("Europe/Copenhagen")); assertTrue(csv.contains("included")); assertFalse(csv.contains("excluded"))
    }
}
