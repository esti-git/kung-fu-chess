package rules.pieces;

import enums.PieceColor;
import enums.PieceKind;
import model.Board;
import model.PendingMove;
import model.Piece;
import model.Position;

import java.util.List;

public class Pawn extends Piece {
    public Pawn(int id, PieceColor color) {
        super(id, color, PieceKind.PAWN, null);
    }

    @Override public String getRepresentation() { return (getColor() == PieceColor.WHITE ? "w" : "b") + "P"; }

    @Override
    public boolean isMovementPatternLegal(int fromRow, int fromCol, int toRow, int toCol, int totalRows) {
        int deltaCol = Math.abs(toCol - fromCol);
        int expectedRowDirection = (getColor() == PieceColor.WHITE) ? -1 : 1;
        int actualRowDirection = toRow - fromRow;
        int startRow = (getColor() == PieceColor.WHITE) ? (totalRows - 2) : 1;

        boolean isForwardOne = (actualRowDirection == expectedRowDirection && deltaCol == 0);
        boolean isForwardTwo = (actualRowDirection == expectedRowDirection * 2 && fromRow == startRow && deltaCol == 0);
        boolean isDiagonalCapture = (actualRowDirection == expectedRowDirection && deltaCol == 1);

        return isForwardOne || isForwardTwo || isDiagonalCapture;
    }

    @Override
    public String validateSpecialMove(Board board, Position source, Position destination, Piece destinationPiece, List<PendingMove> pendingMoves) {
        int fromRow = source.getRow();
        int fromCol = source.getCol();
        int toRow = destination.getRow();
        int toCol = destination.getCol();

        int deltaCol = Math.abs(toCol - fromCol);
        int expectedRowDirection = (getColor() == PieceColor.WHITE) ? -1 : 1;
        int actualRowDirection = toRow - fromRow;

        if (deltaCol == 0) {
            if (actualRowDirection == expectedRowDirection) {
                return board.isEmpty(destination) ? null : "Forward move is blocked";
            }

            // Forward two: isMovementPatternLegal already confirmed fromRow is the starting row.
            int middleRow = fromRow + expectedRowDirection;
            if (!board.isEmpty(new Position(middleRow, fromCol)) || !board.isEmpty(destination)) {
                return "Pawn double-step path is blocked";
            }
            for (PendingMove move : pendingMoves) {
                if (move.getPiece().getId() == getId()) {
                    continue;
                }
                if ((move.getToRow() == middleRow && move.getToCol() == fromCol) ||
                        (move.getToRow() == toRow && move.getToCol() == toCol)) {
                    return "Pawn double-step path is blocked by a pending move";
                }
            }
            return null;
        }

        // Diagonal capture (deltaCol == 1, guaranteed by isMovementPatternLegal).
        return destinationPiece != null ? null : "Pawn capture requires a target piece";
    }
}
