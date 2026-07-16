import board.MatrixBoard;
import common.GameResult;
import engine.GameEngine;
import enums.PieceColor;
import enums.PieceState;
import io.BoardParser;
import model.GameState;
import model.Piece;
import model.Position;
import org.junit.jupiter.api.Test;
import realTime.RealTimeArbiter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

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

    private GameEngine buildEngine(MatrixBoard board) {
        GameEngine engine = new GameEngine(new GameState(board));
        engine.setArbiter(new RealTimeArbiter(board));
        return engine;
    }

    @Test
    void testRequestMoveSuccessStartsMoveViaArbiter() {
        MatrixBoard board = buildBoard("3,0=wR");
        GameEngine engine = buildEngine(board);

        GameResult<Void> result = engine.requestMove(pos(3, 0), pos(3, 4));

        assertTrue(result.isSuccess());
        assertEquals(1, engine.getPendingMoves().size());
        assertEquals(PieceState.MOVING, engine.getPendingMoves().get(0).getPiece().getState());
        assertTrue(board.isEmpty(pos(3, 0)));
    }

    @Test
    void testRequestMoveFailsWhenGameOver() {
        MatrixBoard board = buildBoard("3,0=wR");
        GameEngine engine = buildEngine(board);
        engine.setGameOver(true);

        GameResult<Void> result = engine.requestMove(pos(3, 0), pos(3, 4));

        assertFalse(result.isSuccess());
        assertEquals("Game is over", result.message());
    }

    @Test
    void testRequestMoveFailsForNullPositions() {
        MatrixBoard board = buildBoard("3,0=wR");
        GameEngine engine = buildEngine(board);

        assertFalse(engine.requestMove(null, pos(3, 4)).isSuccess());
        assertFalse(engine.requestMove(pos(3, 0), null).isSuccess());
    }

    @Test
    void testRequestMoveDelegatesRuleEngineFailureMessage() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        GameResult<Void> result = engine.requestMove(pos(3, 0), pos(3, 4));

        assertFalse(result.isSuccess());
        assertEquals("No piece selected", result.message());
    }

    @Test
    void testRequestJumpSuccessSetsJumpingState() {
        MatrixBoard board = buildBoard("4,4=wN");
        GameEngine engine = buildEngine(board);
        Piece knight = board.getPieceAt(pos(4, 4));

        GameResult<Void> result = engine.requestJump(pos(4, 4));

        assertTrue(result.isSuccess());
        assertEquals(PieceState.JUMPING, knight.getState());
        assertEquals(1, engine.getPendingJumps().size());
    }

    @Test
    void testRequestJumpFailsWhenGameOver() {
        MatrixBoard board = buildBoard("4,4=wN");
        GameEngine engine = buildEngine(board);
        engine.setGameOver(true);

        assertFalse(engine.requestJump(pos(4, 4)).isSuccess());
    }

    @Test
    void testRequestJumpFailsForInvalidPosition() {
        MatrixBoard board = buildBoard("4,4=wN");
        GameEngine engine = buildEngine(board);

        assertFalse(engine.requestJump(null).isSuccess());
        assertFalse(engine.requestJump(pos(20, 20)).isSuccess());
    }

    @Test
    void testRequestJumpFailsWhenNoPieceAtPosition() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        GameResult<Void> result = engine.requestJump(pos(4, 4));
        assertFalse(result.isSuccess());
        assertEquals("No piece at jump position", result.message());
    }

    @Test
    void testRequestJumpFailsWhenPieceNotIdle() {
        MatrixBoard board = buildBoard("4,4=wN");
        GameEngine engine = buildEngine(board);
        board.getPieceAt(pos(4, 4)).setState(PieceState.MOVING);

        GameResult<Void> result = engine.requestJump(pos(4, 4));
        assertFalse(result.isSuccess());
        assertEquals("Piece cannot act right now", result.message());
    }

    @Test
    void testRequestJumpFailsWhenPieceAlreadyJumping() {
        // A jump leaves the piece on the board (unlike a move, which clears the source cell),
        // so a second jump request on the same piece is rejected by the IDLE-state check.
        MatrixBoard board = buildBoard("4,4=wN");
        GameEngine engine = buildEngine(board);
        engine.requestJump(pos(4, 4));

        GameResult<Void> result = engine.requestJump(pos(4, 4));
        assertFalse(result.isSuccess());
        assertEquals("Piece cannot act right now", result.message());
    }

    @Test
    void testAdvanceClockEndsGameOnKingCapture() {
        MatrixBoard board = buildBoard("3,0=wR", "3,4=bK");
        GameEngine engine = buildEngine(board);

        engine.requestMove(pos(3, 0), pos(3, 4));
        engine.advanceClock(4000);

        assertTrue(engine.isGameOver());
        assertEquals(PieceColor.WHITE, engine.getWinnerColor());
    }

    @Test
    void testWaitMsIgnoresNegativeValues() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        engine.waitMs(-100);

        assertEquals(0L, engine.getGameClock());
    }

    @Test
    void testWaitMsAdvancesClock() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        engine.waitMs(500);

        assertEquals(500L, engine.getGameClock());
    }

    @Test
    void testHandleRawWaitParsesAndAdvancesClock() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        engine.handleRawWait(new String[]{"wait", "750"});

        assertEquals(750L, engine.getGameClock());
    }

    @Test
    void testHandleRawWaitIgnoresMalformedInput() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        engine.handleRawWait(null);
        engine.handleRawWait(new String[]{"wait"});
        engine.handleRawWait(new String[]{"wait", "not-a-number"});

        assertEquals(0L, engine.getGameClock());
    }

    @Test
    void testCanExecuteCommandWhenGameNotOver() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);

        assertTrue(engine.canExecuteCommand("click"));
        assertTrue(engine.canExecuteCommand("wait"));
        assertTrue(engine.canExecuteCommand("jump"));
        assertTrue(engine.canExecuteCommand("print"));
    }

    @Test
    void testCanExecuteCommandWhenGameOver() {
        MatrixBoard board = buildBoard();
        GameEngine engine = buildEngine(board);
        engine.setGameOver(true);

        assertFalse(engine.canExecuteCommand("click"));
        assertFalse(engine.canExecuteCommand("wait"));
        assertFalse(engine.canExecuteCommand("jump"));
        assertTrue(engine.canExecuteCommand("print"));
    }

    @Test
    void testPieceAtReturnsOptional() {
        MatrixBoard board = buildBoard("3,0=wR");
        GameEngine engine = buildEngine(board);

        Optional<Piece> present = engine.pieceAt(pos(3, 0));
        Optional<Piece> empty = engine.pieceAt(pos(3, 1));
        Optional<Piece> outOfBounds = engine.pieceAt(pos(50, 50));
        Optional<Piece> nullPos = engine.pieceAt(null);

        assertTrue(present.isPresent());
        assertTrue(empty.isEmpty());
        assertTrue(outOfBounds.isEmpty());
        assertTrue(nullPos.isEmpty());
    }

    @Test
    void testCanPieceJumpFalseWhenPieceHasPendingMoveOrJump() {
        MatrixBoard board = buildBoard("3,0=wR", "4,4=wN");
        GameEngine engine = buildEngine(board);

        assertTrue(engine.canPieceJump(3, 0));
        assertTrue(engine.canPieceJump(4, 4));

        engine.requestMove(pos(3, 0), pos(3, 4));
        engine.requestJump(pos(4, 4));

        assertFalse(engine.canPieceJump(3, 0));
        assertFalse(engine.canPieceJump(4, 4));
    }
}
