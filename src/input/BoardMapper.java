package input;

import board.MatrixBoard;
import io.BoardParser;
import model.PieceModel;

import java.util.List;

public class BoardMapper {

    private final MatrixBoard board;
    private final BoardParser parser = new BoardParser();

    public BoardMapper(MatrixBoard board) {
        this.board = board;
    }

    public boolean map(List<String> rawBoardLines) {
        List<PieceModel> pieces = parser.parse(rawBoardLines);
        if (pieces == null) return false;
        board.initialize(pieces, parser.parseRows(rawBoardLines), parser.parseCols(rawBoardLines));
        return true;
    }
}
