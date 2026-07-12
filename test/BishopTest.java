import enums.PieceColor;
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
    }

    @Test
    void testNonDiagonalMoveIsIllegal() {
        Piece bishop = new Bishop(1, PieceColor.WHITE);
        assertFalse(bishop.isMovementPatternLegal(2, 2, 2, 5, 8));
        assertFalse(bishop.isMovementPatternLegal(2, 2, 5, 2, 8));
        assertFalse(bishop.isMovementPatternLegal(2, 2, 4, 3, 8));
    }
}
