package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class King extends Piece {
    public King(int id, PieceColor color) {
        super(id, color, PieceKind.KING, null);
    }

    @Override protected char code() { return 'K'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = rowDelta(fromRow, toRow);
        int deltaCol = colDelta(fromCol, toCol);
        return deltaRow <= 1 && deltaCol <= 1;
    }
}
