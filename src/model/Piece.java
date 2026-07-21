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

    /** True for pieces that must have every intervening square clear along their path (rook/bishop/queen). */
    public boolean isSlidingPiece() { return false; }

    /**
     * Extra validation beyond the geometric movement pattern, for pieces with rules that
     * depend on board/pending-move state (e.g. pawn double-step path clearance and
     * capture-only diagonals). Called only after {@link #isMovementPatternLegal} has passed.
     *
     * @return an error message if the move is illegal, or null if this piece has no objection.
     */
    public String validateSpecialMove(Board board, Position source, Position destination, Piece destinationPiece, List<PendingMove> pendingMoves) {
        return null;
    }
}
