package tables

import application.DatabaseFactory
import models.Word
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.sql.Connection
import java.sql.PreparedStatement

private const val WORD_MAX = 255
private const val TRANSLATION_MAX = 255
private const val PARAMETER_INDEX = 255

object Words : Table("words") {
    val id: Column<Int> = integer("id").autoIncrement()
    val word: Column<String> = varchar("word", WORD_MAX)
    val translation: Column<String> = varchar("translation", TRANSLATION_MAX)

    override val primaryKey = PrimaryKey(id)

    fun getAllWords(): List<Word> = transaction {
        selectAll().map { row ->
            Word(id = row[Words.id], word = row[word], translation = row[translation])
        }
    }

    fun insertWord(newWord: Word) = transaction {
        insert {
            it[word] = newWord.word
            it[translation] = newWord.translation
        }
    }

    fun updateWord(id: Int, updatedWord: Word): Boolean = transaction {
        update({ Words.id eq id }) {
            it[word] = updatedWord.word
            it[translation] = updatedWord.translation
        } > 0
    }

    fun deleteWord(id: Int): Boolean = transaction {
        deleteWhere { Words.id eq id } > 0
    }

    fun getRandomWord(): Word? {
        return DatabaseFactory.getConnection().use { connection ->
            val query = "SELECT * FROM words ORDER BY RANDOM() LIMIT 1"
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    Word(
                        id = resultSet.getInt("id"),
                        word = resultSet.getString("word"),
                        translation = resultSet.getString("translation")
                    )
                } else null
            }
        }
    }

    fun getRandomTranslations(excludeTranslation: String, limit: Int = 3): List<String> {
        return DatabaseFactory.getConnection().use { connection ->
            val query = "SELECT translation FROM words WHERE translation != ? ORDER BY RANDOM() LIMIT ?"
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, excludeTranslation)
                statement.setInt(2, limit) // ✔ исправлено

                val resultSet = statement.executeQuery()
                val translations = mutableListOf<String>()
                while (resultSet.next()) {
                    translations.add(resultSet.getString("translation"))
                }
                translations
            }
        }
    }
}
