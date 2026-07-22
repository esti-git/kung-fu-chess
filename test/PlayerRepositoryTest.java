import org.junit.jupiter.api.Test;
import protocol.LoginResult;
import server.PlayerRepository;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRepositoryTest {

    private PlayerRepository newRepository() {
        return new PlayerRepository("jdbc:sqlite::memory:");
    }

    @Test
    void testFirstLoginRegistersNewPlayerAtStartingRating() {
        PlayerRepository repo = newRepository();

        LoginResult result = repo.loginOrRegister("alice", "password123");

        assertTrue(result.success);
        assertEquals(1200, result.rating);
        assertNull(result.message);
        assertFalse(result.reconnected);
    }

    @Test
    void testSecondLoginWithCorrectPasswordSucceeds() {
        PlayerRepository repo = newRepository();
        repo.loginOrRegister("alice", "password123");

        LoginResult result = repo.loginOrRegister("alice", "password123");

        assertTrue(result.success);
        assertEquals(1200, result.rating);
    }

    @Test
    void testLoginWithWrongPasswordFails() {
        PlayerRepository repo = newRepository();
        repo.loginOrRegister("alice", "password123");

        LoginResult result = repo.loginOrRegister("alice", "wrongpassword");

        assertFalse(result.success);
        assertNotNull(result.message);
    }

    @Test
    void testUpdateRatingPersistsAcrossLogins() {
        PlayerRepository repo = newRepository();
        repo.loginOrRegister("alice", "password123");

        repo.updateRating("alice", 1450);
        LoginResult result = repo.loginOrRegister("alice", "password123");

        assertEquals(1450, result.rating);
    }

    @Test
    void testDifferentUsernamesAreIndependent() {
        PlayerRepository repo = newRepository();

        repo.loginOrRegister("alice", "secret1");
        repo.loginOrRegister("bob", "secret2");
        repo.updateRating("alice", 1600);

        LoginResult bobResult = repo.loginOrRegister("bob", "secret2");
        assertEquals(1200, bobResult.rating);
    }
}
