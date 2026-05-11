import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import application.DatabaseFactory
import models.Word
import tables.Words
import utils.PasswordHasher

class MemoryLoadTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            DatabaseFactory.init()
            DatabaseFactory.createTables()
        }
    }

    @Test
    fun loadTestPasswordHasher() {
        repeat(10000) { i ->
            val password = "password$i"
            val hash = PasswordHasher.hash(password)
            PasswordHasher.verify(password, hash)
        }
    }

    @Test
    fun loadTestWords() {
        repeat(1000) { i ->
            Words.insertWord(Word(0, "word$i", "translation$i"))
        }

        repeat(1000) {
            Words.getAllWords()
        }

        repeat(1000) {
            Words.getRandomWord()
            //Words.getRandomTranslations("translation1")
        }
    }

    @Test
    fun heavyLoadTest() {
        repeat(5) { round ->
            println("ROUND $round")

            repeat(5000) { i ->
                val password = "pass$round-$i"
                val hash = PasswordHasher.hash(password)
                PasswordHasher.verify(password, hash)
            }

            repeat(2000) { i ->
                Words.insertWord(Word(0, "word$round-$i", "translation$round-$i"))
            }

            repeat(2000) {
                Words.getAllWords()
            }
        }
    }
}