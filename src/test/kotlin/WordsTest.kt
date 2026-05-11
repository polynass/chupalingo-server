import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import models.Word
import tables.Words

class WordsTest : BaseTest() {

    @Test
    fun `insert and getAllWords should work`() {
        val word = Word(0, "cat", "кот")

        Words.insertWord(word)
        val words = Words.getAllWords()

        assertTrue(words.any { it.word == "cat" })
    }

    @Test
    fun `updateWord should return false for non-existing id`() {
        val result = Words.updateWord(-1, Word(-1, "dog", "собака"))

        assertFalse(result)
    }

    @Test
    fun `deleteWord should return false for invalid id`() {
        val result = Words.deleteWord(-1)

        assertFalse(result)
    }
}