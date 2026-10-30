package dk.babyapp.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SongbookImportTest {
    // Made-up words keep import assertions independent of built-in lyrics.
    @Test fun splitsPastedTextIntoKnownAndNewSongs() {
        val text = """
            Noter fra min telefon

            MESTER JAKOB!
            første linje
            anden linje

            andet vers

            # Vores egen godnatsang
            la la la

            Bjørnen sover
        """.trimIndent()
        val songs = parseLyricsImport(text, dk.babyapp.domain.songs)
        assertEquals(listOf("Mester Jakob", "Vores egen godnatsang"), songs.map { it.title })
        assertEquals("mester-jakob", songs[0].songId)
        assertEquals("første linje\nanden linje\n\nandet vers", songs[0].words)
        assertEquals(null, songs[1].songId)
        assertEquals("la la la", songs[1].words)
    }

    @Test fun theUgleSongsHaveLinksAndTheListHasNoDuplicates() {
        assertEquals(songs.size, songs.map { it.id }.distinct().size)
        assertEquals(songs.size, songs.map { it.title.lowercase() }.distinct().size)
        assertEquals(61, songs.count { it.link.startsWith("https://ugle.dk/") })
    }
}
