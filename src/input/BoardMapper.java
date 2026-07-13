package input;

import config.GameConfig;
import model.Board;
import model.Position;

import java.util.Optional;

public class BoardMapper {

    private final Board board;

    public BoardMapper(Board board) {
        this.board = board;
    }

    public Optional<Position> pixelToCell(int x, int y) {
        Position position = new Position(y / GameConfig.CELL_SIZE, x / GameConfig.CELL_SIZE);
        if (board.isValidPosition(position)) {
            return Optional.of(position);
        }
        return Optional.empty();
    }
}
