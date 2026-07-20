package protocol;

import enums.PieceColor;
import view.BoardSnapshot;

/** Decoded form of a "state" message: the board snapshot plus overall game-over status. */
public class NetworkState {
    public final BoardSnapshot snapshot;
    public final boolean gameOver;
    public final PieceColor winnerColor;

    public NetworkState(BoardSnapshot snapshot, boolean gameOver, PieceColor winnerColor) {
        this.snapshot = snapshot;
        this.gameOver = gameOver;
        this.winnerColor = winnerColor;
    }
}
