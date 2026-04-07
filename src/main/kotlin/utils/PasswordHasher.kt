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
    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return "${salt.toHex()}:${hash.toHex()}"
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != HEX_PAIR_LENGTH) return false

        val salt = parts[0].hexToBytes()
        val hash = parts[1].hexToBytes()
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val testHash = factory.generateSecret(spec).encoded
        return hash.contentEquals(testHash)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        val len = length
        val data = ByteArray(len / HEX_PAIR_LENGTH)
        var i = 0
        while (i < len) {
            data[i / HEX_PAIR_LENGTH] =
                (
                    (Character.digit(this[i], HEX_RADIX) shl HALF_BYTE_SHIFT) +
                        Character.digit(this[i + 1], HEX_RADIX)
                    ).toByte()
            i += HEX_PAIR_LENGTH
        }
        return data
    }
}
