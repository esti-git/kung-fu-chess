public class Piece {
    private final char color;
    private final char type;

    public Piece(char color, char type) {
        this.color = color;
        this.type = type;
    }

    public char getColor() {
        return color;
    }

    public char getType() {
        return type;
    }

    public String getRepresentation() {
        return "" + color + type;
    }

    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int rows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);

        switch (type) {
            case 'K':
                return (deltaRow <= 1 && deltaCol <= 1);

            case 'N':
                return (deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2);

            case 'R':
                return (fromRow == toRow || fromCol == toCol);

            case 'B':
                return (deltaRow == deltaCol);

            case 'Q':
                return (fromRow == toRow || fromCol == toCol) || (deltaRow == deltaCol);

            case 'P':
                int expectedRowDirection = (color == 'w') ? -1 : 1;
                int actualRowDirection = toRow - fromRow;
                int startRow = (color == 'w') ? (rows - 1) : 0;

                if (deltaCol == 0) {
                    if (actualRowDirection == expectedRowDirection) {
                        return true;
                    }
                    if (actualRowDirection == expectedRowDirection * 2 && fromRow == startRow) {
                        return true;
                    }
                }
                if (deltaCol == 1 && actualRowDirection == expectedRowDirection) {
                    return true;
                }
                return false;

            default:
                return false;
        }
    }
}