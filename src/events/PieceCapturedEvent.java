package events;

import enums.PieceColor;
import enums.PieceKind;
import lombok.Getter;

/** Published whenever a piece is removed from the board by an opposing piece. */
@Getter
public class PieceCapturedEvent extends Event {

    public static final String TYPE = "PIECE_CAPTURED";

    private final PieceColor capturedColor;
    private final PieceKind capturedKind;
    private final PieceColor capturedBy;

    public PieceCapturedEvent(PieceColor capturedColor, PieceKind capturedKind, PieceColor capturedBy) {
        super(TYPE);
        this.capturedColor = capturedColor;
        this.capturedKind = capturedKind;
        this.capturedBy = capturedBy;
    }
}
