import enums.PieceColor;
import enums.PieceKind;
import io.BoardParser;
import model.Piece;
import model.Position;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardParserTest {

    private final BoardParser parser = new BoardParser();

    @Test
    void testParseEmptyListReturnsNull() {
        assertNull(parser.parse(Collections.emptyList()));
    }

    @Test
    void testParseSkipsEmptyCells() {
        List<String> lines = Arrays.asList(
                "wR wN . .",
                ". . bK ."
        );

        List<Piece> pieces = parser.parse(lines);

        assertNotNull(pieces);
        assertEquals(3, pieces.size());
    }

    @Test
    void testParseAssignsColorKindAndPosition() {
        List<String> lines = Collections.singletonList("wK . bQ");

        List<Piece> pieces = parser.parse(lines);

        assertNotNull(pieces);
        assertEquals(2, pieces.size());

        Piece king = pieces.get(0);
        assertEquals(PieceColor.WHITE, king.getColor());
        assertEquals(PieceKind.KING, king.getKind());
        assertEquals(new Position(0, 0), king.getCell());

        Piece queen = pieces.get(1);
        assertEquals(PieceColor.BLACK, queen.getColor());
        assertEquals(PieceKind.QUEEN, queen.getKind());
        assertEquals(new Position(0, 2), queen.getCell());
    }

    @Test
    void testParseAssignsSequentialIds() {
        List<String> lines = Collections.singletonList("wP wP wP");

        List<Piece> pieces = parser.parse(lines);

        assertNotNull(pieces);
        assertEquals(0, pieces.get(0).getId());
        assertEquals(1, pieces.get(1).getId());
        assertEquals(2, pieces.get(2).getId());
    }

    @Test
    void testParseReturnsNullOnRowWidthMismatch() {
        List<String> lines = Arrays.asList(
                "wR wN . .",
                ". bK ."
        );

        assertNull(parser.parse(lines));
    }

    @Test
    void testParseReturnsNullOnUnknownToken() {
        List<String> lines = Collections.singletonList("wX . .");

        assertNull(parser.parse(lines));
    }

    @Test
    void testParseReturnsNullOnInvalidColorPrefix() {
        List<String> lines = Collections.singletonList("xK . .");

        assertNull(parser.parse(lines));
    }

    @Test
    void testParseRows() {
        List<String> lines = Arrays.asList("wR . .", ". . .", ". . bK");
        assertEquals(3, parser.parseRows(lines));
    }

    @Test
    void testParseColsOnEmptyListReturnsZero() {
        assertEquals(0, parser.parseCols(Collections.emptyList()));
    }

    @Test
    void testParseCols() {
        List<String> lines = Collections.singletonList("wR . bK .");
        assertEquals(4, parser.parseCols(lines));
    }
}
