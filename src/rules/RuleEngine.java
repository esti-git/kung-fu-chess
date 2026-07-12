package rules;

import model.Board;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.Position;

import java.util.List;

public class RuleEngine {

    private final Board board;
    private final List<PendingMove> pendingMoves;
    private final List<PendingJump> pendingJumps;

    public RuleEngine(Board board, List<PendingMove> pendingMoves, List<PendingJump> pendingJumps) {
        this.board = board;
        this.pendingMoves = pendingMoves;
        this.pendingJumps = pendingJumps;
    }

    public boolean isMoveLegal(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow && fromCol == toCol) return false;

        Piece movingPiece = board.getPieceAt(new Position(fromRow, fromCol));
        if (movingPiece == null) return false;
        enums.PieceColor movingColor = movingPiece.getColor();

        for (PendingJump jump : pendingJumps) {
            if (jump.getRow() == fromRow && jump.getCol() == fromCol) return false;
            if (jump.getRow() == toRow && jump.getCol() == toCol) {
                if (jump.getPiece().getColor() == movingColor) return false;
            }
        }

        for (PendingMove move : pendingMoves) {
            if (move.getFromRow() == fromRow && move.getFromCol() == fromCol) return false;
            if (move.getToRow() == toRow && move.getToCol() == toCol
                    && move.getPiece().getColor() == movingColor) return false;
        }

        Piece destinationPiece = board.getPieceAt(new Position(toRow, toCol));

        if (destinationPiece != null) {
            if (movingColor == destinationPiece.getColor()) return false;
        }

        if (!movingPiece.isMovementPatternLegal(fromRow, fromCol, toRow, toCol, board.getRows())) {
            return false;
        }

        enums.PieceKind pieceKind = movingPiece.getKind();
        if (pieceKind == enums.PieceKind.ROOK || pieceKind == enums.PieceKind.BISHOP || pieceKind == enums.PieceKind.QUEEN) {
            return isPathClear(fromRow, fromCol, toRow, toCol);
        }

        if (pieceKind == enums.PieceKind.PAWN) {
            int deltaCol = Math.abs(toCol - fromCol);
            int expectedRowDirection = (movingColor == enums.PieceColor.WHITE) ? -1 : 1;
            int actualRowDirection = toRow - fromRow;

            if (deltaCol == 0) {
                if (actualRowDirection == expectedRowDirection) {
                    return board.isEmpty(new Position(toRow, toCol));
                }
                if (actualRowDirection == expectedRowDirection * 2) {
                    int startRow = (movingColor == enums.PieceColor.WHITE) ? (board.getRows() - 2) : 1;
                    if (fromRow != startRow) return false;
                    int middleRow = fromRow + expectedRowDirection;
                    
                    if (!board.isEmpty(new Position(middleRow, fromCol)) || !board.isEmpty(new Position(toRow, toCol))) {
                        return false;
                    }
                    
                    for (PendingMove move : pendingMoves) {
                        if (move.getPiece().getId() == movingPiece.getId()) {
                            continue;
                        }
                        if ((move.getToRow() == middleRow && move.getToCol() == fromCol) ||
                                (move.getToRow() == toRow && move.getToCol() == toCol)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            if (deltaCol == 1) {
                return destinationPiece != null;
            }
        }

        return true;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int stepRow = Integer.compare(toRow, fromRow);
        int stepCol = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + stepRow;
        int currentCol = fromCol + stepCol;

        while (currentRow != toRow || currentCol != toCol) {
            if (!board.isEmpty(new Position(currentRow, currentCol))) return false;

            for (PendingMove move : pendingMoves) {
                if (move.getToRow() == currentRow && move.getToCol() == currentCol) return false;
            }
            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }
}