package application

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tables.Users
import tables.Words
import tables.UserStatisticsTable
import java.sql.Connection

object DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/german_words_db"
            driverClassName = "org.postgresql.Driver"
            username = "postgres"
            password = "password"
            maximumPoolSize = 10
        }

        dataSource = HikariDataSource(config)

        Database.connect(dataSource)
    }

    fun createTables() {
        transaction {
            SchemaUtils.create(Users, Words, UserStatisticsTable)
        }
    }

    fun getConnection(): Connection {
        if (!::dataSource.isInitialized) {
            throw IllegalStateException("DataSource is not initialized. Call init() first.")
        }
        return dataSource.connection
    }
}