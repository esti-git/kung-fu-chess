package io;

import interfaces.Board;
import interfaces.Piece;

public class BoardPrinter {

    private final Board board;

    public BoardPrinter(Board board) {
        this.board = board;
    }

    public void print() {
        for (int i = 0; i < board.getRows(); i++) {
            StringBuilder rowStr = new StringBuilder();
            for (int j = 0; j < board.getCols(); j++) {
                Piece piece = board.getPieceAt(i, j);
                rowStr.append(piece == null ? "." : piece.getRepresentation());
                if (j < board.getCols() - 1) rowStr.append(" ");
            }
            System.out.println(rowStr.toString());
        }
    }
}
