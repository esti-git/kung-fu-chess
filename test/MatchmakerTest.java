import org.java_websocket.WebSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import server.Matchmaker;
import server.PlayerSession;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class MatchmakerTest {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Matchmaker matchmaker = new Matchmaker(scheduler);

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    private WebSocket fakeSocket() {
        return (WebSocket) Proxy.newProxyInstance(
                WebSocket.class.getClassLoader(),
                new Class<?>[] { WebSocket.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "toString": return "FakeWebSocket@" + System.identityHashCode(proxy);
                        default: return null;
                    }
                });
    }

    @Test
    void testIsWaitingFalseBeforeAdd() {
        assertFalse(matchmaker.isWaiting(fakeSocket()));
    }

    @Test
    void testAddWaitingMarksSocketAsWaiting() {
        WebSocket socket = fakeSocket();
        matchmaker.addWaiting(socket, new PlayerSession("alice", 1200), () -> {});

        assertTrue(matchmaker.isWaiting(socket));
    }

    @Test
    void testRemoveReturnsSessionAndClearsWaiting() {
        WebSocket socket = fakeSocket();
        PlayerSession session = new PlayerSession("alice", 1200);
        matchmaker.addWaiting(socket, session, () -> {});

        PlayerSession removed = matchmaker.remove(socket);

        assertSame(session, removed);
        assertFalse(matchmaker.isWaiting(socket));
    }

    @Test
    void testRemoveNonExistentSocketReturnsNull() {
        assertNull(matchmaker.remove(fakeSocket()));
    }

    @Test
    void testOnTimeoutNotInvokedImmediately() {
        AtomicBoolean fired = new AtomicBoolean(false);
        matchmaker.addWaiting(fakeSocket(), new PlayerSession("alice", 1200), () -> fired.set(true));

        assertFalse(fired.get());
    }

    @Test
    void testFindCompatiblePairReturnsNullWithFewerThanTwoWaiting() {
        matchmaker.addWaiting(fakeSocket(), new PlayerSession("alice", 1200), () -> {});

        assertNull(matchmaker.findCompatiblePair());
    }

    @Test
    void testFindCompatiblePairWithinRatingRange() {
        WebSocket socketA = fakeSocket();
        WebSocket socketB = fakeSocket();
        matchmaker.addWaiting(socketA, new PlayerSession("alice", 1200), () -> {});
        matchmaker.addWaiting(socketB, new PlayerSession("bob", 1250), () -> {});

        List<Map.Entry<WebSocket, PlayerSession>> pair = matchmaker.findCompatiblePair();

        assertNotNull(pair);
        assertEquals(2, pair.size());
    }

    @Test
    void testFindCompatiblePairReturnsNullWhenRatingsTooFarApart() {
        matchmaker.addWaiting(fakeSocket(), new PlayerSession("alice", 1000), () -> {});
        matchmaker.addWaiting(fakeSocket(), new PlayerSession("bob", 1300), () -> {});

        assertNull(matchmaker.findCompatiblePair());
    }

    @Test
    void testFindCompatiblePairExcludesRemovedPlayer() {
        WebSocket socketA = fakeSocket();
        WebSocket socketB = fakeSocket();
        matchmaker.addWaiting(socketA, new PlayerSession("alice", 1200), () -> {});
        matchmaker.addWaiting(socketB, new PlayerSession("bob", 1210), () -> {});

        matchmaker.remove(socketB);

        assertNull(matchmaker.findCompatiblePair());
    }
}
