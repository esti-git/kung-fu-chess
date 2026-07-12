import enums.PieceColor;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.Pawn;

import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    @Test
    void testWhitePawnOneStepForward() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertTrue(pawn.isMovementPatternLegal(6, 4, 5, 4, 8));
    }

    @Test
    void testWhitePawnTwoStepsFromStartRow() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertTrue(pawn.isMovementPatternLegal(7, 4, 5, 4, 8));
    }

    @Test
    void testWhitePawnTwoStepsNotFromStartRowIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertFalse(pawn.isMovementPatternLegal(5, 4, 3, 4, 8));
    }

    @Test
    void testWhitePawnBackwardIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertFalse(pawn.isMovementPatternLegal(5, 4, 6, 4, 8));
    }

    @Test
    void testBlackPawnOneStepForward() {
        Piece pawn = new Pawn(1, PieceColor.BLACK);
        assertTrue(pawn.isMovementPatternLegal(1, 2, 2, 2, 8));
    }

    @Test
    void testBlackPawnTwoStepsFromStartRow() {
        Piece pawn = new Pawn(1, PieceColor.BLACK);
        assertTrue(pawn.isMovementPatternLegal(0, 2, 2, 2, 8));
    }

    @Test
    void testBlackPawnBackwardIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.BLACK);
        assertFalse(pawn.isMovementPatternLegal(2, 2, 1, 2, 8));
    }

    @Test
    void testDiagonalCaptureIsLegal() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertTrue(pawn.isMovementPatternLegal(5, 4, 4, 5, 8));
        assertTrue(pawn.isMovementPatternLegal(5, 4, 4, 3, 8));
    }
}
