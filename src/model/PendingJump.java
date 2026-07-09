package model;

import interfaces.Piece;

public class PendingJump {
    private int row, col;
    private Piece piece;
    private long startTime;
    private long endTime;

    public PendingJump(int row, int col, Piece piece, long startTime) {
        this.row = row;
        this.col = col;
        this.piece = piece;
        this.startTime = startTime;
        this.endTime = startTime + 1000L;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Piece getPiece() { return piece; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
}