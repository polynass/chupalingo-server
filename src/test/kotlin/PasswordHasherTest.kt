import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import utils.PasswordHasher

class PasswordHasherTest {

    @Test
    fun `hash should generate non-empty string`() {
        val password = "test123"
        val hash = PasswordHasher.hash(password)

        assertNotNull(hash)
        assertTrue(hash.contains(":"))
    }

    @Test
    fun `verify should return true for correct password`() {
        val password = "securePassword"
        val hash = PasswordHasher.hash(password)

        val result = PasswordHasher.verify(password, hash)

        assertTrue(result)
    }

    @Test
    fun `verify should return false for incorrect password`() {
        val password = "securePassword"
        val hash = PasswordHasher.hash(password)

        val result = PasswordHasher.verify("wrongPassword", hash)

        assertFalse(result)
    }

    @Test
    fun `verify should return false for invalid hash format`() {
        val result = PasswordHasher.verify("test", "invalid_hash")

        assertFalse(result)
    }
}