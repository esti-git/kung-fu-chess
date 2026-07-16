package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Bishop extends Piece {
    public Bishop(int id, PieceColor color) {
        super(id, color, PieceKind.BISHOP, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "B"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return deltaRow == deltaCol;
    }

    @Override public boolean isSlidingPiece() { return true; }
}
