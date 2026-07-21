package events;

import enums.PieceColor;
import lombok.Getter;

/** Published the moment a winner is decided (currently: a king is captured). */
@Getter
public class GameEndedEvent extends Event {

    public static final String TYPE = "GAME_ENDED";

    private final PieceColor winnerColor;

    public GameEndedEvent(PieceColor winnerColor) {
        super(TYPE);
        this.winnerColor = winnerColor;
    }
}
