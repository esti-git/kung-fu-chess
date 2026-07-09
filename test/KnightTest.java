import interfaces.Piece;
import org.junit.jupiter.api.Test;
import pieces.Knight;

import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    @Test
    void testKnightMovementPattern() {
        Piece knight = new Knight('w');

        // מהלכי L חוקיים (2 שורות ו-1 עמודה, או שורה 1 ו-2 עמודות)
        assertTrue(knight.isMovementPatternLegal(4, 4, 6, 5, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 5, 2, 8)); // תוקן כאן ל-Pattern

        // מהלכים ישרים או אלכסוניים רגילים - אסור לפרש
        assertFalse(knight.isMovementPatternLegal(4, 4, 4, 6, 8));
        assertFalse(knight.isMovementPatternLegal(4, 4, 6, 6, 8));
    }
}