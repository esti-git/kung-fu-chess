import interfaces.Piece;
import org.junit.jupiter.api.Test;
import pieces.Bishop;

import static org.junit.jupiter.api.Assertions.*;

class BishopTest {

    @Test
    void testBishopMovementPattern() {
        Piece bishop = new Bishop('w');

        // אלכסונים מושלמים (מרחק השורות שווה למרחק העמודות) - חוקי
        assertTrue(bishop.isMovementPatternLegal(2, 2, 5, 5, 8));
        assertTrue(bishop.isMovementPatternLegal(4, 4, 2, 6, 8));

        // תנועה ישרה - אסור לרץ
        assertFalse(bishop.isMovementPatternLegal(2, 2, 2, 5, 8));
        assertFalse(bishop.isMovementPatternLegal(2, 2, 4, 3, 8));
    }
}