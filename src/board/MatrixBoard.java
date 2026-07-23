package board;

import model.Board;
import model.Piece;
import model.Position;
import java.util.List;

public class MatrixBoard implements Board {
    private Piece[][] matrix;
    private int rows;
    private int cols;

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }

    public void initialize(List<Piece> pieces, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new Piece[rows][cols];
        for (Piece piece : pieces) {
            addPiece(piece.getCell(), piece);
        }
    }

    @Override
    public Piece getPieceAt(Position pos) {
        requireValid(pos);
        return matrix[pos.getRow()][pos.getCol()];
    }

    @Override
    public void addPiece(Position pos, Piece piece) {
        requireValid(pos);
        if (matrix[pos.getRow()][pos.getCol()] != null) {
            throw new IllegalStateException("Cell already occupied at: " + pos);
        }
        matrix[pos.getRow()][pos.getCol()] = piece;
        if (piece != null) {
            piece.setCell(pos);
        }
    }

    @Override
    public void clearCellOnly(Position position) {
        requireValid(position);
        matrix[position.getRow()][position.getCol()] = null;
    }

    @Override
    public Piece removePiece(Position pos) {
        requireValid(pos);
        Piece removed = getPieceAt(pos);
        matrix[pos.getRow()][pos.getCol()] = null;
        if (removed != null) {
            removed.setState(enums.PieceState.CAPTURED);
        }
        return removed;
    }

    @Override
    public boolean isEmpty(Position pos) {
        return getPieceAt(pos) == null;
    }

    @Override
    public boolean isValidPosition(Position pos) {
        return pos != null && pos.getRow() >= 0 && pos.getRow() < rows && pos.getCol() >= 0 && pos.getCol() < cols;
    }

    private void requireValid(Position pos) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Outside board bounds: " + pos);
        }
    }
}