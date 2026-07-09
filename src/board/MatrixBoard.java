package board;

import model.Board;
import model.Piece;

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
    public Piece getPieceAt(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return null;
        return matrix[r][c];
    }

    @Override
    public void setPieceAt(int r, int c, Piece piece) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            matrix[r][c] = piece;
        }
    }

    @Override
    public boolean isEmpty(int r, int c) {
        return getPieceAt(r, c) == null;
    }
}
