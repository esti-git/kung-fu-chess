package server;

import enums.PieceColor;

/** Per-connection identity, set once a client's login is accepted. Rating is mutable - it's
 *  updated in place on this same object when a game ends, so it stays in sync with what's
 *  already persisted via PlayerRepository without needing to rebuild the session. */
public class PlayerSession {
    public final String username;
    public final PieceColor color;
    public int rating;

    public PlayerSession(String username, PieceColor color, int rating) {
        this.username = username;
        this.color = color;
        this.rating = rating;
    }
}
