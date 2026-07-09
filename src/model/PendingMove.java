package model;

import interfaces.Piece;

public class PendingMove {
    private int fromRow, fromCol;
    private int toRow, toCol;
    private Piece piece;
    private long arrivalTime;

    public PendingMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, long arrivalTime) {
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
    public Piece getPiece() { return piece; }
    public long getArrivalTime() { return arrivalTime; }
}