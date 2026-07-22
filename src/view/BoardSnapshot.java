package view;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class BoardSnapshot {
    private final int rows;
    private final int cols;
    @Getter(AccessLevel.NONE)
    private final PieceSnapshot[][] cells;
    private final List<PendingMoveSnapshot> pendingMoves;
    private final List<PendingJumpSnapshot> pendingJumps;
    private final List<PendingRestSnapshot> pendingRests;
    private final List<CaptureSnapshot> captureLog;
    private final long gameClock;

    public BoardSnapshot(int rows, int cols, PieceSnapshot[][] cells,
                          List<PendingMoveSnapshot> pendingMoves,
                          List<PendingJumpSnapshot> pendingJumps,
                          List<PendingRestSnapshot> pendingRests,
                          List<CaptureSnapshot> captureLog,
                          long gameClock) {
        this.rows = rows;
        this.cols = cols;
        this.cells = cells;
        this.pendingMoves = Collections.unmodifiableList(pendingMoves);
        this.pendingJumps = Collections.unmodifiableList(pendingJumps);
        this.pendingRests = Collections.unmodifiableList(pendingRests);
        this.captureLog = Collections.unmodifiableList(captureLog);
        this.gameClock = gameClock;
    }

    public PieceSnapshot getPieceAt(int row, int col) {
        return cells[row][col];
    }
}
