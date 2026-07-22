package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Rook extends Piece {
    public Rook(int id, PieceColor color) {
        super(id, color, PieceKind.ROOK, null);
    }

    @Override protected char code() { return 'R'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        return isStraightLine(fromRow, fromCol, toRow, toCol);
    }

    @Override public boolean isSlidingPiece() { return true; }

    static boolean isStraightLine(int fromRow, int fromCol, int toRow, int toCol) {
        return fromRow == toRow || fromCol == toCol;
    }
}
