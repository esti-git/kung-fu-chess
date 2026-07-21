package client;

import events.Event;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import protocol.StateCodec;

import java.net.URI;
import java.util.function.Consumer;

/** Thin WebSocket connection to the game server - no game logic lives here, only message routing. */
public class GameClient extends WebSocketClient {

    private final String username;
    private final String password;
    private final Consumer<protocol.NetworkState> onState;
    private final Consumer<String> onError;
    private final Consumer<Event> onEvent;
    private final Consumer<protocol.AssignedIdentity> onAssign;
    private final Consumer<String> onRejected;
    private final Consumer<String> onOpponentDisconnected;
    private final Consumer<String> onConnectionClosed;
    private final Consumer<protocol.LoginResult> onLoginResult;

    public GameClient(URI serverUri, String username, String password, Consumer<protocol.NetworkState> onState,
                       Consumer<String> onError, Consumer<Event> onEvent, Consumer<protocol.AssignedIdentity> onAssign,
                       Consumer<String> onRejected, Consumer<String> onOpponentDisconnected,
                       Consumer<String> onConnectionClosed, Consumer<protocol.LoginResult> onLoginResult) {
        super(serverUri);
        this.username = username;
        this.password = password;
        this.onState = onState;
        this.onError = onError;
        this.onEvent = onEvent;
        this.onAssign = onAssign;
        this.onRejected = onRejected;
        this.onOpponentDisconnected = onOpponentDisconnected;
        this.onConnectionClosed = onConnectionClosed;
        this.onLoginResult = onLoginResult;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to game server");
        send(StateCodec.encodeLogin(username, password));
    }

    @Override
    public void onMessage(String message) {
        String type = StateCodec.peekType(message);
        if ("state".equals(type)) {
            onState.accept(StateCodec.decodeState(message));
        } else if ("error".equals(type)) {
            onError.accept(StateCodec.decodeErrorMessage(message));
        } else if ("event".equals(type)) {
            onEvent.accept(StateCodec.decodeEvent(message));
        } else if ("assign".equals(type)) {
            onAssign.accept(StateCodec.decodeAssign(message));
        } else if ("rejected".equals(type)) {
            onRejected.accept(StateCodec.decodeErrorMessage(message));
        } else if ("opponentDisconnected".equals(type)) {
            onOpponentDisconnected.accept(StateCodec.decodeErrorMessage(message));
        } else if ("loginResult".equals(type)) {
            onLoginResult.accept(StateCodec.decodeLoginResult(message));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from game server: " + reason);
        onConnectionClosed.accept(reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public void sendMove(String command) {
        send(command);
    }

    public void sendJump(String command) {
        send(command);
    }
}
