import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.PawnPromotion;
import rules.pieces.Pawn;
import rules.pieces.Rook;

import static org.junit.jupiter.api.Assertions.*;

class PawnPromotionTest {

    @Test
    void testWhitePawnPromotesAtRowZero() {
        Piece pawn = new Pawn(5, PieceColor.WHITE);
        Piece promoted = PawnPromotion.applyPromotion(pawn, 0, 8);

        assertEquals(PieceKind.QUEEN, promoted.getKind());
        assertEquals(PieceColor.WHITE, promoted.getColor());
        assertEquals(5, promoted.getId());
        assertNotSame(pawn, promoted);
    }

    @Test
    void testBlackPawnPromotesAtLastRow() {
        Piece pawn = new Pawn(6, PieceColor.BLACK);
        Piece promoted = PawnPromotion.applyPromotion(pawn, 7, 8);

        assertEquals(PieceKind.QUEEN, promoted.getKind());
        assertEquals(PieceColor.BLACK, promoted.getColor());
        assertEquals(6, promoted.getId());
    }

    @Test
    void testWhitePawnNotAtPromotionRowIsUnchanged() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        Piece result = PawnPromotion.applyPromotion(pawn, 3, 8);

        assertSame(pawn, result);
    }

    @Test
    void testBlackPawnNotAtPromotionRowIsUnchanged() {
        Piece pawn = new Pawn(2, PieceColor.BLACK);
        Piece result = PawnPromotion.applyPromotion(pawn, 4, 8);

        assertSame(pawn, result);
    }

    @Test
    void testNonPawnPieceIsUnchangedEvenAtPromotionRow() {
        Piece rook = new Rook(3, PieceColor.WHITE);
        Piece result = PawnPromotion.applyPromotion(rook, 0, 8);

        assertSame(rook, result);
    }

    @Test
    void testPromotionRowScalesWithBoardSize() {
        Piece blackPawn = new Pawn(9, PieceColor.BLACK);

        assertSame(blackPawn, PawnPromotion.applyPromotion(blackPawn, 4, 6));

        Piece promoted = PawnPromotion.applyPromotion(blackPawn, 5, 6);
        assertEquals(PieceKind.QUEEN, promoted.getKind());
    }
}
