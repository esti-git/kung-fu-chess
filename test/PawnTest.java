import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    @Test
    void testWhitePawnMovement() {
        Piece whitePawn = new Pawn('w');

        // צעד אחד למעלה (שורה 7 ל-6) - חוקי
        assertTrue(whitePawn.isMovementPatternLegal(7, 4, 6, 4, 8));

        // צעד כפול משורת הבסיס (7 ל-5) - חוקי
        assertTrue(whitePawn.isMovementPatternLegal(7, 4, 5, 4, 8));

        // צעד כפול שלא משורת הבסיס (5 ל-3) - אסור
        assertFalse(whitePawn.isMovementPatternLegal(5, 4, 3, 4, 8));

        // לזוז אחורה - אסור
        assertFalse(whitePawn.isMovementPatternLegal(6, 4, 7, 4, 8));
    }

    @Test
    void testBlackPawnMovement() {
        Piece blackPawn = new Pawn('b');

        // צעד אחד למטה (שורה 0 ל-1) - חוקי
        assertTrue(blackPawn.isMovementPatternLegal(0, 2, 1, 2, 8));

        // צעד כפול משורת הבסיס (0 ל-2) - חוקי
        assertTrue(blackPawn.isMovementPatternLegal(0, 2, 2, 2, 8));

        // לזוז אחורה (למעלה) - אסור
        assertFalse(blackPawn.isMovementPatternLegal(1, 2, 0, 2, 8));
    }
}