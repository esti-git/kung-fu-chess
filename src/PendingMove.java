public class PendingMove {
    int fromRow, fromCol;
    int toRow, toCol;
    Piece piece;
    long arrivalTime;

    public PendingMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, long arrivalTime) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.piece = piece;
        this.arrivalTime = arrivalTime;
    }
}