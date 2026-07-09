package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class King extends Piece {
    public King(int id, PieceColor color) {
        super(id, color, PieceKind.KING, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "K"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return deltaRow <= 1 && deltaCol <= 1;
    }
}
