package ch.hslu.newcmpproject.security

import ch.hslu.newcmpproject.domain.entity.User
import java.security.MessageDigest
import java.security.SecureRandom

class PasswordService {

    fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Invalid hex string" }
        return hex.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun verifyPassword(password: String, user: User): Boolean {
        // Salt zurück in ByteArray konvertieren
        val saltBytes = hexToBytes(user.salt)

        // Passwort hashen
        val hashAttempt = hashPasswordWithSalt(password, saltBytes)

        // Vergleich
        return hashAttempt == user.passwordHash
    }

    fun hashPasswordWithSalt(password: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        // Combine password bytes with salt
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val saltedPassword = passwordBytes + salt
        val hash = digest.digest(saltedPassword)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun generateSalt(length: Int = 16): ByteArray {
        val salt = ByteArray(length)
        SecureRandom().nextBytes(salt)
        return salt
    }

}
