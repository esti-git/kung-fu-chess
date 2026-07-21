package events;

import enums.PieceColor;
import lombok.Getter;

@Getter
public class GameEndedEvent extends Event {

    public static final String TYPE = "GAME_ENDED";

    private final PieceColor winnerColor;

    public GameEndedEvent(PieceColor winnerColor) {
        super(TYPE);
        this.winnerColor = winnerColor;
    }
}
