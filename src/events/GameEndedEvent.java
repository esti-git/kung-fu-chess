package events;

import enums.PieceColor;

/** Published the moment a winner is decided (currently: a king is captured). */
public class GameEndedEvent extends Event {

    public static final String TYPE = "GAME_ENDED";

    private final PieceColor winnerColor;

    public GameEndedEvent(PieceColor winnerColor) {
        super(TYPE);
        this.winnerColor = winnerColor;
    }

    public PieceColor getWinnerColor() {
        return winnerColor;
    }
}
