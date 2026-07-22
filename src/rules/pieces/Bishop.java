package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Bishop extends Piece {
    public Bishop(int id, PieceColor color) {
        super(id, color, PieceKind.BISHOP, null);
    }

    @Override protected char code() { return 'B'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        return isDiagonal(fromRow, fromCol, toRow, toCol);
    }

    @Override public boolean isSlidingPiece() { return true; }

    static boolean isDiagonal(int fromRow, int fromCol, int toRow, int toCol) {
        return rowDelta(fromRow, toRow) == colDelta(fromCol, toCol);
    }
}
