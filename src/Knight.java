public class Knight implements Piece {
    private final char color;
    public Knight(char color) { this.color = color; }

    @Override
    public char getColor() { return color; }
    @Override
    public char getType() { return 'N'; }
    @Override
    public String getRepresentation() { return color + "N"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaRow = Math.abs(toRow - fromRow);
        int deltaCol = Math.abs(toCol - fromCol);
        return (deltaRow == 2 && deltaCol == 1) || (deltaRow == 1 && deltaCol == 2);
    }
}