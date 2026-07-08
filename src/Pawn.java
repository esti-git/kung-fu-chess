public class Pawn implements Piece {
    private final char color;

    public Pawn(char color) {
        this.color = color;
    }

    @Override
    public char getColor() { return color; }

    @Override
    public char getType() { return 'P'; }

    @Override
    public String getRepresentation() { return "" + color + 'P'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaCol = Math.abs(toCol - fromCol);
        int expectedRowDirection = (color == 'w') ? -1 : 1;
        int actualRowDirection = toRow - fromRow;
        int startRow = (color == 'w') ? (totalRows - 1) : 0;

        // תנועה ישר קדימה
        if (deltaCol == 0) {
            if (actualRowDirection == expectedRowDirection) {
                return true;
            }
            // צעד כפול משורת הבסיס
            if (actualRowDirection == expectedRowDirection * 2 && fromRow == startRow) {
                return true;
            }
        }

        // תנועת הכאה באלכסון (הבדיקה אם יש שם כלי אויב בפועל תתבצע ב-Validator)
        if (deltaCol == 1 && actualRowDirection == expectedRowDirection) {
            return true;
        }

        return false;
    }
}