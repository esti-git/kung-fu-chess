package model;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;

import java.util.List;

public abstract class Piece {
    private final int id;
    private final PieceColor color;
    private final PieceKind kind;
    private Position cell;
    private PieceState state;

    public Piece(int id, PieceColor color, PieceKind kind, Position cell) {
        this.id = id;
        this.color = color;
        this.kind = kind;
        this.cell = cell;
        this.state = PieceState.IDLE;
    }

    public int getId() { return id; }
    public PieceColor getColor() { return color; }
    public PieceKind getKind() { return kind; }
    public Position getCell() { return cell; }
    public void setCell(Position cell) { this.cell = cell; }
    public PieceState getState() { return state; }
    public void setState(PieceState state) { this.state = state; }

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
