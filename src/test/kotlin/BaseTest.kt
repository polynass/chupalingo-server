import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import tables.UserStatisticsTable
import tables.Users
import tables.Words


open class BaseTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun init() {
            Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver"
            )

            transaction {
                SchemaUtils.create(
                    // сюда добавь свои таблицы:
                    UserStatisticsTable,
                    Words,
                    Users
                )
            }
        }
    }


}