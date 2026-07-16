import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.Queen;

import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    @Test
    void testStraightMoveIsLegal() {
        Piece queen = new Queen(1, PieceColor.WHITE);
        assertTrue(queen.isMovementPatternLegal(3, 3, 3, 7, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 7, 3, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 3, 0, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 0, 3, 8));
    }

    @Test
    void testDiagonalMoveIsLegal() {
        Piece queen = new Queen(1, PieceColor.WHITE);
        assertTrue(queen.isMovementPatternLegal(3, 3, 6, 6, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 1, 1, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 5, 1, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 1, 5, 8));
    }

    @Test
    void testKnightLikeMoveIsIllegal() {
        Piece queen = new Queen(1, PieceColor.WHITE);
        assertFalse(queen.isMovementPatternLegal(3, 3, 5, 4, 8));
        assertFalse(queen.isMovementPatternLegal(3, 3, 4, 5, 8));
    }

    @Test
    void testIsSlidingPiece() {
        assertTrue(new Queen(1, PieceColor.WHITE).isSlidingPiece());
    }

    @Test
    void testRepresentationAndIdentity() {
        Piece whiteQueen = new Queen(9, PieceColor.WHITE);
        Piece blackQueen = new Queen(10, PieceColor.BLACK);
        assertEquals("wQ", whiteQueen.getRepresentation());
        assertEquals("bQ", blackQueen.getRepresentation());
        assertEquals(PieceKind.QUEEN, whiteQueen.getKind());
        assertEquals(9, whiteQueen.getId());
        assertEquals(PieceColor.BLACK, blackQueen.getColor());
    }
}
