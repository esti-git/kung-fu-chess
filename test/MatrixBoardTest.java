import board.MatrixBoard;
import enums.PieceColor;
import enums.PieceKind;
import io.BoardParser;
import model.Piece;
import model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rules.pieces.Queen;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatrixBoardTest {

    private MatrixBoard board;
    private BoardParser parser;

    @BeforeEach
    void setUp() {
        parser = new BoardParser();
        board = new MatrixBoard();
        List<String> rawLines = Arrays.asList("wR . bK", ". wP .");
        List<Piece> pieces = parser.parse(rawLines);
        board.initialize(pieces, parser.parseRows(rawLines), parser.parseCols(rawLines));
    }

    @Test
    void testBoardDimensions() {
        assertEquals(2, board.getRows());
        assertEquals(3, board.getCols());
    }

    @Test
    void testPieceKindAndColor() {
        assertEquals(PieceKind.ROOK, board.getPieceAt(new Position(0, 0)).getKind());
        assertEquals(PieceColor.WHITE, board.getPieceAt(new Position(0, 0)).getColor());
        assertEquals(PieceKind.KING, board.getPieceAt(new Position(0, 2)).getKind());
        assertEquals(PieceColor.BLACK, board.getPieceAt(new Position(0, 2)).getColor());
    }

    @Test
    void testEmptyCell() {
        assertTrue(board.isEmpty(new Position(0, 1)));
        assertNull(board.getPieceAt(new Position(0, 1)));
    }

    @Test
    void testGetPieceAtOutOfBounds() {
        assertNull(board.getPieceAt(new Position(-1, 0)));
        assertNull(board.getPieceAt(new Position(5, 5)));
        assertNull(board.getPieceAt(null));
    }

    @Test
    void testIsValidPosition() {
        assertTrue(board.isValidPosition(new Position(0, 0)));
        assertFalse(board.isValidPosition(new Position(-1, 0)));
        assertFalse(board.isValidPosition(new Position(5, 5)));
        assertFalse(board.isValidPosition(null));
    }

    @Test
    void testSetPieceAt() {
        Piece queen = new Queen(10, PieceColor.WHITE);
        board.setPieceAt(new Position(0, 1), queen);
        assertFalse(board.isEmpty(new Position(0, 1)));
        assertEquals(PieceKind.QUEEN, board.getPieceAt(new Position(0, 1)).getKind());
    }

    @Test
    void testSetPieceAtNull() {
        board.setPieceAt(new Position(0, 0), null);
        assertTrue(board.isEmpty(new Position(0, 0)));
    }

    @Test
    void testParserInvalidRowWidth() {
        List<String> rawLines = Arrays.asList("wR . bK", ". wP");
        assertNull(parser.parse(rawLines));
    }

    @Test
    void testParserInvalidToken() {
        List<String> rawLines = Arrays.asList("wR . bK", ". wX .");
        assertNull(parser.parse(rawLines));
    }
}
