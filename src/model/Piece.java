package model;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public abstract class Piece {
    private final int id;
    private final PieceColor color;
    private final PieceKind kind;
    @Setter
    private Position cell;
    @Setter
    private PieceState state;

    public Piece(int id, PieceColor color, PieceKind kind, Position cell) {
        this.id = id;
        this.color = color;
        this.kind = kind;
        this.cell = cell;
        this.state = PieceState.IDLE;
    }

    public abstract String getRepresentation();
    public abstract boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows);

    public boolean isSlidingPiece() { return false; }

    public String validateSpecialMove(Board board, Position source, Position destination, Piece destinationPiece, List<PendingMove> pendingMoves) {
        return null;
    }
}
