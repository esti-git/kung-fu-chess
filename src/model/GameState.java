package model;

public class GameState {
    private final Board board;
    private boolean gameOver = false;

    public GameState(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    public void endGame() {
        this.gameOver = true;
    }
}