import interfaces.Piece;
import org.junit.jupiter.api.Test;
import pieces.Rook;

import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    @Test
    void testRookMovementPattern() {
        Piece rook = new Rook('b');

        // תנועה חוקית לאורך השורה או העמודה
        assertTrue(rook.isMovementPatternLegal(3, 3, 3, 7, 8)); // ימינה בשורה
        assertTrue(rook.isMovementPatternLegal(3, 3, 0, 3, 8)); // למעלה בעמודה

        // תנועה באלכסון - אסור לצריח
        assertFalse(rook.isMovementPatternLegal(3, 3, 5, 5, 8));
    }
}