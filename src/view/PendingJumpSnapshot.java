package view;

/** תמונת מצב קפואה של קפיצה פעילה - כמו model.PendingJump, אבל עם PieceSnapshot במקום Piece חי */
public class PendingJumpSnapshot {
    private final int row, col;
    private final PieceSnapshot piece;
    private final long startTime;
    private final long endTime;

    public PendingJumpSnapshot(int row, int col, PieceSnapshot piece, long startTime, long endTime) {
        this.row = row;
        this.col = col;
        this.piece = piece;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public PieceSnapshot getPiece() { return piece; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
}
