import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.Bishop;

import static org.junit.jupiter.api.Assertions.*;

class BishopTest {

    @Test
    void testDiagonalMoveIsLegal() {
        Piece bishop = new Bishop(1, PieceColor.WHITE);
        assertTrue(bishop.isMovementPatternLegal(2, 2, 5, 5, 8));
        assertTrue(bishop.isMovementPatternLegal(4, 4, 2, 6, 8));
        assertTrue(bishop.isMovementPatternLegal(4, 4, 1, 1, 8));
        assertTrue(bishop.isMovementPatternLegal(0, 0, 7, 7, 8));
        assertTrue(bishop.isMovementPatternLegal(4, 4, 5, 3, 8));
    }

    @Test
    void testNonDiagonalMoveIsIllegal() {
        Piece bishop = new Bishop(1, PieceColor.WHITE);
        assertFalse(bishop.isMovementPatternLegal(2, 2, 2, 5, 8));
        assertFalse(bishop.isMovementPatternLegal(2, 2, 5, 2, 8));
        assertFalse(bishop.isMovementPatternLegal(2, 2, 4, 3, 8));
    }

    @Test
    void testKnightShapedMoveIsIllegal() {
        Piece bishop = new Bishop(1, PieceColor.WHITE);
        assertFalse(bishop.isMovementPatternLegal(4, 4, 6, 5, 8));
    }

    @Test
    void testIsSlidingPiece() {
        assertTrue(new Bishop(1, PieceColor.WHITE).isSlidingPiece());
    }

    @Test
    void testRepresentationAndIdentity() {
        Piece whiteBishop = new Bishop(7, PieceColor.WHITE);
        Piece blackBishop = new Bishop(8, PieceColor.BLACK);
        assertEquals("wB", whiteBishop.getRepresentation());
        assertEquals("bB", blackBishop.getRepresentation());
        assertEquals(PieceKind.BISHOP, whiteBishop.getKind());
        assertEquals(7, whiteBishop.getId());
        assertEquals(PieceColor.BLACK, blackBishop.getColor());
    }
}
