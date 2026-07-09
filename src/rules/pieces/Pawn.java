package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Pawn extends Piece {
    public Pawn(int id, PieceColor color) {
        super(id, color, PieceKind.PAWN, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "P"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaCol = Math.abs(toCol - fromCol);
        int expectedRowDirection = (getColor() == PieceColor.WHITE) ? -1 : 1;
        int actualRowDirection = toRow - fromRow;
        int startRow = (getColor() == PieceColor.WHITE) ? (totalRows - 1) : 0;

        if (deltaCol == 0) {
            if (actualRowDirection == expectedRowDirection) return true;
            if (actualRowDirection == expectedRowDirection * 2 && fromRow == startRow) return true;
        }

        if (deltaCol == 1 && actualRowDirection == expectedRowDirection) return true;

        return false;
    }
}
