public class Rook implements Piece {
    private final char color;

    public Rook(char color) {
        this.color = color;
    }

    @Override
    public char getColor() { return color; }

    @Override
    public char getType() { return 'R'; }

    @Override
    public String getRepresentation() { return "" + color + 'R'; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        return (fromRow == toRow || fromCol == toCol);
    }
}