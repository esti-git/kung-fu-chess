package events;

import enums.PieceColor;
import enums.PieceKind;
import lombok.Getter;
import model.Position;

@Getter
public class MoveMadeEvent extends Event {

    public static final String TYPE = "MOVE_MADE";

    private final PieceColor player;
    private final PieceKind pieceKind;
    private final String pieceRepresentation;
    private final Position from;
    private final Position to;
    private final int boardRows;

    public MoveMadeEvent(PieceColor player, PieceKind pieceKind, String pieceRepresentation,
                          Position from, Position to, int boardRows) {
        super(TYPE);
        this.player = player;
        this.pieceKind = pieceKind;
        this.pieceRepresentation = pieceRepresentation;
        this.from = from;
        this.to = to;
        this.boardRows = boardRows;
    }
}
