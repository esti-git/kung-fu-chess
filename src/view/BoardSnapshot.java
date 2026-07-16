package view;

import java.util.Collections;
import java.util.List;

/**
 * תמונת מצב קפואה של כל מה שהתצוגה צריכה כדי לצייר/לעדכן פריים אחד -
 * לא מחזיקה שום הפניה חיה ללוח (Board) או לכלים (Piece) עצמם.
 */
public class BoardSnapshot {
    private final int rows;
    private final int cols;
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

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public PieceSnapshot getPieceAt(int row, int col) {
        return cells[row][col];
    }

    public List<PendingMoveSnapshot> getPendingMoves() { return pendingMoves; }
    public List<PendingJumpSnapshot> getPendingJumps() { return pendingJumps; }
    public List<PendingRestSnapshot> getPendingRests() { return pendingRests; }
    public List<CaptureSnapshot> getCaptureLog() { return captureLog; }
    public long getGameClock() { return gameClock; }
}
