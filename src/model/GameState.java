package model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private Board board;
    private long gameClock = 0;
    private Position selectedPosition = null;
    private List<PendingMove> pendingMoves = new ArrayList<>();
    private List<PendingJump> pendingJumps = new ArrayList<>();
    private boolean isGameOver = false;

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }
    public long getGameClock() { return gameClock; }
    public void setGameClock(long gameClock) { this.gameClock = gameClock; }
    public Position getSelectedPosition() { return selectedPosition; }
    public void setSelectedPosition(Position selectedPosition) { this.selectedPosition = selectedPosition; }
    public List<PendingMove> getPendingMoves() { return pendingMoves; }
    public List<PendingJump> getPendingJumps() { return pendingJumps; }
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean gameOver) { isGameOver = gameOver; }
}
