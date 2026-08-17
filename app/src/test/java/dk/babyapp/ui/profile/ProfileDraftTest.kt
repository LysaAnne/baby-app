package dk.babyapp.ui.profile

import java.time.LocalDate
import dk.babyapp.data.profile.BirthStatus
import dk.babyapp.data.profile.BiologicalSex
import dk.babyapp.data.preferences.MeasurementUnits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileDraftTest {
    @Test
    fun `valid required profile fields pass validation`() {
        val draft = ProfileDraft(name = "Alma", birthDate = "2026-01-20", sex = BiologicalSex.Female)

        assertNull(draft.validate(LocalDate.of(2026, 2, 1)))
    }

    @Test
    fun `future birth date is rejected`() {
        val draft = ProfileDraft(name = "Alma", birthDate = "2026-02-02")

        assertEquals(
            ProfileValidationError.BirthDateInFuture,
            draft.validate(LocalDate.of(2026, 2, 1)),
        )
    }

    @Test
    fun `invalid gestational days are rejected`() {
        val draft = ProfileDraft(
            name = "Alma",
            birthDate = "2026-01-20",
            gestationalWeeks = "35",
            gestationalDays = "7",
            sex = BiologicalSex.Female,
        )

        assertEquals(
            ProfileValidationError.InvalidGestation,
            draft.validate(LocalDate.of(2026, 2, 1)),
        )
    }

    @Test
    fun `expected child requires due date but not birth date`() {
        val missing = ProfileDraft(name = "Baby", birthStatus = BirthStatus.Expected, sex = BiologicalSex.PreferNotToSay)
        val valid = missing.copy(dueDate = "2026-10-01")

        assertEquals(ProfileValidationError.DueDateRequired, missing.validate(LocalDate.of(2026, 2, 1)))
        assertNull(valid.validate(LocalDate.of(2026, 2, 1)))
    }

    @Test
    fun `imperial measurements are converted to canonical metric values`() {
        val profile = ProfileDraft(
            name = "Alma", birthDate = "2026-01-20", birthWeightGrams = "7.5", birthLengthCm = "20",
        ).toProfile(units = MeasurementUnits.Imperial)

        assertEquals(3402, profile.birthWeightGrams)
        assertEquals(50.8, profile.birthLengthCm!!, 0.001)
    }

    @Test
    fun `due date and registry information remain when a born child is saved`() {
        val profile = ProfileDraft(
            name = "Alma",
            birthDate = "2026-01-20",
            dueDate = "2026-01-25",
            sex = BiologicalSex.Female,
            fullName = "Alma Test Jensen",
            registeredAddress = "Testvej 1",
            nationality = "Dansk",
        ).toProfile()

        assertEquals(LocalDate.of(2026, 1, 25), profile.dueDate)
        assertEquals("Alma Test Jensen", profile.fullName)
        assertEquals("Testvej 1", profile.registeredAddress)
        assertEquals("Dansk", profile.nationality)
    }
}
