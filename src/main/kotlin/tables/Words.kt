package tables

import application.DatabaseFactory
import models.Word
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Words : Table() {
    val id = integer("id").autoIncrement()
    val word = varchar("word", 255)
    val translation = varchar("translation", 255)

    override val primaryKey = PrimaryKey(id)

    fun getAllWords(): List<Word> {
        return transaction {
            Words.selectAll().map { row ->
                Word(
                    id = row[Words.id],
                    word = row[word],
                    translation = row[translation]
                )
            }
        }
    }

    fun insertWord(newWord: Word) {
        transaction {
            try {
                insert {
                    it[word] = newWord.word
                    it[translation] = newWord.translation
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun updateWord(id: Int, updatedWord: Word): Boolean = transaction {
        val updatedWordWithId = updatedWord.copy(id = id)
        val updatedCount = update({ Words.id eq id }) {
            it[word] = updatedWordWithId.word
            it[translation] = updatedWordWithId.translation
        }
        updatedCount > 0
    }

    fun deleteWord(id: Int): Boolean = transaction {
        val deletedCount = deleteWhere { Words.id eq id }
        deletedCount > 0
    }

    fun getRandomWord(): Word? {
        return transaction {
            Words.selectAll().orderBy(Random()).limit(1).map { row ->
                Word(
                    id = row[Words.id],
                    word = row[word],
                    translation = row[translation]
                )
            }.firstOrNull()
        }
    }

    fun getRandomTranslations(excludeTranslation: String, limit: Int = 3): List<String> {
        val connection = DatabaseFactory.getConnection()
        val query = "SELECT translation FROM words WHERE translation != ? ORDER BY RANDOM() LIMIT ?"
        val statement = connection.prepareStatement(query)
        statement.setString(1, excludeTranslation)
        statement.setInt(2, limit)
        val resultSet = statement.executeQuery()

        val translations = mutableListOf<String>()
        while (resultSet.next()) {
            translations.add(resultSet.getString("translation"))
        }
        resultSet.close()
        statement.close()
        connection.close()
        return translations
    }

}
