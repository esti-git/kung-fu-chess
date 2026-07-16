import board.MatrixBoard;
import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import io.BoardParser;
import model.Piece;
import model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rules.pieces.Queen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    void testPieceCellIsSetOnInitialize() {
        Piece rook = board.getPieceAt(new Position(0, 0));
        assertEquals(new Position(0, 0), rook.getCell());
    }

    @Test
    void testEmptyCell() {
        assertTrue(board.isEmpty(new Position(0, 1)));
        assertNull(board.getPieceAt(new Position(0, 1)));
    }

    @Test
    void testGetPieceAtOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> board.getPieceAt(new Position(-1, 0)));
        assertThrows(IllegalArgumentException.class, () -> board.getPieceAt(new Position(5, 5)));
        assertThrows(IllegalArgumentException.class, () -> board.getPieceAt(null));
    }

    @Test
    void testIsValidPosition() {
        assertTrue(board.isValidPosition(new Position(0, 0)));
        assertTrue(board.isValidPosition(new Position(1, 2)));
        assertFalse(board.isValidPosition(new Position(-1, 0)));
        assertFalse(board.isValidPosition(new Position(2, 0)));
        assertFalse(board.isValidPosition(new Position(0, 3)));
        assertFalse(board.isValidPosition(new Position(5, 5)));
        assertFalse(board.isValidPosition(null));
    }

    @Test
    void testAddPieceToEmptyCell() {
        Piece queen = new Queen(10, PieceColor.WHITE);
        Position dest = new Position(0, 1);
        board.addPiece(dest, queen);
        assertFalse(board.isEmpty(dest));
        assertEquals(PieceKind.QUEEN, board.getPieceAt(dest).getKind());
        assertEquals(dest, queen.getCell());
    }

    @Test
    void testAddPieceToOccupiedCellThrows() {
        Position occupied = new Position(0, 0);
        assertThrows(IllegalStateException.class,
                () -> board.addPiece(occupied, new Queen(11, PieceColor.WHITE)));
    }

    @Test
    void testRemovePieceClearsCellAndMarksCaptured() {
        Position source = new Position(0, 0);
        Piece removed = board.removePiece(source);
        assertNotNull(removed);
        assertEquals(PieceState.CAPTURED, removed.getState());
        assertTrue(board.isEmpty(source));
    }

    @Test
    void testRemovePieceFromEmptyCellReturnsNull() {
        assertNull(board.removePiece(new Position(0, 1)));
    }

    @Test
    void testClearCellOnlyDoesNotChangePieceState() {
        Position source = new Position(0, 0);
        Piece rook = board.getPieceAt(source);
        board.clearCellOnly(source);
        assertTrue(board.isEmpty(source));
        assertEquals(PieceState.IDLE, rook.getState());
    }

    @Test
    void testParserEmptyInputReturnsNull() {
        assertNull(parser.parse(Collections.emptyList()));
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

    @Test
    void testParserAssignsSequentialIds() {
        List<String> rawLines = Arrays.asList("wR . bK", ". wP .");
        List<Piece> pieces = parser.parse(rawLines);
        List<Integer> ids = new ArrayList<>();
        for (Piece piece : pieces) ids.add(piece.getId());
        assertEquals(Arrays.asList(0, 1, 2), ids);
    }

    @Test
    void testParseRowsAndColsOnEmptyInput() {
        assertEquals(0, parser.parseRows(Collections.emptyList()));
        assertEquals(0, parser.parseCols(Collections.emptyList()));
    }
}
