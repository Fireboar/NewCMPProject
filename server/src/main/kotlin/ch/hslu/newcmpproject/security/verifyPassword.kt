package ch.hslu.newcmpproject.security

import ch.hslu.newcmpproject.entity.User

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
