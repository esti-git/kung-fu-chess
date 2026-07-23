import enums.PieceColor;
import org.junit.jupiter.api.Test;
import protocol.AssignedIdentity;
import protocol.LoginResult;
import protocol.RoomJoined;
import protocol.StateCodec;
import server.GameServer;
import server.PlayerRepository;

import static org.junit.jupiter.api.Assertions.*;

class GameServerTest {

    private GameServer newServer() {
        return new GameServer(0, new PlayerRepository("jdbc:sqlite::memory:"));
    }

    private void login(GameServer server, FakeWebSocket conn, String username, String password) {
        server.onMessage(conn.socket(), StateCodec.encodeLogin(username, password));
    }

    @Test
    void testCreateRoomNormalizesRoomIdToUppercaseTrimmed() {
        GameServer server = newServer();
        FakeWebSocket alice = new FakeWebSocket();
        login(server, alice, "alice", "pw1");

        server.onMessage(alice.socket(), StateCodec.encodeCreateRoom("  abc  "));

        RoomJoined joined = StateCodec.decodeRoomJoined(alice.messagesOfType("roomJoined").get(0));
        assertEquals("ABC", joined.roomId);
    }

    @Test
    void testJoinRoomNormalizesRoomIdCaseAndWhitespace() {
        GameServer server = newServer();
        FakeWebSocket alice = new FakeWebSocket();
        login(server, alice, "alice", "pw1");
        server.onMessage(alice.socket(), StateCodec.encodeCreateRoom("xyz"));

        FakeWebSocket bob = new FakeWebSocket();
        login(server, bob, "bob", "pw2");
        server.onMessage(bob.socket(), StateCodec.encodeJoinRoom("  xyz  "));

        RoomJoined joined = StateCodec.decodeRoomJoined(bob.messagesOfType("roomJoined").get(0));
        assertEquals("XYZ", joined.roomId);
    }

    @Test
    void testJoinRoomNotFoundWhenRoomIdDoesNotExist() {
        GameServer server = newServer();
        FakeWebSocket alice = new FakeWebSocket();
        login(server, alice, "alice", "pw1");

        server.onMessage(alice.socket(), StateCodec.encodeJoinRoom("nope"));

        assertTrue(alice.hasMessageOfType("roomError"));
    }

    @Test
    void testMessageWithoutRoomBindingReturnsNotInGameError() {
        GameServer server = newServer();
        FakeWebSocket stray = new FakeWebSocket();

        server.onMessage(stray.socket(), "JWRa1");

        assertTrue(stray.hasMessageOfType("error"));
        assertEquals("Not in a game.", StateCodec.decodeErrorMessage(stray.sentMessages().get(0)));
    }

    @Test
    void testSeekMatchesTwoCompatiblePlayersIntoRoomWithColors() {
        GameServer server = newServer();
        FakeWebSocket alice = new FakeWebSocket();
        FakeWebSocket bob = new FakeWebSocket();
        login(server, alice, "alice", "pw1");
        login(server, bob, "bob", "pw2");

        server.onMessage(alice.socket(), StateCodec.encodeSeek());
        server.onMessage(bob.socket(), StateCodec.encodeSeek());

        assertTrue(alice.hasMessageOfType("assign"));
        assertTrue(bob.hasMessageOfType("assign"));
        AssignedIdentity aliceAssign = StateCodec.decodeAssign(alice.messagesOfType("assign").get(0));
        AssignedIdentity bobAssign = StateCodec.decodeAssign(bob.messagesOfType("assign").get(0));
        assertNotEquals(aliceAssign.color, bobAssign.color);
        assertTrue(aliceAssign.color == PieceColor.WHITE || aliceAssign.color == PieceColor.BLACK);
    }

    @Test
    void testCloseWhileSeekingCancelsMatchmakingForThatPlayer() {
        GameServer server = newServer();
        FakeWebSocket alice = new FakeWebSocket();
        FakeWebSocket bob = new FakeWebSocket();
        login(server, alice, "alice", "pw1");
        login(server, bob, "bob", "pw2");

        server.onMessage(alice.socket(), StateCodec.encodeSeek());
        server.onClose(alice.socket(), 1000, "gone", false);
        server.onMessage(bob.socket(), StateCodec.encodeSeek());

        assertFalse(bob.hasMessageOfType("assign"));
    }

    @Test
    void testReconnectAfterDisconnectRebindsSessionAndRoom() {
        GameServer server = newServer();
        FakeWebSocket alice = new FakeWebSocket();
        FakeWebSocket bob = new FakeWebSocket();
        login(server, alice, "alice", "pw1");
        server.onMessage(alice.socket(), StateCodec.encodeCreateRoom(""));
        login(server, bob, "bob", "pw2");
        String roomId = StateCodec.decodeRoomJoined(alice.messagesOfType("roomJoined").get(0)).roomId;
        server.onMessage(bob.socket(), StateCodec.encodeJoinRoom(roomId));

        server.onClose(alice.socket(), 1000, "gone", false);

        FakeWebSocket aliceReconnect = new FakeWebSocket();
        login(server, aliceReconnect, "alice", "pw1");

        LoginResult result = StateCodec.decodeLoginResult(aliceReconnect.messagesOfType("loginResult").get(0));
        assertTrue(result.reconnected);

        server.onMessage(aliceReconnect.socket(), "JWRa1");
        assertFalse(aliceReconnect.hasMessageOfType("error"));
    }
}
