import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    @Test
    void testKingMovementPattern() {
        Piece king = new King('w');

        // צעד אחד לכל כיוון (כולל אלכסון) - חוקי
        assertTrue(king.isMovementPatternLegal(4, 4, 5, 5, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 4, 3, 8));

        // מעבר לצעד אחד (למשל 2 משבצות ימינה) - אסור
        assertFalse(king.isMovementPatternLegal(4, 4, 4, 6, 8));
    }
}