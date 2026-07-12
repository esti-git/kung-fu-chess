package real_time;

import model.Board;
import model.Piece;
import engine.GameEngine;
import enums.PieceColor;
import enums.PieceKind;
import model.PendingJump;
import model.PendingMove;
import model.Position;
import rules.pieces.Queen;

import java.util.ArrayList;
import java.util.List;

public class RealTimeUpdater {

    private final Board board;
    private final GameEngine engine;

    public RealTimeUpdater(Board board, GameEngine engine) {
        this.board = board;
        this.engine = engine;
    }

    public void advance(long ms) {
        engine.advanceClock(ms);
        updateBoardPositions();
    }

    private void updateBoardPositions() {
        List<PendingMove> pendingMoves = engine.getPendingMoves();
        List<PendingJump> pendingJumps = engine.getPendingJumps();
        long gameClock = engine.getGameClock();

        List<PendingMove> completedMoves = new ArrayList<>();
        for (PendingMove move : pendingMoves) {
            if (gameClock >= move.getArrivalTime()) {
                completedMoves.add(move);
            }
        }

        for (PendingMove move : completedMoves) {
            board.setPieceAt(new Position(move.getFromRow(), move.getFromCol()), null);
        }

        for (PendingMove move : completedMoves) {
            boolean capturedByAirborne = false;

            for (PendingJump jump : pendingJumps) {
                if (move.getArrivalTime() >= jump.getStartTime() && move.getArrivalTime() <= jump.getEndTime()) {
                    if (jump.getRow() == move.getToRow() && jump.getCol() == move.getToCol()) {
                        if (jump.getPiece().getColor() != move.getPiece().getColor()) {
                            capturedByAirborne = true;
                            break;
                        }
                    }
                }
            }

            if (capturedByAirborne) {
                if (move.getPiece().getKind() == PieceKind.KING) engine.setGameOver(true);
                continue;
            }

            Piece target = board.getPieceAt(new Position(move.getToRow(), move.getToCol()));
            if (target != null && target.getKind() == PieceKind.KING) {
                engine.setGameOver(true);
            }

            Piece finalPiece = move.getPiece();
            if (finalPiece.getKind() == PieceKind.PAWN) {
                int promotionRow = (finalPiece.getColor() == PieceColor.WHITE) ? 0 : (board.getRows() - 1);
                if (move.getToRow() == promotionRow) {
                    finalPiece = new Queen(finalPiece.getId(), finalPiece.getColor());
                }
            }

            board.setPieceAt(new Position(move.getToRow(), move.getToCol()), finalPiece);
        }

        pendingMoves.removeAll(completedMoves);

        List<PendingJump> completedJumps = new ArrayList<>();
        for (PendingJump jump : pendingJumps) {
            if (gameClock >= jump.getEndTime()) {
                completedJumps.add(jump);
                if (board.isEmpty(new Position(jump.getRow(), jump.getCol()))) {
                    board.setPieceAt(new Position(jump.getRow(), jump.getCol()), jump.getPiece());
                }
            }
        }
        pendingJumps.removeAll(completedJumps);
    }
}
