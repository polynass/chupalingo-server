package tables

import models.UserStatisticsData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object UserStatisticsTable : Table("user_statistics") {
    val userId = varchar("user_id", 50).uniqueIndex() // ID пользователя
    val learnedWords = integer("learned_words").default(0) // Количество изученных слов
    val solvedTests = integer("solved_tests").default(0) // Количество решённых тестов
    val mistakes = integer("mistakes").default(0) // Количество ошибок

    override val primaryKey = PrimaryKey(userId)

    fun getStatistics(userId: String): UserStatisticsData? {
        return transaction {
            select { UserStatisticsTable.userId eq userId }
                .singleOrNull()
                ?.let { row ->
                    UserStatisticsData(
                        userId = row[UserStatisticsTable.userId],
                        learnedWords = row[UserStatisticsTable.learnedWords],
                        solvedTests = row[UserStatisticsTable.solvedTests],
                        mistakes = row[UserStatisticsTable.mistakes]
                    )
                }
        }
    }

    fun updateStatistics(userId: String, learnedWords: Int = 0, solvedTests: Int = 0, mistakes: Int = 0) {
        transaction {
            val existing = select { UserStatisticsTable.userId eq userId }.singleOrNull()
            if (existing == null) {
                insert {
                    it[UserStatisticsTable.userId] = userId
                    it[UserStatisticsTable.learnedWords] = learnedWords
                    it[UserStatisticsTable.solvedTests] = solvedTests
                    it[UserStatisticsTable.mistakes] = mistakes
                }
            } else {
                update({ UserStatisticsTable.userId eq userId }) {
                    it[UserStatisticsTable.learnedWords] = existing[UserStatisticsTable.learnedWords] + learnedWords
                    it[UserStatisticsTable.solvedTests] = existing[UserStatisticsTable.solvedTests] + solvedTests
                    it[UserStatisticsTable.mistakes] = existing[UserStatisticsTable.mistakes] + mistakes
                }
            }
        }
    }
    fun incrementLearnedWords(userId: String, count: Int = 1) {
        transaction {
            updateStatistics(userId, learnedWords = count)
        }
    }

    fun incrementSolvedTests(userId: String, count: Int = 1) {
        transaction {
            updateStatistics(userId, solvedTests = count)
        }
    }

    fun incrementMistakes(userId: String, count: Int = 1) {
        transaction {
            updateStatistics(userId, mistakes = count)
        }
    }

    fun getLearnedWords(userId: String): Int {
        return transaction {
            select { UserStatisticsTable.userId eq userId }
                .singleOrNull()
                ?.get(learnedWords) ?: 0
        }
    }

    fun getSolvedTests(userId: String): Int {
        return transaction {
            select { UserStatisticsTable.userId eq userId }
                .singleOrNull()
                ?.get(solvedTests) ?: 0
        }
    }

    fun getMistakes(userId: String): Int {
        return transaction {
            select { UserStatisticsTable.userId eq userId }
                .singleOrNull()
                ?.get(mistakes) ?: 0
        }
    }
}
