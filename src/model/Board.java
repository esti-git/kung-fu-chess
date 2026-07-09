package model;

public interface Board {
    Piece getPieceAt(Position pos);
    void setPieceAt(Position pos, Piece piece);
    boolean isEmpty(Position pos);
    
    boolean isValidPosition(Position pos);

    int getRows();
    int getCols();
}