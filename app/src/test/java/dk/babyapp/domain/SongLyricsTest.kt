package dk.babyapp.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class SongLyricsTest {
    @Test fun everyLyricsKeyBelongsToAnExistingSong() {
        assertTrue(songLyrics.isNotEmpty())
        assertTrue(songs.map { it.id }.containsAll(songLyrics.keys))
    }

    @Test fun noLyricsAreBlank() {
        songLyrics.forEach { (id, text) -> assertTrue(id, text.isNotBlank()) }
    }

    @Test fun linesHaveNoTrailingWhitespace() {
        songLyrics.forEach { (id, text) ->
            assertTrue(id, text.lines().all { it == it.trimEnd() })
        }
    }
}
