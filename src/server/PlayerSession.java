package server;

import enums.PieceColor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ScheduledFuture;

/** Per-connection identity, alive from login until the socket is gone for good (across matchmaking,
 *  play, and any disconnect-grace/reconnect cycle). Rating is mutated in place when a game ends, so
 *  it stays in sync with what's already persisted via PlayerRepository without rebuilding the session. */
@Getter
public class PlayerSession {
    private final String username;
    @Setter
    private PieceColor color;
    @Setter
    private int rating;
    @Setter
    private SessionState state = SessionState.IDLE;
    @Setter
    private ScheduledFuture<?> disconnectTimer;

    public PlayerSession(String username, int rating) {
        this.username = username;
        this.rating = rating;
    }
}
