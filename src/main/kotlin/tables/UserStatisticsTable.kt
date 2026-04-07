package tables

import models.UserStatisticsData
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

private const val USER_ID_MAX = 50

object UserStatisticsTable : Table("user_statistics") {
    val userId: Column<String> = varchar("user_id", USER_ID_MAX).uniqueIndex()
    val learnedWords: Column<Int> = integer("learned_words").default(0)
    val solvedTests: Column<Int> = integer("solved_tests").default(0)
    val mistakes: Column<Int> = integer("mistakes").default(0)

    override val primaryKey = PrimaryKey(userId)

    fun getStatistics(userId: String): UserStatisticsData? = transaction {
        this@UserStatisticsTable.select { this@UserStatisticsTable.userId eq userId }
            .singleOrNull()?.let { row ->
                UserStatisticsData(
                    userId = row[this@UserStatisticsTable.userId],
                    learnedWords = row[learnedWords],
                    solvedTests = row[solvedTests],
                    mistakes = row[mistakes]
                )
            }
    }

    fun updateStatistics(
        userId: String,
        learnedWordsInc: Int = 0,
        solvedTestsInc: Int = 0,
        mistakesInc: Int = 0
    ) = transaction {
        val existing = this@UserStatisticsTable.select { this@UserStatisticsTable.userId eq userId }
            .singleOrNull()

        if (existing == null) {
            this@UserStatisticsTable.insert { stmt: UpdateBuilder<*> ->
                stmt[this@UserStatisticsTable.userId] = userId
                stmt[learnedWords] = learnedWordsInc
                stmt[solvedTests] = solvedTestsInc
                stmt[mistakes] = mistakesInc
            }
        } else {
            this@UserStatisticsTable.update({ this@UserStatisticsTable.userId eq userId }) { stmt ->
                stmt[learnedWords] = existing[learnedWords] + learnedWordsInc
                stmt[solvedTests] = existing[solvedTests] + solvedTestsInc
                stmt[mistakes] = existing[mistakes] + mistakesInc
            }
        }
    }

    fun incrementLearnedWords(userId: String, count: Int = 1) =
        transaction { updateStatistics(userId, learnedWordsInc = count) }

    fun incrementSolvedTests(userId: String, count: Int = 1) =
        transaction { updateStatistics(userId, solvedTestsInc = count) }

    fun incrementMistakes(userId: String, count: Int = 1) =
        transaction { updateStatistics(userId, mistakesInc = count) }

    fun getLearnedWords(userId: String): Int = transaction {
        this@UserStatisticsTable.select { this@UserStatisticsTable.userId eq userId }
            .singleOrNull()?.get(learnedWords) ?: 0
    }

    fun getSolvedTests(userId: String): Int = transaction {
        this@UserStatisticsTable.select { this@UserStatisticsTable.userId eq userId }
            .singleOrNull()?.get(solvedTests) ?: 0
    }

    fun getMistakes(userId: String): Int = transaction {
        this@UserStatisticsTable.select { this@UserStatisticsTable.userId eq userId }
            .singleOrNull()?.get(mistakes) ?: 0
    }
}
