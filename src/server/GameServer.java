package server;

import common.GameResult;
import engine.GameEngine;
import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.GameEndedEvent;
import events.GameStartedEvent;
import events.MoveMadeEvent;
import events.PieceCapturedEvent;
import input.GameFactory;
import model.Piece;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import protocol.JumpCommand;
import protocol.LoginResult;
import protocol.MoveCommand;
import protocol.StateCodec;
import view.BoardSnapshot;
import view.BoardSnapshotFactory;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Local WebSocket server hosting a single shared game. All board/rule state lives only here -
 * clients are sent a JSON {@link BoardSnapshot} after every applied move and every clock tick.
 */
public class GameServer extends WebSocketServer {

    private static final int TICK_MS = 16;
    private static final int RESTART_DELAY_MS = 3000;

    private final GameFactory factory;
    private final GameEngine engine;
    private final PlayerRepository repository;
    private final BoardSnapshotFactory snapshotFactory = new BoardSnapshotFactory();
    private final Object engineLock = new Object();

    private volatile boolean running = true;
    private boolean gameOverHandled;
    private long gameOverAtMillis;
    private boolean ratingsAppliedForCurrentGame;

    private PlayerSession whiteSession;
    private PlayerSession blackSession;
    private WebSocket whiteConn;
    private WebSocket blackConn;

    public GameServer(int port, GameFactory factory, PlayerRepository repository) {
        super(new InetSocketAddress(port));
        this.factory = factory;
        this.engine = factory.getEngine();
        this.repository = repository;

        EventBus eventBus = factory.getEventBus();
        eventBus.subscribe(MoveMadeEvent.TYPE, this::broadcastEvent);
        eventBus.subscribe(PieceCapturedEvent.TYPE, this::broadcastEvent);
        eventBus.subscribe(GameStartedEvent.TYPE, this::broadcastEvent);
        eventBus.subscribe(GameEndedEvent.TYPE, this::broadcastEvent);
        eventBus.subscribe(GameEndedEvent.TYPE, this::applyEloOnGameEnd);
    }

