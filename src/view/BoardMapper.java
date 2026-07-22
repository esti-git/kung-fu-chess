package view;

import config.GameConfig;
import model.Position;

import java.util.Optional;

public final class BoardMapper {

    private BoardMapper() {
    }

    public static Optional<Position> pixelToCell(int x, int y, int rows, int cols) {
        int cellSize = GameConfig.CELL_SIZE;
        int margin = GameConfig.BOARD_LABEL_MARGIN;

        int adjustedX = x - margin;
        int adjustedY = y - margin;
        if (adjustedX < 0 || adjustedY < 0) {
            return Optional.empty();
        }

        int col = adjustedX / cellSize;
        int row = adjustedY / cellSize;

        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return Optional.of(new Position(row, col));
        }

        return Optional.empty();
    }
}
