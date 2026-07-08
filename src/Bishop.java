public class Bishop implements Piece {
    private final char color;

    public Bishop(char color) {
        this.color = color;
    }

    @Override
    public char getColor() { return color; }

    @Override
    public char getType() { return 'B'; }

    @Override
    public String getRepresentation() { return "" + color + 'B'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return (deltaRow == deltaCol);
    }
}