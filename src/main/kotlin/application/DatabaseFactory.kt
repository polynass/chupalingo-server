package application

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tables.UserStatisticsTable
import tables.Users
import tables.Words
import java.sql.Connection

private const val MAX_POOL_SIZE = 10

object DatabaseFactory {
    private lateinit var dataSource: HikariDataSource

    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/german_words_db"
            driverClassName = "org.postgresql.Driver"
            username = "postgres"
            password = "password"
            maximumPoolSize = MAX_POOL_SIZE
        }
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }

    fun createTables() = transaction { SchemaUtils.create(Users, Words, UserStatisticsTable) }

    fun getConnection(): Connection {
        check(::dataSource.isInitialized) { "DataSource is not initialized. Call init() first." }
        return dataSource.connection
    }
}
