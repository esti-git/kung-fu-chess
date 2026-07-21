package protocol;

import enums.PieceColor;
import lombok.AllArgsConstructor;
import view.BoardSnapshot;

@AllArgsConstructor
public class NetworkState {
    public final BoardSnapshot snapshot;
    public final boolean gameOver;
    public final PieceColor winnerColor;
}
