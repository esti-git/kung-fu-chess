package board;

import model.Board;
import model.Piece;
import model.Position;

import java.util.List;

public class MatrixBoard implements Board {
    private Piece[][] matrix;
    private int rows;
    private int cols;

    @Override public int getRows() { return rows; }
    @Override public int getCols() { return cols; }

    public void initialize(List<Piece> pieces, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new Piece[rows][cols];
        for (Piece piece : pieces) {
            matrix[piece.getCell().getRow()][piece.getCell().getCol()] = piece;
        }
    }

    @Override
    public Piece getPieceAt(Position pos) {
        if (pos == null || pos.getRow() < 0 || pos.getRow() >= rows || pos.getCol() < 0 || pos.getCol() >= cols) return null;
        return matrix[pos.getRow()][pos.getCol()];
    }

    @Override
    public void setPieceAt(Position pos, Piece piece) {
        if (pos != null && pos.getRow() >= 0 && pos.getRow() < rows && pos.getCol() >= 0 && pos.getCol() < cols) {
            matrix[pos.getRow()][pos.getCol()] = piece;
        }
    }

    @Override
    public boolean isEmpty(Position pos) {
        return getPieceAt(pos) == null;
    }

    @Override
    public boolean isValidPosition(Position pos) {
        return pos != null && pos.getRow() >= 0 && pos.getRow() < rows && pos.getCol() >= 0 && pos.getCol() < cols;
    }
}
