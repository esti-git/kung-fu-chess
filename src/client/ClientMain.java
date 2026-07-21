package client;

import java.net.URI;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "ws://localhost:8887";

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            username = "Player";
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        ClientView view = new ClientView();
        HomeView home = new HomeView();
        boolean[] boardShown = {false};

        GameClient client = new GameClient(new URI(url), username, password, view::onState, view::onError, view::onEvent,
                identity -> {
                    home.close();
                    if (!boardShown[0]) {
                        boardShown[0] = true;
                        view.show();
                    }
                    view.onAssign(identity);
                },
                view::onRejected, view::onOpponentDisconnected, view::onConnectionClosed,
                result -> {
                    view.onLoginResult(result);
                    if (result.success) home.show();
                },
                home::onSeekTimeout, view::onDisconnectCountdown);
        view.setClient(client);

        home.setOnPlay(() -> {
            client.sendSeek();
            home.showSearching();
        });

        client.connectBlocking();
    }
}
