package view;

import engine.GameEngine;
import model.CaptureRecord;
import model.PendingJump;
import model.PendingMove;
import model.PendingRest;
import model.Piece;

import java.util.ArrayList;
import java.util.List;

public class BoardSnapshotFactory {

    public BoardSnapshot capture(GameEngine engine) {
        int rows = engine.getBoardRows();
        int cols = engine.getBoardCols();

        PieceSnapshot[][] cells = new PieceSnapshot[rows][cols];
        engine.forEachPiece((pos, piece) -> cells[pos.getRow()][pos.getCol()] = toSnapshot(piece));

        List<PendingMoveSnapshot> moveSnapshots = new ArrayList<>();
        for (PendingMove move : engine.getPendingMoves()) {
            moveSnapshots.add(new PendingMoveSnapshot(
                    move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(),
                    toSnapshot(move.getPiece()), move.getArrivalTime()));
        }

        List<PendingJumpSnapshot> jumpSnapshots = new ArrayList<>();
        for (PendingJump jump : engine.getPendingJumps()) {
            jumpSnapshots.add(new PendingJumpSnapshot(
                    jump.getRow(), jump.getCol(), toSnapshot(jump.getPiece()), jump.getStartTime(), jump.getEndTime()));
        }

        List<PendingRestSnapshot> restSnapshots = new ArrayList<>();
        for (PendingRest rest : engine.getPendingRests()) {
            restSnapshots.add(new PendingRestSnapshot(toSnapshot(rest.getPiece()), rest.getEndTime()));
        }

        List<CaptureSnapshot> captureSnapshots = new ArrayList<>();
        for (CaptureRecord record : engine.getCaptureLog()) {
            captureSnapshots.add(new CaptureSnapshot(record.getCapturedColor(), record.getCapturedKind()));
        }

        return new BoardSnapshot(rows, cols, cells, moveSnapshots, jumpSnapshots, restSnapshots, captureSnapshots, engine.getGameClock());
    }

    private PieceSnapshot toSnapshot(Piece piece) {
        return new PieceSnapshot(piece.getId(), piece.getColor(), piece.getKind(), piece.getRepresentation(), piece.getState());
    }
}
