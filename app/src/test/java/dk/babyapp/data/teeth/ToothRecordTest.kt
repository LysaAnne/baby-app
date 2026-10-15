package dk.babyapp.data.teeth

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ToothRecordTest {
    private val today = LocalDate.of(2026, 10, 13)
    @Test fun chartHasTwentyDistinctMilkTeeth() {
        assertEquals(20, (upperMilkTeeth + lowerMilkTeeth).distinct().size)
        (upperMilkTeeth + lowerMilkTeeth).forEach { ToothRecord("child", it).validate(today) }
    }
    @Test fun lossCanHaveUnknownEruptionButCannotPrecedeKnownEruption() {
        ToothRecord("child", 51, lost = "2026-10-01").validate(today)
        assertTrue(runCatching { ToothRecord("child", 51, "2026-10-02", "2026-10-01").validate(today) }.isFailure)
        assertTrue(runCatching { ToothRecord("child", 51, "2026-10-14").validate(today) }.isFailure)
        assertTrue(runCatching { ToothRecord("child", 11).validate(today) }.isFailure)
    }
}
