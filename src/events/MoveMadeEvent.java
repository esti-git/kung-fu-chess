package events;

import enums.PieceColor;
import enums.PieceKind;
import model.Position;

/** Published whenever a piece finishes landing on a square, from either a move or a jump. */
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

    public PieceColor getPlayer() {
        return player;
    }

    public PieceKind getPieceKind() {
        return pieceKind;
    }

    public String getPieceRepresentation() {
        return pieceRepresentation;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public int getBoardRows() {
        return boardRows;
    }
}
