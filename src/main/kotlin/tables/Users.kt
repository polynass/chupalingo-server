package tables

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private const val USERNAME_MAX = 50
private const val PASSWORD_HASH_MAX = 255
private const val TOKEN_MAX = 255

object Users : Table("users") {
    val username: Column<String> = varchar("username", USERNAME_MAX).uniqueIndex()
    val passwordHash: Column<String> = varchar("password_hash", PASSWORD_HASH_MAX)
    val token: Column<String?> = varchar("token", TOKEN_MAX).nullable()

    fun getUser(username: String): ResultRow? = transaction { select { Users.username eq username }.singleOrNull() }

    fun updateToken(username: String, token: String?) =
        transaction { update({ Users.username eq username }) { it[Users.token] = token } }

    fun updatePassword(username: String, newPasswordHash: String): Boolean =
        transaction { update({ Users.username eq username }) { it[passwordHash] = newPasswordHash } > 0 }

    fun updateUsername(oldUsername: String, newUsername: String): Boolean = transaction {
        if (userExists(newUsername)) {
            false
        } else {
            update({ Users.username eq oldUsername }) { it[username] = newUsername } > 0
        }
    }

    fun insertUser(username: String, passwordHash: String) = transaction {
        insert { it[Users.username] = username; it[Users.passwordHash] = passwordHash; it[token] = null }
    }

    private fun userExists(username: String): Boolean =
        transaction { select { Users.username eq username }.count() > 0 }
}
