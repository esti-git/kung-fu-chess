package input;

import config.GameConfig;
import model.Board; // או model.MatrixBoard, ודאי שהייבוא מתאים ללוח שלך
import model.Position;
import java.util.Optional;

public class BoardMapper {

    private final Board board;

    // הבנאי כעת מקבל את אובייקט הלוח ישירות כפי שמתבצע ב-GameFactory
    public BoardMapper(Board board) {
        this.board = board;
    }

    /**
     * מתרגם קואורדינטות פיקסל מהמסך למשבצת לוגית בלוח.
     * x מייצג את הרוחב (עמודות - Col)
     * y מייצג את הגובה (שורות - Row)
     */
    public Optional<Position> pixelToCell(int x, int y) {
        int cellSize = GameConfig.CELL_SIZE;

        // חישוב האינדקסים על בסיס גודל המשבצת
        int col = x / cellSize;
        int row = y / cellSize;

        // וידוא שהלחיצה נמצאת בתוך גבולות הלוח האמיתיים
        if (row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols()) {
            return Optional.of(new Position(row, col));
        }

        return Optional.empty();
    }
}