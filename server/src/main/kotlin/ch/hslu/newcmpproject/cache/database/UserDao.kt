package ch.hslu.newcmpproject.cache.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import ch.hslu.cmpproject.cache.AppDatabaseQueries
import ch.hslu.newcmpproject.cache.database.mapper.UserMapper
import ch.hslu.newcmpproject.entity.User
import ch.hslu.newcmpproject.security.PasswordService

class UserDao(private val queries: AppDatabaseQueries) {

    private val passwordService = PasswordService()

    suspend fun getAll(): List<User> =
        queries.selectAllUsers(UserMapper::map).awaitAsList()

    suspend fun getById(id: Long): User? =
        queries.selectUserById(id, UserMapper::map).awaitAsOneOrNull()

    suspend fun getByUsername(username: String): User? =
        queries.selectUserByUsername(username, UserMapper::map).awaitAsOneOrNull()

    suspend fun insert(username: String, password: String, role: String): Long {
        val salt = passwordService.generateSalt()
        val hash = passwordService.hashPasswordWithSalt(password, salt)
        val user = insertSecure(
            username = username,
            passwordHash = hash,
            salt = salt,
            role= role)
        return user.id
    }

    suspend fun insertSecure(
        username: String,
        passwordHash: String,
        salt: ByteArray,
        role: String
    ): User = queries.transactionWithResult {
        // Salt in Hex umwandeln
        val saltHex = salt.joinToString("") { "%02x".format(it) }

        // User einfügen
        queries.insertUser(
            username = username,
            passwordHash = passwordHash,
            salt = saltHex,
            role = role
        )

        // Letzte ID holen
        val newId = queries.lastInsertRowId().awaitAsOne()
        if (newId == 0L) {
            throw IllegalStateException("Insert failed: no ID returned.")
        }

        // User anhand der ID abrufen
        queries.selectUserById(newId, UserMapper::map).awaitAsOne()
    }

    // Folgender Code

    suspend fun updateUsername(id: Long, username: String) =
        queries.updateUsername(username, id)

    suspend fun updatePassword(id: Long, newPassword:String) {
        val newSalt = passwordService.generateSalt()
        val newPasswordHash = passwordService.hashPasswordWithSalt(
            password = newPassword,
            salt = newSalt
        )
        updatePasswordSecure(
            id,
            newPasswordHash,
            newSalt.joinToString("") { "%02x".format(it) })
    }

    suspend fun updatePasswordSecure(id: Long, hash: String, salt: String) =
        queries.updatePassword(hash, salt, id)

    suspend fun delete(id: Long) =
        queries.deleteUserById(id)
}