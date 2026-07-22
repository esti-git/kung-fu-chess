package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Queen extends Piece {
    public Queen(int id, PieceColor color) {
        super(id, color, PieceKind.QUEEN, null);
    }

    @Override protected char code() { return 'Q'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        return Rook.isStraightLine(fromRow, fromCol, toRow, toCol) || Bishop.isDiagonal(fromRow, fromCol, toRow, toCol);
    }

    @Override public boolean isSlidingPiece() { return true; }
}
