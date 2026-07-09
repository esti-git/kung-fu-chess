import board.MatrixBoard;
import interfaces.Piece;
import org.junit.jupiter.api.Test;
import pieces.Queen;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MatrixBoardTest {

    @Test
    void testValidBoardInitialization() {
        MatrixBoard board = new MatrixBoard();
        List<String> rawLines = new ArrayList<>();
        rawLines.add("wR .  bK");
        rawLines.add(".  wP .");

        // וידוא שהאתחול מצליח
        boolean success = board.validateAndInitialize(rawLines);
        assertTrue(success, "הלוח אמור להתאתחל בהצלחה עבור קלט תקין");

        // בדיקת ממדי הלוח
        assertEquals(2, board.getRows());
        assertEquals(3, board.getCols());

        // בדיקה שהכלים נוצרו מסוג הקלאס הנכון (פולימורפיזם)
        assertNotNull(board.getPieceAt(0, 0));
        assertEquals('R', board.getPieceAt(0, 0).getType());
        assertEquals('w', board.getPieceAt(0, 0).getColor());

        // בדיקת משבצת ריקה
        assertTrue(board.isEmpty(0, 1));
        assertNull(board.getPieceAt(0, 1));

        // בדיקה של כלי אויב בשורה הראשונה
        assertEquals('K', board.getPieceAt(0, 2).getType());
        assertEquals('b', board.getPieceAt(0, 2).getColor());
    }

    @Test
    void testInvalidBoardRowWidthMismatch() {
        MatrixBoard board = new MatrixBoard();
        List<String> rawLines = new ArrayList<>();
        rawLines.add("wR . bK");
        rawLines.add(". wP"); // שורה קצרה יותר (רק 2 עמודות במקום 3)

        boolean success = board.validateAndInitialize(rawLines);
        assertFalse(success, "האתחול אמור להיכשל כשיש חוסר התאמה באורך השורות");
    }

    @Test
    void testInvalidBoardUnknownToken() {
        MatrixBoard board = new MatrixBoard();
        List<String> rawLines = new ArrayList<>();
        rawLines.add("wR . bK");
        rawLines.add(". wX ."); // כלי בשם X לא קיים במשחק

        boolean success = board.validateAndInitialize(rawLines);
        assertFalse(success, "האתחול אמור להיכשל כשיש סימן (Token) לא מוכר");
    }

    @Test
    void testGetPieceAtOutOfBounds() {
        MatrixBoard board = new MatrixBoard();
        List<String> rawLines = new ArrayList<>();
        rawLines.add(". .");
        board.validateAndInitialize(rawLines);

        // פנייה מחוץ לגבולות המטריצה צריכה להחזיר null בבטחה ולא להקריס את התוכנית
        assertNull(board.getPieceAt(-1, 0), "חריגה מלמעלה צריכה להחזיר null");
        assertNull(board.getPieceAt(5, 5), "חריגה ימינה/למטה צריכה להחזיר null");
    }

    @Test
    void testSetPieceAt() {
        MatrixBoard board = new MatrixBoard();
        List<String> rawLines = new ArrayList<>();
        rawLines.add(". .");
        board.validateAndInitialize(rawLines);

        // יצירת כלי חדש ידנית והשמתו על הלוח
        Piece newQueen = new Queen('w');
        board.setPieceAt(0, 1, newQueen);

        assertFalse(board.isEmpty(0, 1), "המשבצת לא אמורה להיות ריקה לאחר ההשמה");
        assertEquals('Q', board.getPieceAt(0, 1).getType());
    }
}