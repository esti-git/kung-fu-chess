package client;

import java.net.URI;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "ws://localhost:8887";

        ClientView view = new ClientView();
        GameClient client = new GameClient(new URI(url), view::onState, view::onError, view::onEvent);
        view.setClient(client);

        client.connectBlocking();
        view.show();
    }
}
