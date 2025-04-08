package tables

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table("users") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val token = varchar("token", 255).nullable()

    fun getUser(username: String): ResultRow? = transaction {
        select { Users.username eq username }.singleOrNull()
    }

    fun updateToken(username: String, token: String?) {
        transaction {
            update({ Users.username eq username }) {
                it[Users.token] = token
            }
        }
    }

    fun updatePassword(username: String, newPasswordHash: String): Boolean {
        return transaction {
            update({ Users.username eq username }) {
                it[passwordHash] = newPasswordHash // Принимаем уже хешированный пароль
            } > 0
        }
    }

    fun updateUsername(oldUsername: String, newUsername: String): Boolean {
        return transaction {
            if (userExists(newUsername)) {
                false
            } else {
                update({ Users.username eq oldUsername }) {
                    it[username] = newUsername
                } > 0
            }
        }
    }

    fun insertUser(username: String, passwordHash: String) {
        transaction {
            insert {
                it[Users.username] = username
                it[Users.passwordHash] = passwordHash
                it[token] = null
            }
        }
    }

    private fun userExists(username: String): Boolean {
        return transaction {
            select { Users.username eq username }.count() > 0
        }
    }
}