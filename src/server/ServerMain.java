package server;

import input.GameFactory;

public class ServerMain {
    private static final int PORT = 8887;

    public static void main(String[] args) throws Exception {
        GameFactory factory = new GameFactory();
        factory.initializeStandardBoard();

        PlayerRepository repository = new PlayerRepository();
        GameServer server = new GameServer(PORT, factory, repository);
        server.start();
        server.runGameLoop();
    }
}
