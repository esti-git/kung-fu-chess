package model;

import model.Piece;
import model.Position;

public interface Board {
    Piece getPieceAt(Position pos);
    void setPieceAt(Position pos, Piece piece);
    void addPiece(Position pos, Piece piece);
    void movePiece(Position source, Position destination);
    Piece removePiece(Position pos);
    boolean isEmpty(Position pos);
    boolean isValidPosition(Position pos);
    int getRows();
    int getCols();
}