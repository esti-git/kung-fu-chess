import enums.PieceColor;
import enums.PieceKind;
import org.junit.jupiter.api.Test;
import protocol.PieceCodes;

import static org.junit.jupiter.api.Assertions.*;

class PieceCodesTest {

    @Test
    void testColorChar() {
        assertEquals('W', PieceCodes.colorChar(PieceColor.WHITE));
        assertEquals('B', PieceCodes.colorChar(PieceColor.BLACK));
    }

    @Test
    void testColorFromChar() {
        assertEquals(PieceColor.WHITE, PieceCodes.colorFromChar('w'));
        assertEquals(PieceColor.WHITE, PieceCodes.colorFromChar('W'));
        assertEquals(PieceColor.BLACK, PieceCodes.colorFromChar('b'));
        assertNull(PieceCodes.colorFromChar('x'));
    }

    @Test
    void testKindChar() {
        assertEquals('K', PieceCodes.kindChar(PieceKind.KING));
        assertEquals('Q', PieceCodes.kindChar(PieceKind.QUEEN));
        assertEquals('R', PieceCodes.kindChar(PieceKind.ROOK));
        assertEquals('B', PieceCodes.kindChar(PieceKind.BISHOP));
        assertEquals('N', PieceCodes.kindChar(PieceKind.KNIGHT));
        assertEquals('P', PieceCodes.kindChar(PieceKind.PAWN));
    }

    @Test
    void testKindFromChar() {
        assertEquals(PieceKind.KING, PieceCodes.kindFromChar('k'));
        assertEquals(PieceKind.QUEEN, PieceCodes.kindFromChar('Q'));
        assertEquals(PieceKind.ROOK, PieceCodes.kindFromChar('r'));
        assertEquals(PieceKind.BISHOP, PieceCodes.kindFromChar('B'));
        assertEquals(PieceKind.KNIGHT, PieceCodes.kindFromChar('n'));
        assertEquals(PieceKind.PAWN, PieceCodes.kindFromChar('P'));
        assertNull(PieceCodes.kindFromChar('X'));
    }

    @Test
    void testKindCharAndKindFromCharRoundTrip() {
        for (PieceKind kind : PieceKind.values()) {
            char code = PieceCodes.kindChar(kind);
            assertEquals(kind, PieceCodes.kindFromChar(code));
        }
    }
}
