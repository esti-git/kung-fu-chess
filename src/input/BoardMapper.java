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
        int cellSize = GameConfig.CELL_SIZE;
        int margin = GameConfig.BOARD_LABEL_MARGIN;

        int adjustedX = x - margin;
        int adjustedY = y - margin;
        if (adjustedX < 0 || adjustedY < 0) {
            return Optional.empty();
        }

        int col = adjustedX / cellSize;
        int row = adjustedY / cellSize;

        if (row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols()) {
            return Optional.of(new Position(row, col));
        }

        return Optional.empty();
    }
}