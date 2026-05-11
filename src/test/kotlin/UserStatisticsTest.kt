import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tables.UserStatisticsTable

class UserStatisticsTest : BaseTest() {

    @Test
    fun `getStatistics should return null for unknown user`() {
        val stats = UserStatisticsTable.getStatistics("unknown_user")
        assertNull(stats)
    }

    @Test
    fun `incrementLearnedWords should increase value`() {
        val userId = "test_user"

        UserStatisticsTable.incrementLearnedWords(userId, 2)
        val result = UserStatisticsTable.getLearnedWords(userId)

        assertTrue(result >= 2)
    }
}