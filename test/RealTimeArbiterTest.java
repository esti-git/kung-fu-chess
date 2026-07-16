import board.MatrixBoard;
import config.GameConfig;
import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import io.BoardParser;
import model.CaptureRecord;
import model.Piece;
import model.Position;
import org.junit.jupiter.api.Test;
import realTime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RealTimeArbiterTest {

    private static final int SIZE = 8;

    /** Builds an 8x8 board where each entry is "row,col=wR" style placement; everything else is empty. */
    private MatrixBoard buildBoard(String... placements) {
        String[][] grid = new String[SIZE][SIZE];
        for (String[] row : grid) Arrays.fill(row, ".");
        for (String placement : placements) {
            String[] parts = placement.split("=");
            String[] coords = parts[0].split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            grid[row][col] = parts[1];
        }
        List<String> rawLines = new ArrayList<>();
        for (String[] row : grid) rawLines.add(String.join(" ", row));

        BoardParser parser = new BoardParser();
        MatrixBoard board = new MatrixBoard();
        board.initialize(parser.parse(rawLines), parser.parseRows(rawLines), parser.parseCols(rawLines));
        return board;
    }

    private Position pos(int row, int col) { return new Position(row, col); }

    @Test
    void testStartMoveSetsMovingStateAndClearsSourceCell() {
        MatrixBoard board = buildBoard("3,0=wR");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));

        assertEquals(PieceState.MOVING, rook.getState());
        assertTrue(board.isEmpty(pos(3, 0)));
        assertEquals(1, arbiter.getActiveMoves().size());
        assertEquals(4 * GameConfig.MS_PER_CELL, arbiter.getActiveMoves().get(0).getArrivalTime());
    }

    @Test
    void testAdvanceMovesPieceAndEntersLongRestThenIdle() {
        MatrixBoard board = buildBoard("3,0=wR");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));
        arbiter.advance(4 * GameConfig.MS_PER_CELL);

        assertEquals(rook, board.getPieceAt(pos(3, 4)));
        assertTrue(board.isEmpty(pos(3, 0)));
        assertEquals(PieceState.LONG_REST, rook.getState());
        assertTrue(arbiter.getActiveMoves().isEmpty());
        assertEquals(1, arbiter.getActiveRests().size());

        arbiter.advance(GameConfig.LONG_REST_DURATION_MS - 1);
        assertEquals(PieceState.LONG_REST, rook.getState());

        arbiter.advance(1);
        assertEquals(PieceState.IDLE, rook.getState());
        assertTrue(arbiter.getActiveRests().isEmpty());
    }

    @Test
    void testAdvanceCapturesEnemyOnArrivalAndLogsCapture() {
        MatrixBoard board = buildBoard("3,0=wR", "3,4=bP");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));
        boolean kingCaptured = arbiter.advance(4 * GameConfig.MS_PER_CELL);

        assertFalse(kingCaptured);
        assertEquals(rook, board.getPieceAt(pos(3, 4)));
        assertEquals(1, arbiter.getCaptureLog().size());
        CaptureRecord record = arbiter.getCaptureLog().get(0);
        assertEquals(PieceColor.BLACK, record.getCapturedColor());
        assertEquals(PieceKind.PAWN, record.getCapturedKind());
        assertEquals(PieceState.LONG_REST, rook.getState());
    }

    @Test
    void testAdvanceCapturingKingEndsGameAndSetsWinner() {
        MatrixBoard board = buildBoard("3,0=wR", "3,4=bK");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));
        boolean kingCaptured = arbiter.advance(4 * GameConfig.MS_PER_CELL);

        assertTrue(kingCaptured);
        assertEquals(PieceColor.WHITE, arbiter.getWinnerColor());
    }

    @Test
    void testMoveOntoFriendlyOccupiedDestinationBouncesBack() {
        MatrixBoard board = buildBoard("3,0=wR", "3,4=wP");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));
        Piece friendlyPawn = board.getPieceAt(pos(3, 4));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));
        boolean kingCaptured = arbiter.advance(4 * GameConfig.MS_PER_CELL);

        assertFalse(kingCaptured);
        assertEquals(rook, board.getPieceAt(pos(3, 0)));
        assertEquals(friendlyPawn, board.getPieceAt(pos(3, 4)));
        assertEquals(PieceState.LONG_REST, rook.getState());
        assertTrue(arbiter.getCaptureLog().isEmpty());
    }

    @Test
    void testPawnPromotionOnArrival() {
        MatrixBoard board = buildBoard("1,4=wP");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece pawn = board.getPieceAt(pos(1, 4));
        int originalId = pawn.getId();

        arbiter.startMove(pawn, pos(1, 4), pos(0, 4));
        arbiter.advance(GameConfig.MS_PER_CELL);

        Piece promoted = board.getPieceAt(pos(0, 4));
        assertEquals(PieceKind.QUEEN, promoted.getKind());
        assertEquals(PieceColor.WHITE, promoted.getColor());
        assertEquals(originalId, promoted.getId());
        assertEquals(PieceState.LONG_REST, promoted.getState());
    }

    @Test
    void testStartJumpSetsJumpingStateWithoutLeavingBoard() {
        MatrixBoard board = buildBoard("4,4=wN");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece knight = board.getPieceAt(pos(4, 4));

        arbiter.startJump(knight, pos(4, 4));

        assertEquals(PieceState.JUMPING, knight.getState());
        assertEquals(knight, board.getPieceAt(pos(4, 4)));
        assertEquals(1, arbiter.getActiveJumps().size());
    }

    @Test
    void testJumpLandsSafelyWhenUndisturbedThenEntersShortRestThenIdle() {
        MatrixBoard board = buildBoard("4,4=wN");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece knight = board.getPieceAt(pos(4, 4));

        arbiter.startJump(knight, pos(4, 4));
        arbiter.advance(GameConfig.JUMP_DURATION_MS);

        assertEquals(knight, board.getPieceAt(pos(4, 4)));
        assertEquals(PieceState.SHORT_REST, knight.getState());
        assertTrue(arbiter.getActiveJumps().isEmpty());

        arbiter.advance(GameConfig.SHORT_REST_DURATION_MS);
        assertEquals(PieceState.IDLE, knight.getState());
    }

    @Test
    void testJumpLandsOnEmptyCellAfterBeingVacated() {
        MatrixBoard board = buildBoard("4,4=wN");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece knight = board.getPieceAt(pos(4, 4));

        arbiter.startJump(knight, pos(4, 4));
        board.clearCellOnly(pos(4, 4));
        assertTrue(board.isEmpty(pos(4, 4)));

        arbiter.advance(GameConfig.JUMP_DURATION_MS);

        assertEquals(knight, board.getPieceAt(pos(4, 4)));
        assertEquals(PieceState.SHORT_REST, knight.getState());
    }

    @Test
    void testMoveOntoJumpingPieceDoesNotCaptureItAndJumperCapturesIntruderOnReturn() {
        MatrixBoard board = buildBoard("4,4=wN", "4,3=bR");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece knight = board.getPieceAt(pos(4, 4));
        Piece rook = board.getPieceAt(pos(4, 3));

        // Rook starts moving into the knight's square (1 cell -> arrives at t=1000).
        arbiter.startMove(rook, pos(4, 3), pos(4, 4));
        arbiter.advance(500);

        // Knight jumps in place mid-flight; its jump ends later, at t=1500.
        arbiter.startJump(knight, pos(4, 4));

        // Advance to t=1000: rook arrives while the knight is still airborne.
        arbiter.advance(500);
        assertEquals(rook, board.getPieceAt(pos(4, 4)));
        assertEquals(PieceState.JUMPING, knight.getState());
        assertTrue(arbiter.getCaptureLog().isEmpty(), "the jumping knight must not be captured mid-air");

        // Advance to t=1500: the knight lands back on its square, now occupied by the enemy rook.
        arbiter.advance(500);
        assertEquals(knight, board.getPieceAt(pos(4, 4)));
        assertEquals(PieceState.SHORT_REST, knight.getState());
        assertEquals(1, arbiter.getCaptureLog().size());
        assertEquals(PieceKind.ROOK, arbiter.getCaptureLog().get(0).getCapturedKind());
        assertEquals(PieceColor.BLACK, arbiter.getCaptureLog().get(0).getCapturedColor());
    }

    @Test
    void testResetClearsAllState() {
        MatrixBoard board = buildBoard("3,0=wR", "3,4=bK");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));
        arbiter.advance(4 * GameConfig.MS_PER_CELL);
        assertFalse(arbiter.getCaptureLog().isEmpty());
        assertNotNull(arbiter.getWinnerColor());

        arbiter.reset();

        assertTrue(arbiter.getActiveMoves().isEmpty());
        assertTrue(arbiter.getActiveJumps().isEmpty());
        assertTrue(arbiter.getActiveRests().isEmpty());
        assertTrue(arbiter.getCaptureLog().isEmpty());
        assertNull(arbiter.getWinnerColor());
    }

    @Test
    void testIsPieceBusyAndActivePieceIds() {
        MatrixBoard board = buildBoard("3,0=wR", "4,4=wN");
        RealTimeArbiter arbiter = new RealTimeArbiter(board);
        Piece rook = board.getPieceAt(pos(3, 0));
        Piece knight = board.getPieceAt(pos(4, 4));

        assertFalse(arbiter.isPieceBusy(rook.getId()));
        assertFalse(arbiter.isPieceBusy(knight.getId()));

        arbiter.startMove(rook, pos(3, 0), pos(3, 4));
        arbiter.startJump(knight, pos(4, 4));

        assertTrue(arbiter.isPieceBusy(rook.getId()));
        assertTrue(arbiter.isPieceBusy(knight.getId()));
        assertEquals(new java.util.HashSet<>(Arrays.asList(rook.getId(), knight.getId())), arbiter.activePieceIds());
    }
}
