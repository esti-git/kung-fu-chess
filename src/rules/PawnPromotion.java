package rules;

import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import rules.pieces.Queen;

public class PawnPromotion {

    public static Piece applyPromotion(Piece piece, int toRow, int totalRows) {
        if (piece.getKind() == PieceKind.PAWN) {
            int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : (totalRows - 1);
            if (toRow == promotionRow) {
                return new Queen(piece.getId(), piece.getColor());
            }
        }
        return piece;
    }
}
