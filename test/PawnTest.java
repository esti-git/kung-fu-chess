import enums.PieceColor;
import enums.PieceKind;
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
        assertTrue(pawn.isMovementPatternLegal(6, 4, 4, 4, 8));
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
    void testWhitePawnThreeStepsIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertFalse(pawn.isMovementPatternLegal(6, 4, 3, 4, 8));
    }

    @Test
    void testBlackPawnOneStepForward() {
        Piece pawn = new Pawn(1, PieceColor.BLACK);
        assertTrue(pawn.isMovementPatternLegal(1, 2, 2, 2, 8));
    }

    @Test
    void testBlackPawnTwoStepsFromStartRow() {
        Piece pawn = new Pawn(1, PieceColor.BLACK);
        assertTrue(pawn.isMovementPatternLegal(1, 2, 3, 2, 8));
    }

    @Test
    void testBlackPawnTwoStepsNotFromStartRowIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.BLACK);
        assertFalse(pawn.isMovementPatternLegal(2, 2, 4, 2, 8));
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

    @Test
    void testSidewaysMoveIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertFalse(pawn.isMovementPatternLegal(5, 4, 5, 5, 8));
    }

    @Test
    void testDiagonalMoveTwoColumnsIsIllegal() {
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertFalse(pawn.isMovementPatternLegal(5, 4, 4, 6, 8));
    }

    @Test
    void testStartRowScalesWithBoardSize() {
        // On a 6-row board white starts at row 4, so a two-step move is only
        // legal from row 4, not from row 6 (the 8-row default start row).
        Piece pawn = new Pawn(1, PieceColor.WHITE);
        assertTrue(pawn.isMovementPatternLegal(4, 2, 2, 2, 6));
        assertFalse(pawn.isMovementPatternLegal(5, 2, 3, 2, 6));
    }

    @Test
    void testIsNotSlidingPiece() {
        assertFalse(new Pawn(1, PieceColor.WHITE).isSlidingPiece());
    }

    @Test
    void testRepresentationAndIdentity() {
        Piece whitePawn = new Pawn(20, PieceColor.WHITE);
        Piece blackPawn = new Pawn(21, PieceColor.BLACK);
        assertEquals("wP", whitePawn.getRepresentation());
        assertEquals("bP", blackPawn.getRepresentation());
        assertEquals(PieceKind.PAWN, whitePawn.getKind());
        assertEquals(20, whitePawn.getId());
        assertEquals(PieceColor.BLACK, blackPawn.getColor());
    }
}
