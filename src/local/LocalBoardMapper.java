package local;

import model.Board;
import model.Position;
import view.BoardMapper;

import java.util.Optional;

public class LocalBoardMapper {

    private final Board board;

    public LocalBoardMapper(Board board) {
        this.board = board;
    }

    public Optional<Position> pixelToCell(int x, int y) {
        return BoardMapper.pixelToCell(x, y, board.getRows(), board.getCols());
    }
}
