package ch.hslu.newcmpproject.cache.database.mapper

import ch.hslu.newcmpproject.domain.entity.User

object UserMapper {
    fun map(
        id: Long,
        username: String,
        passwordHash: String,
        salt: String,
        role: String
    ) = User(id, username, passwordHash, salt, role)
}