import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tables.Users
import utils.PasswordHasher

class UsersTest : BaseTest() {

    @Test
    fun `insertUser and getUser should work`() {
        val username = "test_user"
        val password = PasswordHasher.hash("1234")

        Users.insertUser(username, password)
        val user = Users.getUser(username)

        assertNotNull(user)
    }

    @Test
    fun `updateUsername should fail if user exists`() {
        val username = "user1"
        val password = PasswordHasher.hash("123")

        Users.insertUser(username, password)

        val result = Users.updateUsername(username, username)

        assertFalse(result)
    }
}