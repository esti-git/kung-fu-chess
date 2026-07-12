package input;

import board.MatrixBoard;
import io.BoardParser;
import model.Piece;

import java.util.List;

public class BoardMapper {

    private final MatrixBoard board;
    private final BoardParser parser = new BoardParser();

    public BoardMapper(MatrixBoard board) {
        this.board = board;
    }

    public boolean map(List<String> rawBoardLines) {
        List<Piece> pieces = parser.parse(rawBoardLines);
        if (pieces == null) return false;
        board.initialize(pieces, parser.parseRows(rawBoardLines), parser.parseCols(rawBoardLines));
        return true;
    }
}
