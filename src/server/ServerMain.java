package server;

public class ServerMain {
    private static final int PORT = 8887;

    public static void main(String[] args) throws Exception {
        PlayerRepository repository = new PlayerRepository();
        GameServer server = new GameServer(PORT, repository);
        server.start();
        server.runGameLoop();
    }
}
