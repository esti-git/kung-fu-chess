package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Knight extends Piece {
    public Knight(int id, PieceColor color) {
        super(id, color, PieceKind.KNIGHT, null);
    }

    @Override protected char code() { return 'N'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = rowDelta(fromRow, toRow);
        int deltaCol = colDelta(fromCol, toCol);
        return (deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2);
    }
}
