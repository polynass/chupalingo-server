package utils

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private const val SALT_LENGTH = 16
private const val ITERATIONS = 10000
private const val KEY_LENGTH = 256
private const val ALGORITHM = "PBKDF2WithHmacSHA256"
private const val HEX_RADIX = 16
private const val HEX_PAIR_LENGTH = 2
private const val HALF_BYTE_SHIFT = 4

object PasswordHasher {

    private const val MAX_PASSWORD_LENGTH = 64

    fun hash(password: String): String {
        require(password.length <= MAX_PASSWORD_LENGTH) {
            "Password too long"
        }

        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return "${salt.toHex()}:${hash.toHex()}"
    }

    fun verify(password: String, storedHash: String): Boolean {
        if (password.length > MAX_PASSWORD_LENGTH) return false

        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val salt = parts[0].hexToBytes()
        val hash = parts[1].hexToBytes()

        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val testHash = factory.generateSecret(spec).encoded

        return hash.contentEquals(testHash)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        val data = ByteArray(length / 2)
        var i = 0
        while (i < length) {
            data[i / 2] =
                ((Character.digit(this[i], 16) shl 4) +
                        Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
