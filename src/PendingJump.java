public class PendingJump {
    int row, col;
    Piece piece;
    long startTime;
    long endTime;

    public PendingJump(int row, int col, Piece piece, long startTime) {
        this.row = row;
        this.col = col;
        this.piece = piece;
        this.startTime = startTime;
        this.endTime = startTime + 1000L;
    }
}