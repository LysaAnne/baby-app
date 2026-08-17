package dk.babyapp.data.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DanishHealthScheduleTest {
    @Test
    fun `templates contain seven Danish preventive child examinations`() {
        assertEquals(7, danishPreventiveExaminationTemplates.size)
        assertEquals("5-ugers børneundersøgelse", danishPreventiveExaminationTemplates.first().title)
        assertEquals("5-års børneundersøgelse", danishPreventiveExaminationTemplates.last().title)
    }

    @Test
    fun `templates contain current childhood vaccination milestones`() {
        assertEquals(11, danishVaccinationTemplates.size)
        assertTrue(danishVaccinationTemplates.any { it.vaccineName == "MFR" && it.title.startsWith("15 måneder") })
        assertTrue(danishVaccinationTemplates.any { it.vaccineName == "DiTeKiPol revaccination" })
        assertEquals(2, danishVaccinationTemplates.count { it.vaccineName == "HPV" })
    }

    @Test
    fun `templates do not create appointments or dates`() {
        val template = danishPreventiveExaminationTemplates.first()

        assertEquals("exam-5-weeks", template.key)
        assertEquals("5-ugers børneundersøgelse", template.title)
    }
}
