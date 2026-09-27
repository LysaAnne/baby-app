package dk.babyapp.data.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class EncryptedBackupTest {
    @Test fun `encrypted backup round trips and does not expose plaintext`() {
        val plain = "Freja CPR og registreringer".toByteArray()
        val encrypted = encrypt(plain, "hemmelig123".toCharArray())
        assertFalse(String(encrypted).contains("Freja"))
        assertArrayEquals(plain, decrypt(encrypted, "hemmelig123".toCharArray()))
    }
}
