package ch.hslu.newcmpproject.security
import java.security.MessageDigest
import java.security.SecureRandom

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