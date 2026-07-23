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
import config.GameConfig;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GameServer extends WebSocketServer {

    private static final int TICK_MS = GameConfig.TICK_MS ;
    private static final int MAX_ROOM_NAME_LENGTH = GameConfig.MAX_ROOM_NAME_LENGTH;

    private final PlayerRepository repository;
    private final Object globalLock = new Object();

    private volatile boolean running = true;

    private final SessionRegistry sessionRegistry = new SessionRegistry();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "matchmaking-timers");
        thread.setDaemon(true);
        return thread;
    });
    private final Matchmaker matchmaker = new Matchmaker(scheduler);
    private final RoomRegistry roomRegistry = new RoomRegistry();
    private final MatchService matchService;

    public GameServer(int port, PlayerRepository repository) {
        super(new InetSocketAddress(port));
        this.repository = repository;
        this.matchService = new MatchService(matchmaker, roomRegistry, repository, scheduler);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        ServerLog.info("Client connected: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        ServerLog.info("Client disconnected: " + conn.getRemoteSocketAddress());

        synchronized (globalLock) {
            matchmaker.remove(conn);
            sessionRegistry.remove(conn);
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
            handleReconnectLogin(conn, name, result, reconnectRoom);
        } else {
            handleFreshLogin(conn, name, result);
        }
    }

    private void handleReconnectLogin(WebSocket conn, String name, LoginResult result, Room reconnectRoom) {
        PlayerSession session = reconnectRoom.findDisconnectedSession(name);
        reconnectRoom.reconnect(conn, session, result.rating);
        sessionRegistry.put(conn, session);
        roomRegistry.bind(conn, reconnectRoom.roomId);
        conn.send(StateCodec.encodeLoginResult(true, result.rating, null, true));
        ServerLog.info(name + " reconnected to room " + reconnectRoom.roomId);
    }

    private void handleFreshLogin(WebSocket conn, String name, LoginResult result) {
        PlayerSession session = new PlayerSession(name, result.rating);
        sessionRegistry.put(conn, session);
        conn.send(StateCodec.encodeLoginResult(true, result.rating, null, false));
        ServerLog.info("Login: " + name + " (rating " + result.rating + ")");
    }

    private void handleSeek(WebSocket conn) {
        PlayerSession session = sessionRegistry.get(conn);
        if (session == null || session.getState() != SessionState.IDLE) {
            return;
        }
        session.setState(SessionState.SEEKING);
        matchmaker.addWaiting(conn, session, () -> handleSeekTimeout(conn));
        matchService.tryPromoteFromQueue();
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

    private PlayerSession requireIdleSession(WebSocket conn, String errorMessage) {
        PlayerSession session = sessionRegistry.get(conn);
        if (session == null || session.getState() != SessionState.IDLE) {
            conn.send(StateCodec.encodeRoomError(errorMessage));
            return null;
        }
        return session;
    }

    private void handleCreateRoom(WebSocket conn, String desiredRoomId) {
        PlayerSession session = requireIdleSession(conn, "Log in before creating a room.");
        if (session == null) return;

        String normalizedId = RoomRegistry.normalizeRoomId(desiredRoomId);
        Room room;
        if (normalizedId.isEmpty()) {
            room = roomRegistry.createRoom(repository, scheduler);
        } else if (normalizedId.length() > MAX_ROOM_NAME_LENGTH) {
            conn.send(StateCodec.encodeRoomError("Room name is too long (max " + MAX_ROOM_NAME_LENGTH + " characters)."));
            return;
        } else {
            room = roomRegistry.createRoomWithId(normalizedId, repository, scheduler);
            if (room == null) {
                conn.send(StateCodec.encodeRoomError("Room name \"" + normalizedId + "\" is already in use."));
                return;
            }
        }

        MatchService.assignAndActivate(session, PieceColor.WHITE);
        room.seatCreator(conn, session);
        roomRegistry.bind(conn, room.roomId);
        conn.send(StateCodec.encodeRoomJoined(room.roomId, PlayerRole.WHITE));
        ServerLog.info(session.getUsername() + " created room " + room.roomId);
    }

    private void handleJoinRoom(WebSocket conn, String roomId) {
        PlayerSession session = requireIdleSession(conn, "Log in before joining a room.");
        if (session == null) return;

        String normalizedId = RoomRegistry.normalizeRoomId(roomId);
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
