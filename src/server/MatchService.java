package server;

import enums.PieceColor;
import org.java_websocket.WebSocket;
import server.logging.ServerLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class MatchService {

    private final Matchmaker matchmaker;
    private final RoomRegistry roomRegistry;
    private final PlayerRepository repository;
    private final ScheduledExecutorService scheduler;

    public MatchService(Matchmaker matchmaker, RoomRegistry roomRegistry,
                         PlayerRepository repository, ScheduledExecutorService scheduler) {
        this.matchmaker = matchmaker;
        this.roomRegistry = roomRegistry;
        this.repository = repository;
        this.scheduler = scheduler;
    }

    public static void assignAndActivate(PlayerSession session, PieceColor color) {
        session.setColor(color);
        session.setState(SessionState.PLAYING);
    }

    public void tryPromoteFromQueue() {
        List<Map.Entry<WebSocket, PlayerSession>> pair;
        while ((pair = matchmaker.findCompatiblePair()) != null) {
            Map.Entry<WebSocket, PlayerSession> first = pair.get(0);
            Map.Entry<WebSocket, PlayerSession> second = pair.get(1);
            matchmaker.remove(first.getKey());
            matchmaker.remove(second.getKey());

            PlayerSession newWhite = first.getValue();
            PlayerSession newBlack = second.getValue();
            assignAndActivate(newWhite, PieceColor.WHITE);
            assignAndActivate(newBlack, PieceColor.BLACK);

            Room room = roomRegistry.createRoom(repository, scheduler);
            room.seatMatch(first.getKey(), newWhite, second.getKey(), newBlack);
            roomRegistry.bind(first.getKey(), room.roomId);
            roomRegistry.bind(second.getKey(), room.roomId);
            ServerLog.info("Matched " + newWhite.getUsername() + " vs " + newBlack.getUsername() + " into room " + room.roomId);
        }
    }
}
