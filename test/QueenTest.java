import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    @Test
    void testQueenMovementPattern() {
        Piece queen = new Queen('b');

        // תנועה קווית (כמו צריח) - חוקי
        assertTrue(queen.isMovementPatternLegal(3, 3, 3, 7, 8));

        // תנועה אלכסונית (כמו רץ) - חוקי
        assertTrue(queen.isMovementPatternLegal(3, 3, 6, 6, 8));

        // מהלך "משולב" שאינו קו ישר או אלכסון (כמו פרש) - אסור
        assertFalse(queen.isMovementPatternLegal(3, 3, 5, 4, 8));
    }
}