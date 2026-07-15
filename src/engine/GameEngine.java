package engine;

import config.GameConfig;
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
import view.BoardSnapshot;
import view.CaptureSnapshot;
import view.PendingJumpSnapshot;
import view.PendingMoveSnapshot;
import view.PendingRestSnapshot;
import view.PieceSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import realTime.RealTimeArbiter;

public class GameEngine {

    private final GameState state;
    private final RuleEngine ruleEngine;
    private RealTimeArbiter arbiter;

    public GameEngine(GameState state) {
        this.state = state;
        this.ruleEngine = new RuleEngine();
    }

    public GameState getState() { return state; }

    public boolean isGameOver() { return state.isGameOver(); }
    public void setGameOver(boolean value) { state.setGameOver(value); }

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
    
    public void setArbiter(RealTimeArbiter arbiter) { this.arbiter = arbiter; }

    public List<PendingMove> getPendingMoves() {
        return (arbiter != null) ? arbiter.getActiveMoves() : new ArrayList<>();
    }
    
    public List<PendingJump> getPendingJumps() {
        return (arbiter != null) ? arbiter.getActiveJumps() : new ArrayList<>();
    }

    public List<PendingRest> getPendingRests() {
        return (arbiter != null) ? arbiter.getActiveRests() : new ArrayList<>();
    }

    /**
     * בונה תמונת מצב קפואה של הלוח והפעולות הפעילות - זה היחיד שקורא ל-board.getPieceAt(...) לצורך התצוגה;
     * כל שאר קוד התצוגה (renderer, היסטוריית מהלכים, ניקוד) קורא רק מהתמונה הזו, לא מהלוח החי.
     */
    public BoardSnapshot captureSnapshot() {
        Board board = state.getBoard();
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
        for (PendingMove move : getPendingMoves()) {
            moveSnapshots.add(new PendingMoveSnapshot(
                    move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol(),
                    toSnapshot(move.getPiece()), move.getArrivalTime()));
        }

        List<PendingJumpSnapshot> jumpSnapshots = new ArrayList<>();
        for (PendingJump jump : getPendingJumps()) {
            jumpSnapshots.add(new PendingJumpSnapshot(
                    jump.getRow(), jump.getCol(), toSnapshot(jump.getPiece()), jump.getStartTime(), jump.getEndTime()));
        }

        List<PendingRestSnapshot> restSnapshots = new ArrayList<>();
        for (PendingRest rest : getPendingRests()) {
            restSnapshots.add(new PendingRestSnapshot(toSnapshot(rest.getPiece()), rest.getEndTime()));
        }

        List<CaptureSnapshot> captureSnapshots = new ArrayList<>();
        if (arbiter != null) {
            for (CaptureRecord record : arbiter.getCaptureLog()) {
                captureSnapshots.add(new CaptureSnapshot(record.getCapturedColor(), record.getCapturedKind()));
            }
        }

        return new BoardSnapshot(rows, cols, cells, moveSnapshots, jumpSnapshots, restSnapshots, captureSnapshots, getGameClock());
    }

    private PieceSnapshot toSnapshot(Piece piece) {
        return new PieceSnapshot(piece.getId(), piece.getColor(), piece.getKind(), piece.getRepresentation(), piece.getState());
    }

    public Optional<Piece> pieceAt(Position pos) {
        if (pos == null || !state.getBoard().isValidPosition(pos)) {
            return Optional.empty();
        }
        return Optional.ofNullable(state.getBoard().getPieceAt(pos));
    }

    public boolean isPieceBusy(int pieceId) {
        for (PendingMove move : getPendingMoves()) {
            if (move.getPiece() != null && move.getPiece().getId() == pieceId) {
                return true;
            }
        }
        for (PendingJump jump : getPendingJumps()) {
            if (jump.getPiece() != null && jump.getPiece().getId() == pieceId) {
                return true;
            }
        }
        return false;
    }

    public void handleRawWait(String[] parts) {
        if (parts == null || parts.length < 2) return;
        try {
            long milliseconds = Long.parseLong(parts[1]);
            waitMs(milliseconds);
        } catch (NumberFormatException ignored) {}
    }

    public void waitMs(long milliseconds) {
        if (milliseconds < 0) return;
        advanceClock(milliseconds);
    }

    public GameResult<Void> requestMove(Position from, Position to) {
        if (state.isGameOver()) return GameResult.fail("Game is over");
        if (from == null || to == null) return GameResult.fail("Invalid move positions");

        Piece movingPiece = state.getBoard().getPieceAt(from);
        
        if (movingPiece != null) {
            for (PendingJump jump : getPendingJumps()) {
                if (jump.getRow() == to.getRow() && jump.getCol() == to.getCol()) {
                    if (jump.getPiece() != null && jump.getPiece().getColor() == movingPiece.getColor()) {
                        return GameResult.fail("Destination reserved by friendly airborne piece");
                    }
                }
            }
        }

        GameResult<Void> validation = ruleEngine.validateMove(
                state.getBoard(),
                from,
                to,
                getPendingMoves(),
                getPendingJumps()
        );
        if (!validation.isSuccess()) return validation;

        executeMove(from.getRow(), from.getCol(), to.getRow(), to.getCol());
        return GameResult.success();
    }

public GameResult<Void> requestJump(Position pos) {
    if (state.isGameOver()) return GameResult.fail("Game is over");
    if (pos == null || !state.getBoard().isValidPosition(pos)) return GameResult.fail("Invalid jump position");

    Piece currentPiece = state.getBoard().getPieceAt(pos);
    if (currentPiece == null) return GameResult.fail("No piece at jump position");
    if (currentPiece.getState() != enums.PieceState.IDLE) return GameResult.fail("Piece cannot act right now");
    if (!canPieceJump(pos.getRow(), pos.getCol())) return GameResult.fail("Jump is not currently possible");

    if (arbiter != null) {
        arbiter.startJump(currentPiece, pos);
    } else {
        getPendingJumps().add(new PendingJump(pos.getRow(), pos.getCol(), currentPiece, getGameClock()));
        currentPiece.setState(enums.PieceState.JUMPING);
    }

    return GameResult.success();
}

    public boolean canExecuteCommand(String commandType) {
        if (!isGameOver()) return true;
        return !commandType.equals("click") && !commandType.equals("wait") && !commandType.equals("jump");
    }

    public boolean canPieceJump(int r, int c) {
        for (PendingMove move : getPendingMoves()) {
            if (move.getFromRow() == r && move.getFromCol() == c) return false;
        }
        for (PendingJump jump : getPendingJumps()) {
            if (jump.getRow() == r && jump.getCol() == c) return false;
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

        if (arbiter != null) {
            arbiter.startMove(piece, from, to);
        } else {
            int distance = Math.max(Math.abs(toRow - fromRow), Math.abs(toCol - fromCol));
            long arrivalTime = getGameClock() + distance * GameConfig.MS_PER_CELL;
            getPendingMoves().add(new PendingMove(fromRow, fromCol, toRow, toCol, piece, arrivalTime));
        }
    }
}