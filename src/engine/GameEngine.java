package engine;

import config.GameConfig;
import enums.PieceColor;
import model.GameState;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.Position;
import rules.RuleEngine;

import java.util.List;

public class GameEngine {

    private final GameState state;
    private final RuleEngine ruleEngine;

    public GameEngine(GameState state) {
        this.state = state;
        this.ruleEngine = new RuleEngine(state.getBoard(), state.getPendingMoves(), state.getPendingJumps());
    }

    public GameState getState() { return state; }

    public boolean isGameOver() { return state.isGameOver(); }
    public void setGameOver(boolean value) { state.setGameOver(value); }
    public long getGameClock() { return state.getGameClock(); }
    public void advanceClock(long ms) { state.setGameClock(state.getGameClock() + ms); }

    public List<PendingMove> getPendingMoves() { return state.getPendingMoves(); }
    public List<PendingJump> getPendingJumps() { return state.getPendingJumps(); }

    public void handleClick(int x, int y) {
        int clickedCol = x / GameConfig.CELL_SIZE;
        int clickedRow = y / GameConfig.CELL_SIZE;

        if (!state.getBoard().isValidPosition(new Position(clickedRow, clickedCol))) return;

        Piece currentSquarePiece = state.getBoard().getPieceAt(new Position(clickedRow, clickedCol));
        Position clickedPos = new Position(clickedRow, clickedCol);

        if (hasSelection() && state.getSelectedPosition().equals(clickedPos)) {
            if (canPieceJump(clickedRow, clickedCol)) {
                state.getPendingJumps().add(new PendingJump(clickedRow, clickedCol, currentSquarePiece, state.getGameClock()));
                state.getBoard().setPieceAt(clickedPos, null);
            }
            clearSelection();
            return;
        }

        if (currentSquarePiece != null) {
            PieceColor currentPieceColor = currentSquarePiece.getColor();

            if (hasSelection() && getSelectedPieceColor() == currentPieceColor) {
                state.setSelectedPosition(clickedPos);
            } else if (hasSelection()) {
                Position sel = state.getSelectedPosition();
                if (ruleEngine.isMoveLegal(sel.getRow(), sel.getCol(), clickedRow, clickedCol)) {
                    executeMove(sel.getRow(), sel.getCol(), clickedRow, clickedCol);
                } else {
                    clearSelection();
                }
            } else {
                state.setSelectedPosition(clickedPos);
            }
        } else {
            if (hasSelection()) {
                Position sel = state.getSelectedPosition();
                if (ruleEngine.isMoveLegal(sel.getRow(), sel.getCol(), clickedRow, clickedCol)) {
                    executeMove(sel.getRow(), sel.getCol(), clickedRow, clickedCol);
                } else {
                    clearSelection();
                }
            }
        }
    }

    public void handleJumpCommand(int x, int y) {
        int clickedCol = x / GameConfig.CELL_SIZE;
        int clickedRow = y / GameConfig.CELL_SIZE;

        if (!state.getBoard().isValidPosition(new Position(clickedRow, clickedCol))) return;

        Piece currentPiece = state.getBoard().getPieceAt(new Position(clickedRow, clickedCol));
        if (currentPiece != null && canPieceJump(clickedRow, clickedCol)) {
            state.getPendingJumps().add(new PendingJump(clickedRow, clickedCol, currentPiece, state.getGameClock()));
            state.getBoard().setPieceAt(new Position(clickedRow, clickedCol), null);
        }
        clearSelection();
    }

    public boolean canPieceJump(int r, int c) {
        for (PendingMove move : state.getPendingMoves()) {
            if (move.getFromRow() == r && move.getFromCol() == c) return false;
        }
        for (PendingJump jump : state.getPendingJumps()) {
            if (jump.getRow() == r && jump.getCol() == c) return false;
        }
        return true;
    }

    public void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = state.getBoard().getPieceAt(new Position(fromRow, fromCol));
        int distance = Math.max(Math.abs(toRow - fromRow), Math.abs(toCol - fromCol));
        long arrivalTime = state.getGameClock() + distance * GameConfig.MS_PER_CELL;
        state.getPendingMoves().add(new PendingMove(fromRow, fromCol, toRow, toCol, piece, arrivalTime));
        clearSelection();
    }

    private boolean hasSelection() { return state.getSelectedPosition() != null; }

    private PieceColor getSelectedPieceColor() {
        return state.getBoard().getPieceAt(state.getSelectedPosition()).getColor();
    }

    private void clearSelection() { state.setSelectedPosition(null); }
}
