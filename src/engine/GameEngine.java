package engine;

import config.GameConfig;
import model.GameState;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.PendingRest;
import model.Position;
import rules.RuleEngine;

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