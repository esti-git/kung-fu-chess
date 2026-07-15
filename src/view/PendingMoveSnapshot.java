package view;

/** תמונת מצב קפואה של תנועה פעילה - כמו model.PendingMove, אבל עם PieceSnapshot במקום Piece חי */
public class PendingMoveSnapshot {
    private final int fromRow, fromCol;
    private final int toRow, toCol;
    private final PieceSnapshot piece;
    private final long arrivalTime;

    public PendingMoveSnapshot(int fromRow, int fromCol, int toRow, int toCol, PieceSnapshot piece, long arrivalTime) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.piece = piece;
        this.arrivalTime = arrivalTime;
    }

    public int getFromRow() { return fromRow; }
    public int getFromCol() { return fromCol; }
    public int getToRow() { return toRow; }
    public int getToCol() { return toCol; }
    public PieceSnapshot getPiece() { return piece; }
    public long getArrivalTime() { return arrivalTime; }
}
