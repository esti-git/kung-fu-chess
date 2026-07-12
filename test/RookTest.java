import enums.PieceColor;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.Rook;

import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    @Test
    void testStraightMoveIsLegal() {
        Piece rook = new Rook(1, PieceColor.WHITE);
        assertTrue(rook.isMovementPatternLegal(3, 3, 3, 7, 8));
        assertTrue(rook.isMovementPatternLegal(3, 3, 0, 3, 8));
    }

    @Test
    void testDiagonalMoveIsIllegal() {
        Piece rook = new Rook(1, PieceColor.WHITE);
        assertFalse(rook.isMovementPatternLegal(3, 3, 5, 5, 8));
        assertFalse(rook.isMovementPatternLegal(3, 3, 1, 5, 8));
    }
}
