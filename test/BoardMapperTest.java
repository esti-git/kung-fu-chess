import board.MatrixBoard;
import config.GameConfig;
import local.LocalBoardMapper;
import model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BoardMapperTest {

    private LocalBoardMapper mapper;

    @BeforeEach
    void setUp() {
        MatrixBoard board = new MatrixBoard();
        board.initialize(Collections.emptyList(), 8, 8);
        mapper = new LocalBoardMapper(board);
    }

    @Test
    void testClickInsideLabelMarginReturnsEmpty() {
        assertTrue(mapper.pixelToCell(10, 10).isEmpty());
        assertTrue(mapper.pixelToCell(GameConfig.BOARD_LABEL_MARGIN - 1, 50).isEmpty());
    }

    @Test
    void testClickMapsToExpectedCell() {
        int margin = GameConfig.BOARD_LABEL_MARGIN;
        int cellSize = GameConfig.CELL_SIZE;
        int x = margin + cellSize * 1 + 50; // inside column 1
        int y = margin + cellSize * 2 + 50; // inside row 2

        Optional<Position> result = mapper.pixelToCell(x, y);

        assertTrue(result.isPresent());
        assertEquals(new Position(2, 1), result.get());
    }

    @Test
    void testClickAtCellBoundaryRoundsDownToNextCell() {
        int margin = GameConfig.BOARD_LABEL_MARGIN;
        int cellSize = GameConfig.CELL_SIZE;

        Optional<Position> result = mapper.pixelToCell(margin + cellSize, margin);

        assertTrue(result.isPresent());
        assertEquals(new Position(0, 1), result.get());
    }

    @Test
    void testClickAtTopLeftOfBoardMapsToOrigin() {
        int margin = GameConfig.BOARD_LABEL_MARGIN;

        Optional<Position> result = mapper.pixelToCell(margin, margin);

        assertTrue(result.isPresent());
        assertEquals(new Position(0, 0), result.get());
    }

    @Test
    void testClickBeyondBoardBoundsReturnsEmpty() {
        int margin = GameConfig.BOARD_LABEL_MARGIN;
        int cellSize = GameConfig.CELL_SIZE;
        // 8x8 board: column/row index 8 is one past the last valid index (7).
        int x = margin + cellSize * 8;
        int y = margin;

        assertTrue(mapper.pixelToCell(x, y).isEmpty());
    }

    @Test
    void testNegativeCoordinatesReturnEmpty() {
        assertTrue(mapper.pixelToCell(-5, -5).isEmpty());
    }
}
