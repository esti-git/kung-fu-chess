import enums.PieceColor;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.King;

import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    @Test
    void testOneStepMoveIsLegal() {
        Piece king = new King(1, PieceColor.WHITE);
        assertTrue(king.isMovementPatternLegal(4, 4, 5, 5, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 4, 3, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 3, 4, 8));
    }

    @Test
    void testMoreThanOneStepIsIllegal() {
        Piece king = new King(1, PieceColor.WHITE);
        assertFalse(king.isMovementPatternLegal(4, 4, 4, 6, 8));
        assertFalse(king.isMovementPatternLegal(4, 4, 2, 4, 8));
        assertFalse(king.isMovementPatternLegal(4, 4, 6, 6, 8));
    }
}
