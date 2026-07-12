package io;

import model.Board;
import model.Piece;
import model.Position;

public class BoardPrinter {

    private final Board board;

    public BoardPrinter(Board board) {
        this.board = board;
    }

    public void print() {
        for (int i = 0; i < board.getRows(); i++) {
            StringBuilder rowStr = new StringBuilder();
            for (int j = 0; j < board.getCols(); j++) {
                Piece piece = board.getPieceAt(new Position(i, j));
                rowStr.append(piece == null ? "." : piece.getRepresentation());
                if (j < board.getCols() - 1) rowStr.append(" ");
            }
            System.out.println(rowStr.toString());
        }
    }
}
