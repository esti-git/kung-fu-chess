package model;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GameState {
    private final Board board;
    @Setter
    private boolean gameOver = false;

    public GameState(Board board) {
        this.board = board;
    }
}
