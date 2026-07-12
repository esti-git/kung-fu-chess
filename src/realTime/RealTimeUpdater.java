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

        completedMoves.sort((a, b) -> Long.compare(a.getArrivalTime(), b.getArrivalTime()));

        for (PendingMove move : completedMoves) {
            Position fromPos = new Position(move.getFromRow(), move.getFromCol());
            Position toPos = new Position(move.getToRow(), move.getToCol());

            Piece currentPieceOnSrc = board.getPieceAt(fromPos);
            if (currentPieceOnSrc == null || currentPieceOnSrc.getId() != move.getPiece().getId()) {
                continue; 
            }

            board.setPieceAt(fromPos, null);

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

            Piece target = board.getPieceAt(toPos);
            if (target != null) {
                if (target.getKind() == PieceKind.KING) engine.setGameOver(true);
                if (target.getColor() == move.getPiece().getColor()) {
                    // אם מדובר בכלי מאותו צבע (לא אמור לקרות אם הבדיקה המוקדמת חסמה, אך ליתר ביטחון)
                    // מחזירים את הכלי למקור שלו כדי שלא ייעלם
                    board.setPieceAt(fromPos, move.getPiece());
                    continue;
                }
            }

            Piece finalPiece = PawnPromotion.applyPromotion(move.getPiece(), move.getToRow(), board.getRows());
            board.setPieceAt(toPos, finalPiece);
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