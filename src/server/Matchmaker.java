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

/** FIFO queue of players seeking a game, matched by ELO proximity. Not thread-safe on its own -
 *  every method here must be called while the caller holds GameServer's engineLock, same
 *  convention as GameServer's own engineLock-guarded helpers. */
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

    /** Cancels the pending timeout (if any) and removes conn from the queue. Returns the session
     *  that was waiting, or null if conn wasn't queued. */
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

    /** Returns the first two waiting entries within GameConfig.RATING_RANGE of each other,
     *  or null if no such pair exists yet. Does not remove them from the queue. */
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
