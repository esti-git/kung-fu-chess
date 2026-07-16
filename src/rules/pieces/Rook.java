package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;

public class Rook extends Piece {
    public Rook(int id, PieceColor color) {
        super(id, color, PieceKind.ROOK, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "R"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        return fromRow == toRow || fromCol == toCol;
    }

    @Override public boolean isSlidingPiece() { return true; }
}
