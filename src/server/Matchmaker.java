package server;

import config.GameConfig;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Matchmaker {

    private final LinkedHashMap<WebSocket, PlayerSession> waiting = new LinkedHashMap<>();
    private final Map<WebSocket, ScheduledFuture<?>> timeouts = new LinkedHashMap<>();
    private final ScheduledExecutorService scheduler;

    public Matchmaker(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public void addWaiting(WebSocket conn, PlayerSession session, Runnable onTimeout) {
        waiting.put(conn, session);
        ScheduledFuture<?> future = scheduler.schedule(
                onTimeout, GameConfig.SEEK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        timeouts.put(conn, future);
    }

    public PlayerSession remove(WebSocket conn) {
        ScheduledFuture<?> future = timeouts.remove(conn);
        if (future != null) {
            future.cancel(false);
        }
        return waiting.remove(conn);
    }

    public boolean isWaiting(WebSocket conn) {
        return waiting.containsKey(conn);
    }

    public List<Map.Entry<WebSocket, PlayerSession>> findCompatiblePair() {
        List<Map.Entry<WebSocket, PlayerSession>> entries = new ArrayList<>(waiting.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                int ratingA = entries.get(i).getValue().getRating();
                int ratingB = entries.get(j).getValue().getRating();
                if (Math.abs(ratingA - ratingB) <= GameConfig.RATING_RANGE) {
                    List<Map.Entry<WebSocket, PlayerSession>> pair = new ArrayList<>();
                    pair.add(entries.get(i));
                    pair.add(entries.get(j));
                    return pair;
                }
            }
        }
        return null;
    }
}
