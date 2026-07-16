package model;

public class PendingRest {
    private final Piece piece;
    private final long endTime;

    public PendingRest(Piece piece, long endTime) {
        this.piece = piece;
        this.endTime = endTime;
    }

    public Piece getPiece() { return piece; }
    public long getEndTime() { return endTime; }
}
