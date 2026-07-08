import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PieceTest {

    @Test
    void testKnightMovement() {
        // 1. אתחול - יוצרים את הכלי שרוצים לבדוק (פרש לבן)
        Piece knight = new Piece('w', 'N');

        // 2. בדיקה בפועל - נותנים מהלך חוקי לפרש ומצפים לקבל true
        boolean legalMove = knight.isMovementPatternLegal(7, 1, 5, 2, 8);
        assertTrue(legalMove, "הפרש אמור לנוע בצורת L");

        // 3. בדיקה של מהלך לא חוקי - מצפים לקבל false
        boolean illegalMove = knight.isMovementPatternLegal(7, 1, 5, 1, 8);
        assertFalse(illegalMove, "הפרש לא יכול לנוע ישר");
    }
}