package client;

import events.Event;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import protocol.StateCodec;

import java.net.URI;
import java.util.function.Consumer;

/** Thin WebSocket connection to the game server - no game logic lives here, only message routing. */
public class GameClient extends WebSocketClient {

    private final Consumer<protocol.NetworkState> onState;
    private final Consumer<String> onError;
    private final Consumer<Event> onEvent;

    public GameClient(URI serverUri, Consumer<protocol.NetworkState> onState, Consumer<String> onError,
                       Consumer<Event> onEvent) {
        super(serverUri);
        this.onState = onState;
        this.onError = onError;
        this.onEvent = onEvent;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to game server");
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
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from game server: " + reason);
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
