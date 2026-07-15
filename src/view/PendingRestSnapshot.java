package view;

/** תמונת מצב קפואה של מנוחה פעילה - כמו model.PendingRest, אבל עם PieceSnapshot במקום Piece חי */
public class PendingRestSnapshot {
    private final PieceSnapshot piece;
    private final long endTime;

    public PendingRestSnapshot(PieceSnapshot piece, long endTime) {
        this.piece = piece;
        this.endTime = endTime;
    }

    public PieceSnapshot getPiece() { return piece; }
    public long getEndTime() { return endTime; }
}
