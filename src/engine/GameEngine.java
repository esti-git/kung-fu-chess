package engine;

import common.GameResult;
import enums.PieceColor;
import model.Board;
import model.CaptureRecord;
import model.GameState;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.PendingRest;
import model.Position;
import rules.RuleEngine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import realTime.RealTimeArbiter;

public class GameEngine {

    private final GameState state;
    private final RuleEngine ruleEngine;
    private RealTimeArbiter arbiter;

    public GameEngine(GameState state) {
        this.state = state;
        this.ruleEngine = new RuleEngine();
    }

    public GameState getState() {
        return state;
    }

    public boolean isGameOver() {
        return state.isGameOver();
    }

    public void setGameOver(boolean value) {
        state.setGameOver(value);
    }

    public PieceColor getWinnerColor() {
        return (arbiter != null) ? arbiter.getWinnerColor() : null;
    }

    public long getGameClock() {
        return (arbiter != null) ? arbiter.getCurrentTimeMillis() : 0L;
    }

    public void advanceClock(long ms) {
        if (arbiter != null) {
            boolean kingCaptured = arbiter.advance(ms);
            if (kingCaptured) {
                setGameOver(true);
            }
        }
    }

    public void setArbiter(RealTimeArbiter arbiter) {
        this.arbiter = arbiter;
    }

    public List<PendingMove> getPendingMoves() {
        return Collections.unmodifiableList(arbiter.getActiveMoves());
    }

    public List<PendingJump> getPendingJumps() {
        return Collections.unmodifiableList(arbiter.getActiveJumps());
    }

    public List<PendingRest> getPendingRests() {
        return Collections.unmodifiableList(arbiter.getActiveRests());
    }

    public List<CaptureRecord> getCaptureLog() {
        return arbiter.getCaptureLog();
    }

    public Optional<Piece> pieceAt(Position pos) {
        if (pos == null || !state.getBoard().isValidPosition(pos)) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.getBoard().getPieceAt(pos));
    }

    public int getBoardRows() {
        return state.getBoard().getRows();
    }

    public int getBoardCols() {
        return state.getBoard().getCols();
    }

    public void forEachPiece(BiConsumer<Position, Piece> consumer) {
        Board board = state.getBoard();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Position pos = new Position(r, c);
                Piece piece = board.getPieceAt(pos);
                if (piece != null) {
                    consumer.accept(pos, piece);
                }
            }
        }
    }

    public void handleRawWait(String[] parts) {
        if (parts == null || parts.length < 2)
            return;
        try {
            long milliseconds = Long.parseLong(parts[1]);
            waitMs(milliseconds);
        } catch (NumberFormatException ignored) {
        }
    }

    public void waitMs(long milliseconds) {
        if (milliseconds < 0)
            return;
        advanceClock(milliseconds);
    }

    public GameResult<Void> requestMove(Position from, Position to) {
        if (state.isGameOver())
            return GameResult.fail("Game is over");
        if (from == null || to == null)
            return GameResult.fail("Invalid move positions");

        GameResult<Void> validation = ruleEngine.validateMove(
                state.getBoard(),
                from,
                to,
                getPendingMoves(),
                getPendingJumps());
        if (!validation.isSuccess())
            return validation;

        executeMove(from.getRow(), from.getCol(), to.getRow(), to.getCol());
        return GameResult.success();
    }

    public GameResult<Void> requestJump(Position pos) {
        if (state.isGameOver())
            return GameResult.fail("Game is over");
        if (pos == null || !state.getBoard().isValidPosition(pos))
            return GameResult.fail("Invalid jump position");

        Piece currentPiece = state.getBoard().getPieceAt(pos);
        if (currentPiece == null)
            return GameResult.fail("No piece at jump position");
        if (currentPiece.getState() != enums.PieceState.IDLE)
            return GameResult.fail("Piece cannot act right now");
        if (!canPieceJump(pos.getRow(), pos.getCol()))
            return GameResult.fail("Jump is not currently possible");

        arbiter.startJump(currentPiece, pos);

        return GameResult.success();
    }

    public boolean canExecuteCommand(String commandType) {
        if (!isGameOver())
            return true;
        return !commandType.equals("click") && !commandType.equals("wait") && !commandType.equals("jump");
    }

    public boolean canPieceJump(int r, int c) {
        for (PendingMove move : getPendingMoves()) {
            if (move.getFromRow() == r && move.getFromCol() == c)
                return false;
        }
        for (PendingJump jump : getPendingJumps()) {
            if (jump.getRow() == r && jump.getCol() == c)
                return false;
        }
        return true;
    }

    public void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = state.getBoard().getPieceAt(new Position(fromRow, fromCol));
        if (piece == null) {
            return;
        }

        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        arbiter.startMove(piece, from, to);
    }
}