package server;

import enums.PieceColor;

/** Per-connection identity, attached to the WebSocket once a client's join message is accepted. */
public class PlayerSession {
    public final String username;
    public final PieceColor color;

    public PlayerSession(String username, PieceColor color) {
        this.username = username;
        this.color = color;
    }
}
