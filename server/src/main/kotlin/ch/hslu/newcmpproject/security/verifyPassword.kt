package ch.hslu.newcmpproject.security

import ch.hslu.newcmpproject.entity.User

fun verifyPassword(password: String, user: User): Boolean {
    // Hex-Salt in ByteArray konvertieren
    val saltBytes = user.salt.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()

    val hashAttempt = hashPasswordWithSalt(password, saltBytes)
    return hashAttempt == user.passwordHash
}