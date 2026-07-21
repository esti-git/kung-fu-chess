package server;

import common.GameResult;
import config.GameConfig;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private final Map<WebSocket, PlayerSession> sessionsByConn = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "matchmaking-timers");
        thread.setDaemon(true);
        return thread;
    });
    private final Matchmaker matchmaker = new Matchmaker(scheduler);

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
            if (conn == whiteConn || conn == blackConn) {
                handlePlayerDisconnect(conn);
            } else {
                matchmaker.remove(conn);
                sessionsByConn.remove(conn);
            }
        }
    }

    /** Caller must hold engineLock. conn is the live white/black connection that just dropped.
     *  Doesn't tear the game down immediately - keeps the PlayerSession alive as
     *  DISCONNECTED_PENDING and gives it GameConfig.DISCONNECT_GRACE_SECONDS to reconnect
     *  (see handleLogin) before resolveAutoResign ends the game. */
    private void handlePlayerDisconnect(WebSocket conn) {
        PlayerSession disconnected;
        WebSocket remaining;
        if (conn == whiteConn) {
            disconnected = whiteSession;
            remaining = blackConn;
            whiteConn = null;
        } else {
            disconnected = blackSession;
            remaining = whiteConn;
            blackConn = null;
        }

        sessionsByConn.remove(conn);
        disconnected.setState(SessionState.DISCONNECTED_PENDING);

        if (remaining != null) {
            remaining.send(StateCodec.encodeDisconnectCountdown(GameConfig.DISCONNECT_GRACE_SECONDS));
        }

        ScheduledFuture<?> timer = scheduler.schedule(() -> resolveAutoResign(disconnected),
                GameConfig.DISCONNECT_GRACE_SECONDS, TimeUnit.SECONDS);
        disconnected.setDisconnectTimer(timer);
    }

    /** Fires GameConfig.DISCONNECT_GRACE_SECONDS after a disconnect. No-op if the player already
     *  reconnected (handleLogin would have moved them out of DISCONNECTED_PENDING and cancelled
     *  this timer, but the two can race, so re-check state here too). Otherwise resigns the
     *  disconnected side: applies ELO exactly like a normal game end, tears the game down, and
     *  lets any already-queued pair take the now-empty table. */
    private void resolveAutoResign(PlayerSession disconnected) {
        boolean resigned;
        synchronized (engineLock) {
            if (disconnected.getState() != SessionState.DISCONNECTED_PENDING) {
                resigned = false;
            } else {
                resigned = true;

                PieceColor winnerColor = disconnected == whiteSession ? PieceColor.BLACK : PieceColor.WHITE;
                WebSocket remaining = disconnected == whiteSession ? blackConn : whiteConn;

                factory.getEventBus().publish(new GameEndedEvent(winnerColor));

                if (remaining != null) {
                    String message = "Opponent disconnected. Game over.";
                    remaining.send(StateCodec.encodeOpponentDisconnected(message));
                    remaining.close(1000, message);
                }

                whiteConn = null;
                blackConn = null;
                whiteSession = null;
                blackSession = null;

                factory.restartGame();
                gameOverHandled = false;
                ratingsAppliedForCurrentGame = false;

                tryPromoteFromQueue();
            }
        }
        if (resigned) {
            broadcastState();
        }
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
        String type = StateCodec.peekType(message);
        if ("login".equals(type)) {
            handleLogin(conn, StateCodec.decodeLoginUsername(message), StateCodec.decodeLoginPassword(message));
        } else if ("seek".equals(type)) {
            handleSeek(conn);
        }
    }

    /** Caller must hold engineLock. Validates credentials against SQLite (auto-registering a new
     *  username). A username with a DISCONNECTED_PENDING session mid-grace-period reconnects into
     *  its old color slot; otherwise the connection lands at IDLE and waits for a "seek" message -
     *  login no longer auto-assigns a color, only matchmaking does. */
    private void handleLogin(WebSocket conn, String username, String password) {
        String name = (username == null || username.isBlank()) ? "Player" : username;

        LoginResult result = repository.loginOrRegister(name, password);
        if (!result.success) {
            conn.send(StateCodec.encodeLoginResult(false, 0, result.message));
            conn.close();
            return;
        }

        PlayerSession reconnecting = findDisconnectedSessionByUsername(name);
        if (reconnecting != null) {
            reconnectSession(conn, reconnecting, result.rating);
            return;
        }

        PlayerSession session = new PlayerSession(name, result.rating);
        sessionsByConn.put(conn, session);
        conn.send(StateCodec.encodeLoginResult(true, result.rating, null));
    }

    private PlayerSession findDisconnectedSessionByUsername(String username) {
        if (whiteSession != null && whiteSession.getState() == SessionState.DISCONNECTED_PENDING
                && whiteSession.getUsername().equals(username)) {
            return whiteSession;
        }
        if (blackSession != null && blackSession.getState() == SessionState.DISCONNECTED_PENDING
                && blackSession.getUsername().equals(username)) {
            return blackSession;
        }
        return null;
    }

    /** Caller must hold engineLock. Cancels the pending auto-resign timer and rebinds conn into
     *  the reconnecting player's original color slot; broadcastAssignments() doubles as the
     *  "opponent reconnected" signal that cancels the other side's countdown UI. */
    private void reconnectSession(WebSocket conn, PlayerSession session, int rating) {
        ScheduledFuture<?> timer = session.getDisconnectTimer();
        if (timer != null) {
            timer.cancel(false);
            session.setDisconnectTimer(null);
        }
        session.setRating(rating);
        session.setState(SessionState.PLAYING);

        if (session == whiteSession) {
            whiteConn = conn;
        } else {
            blackConn = conn;
        }
        sessionsByConn.put(conn, session);

        conn.send(StateCodec.encodeLoginResult(true, rating, null));
        broadcastAssignments();
    }

    /** Caller must hold engineLock. Queues a logged-in, idle connection to be matched against
     *  another ELO-compatible seeker; times out after GameConfig.SEEK_TIMEOUT_SECONDS. */
    private void handleSeek(WebSocket conn) {
        PlayerSession session = sessionsByConn.get(conn);
        if (session == null || session.getState() != SessionState.IDLE) {
            return;
        }
        session.setState(SessionState.SEEKING);
        matchmaker.addWaiting(conn, session, () -> handleSeekTimeout(conn));
        tryPromoteFromQueue();
    }

    /** Fires GameConfig.SEEK_TIMEOUT_SECONDS after a seek with no match found. */
    private void handleSeekTimeout(WebSocket conn) {
        synchronized (engineLock) {
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

    /** Caller must hold engineLock. Only ever seats a pair while the single shared game is empty -
     *  extra seekers simply wait their turn in the queue. */
    private void tryPromoteFromQueue() {
        if (whiteSession != null || blackSession != null) {
            return;
        }
        List<Map.Entry<WebSocket, PlayerSession>> pair = matchmaker.findCompatiblePair();
        if (pair == null) {
            return;
        }

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

        whiteSession = newWhite;
        blackSession = newBlack;
        whiteConn = first.getKey();
        blackConn = second.getKey();

        ratingsAppliedForCurrentGame = false;
        broadcastAssignments();
    }

    /** Caller must hold engineLock. Sends each connected player their color, both usernames, and both ratings. */
    private void broadcastAssignments() {
        String whiteName = whiteSession == null ? null : whiteSession.getUsername();
        String blackName = blackSession == null ? null : blackSession.getUsername();
        int whiteRating = whiteSession == null ? 1200 : whiteSession.getRating();
        int blackRating = blackSession == null ? 1200 : blackSession.getRating();
        if (whiteConn != null) whiteConn.send(StateCodec.encodeAssign(PieceColor.WHITE, whiteName, blackName, whiteRating, blackRating));
        if (blackConn != null) blackConn.send(StateCodec.encodeAssign(PieceColor.BLACK, whiteName, blackName, whiteRating, blackRating));
    }

    /** GameEndedEvent subscriber. Runs on the same thread already holding engineLock (all event
     *  publishes happen from inside engine.requestMove/requestJump/advanceClock or the
     *  resolveAutoResign teardown, which the server only ever calls under engineLock) - safe
     *  without a second lock since monitors are reentrant. */
    private void applyEloOnGameEnd(Event event) {
        PieceColor winnerColor = ((GameEndedEvent) event).getWinnerColor();
        if (winnerColor == null || whiteSession == null || blackSession == null || ratingsAppliedForCurrentGame) {
            return;
        }
        ratingsAppliedForCurrentGame = true;

        PlayerSession winner = winnerColor == PieceColor.WHITE ? whiteSession : blackSession;
        PlayerSession loser = winnerColor == PieceColor.WHITE ? blackSession : whiteSession;

        int[] updated = EloCalculator.computeNewRatings(winner.getRating(), loser.getRating());
        winner.setRating(updated[0]);
        loser.setRating(updated[1]);

        repository.updateRating(winner.getUsername(), winner.getRating());
        repository.updateRating(loser.getUsername(), loser.getRating());

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
