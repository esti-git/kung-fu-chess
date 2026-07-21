package client;

import java.net.URI;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "ws://localhost:8887";

        System.out.print("Enter username: ");
        String username = new Scanner(System.in).nextLine().trim();
        if (username.isEmpty()) {
            username = "Player";
        }

        ClientView view = new ClientView();
        GameClient client = new GameClient(new URI(url), username, view::onState, view::onError, view::onEvent,
                view::onAssign, view::onRejected, view::onOpponentDisconnected, view::onConnectionClosed);
        view.setClient(client);

        client.connectBlocking();
        view.show();
    }
}
