package server;

import org.java_websocket.WebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {

    private final Map<WebSocket, PlayerSession> sessionsByConn = new ConcurrentHashMap<>();

    public PlayerSession get(WebSocket conn) {
        return sessionsByConn.get(conn);
    }

    public void put(WebSocket conn, PlayerSession session) {
        sessionsByConn.put(conn, session);
    }

    public void remove(WebSocket conn) {
        sessionsByConn.remove(conn);
    }
}
