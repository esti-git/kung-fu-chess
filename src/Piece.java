public interface Piece {
    char getColor(); // 'w' או 'b'
    char getType();  // 'K', 'N', 'R', 'B', 'Q', 'P'
    String getRepresentation(); // למשל "wP" או "bK"

    // בדיקה האם המהלך תואם לתבנית התנועה של הכלי הספציפי
    boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows);
}