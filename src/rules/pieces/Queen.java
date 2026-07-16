package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Queen extends Piece {
    public Queen(int id, PieceColor color) {
        super(id, color, PieceKind.QUEEN, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "Q"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return (fromRow == toRow || fromCol == toCol) || (deltaRow == deltaCol);
    }

    @Override public boolean isSlidingPiece() { return true; }
}
