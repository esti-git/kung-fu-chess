public class King implements Piece {
    private final char color;

    public King(char color) {
        this.color = color;
    }

    @Override
    public char getColor() { return color; }

    @Override
    public char getType() { return 'K'; }

    @Override
    public String getRepresentation() { return "" + color + 'K'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return (deltaRow <= 1 && deltaCol <= 1);
    }
}