    private void broadcastEvent(Event event) {
        broadcast(StateCodec.encodeEvent(event));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Client connected: " + conn.getRemoteSocketAddress());
        synchronized (engineLock) {
            conn.send(currentStateJson());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Client disconnected: " + conn.getRemoteSocketAddress());
        synchronized (engineLock) {
            WebSocket remaining;
            if (conn == whiteConn) {
                remaining = blackConn;
            } else if (conn == blackConn) {
                remaining = whiteConn;
            } else {
                return;
            }

            // Clear slots first so any in-flight move/jump from the remaining client is rejected
            // by isAuthorized() even if it arrives before the close() below takes effect.
            whiteConn = null;
            blackConn = null;
            whiteSession = null;
            blackSession = null;

            if (remaining != null) {
                String message = "Opponent disconnected. Game over.";
                remaining.send(StateCodec.encodeOpponentDisconnected(message));
                remaining.close(1000, message);
            }

            factory.restartGame();
            gameOverHandled = false;
            ratingsAppliedForCurrentGame = false;
        }
        broadcastState();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String trimmed = message.trim();
        if (trimmed.startsWith("{")) {
            synchronized (engineLock) {
                handleControlMessage(conn, trimmed);
            }
            return;
        }
        synchronized (engineLock) {
            if (JumpCommand.isJumpCommand(trimmed)) {
                if (!handleJump(conn, trimmed)) return;
            } else {
                if (!handleMove(conn, trimmed)) return;
            }
        }
        broadcastState();
    }

    /** Caller must hold engineLock. */
    private void handleControlMessage(WebSocket conn, String message) {
        if ("login".equals(StateCodec.peekType(message))) {
            handleLogin(conn, StateCodec.decodeLoginUsername(message), StateCodec.decodeLoginPassword(message));
        }
    }

    /** Caller must hold engineLock. Validates credentials against SQLite (auto-registering a new
     *  username), then assigns White/Black by login order; a third joiner is rejected. */
    private void handleLogin(WebSocket conn, String username, String password) {
        String name = (username == null || username.isBlank()) ? "Player" : username;

        LoginResult result = repository.loginOrRegister(name, password);
        if (!result.success) {
            conn.send(StateCodec.encodeLoginResult(false, 0, result.message));
            conn.close();
            return;
        }

        if (whiteSession == null) {
            whiteSession = new PlayerSession(name, PieceColor.WHITE, result.rating);
            whiteConn = conn;
        } else if (blackSession == null) {
            blackSession = new PlayerSession(name, PieceColor.BLACK, result.rating);
            blackConn = conn;
        } else {
            conn.send(StateCodec.encodeRejected("Game already has two players."));
            conn.close();
            return;
        }

        conn.send(StateCodec.encodeLoginResult(true, result.rating, null));
        ratingsAppliedForCurrentGame = false;
        broadcastAssignments();
    }

    /** Caller must hold engineLock. Sends each connected player their color, both usernames, and both ratings. */
    private void broadcastAssignments() {
        String whiteName = whiteSession == null ? null : whiteSession.username;
        String blackName = blackSession == null ? null : blackSession.username;
        int whiteRating = whiteSession == null ? 1200 : whiteSession.rating;
        int blackRating = blackSession == null ? 1200 : blackSession.rating;
        if (whiteConn != null) whiteConn.send(StateCodec.encodeAssign(PieceColor.WHITE, whiteName, blackName, whiteRating, blackRating));
        if (blackConn != null) blackConn.send(StateCodec.encodeAssign(PieceColor.BLACK, whiteName, blackName, whiteRating, blackRating));
    }

    /** GameEndedEvent subscriber. Runs on the same thread already holding engineLock (all event
     *  publishes happen from inside engine.requestMove/requestJump/advanceClock, which the server
     *  only ever calls under engineLock) - safe without a second lock since monitors are reentrant. */
    private void applyEloOnGameEnd(Event event) {
        PieceColor winnerColor = ((GameEndedEvent) event).getWinnerColor();
        if (winnerColor == null || whiteSession == null || blackSession == null || ratingsAppliedForCurrentGame) {
            return;
        }
        ratingsAppliedForCurrentGame = true;

        PlayerSession winner = winnerColor == PieceColor.WHITE ? whiteSession : blackSession;
        PlayerSession loser = winnerColor == PieceColor.WHITE ? blackSession : whiteSession;

        int[] updated = EloCalculator.computeNewRatings(winner.rating, loser.rating);
        winner.rating = updated[0];
        loser.rating = updated[1];

        repository.updateRating(winner.username, winner.rating);
        repository.updateRating(loser.username, loser.rating);

        broadcastAssignments();
    }

    /** True if conn is the currently registered connection for that color. Guards against stale/in-flight
     *  commands from a connection whose slot was just cleared (e.g. by a disconnect teardown). */
    private boolean isAuthorized(WebSocket conn, PieceColor color) {
        return (color == PieceColor.WHITE && conn == whiteConn) || (color == PieceColor.BLACK && conn == blackConn);
    }

    /** Caller must hold engineLock. Returns false (and reports the error) if the command was rejected. */
    private boolean handleMove(WebSocket conn, String message) {
        MoveCommand command = MoveCommand.parse(message, engine.getState().getBoard().getRows());
        if (command == null) {
            conn.send(StateCodec.encodeError("Malformed command: " + message));
            return false;
        }

        if (!isAuthorized(conn, command.color)) {
            conn.send(StateCodec.encodeError("You can only move your own pieces."));
            return false;
        }

        Optional<Piece> piece = engine.pieceAt(command.from);
        if (!piece.isPresent() || piece.get().getColor() != command.color || piece.get().getKind() != command.kind) {
            conn.send(StateCodec.encodeError("No matching piece at source square"));
            return false;
        }

        GameResult<Void> result = engine.requestMove(command.from, command.to);
        if (!result.isSuccess()) {
            conn.send(StateCodec.encodeError(result.message()));
            return false;
        }
        return true;
    }

    /** Caller must hold engineLock. Returns false (and reports the error) if the command was rejected. */
    private boolean handleJump(WebSocket conn, String message) {
        JumpCommand command = JumpCommand.parse(message, engine.getState().getBoard().getRows());
        if (command == null) {
            conn.send(StateCodec.encodeError("Malformed command: " + message));
            return false;
        }

        if (!isAuthorized(conn, command.color)) {
            conn.send(StateCodec.encodeError("You can only move your own pieces."));
            return false;
        }

        Optional<Piece> piece = engine.pieceAt(command.position);
        if (!piece.isPresent() || piece.get().getColor() != command.color || piece.get().getKind() != command.kind) {
            conn.send(StateCodec.encodeError("No matching piece at jump square"));
            return false;
        }

        GameResult<Void> result = engine.requestJump(command.position);
        if (!result.isSuccess()) {
            conn.send(StateCodec.encodeError(result.message()));
            return false;
        }
        return true;
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Game server listening on port " + getPort());
    }

    /** Advances the shared game clock at a fixed rate and broadcasts state after every tick, mirroring GameLoop. */
    public void runGameLoop() throws InterruptedException {
        long lastTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            lastTime = now;

            synchronized (engineLock) {
                if (engine.isGameOver()) {
                    if (!gameOverHandled) {
                        gameOverHandled = true;
                        gameOverAtMillis = now;
                    } else if (now - gameOverAtMillis >= RESTART_DELAY_MS) {
                        factory.restartGame();
                        gameOverHandled = false;
                        ratingsAppliedForCurrentGame = false;
                    }
                } else {
                    engine.advanceClock(elapsed);
                }
            }
            broadcastState();

            Thread.sleep(TICK_MS);
        }
    }

    private void broadcastState() {
        synchronized (engineLock) {
            broadcast(currentStateJson());
        }
    }

    private String currentStateJson() {
        BoardSnapshot snapshot = snapshotFactory.capture(engine);
        return StateCodec.encodeState(snapshot, engine.isGameOver(), engine.getWinnerColor());
    }
}
