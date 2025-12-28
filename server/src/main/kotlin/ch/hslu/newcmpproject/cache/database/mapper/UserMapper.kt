package ch.hslu.newcmpproject.cache.database.mapper

import ch.hslu.newcmpproject.entity.User

object UserMapper {
    fun map(
        id: Long,
        username: String,
        passwordHash: String,
        salt: String,
        role: String
    ) = User(id, username, passwordHash, salt, role)
}