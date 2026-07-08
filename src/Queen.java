public class Queen implements Piece {
    private final char color;

    public Queen(char color) {
        this.color = color;
    }

    @Override
    public char getColor() { return color; }

    @Override
    public char getType() { return 'Q'; }

    @Override
    public String getRepresentation() { return "" + color + 'Q'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return (fromRow == toRow || fromCol == toCol) || (deltaRow == deltaCol);
    }
}