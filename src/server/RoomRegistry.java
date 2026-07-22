package server;

import org.java_websocket.WebSocket;
import view.BoardSnapshotFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

public class RoomRegistry {

    private static final String ID_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int ID_LENGTH = 6;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<WebSocket, String> roomIdByConn = new ConcurrentHashMap<>();
    private final BoardSnapshotFactory snapshotFactory = new BoardSnapshotFactory();

    public Room createRoom(PlayerRepository repository, ScheduledExecutorService scheduler) {
        String roomId;
        Room room;
        do {
            roomId = generateId();
            room = new Room(roomId, repository, scheduler, snapshotFactory);
        } while (rooms.putIfAbsent(roomId, room) != null);
        return room;
    }

    public Room createRoomWithId(String roomId, PlayerRepository repository, ScheduledExecutorService scheduler) {
        Room room = new Room(roomId, repository, scheduler, snapshotFactory);
        return rooms.putIfAbsent(roomId, room) == null ? room : null;
    }

    public Room get(String roomId) {
        return rooms.get(roomId);
    }

    public Room roomFor(WebSocket conn) {
        String roomId = roomIdByConn.get(conn);
        return roomId == null ? null : rooms.get(roomId);
    }

    public void bind(WebSocket conn, String roomId) {
        roomIdByConn.put(conn, roomId);
    }

    public void unbind(WebSocket conn) {
        roomIdByConn.remove(conn);
    }

    public void removeIfEmpty(String roomId) {
        Room room = rooms.get(roomId);
        if (room != null && room.isEmpty()) {
            rooms.remove(roomId);
        }
    }

    public Room findRoomWithDisconnectedUser(String username) {
        for (Room room : rooms.values()) {
            if (room.findDisconnectedSession(username) != null) {
                return room;
            }
        }
        return null;
    }

    public Collection<Room> allRooms() {
        return rooms.values();
    }

    private String generateId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            sb.append(ID_CHARS.charAt(random.nextInt(ID_CHARS.length())));
        }
        return sb.toString();
    }
}
