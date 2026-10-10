package dk.babyapp.domain

import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.ChildProfile
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ChildAgeLabelTest {
    private val birth = LocalDate.of(2024, 1, 1)
    private val child = ChildProfile(name = "Freja", birthDate = birth)

    @Test fun `age changes units at boundaries`() {
        assertEquals("0 dage", child.ageLabel(birth))
        assertEquals("1 dag", child.ageLabel(birth.plusDays(1)))
        assertEquals("13 dage", child.ageLabel(birth.plusDays(13)))
        assertEquals("2 uger", child.ageLabel(birth.plusDays(14)))
        assertEquals("12 uger og 6 dage", child.ageLabel(birth.plusMonths(3).minusDays(1)))
        assertEquals("3 måneder", child.ageLabel(birth.plusMonths(3)))
        assertEquals("1 år, 11 måneder, 4 uger, 2 dage", child.ageLabel(birth.plusYears(2).minusDays(1)))
        assertEquals("2 år", child.ageLabel(birth.plusYears(2)))
        assertEquals("2 år, 7 dage", child.ageLabel(birth.plusYears(2).plusWeeks(1)))
        assertEquals("2 år, 21 dage", child.ageLabel(birth.plusYears(2).plusWeeks(3)))
    }

    @Test fun `missing future and expected dates do not show negative age`() {
        assertEquals("Ikke født endnu", child.copy(birthStatus = BirthStatus.Expected).ageLabel(birth))
        assertEquals("Alder ikke angivet", child.copy(birthDate = null).ageLabel(birth))
        assertEquals("Alder ikke angivet", child.ageLabel(birth.minusDays(1)))
    }

    @Test fun `leap day birthday stays calendar based`() {
        val leapChild = child.copy(birthDate = LocalDate.of(2024, 2, 29))
        assertEquals("2 år", leapChild.ageLabel(LocalDate.of(2026, 2, 28)))
        assertEquals("2 år, 7 dage", leapChild.ageLabel(LocalDate.of(2026, 3, 7)))
        assertEquals("3 måneder", child.copy(birthDate = LocalDate.of(2026, 1, 31)).ageLabel(LocalDate.of(2026, 4, 30)))
    }
}
