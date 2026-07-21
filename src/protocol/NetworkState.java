package protocol;

import enums.PieceColor;
import lombok.AllArgsConstructor;
import view.BoardSnapshot;

/** Decoded form of a "state" message: the board snapshot plus overall game-over status. */
@AllArgsConstructor
public class NetworkState {
    public final BoardSnapshot snapshot;
    public final boolean gameOver;
    public final PieceColor winnerColor;
}
