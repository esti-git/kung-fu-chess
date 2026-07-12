import enums.PieceColor;
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
    }

    @Test
    void testDiagonalMoveIsLegal() {
        Piece queen = new Queen(1, PieceColor.WHITE);
        assertTrue(queen.isMovementPatternLegal(3, 3, 6, 6, 8));
        assertTrue(queen.isMovementPatternLegal(3, 3, 1, 1, 8));
    }

    @Test
    void testKnightLikeMoveIsIllegal() {
        Piece queen = new Queen(1, PieceColor.WHITE);
        assertFalse(queen.isMovementPatternLegal(3, 3, 5, 4, 8));
        assertFalse(queen.isMovementPatternLegal(3, 3, 4, 5, 8));
    }
}
