package realTime;

import model.Board;
import model.Piece;
import engine.GameEngine;
import enums.PieceKind;
import model.PendingJump;
import model.PendingMove;
import model.Position;
import rules.PawnPromotion;

import java.util.ArrayList;
import java.util.List;

public class RealTimeUpdater {

    private final GameEngine engine;

    public RealTimeUpdater(GameEngine engine) {
        this.engine = engine;
    }

    public void advance(long ms) {
        engine.advanceClock(ms);
        updateBoardPositions();
    }

    private void updateBoardPositions() {
        Board board = engine.getState().getBoard();
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

            Piece finalPiece = PawnPromotion.applyPromotion(move.getPiece(), move.getToRow(), board.getRows());
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
