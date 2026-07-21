package view;

import engine.GameEngine;
import model.Board;
import model.CaptureRecord;
import model.PendingJump;
import model.PendingMove;
import model.PendingRest;
import model.Piece;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class BoardSnapshotFactory {

    public BoardSnapshot capture(GameEngine engine) {
        Board board = engine.getState().getBoard();
        int rows = board.getRows();
        int cols = board.getCols();

        PieceSnapshot[][] cells = new PieceSnapshot[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece != null) {
                    cells[r][c] = toSnapshot(piece);
                }
            }
        }

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
