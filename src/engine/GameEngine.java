package engine;

import model.Board;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.Position;
import enums.PieceColor;
import rules.RuleEngine;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {

    private final Board board;
    private final RuleEngine ruleEngine;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private long gameClock = 0;
    private boolean isGameOver = false;

    public List<PendingMove> pendingMoves = new ArrayList<>();
    public List<PendingJump> pendingJumps = new ArrayList<>();

    public List<PendingMove> getPendingMoves() { return pendingMoves; }
    public List<PendingJump> getPendingJumps() { return pendingJumps; }

    public GameEngine(Board board) {
        this.board = board;
        this.ruleEngine = new RuleEngine(board, pendingMoves, pendingJumps);
    }

    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean value) { isGameOver = value; }
    public long getGameClock() { return gameClock; }
    public void advanceClock(long ms) { gameClock += ms; }

    public void handleClick(int x, int y) {
        int clickedCol = x / 100;
        int clickedRow = y / 100;

        if (clickedRow < 0 || clickedRow >= board.getRows() || clickedCol < 0 || clickedCol >= board.getCols()) {
            return;
        }

        Piece currentSquarePiece = board.getPieceAt(new Position(clickedRow, clickedCol));

        if (hasSelection() && selectedRow == clickedRow && selectedCol == clickedCol) {
            if (canPieceJump(clickedRow, clickedCol)) {
                pendingJumps.add(new PendingJump(clickedRow, clickedCol, board.getPieceAt(new Position(clickedRow, clickedCol)), gameClock));
                board.setPieceAt(new Position(clickedRow, clickedCol), null);
            }
            clearSelection();
            return;
        }

        if (currentSquarePiece != null) {
            PieceColor currentPieceColor = currentSquarePiece.getColor();

            if (hasSelection() && getSelectedPieceColor() == currentPieceColor) {
                selectedRow = clickedRow;
                selectedCol = clickedCol;
            } else if (hasSelection()) {
                if (ruleEngine.isMoveLegal(selectedRow, selectedCol, clickedRow, clickedCol)) {
                    executeMove(selectedRow, selectedCol, clickedRow, clickedCol);
                } else {
                    clearSelection();
                }
            } else {
                selectedRow = clickedRow;
                selectedCol = clickedCol;
            }
        } else {
            if (hasSelection()) {
                if (ruleEngine.isMoveLegal(selectedRow, selectedCol, clickedRow, clickedCol)) {
                    executeMove(selectedRow, selectedCol, clickedRow, clickedCol);
                } else {
                    clearSelection();
                }
            }
        }
    }

    public void handleJumpCommand(int x, int y) {
        int clickedCol = x / 100;
        int clickedRow = y / 100;

        if (clickedRow < 0 || clickedRow >= board.getRows() || clickedCol < 0 || clickedCol >= board.getCols()) {
            return;
        }

        Piece currentPiece = board.getPieceAt(new Position(clickedRow, clickedCol));
        if (currentPiece != null) {
            if (canPieceJump(clickedRow, clickedCol)) {
                pendingJumps.add(new PendingJump(clickedRow, clickedCol, currentPiece, gameClock));
                board.setPieceAt(new Position(clickedRow, clickedCol), null);
            }
        }
        clearSelection();
    }

    public boolean canPieceJump(int r, int c) {
        for (PendingMove move : pendingMoves) {
            if (move.getFromRow() == r && move.getFromCol() == c) return false;
        }
        for (PendingJump jump : pendingJumps) {
            if (jump.getRow() == r && jump.getCol() == c) return false;
        }
        return true;
    }

    public void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPieceAt(new Position(fromRow, fromCol));
        long arrivalTime = gameClock + 1000L;
        pendingMoves.add(new PendingMove(fromRow, fromCol, toRow, toCol, piece, arrivalTime));
        clearSelection();
    }

    private boolean hasSelection() {
        return selectedRow != -1 && selectedCol != -1;
    }

    private PieceColor getSelectedPieceColor() {
        return board.getPieceAt(new Position(selectedRow, selectedCol)).getColor();
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }
}
