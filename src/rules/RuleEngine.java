package rules;

import engine.GameResult;
import model.Board;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.Position;

import java.util.List;

public class RuleEngine {

    public GameResult<Void> validateMove(
            Board board,
            Position source,
            Position destination,
            List<PendingMove> pendingMoves,
            List<PendingJump> pendingJumps
    ) {
        int fromRow = source.getRow();
        int fromCol = source.getCol();
        int toRow = destination.getRow();
        int toCol = destination.getCol();

        if (fromRow == toRow && fromCol == toCol) {
            return GameResult.fail("Move must change position");
        }

        Piece movingPiece = board.getPieceAt(source);
        if (movingPiece == null) {
            return GameResult.fail("No piece selected");
        }

        enums.PieceColor movingColor = movingPiece.getColor();

        for (PendingJump jump : pendingJumps) {
            if (jump.getRow() == fromRow && jump.getCol() == fromCol) {
                return GameResult.fail("Piece is already busy with a jump");
            }
            if (jump.getRow() == toRow && jump.getCol() == toCol && jump.getPiece().getColor() == movingColor) {
                return GameResult.fail("Destination is blocked by a friendly jump");
            }
        }

        for (PendingMove move : pendingMoves) {
            if (move.getFromRow() == fromRow && move.getFromCol() == fromCol) {
                return GameResult.fail("Piece is already moving");
            }
            if (move.getToRow() == toRow && move.getToCol() == toCol && move.getPiece().getColor() == movingColor) {
                return GameResult.fail("Destination is already occupied by a friendly pending move");
            }
        }

        Piece destinationPiece = board.getPieceAt(destination);
        if (destinationPiece != null && movingColor == destinationPiece.getColor()) {
            return GameResult.fail("Destination is occupied by a friendly piece");
        }

        if (!movingPiece.isMovementPatternLegal(fromRow, fromCol, toRow, toCol, board.getRows())) {
            return GameResult.fail("Movement pattern is illegal");
        }

        enums.PieceKind pieceKind = movingPiece.getKind();
        if (pieceKind == enums.PieceKind.ROOK || pieceKind == enums.PieceKind.BISHOP || pieceKind == enums.PieceKind.QUEEN) {
            if (!isPathClear(board, fromRow, fromCol, toRow, toCol, pendingMoves)) {
                return GameResult.fail("Path is blocked");
            }
            return GameResult.success();
        }

        if (pieceKind == enums.PieceKind.PAWN) {
            int deltaCol = Math.abs(toCol - fromCol);
            int expectedRowDirection = (movingColor == enums.PieceColor.WHITE) ? -1 : 1;
            int actualRowDirection = toRow - fromRow;

            if (deltaCol == 0) {
                if (actualRowDirection == expectedRowDirection) {
                    if (!board.isEmpty(destination)) {
                        return GameResult.fail("Forward move is blocked");
                    }
                    return GameResult.success();
                }
                if (actualRowDirection == expectedRowDirection * 2) {
                    int startRow = (movingColor == enums.PieceColor.WHITE) ? (board.getRows() - 2) : 1;
                    if (fromRow != startRow) {
                        return GameResult.fail("Pawn can only move two steps from its starting row");
                    }
                    int middleRow = fromRow + expectedRowDirection;
                    if (!board.isEmpty(new Position(middleRow, fromCol)) || !board.isEmpty(destination)) {
                        return GameResult.fail("Pawn double-step path is blocked");
                    }

                    for (PendingMove move : pendingMoves) {
                        if (move.getPiece().getId() == movingPiece.getId()) {
                            continue;
                        }
                        if ((move.getToRow() == middleRow && move.getToCol() == fromCol) ||
                                (move.getToRow() == toRow && move.getToCol() == toCol)) {
                            return GameResult.fail("Pawn double-step path is blocked by a pending move");
                        }
                    }
                    return GameResult.success();
                }
            }
            if (deltaCol == 1) {
                if (destinationPiece != null) {
                    return GameResult.success();
                }
                return GameResult.fail("Pawn capture requires a target piece");
            }
        }

        return GameResult.success();
    }

    private boolean isPathClear(Board board, int fromRow, int fromCol, int toRow, int toCol, List<PendingMove> pendingMoves) {
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