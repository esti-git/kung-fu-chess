package server;

import enums.PieceColor;
import enums.PlayerRole;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import protocol.JumpCommand;
import protocol.LoginResult;
import protocol.StateCodec;
import server.logging.ServerLog;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GameServer extends WebSocketServer {

    private static final int TICK_MS = 16;
    private static final int MAX_ROOM_NAME_LENGTH = 20;

    private final PlayerRepository repository;
    private final Object globalLock = new Object();

    private volatile boolean running = true;

    private final Map<WebSocket, PlayerSession> sessionsByConn = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "matchmaking-timers");
        thread.setDaemon(true);
        return thread;
    });
    private final Matchmaker matchmaker = new Matchmaker(scheduler);
    private final RoomRegistry roomRegistry = new RoomRegistry();

    public GameServer(int port, PlayerRepository repository) {
        super(new InetSocketAddress(port));
        this.repository = repository;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
        ServerLog.info("Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
        ServerLog.info("Client disconnected: " + conn.getRemoteSocketAddress());

        synchronized (globalLock) {
            matchmaker.remove(conn);
            sessionsByConn.remove(conn);
        }

        Room room = roomRegistry.roomFor(conn);
        if (room != null) {
            room.removeConn(conn);
            roomRegistry.unbind(conn);
            roomRegistry.removeIfEmpty(room.roomId);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String trimmed = message.trim();
        if (trimmed.startsWith("{")) {
            synchronized (globalLock) {
                handleControlMessage(conn, trimmed);
            }
            return;
        }

        Room room = roomRegistry.roomFor(conn);
        if (room == null) {
            conn.send(StateCodec.encodeError("Not in a game."));
            return;
        }
        boolean ok = JumpCommand.isJumpCommand(trimmed) ? room.handleJump(conn, trimmed) : room.handleMove(conn, trimmed);
        if (ok) {
            room.broadcastState();
        }
    }

    private void handleControlMessage(WebSocket conn, String message) {
        String type = StateCodec.peekType(message);
        if ("login".equals(type)) {
            handleLogin(conn, StateCodec.decodeLoginUsername(message), StateCodec.decodeLoginPassword(message));
        } else if ("seek".equals(type)) {
            handleSeek(conn);
        } else if ("createRoom".equals(type)) {
            handleCreateRoom(conn, StateCodec.decodeCreateRoomId(message));
        } else if ("joinRoom".equals(type)) {
            handleJoinRoom(conn, StateCodec.decodeJoinRoomId(message));
        } else if ("playAgain".equals(type)) {
            handlePlayAgain(conn);
        }
    }

    private void handlePlayAgain(WebSocket conn) {
        Room room = roomRegistry.roomFor(conn);
        if (room != null) {
            room.requestRestart(conn);
        }
    }

    private void handleLogin(WebSocket conn, String username, String password) {
        String name = (username == null || username.isBlank()) ? "Player" : username;

        LoginResult result = repository.loginOrRegister(name, password);
        if (!result.success) {
            conn.send(StateCodec.encodeLoginResult(false, 0, result.message, false));
            conn.close();
            ServerLog.warn("Login failed for " + name + ": " + result.message);
            return;
        }

        Room reconnectRoom = roomRegistry.findRoomWithDisconnectedUser(name);
        if (reconnectRoom != null) {
            PlayerSession session = reconnectRoom.findDisconnectedSession(name);
            reconnectRoom.reconnect(conn, session, result.rating);
            sessionsByConn.put(conn, session);
            roomRegistry.bind(conn, reconnectRoom.roomId);
            conn.send(StateCodec.encodeLoginResult(true, result.rating, null, true));
            ServerLog.info(name + " reconnected to room " + reconnectRoom.roomId);
            return;
        }

        PlayerSession session = new PlayerSession(name, result.rating);
        sessionsByConn.put(conn, session);
        conn.send(StateCodec.encodeLoginResult(true, result.rating, null, false));
        ServerLog.info("Login: " + name + " (rating " + result.rating + ")");
    }

    private void handleSeek(WebSocket conn) {
        PlayerSession session = sessionsByConn.get(conn);
        if (session == null || session.getState() != SessionState.IDLE) {
            return;
        }
        session.setState(SessionState.SEEKING);
        matchmaker.addWaiting(conn, session, () -> handleSeekTimeout(conn));
        tryPromoteFromQueue();
    }

    private void handleSeekTimeout(WebSocket conn) {
        synchronized (globalLock) {
            if (!matchmaker.isWaiting(conn)) {
                return;
            }
            PlayerSession session = matchmaker.remove(conn);
            if (session != null) {
                session.setState(SessionState.IDLE);
            }
            conn.send(StateCodec.encodeSeekTimeout("Couldn't find a match. Try again."));
        }
    }

    private void tryPromoteFromQueue() {
        List<Map.Entry<WebSocket, PlayerSession>> pair;
        while ((pair = matchmaker.findCompatiblePair()) != null) {
            Map.Entry<WebSocket, PlayerSession> first = pair.get(0);
            Map.Entry<WebSocket, PlayerSession> second = pair.get(1);
            matchmaker.remove(first.getKey());
            matchmaker.remove(second.getKey());

            PlayerSession newWhite = first.getValue();
            PlayerSession newBlack = second.getValue();
            newWhite.setColor(PieceColor.WHITE);
            newWhite.setState(SessionState.PLAYING);
            newBlack.setColor(PieceColor.BLACK);
            newBlack.setState(SessionState.PLAYING);

            Room room = roomRegistry.createRoom(GameSource.MATCHMAKING, repository, scheduler);
            room.seatMatch(first.getKey(), newWhite, second.getKey(), newBlack);
            roomRegistry.bind(first.getKey(), room.roomId);
            roomRegistry.bind(second.getKey(), room.roomId);
            ServerLog.info("Matched " + newWhite.getUsername() + " vs " + newBlack.getUsername() + " into room " + room.roomId);
        }
    }

    private void handleCreateRoom(WebSocket conn, String desiredRoomId) {
        PlayerSession session = sessionsByConn.get(conn);
        if (session == null || session.getState() != SessionState.IDLE) {
            conn.send(StateCodec.encodeRoomError("Log in before creating a room."));
            return;
        }

        String normalizedId = desiredRoomId == null ? "" : desiredRoomId.trim().toUpperCase();
        Room room;
        if (normalizedId.isEmpty()) {
            room = roomRegistry.createRoom(GameSource.ROOM_CODE, repository, scheduler);
        } else if (normalizedId.length() > MAX_ROOM_NAME_LENGTH) {
            conn.send(StateCodec.encodeRoomError("Room name is too long (max " + MAX_ROOM_NAME_LENGTH + " characters)."));
            return;
        } else {
            room = roomRegistry.createRoomWithId(normalizedId, GameSource.ROOM_CODE, repository, scheduler);
            if (room == null) {
                conn.send(StateCodec.encodeRoomError("Room name \"" + normalizedId + "\" is already in use."));
                return;
            }
        }

        session.setColor(PieceColor.WHITE);
        session.setState(SessionState.PLAYING);
        room.seatCreator(conn, session);
        roomRegistry.bind(conn, room.roomId);
        conn.send(StateCodec.encodeRoomJoined(room.roomId, PlayerRole.WHITE));
        ServerLog.info(session.getUsername() + " created room " + room.roomId);
    }

    private void handleJoinRoom(WebSocket conn, String roomId) {
        PlayerSession session = sessionsByConn.get(conn);
        if (session == null || session.getState() != SessionState.IDLE) {
            conn.send(StateCodec.encodeRoomError("Log in before joining a room."));
            return;
        }
        String normalizedId = roomId == null ? "" : roomId.trim().toUpperCase();
        Room room = roomRegistry.get(normalizedId);
        if (room == null) {
            conn.send(StateCodec.encodeRoomError("Room not found: " + roomId));
            return;
        }

        PlayerRole role = room.join(conn, session);
        session.setState(SessionState.PLAYING);
        roomRegistry.bind(conn, room.roomId);
        conn.send(StateCodec.encodeRoomJoined(room.roomId, role));
        ServerLog.info(session.getUsername() + " joined room " + room.roomId + " as " + role);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        ServerLog.error("WebSocket error", ex);
    }

    @Override
    public void onStart() {
        System.out.println("Game server listening on port " + getPort());
        ServerLog.info("Game server listening on port " + getPort());
    }

    public void runGameLoop() throws InterruptedException {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            lastTime = now;

            for (Room room : roomRegistry.allRooms()) {
                room.tick(now, elapsed);
            }

            Thread.sleep(TICK_MS);
        }
    }
}
