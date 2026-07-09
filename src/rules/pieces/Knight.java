package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Knight extends Piece {
    public Knight(int id, PieceColor color) {
        super(id, color, PieceKind.KNIGHT, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "N"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return (deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2);
    }
}
