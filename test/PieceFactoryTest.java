import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import model.Position;
import org.junit.jupiter.api.Test;
import rules.PieceFactory;
import rules.pieces.Bishop;
import rules.pieces.King;
import rules.pieces.Knight;
import rules.pieces.Pawn;
import rules.pieces.Queen;
import rules.pieces.Rook;

import static org.junit.jupiter.api.Assertions.*;

class PieceFactoryTest {

    @Test
    void testCreatesKing() {
        Piece piece = PieceFactory.create(1, PieceColor.WHITE, 'K', new Position(0, 4));
        assertInstanceOf(King.class, piece);
        assertEquals(PieceKind.KING, piece.getKind());
    }

    @Test
    void testCreatesQueen() {
        Piece piece = PieceFactory.create(2, PieceColor.WHITE, 'Q', new Position(0, 3));
        assertInstanceOf(Queen.class, piece);
        assertEquals(PieceKind.QUEEN, piece.getKind());
    }

    @Test
    void testCreatesRook() {
        Piece piece = PieceFactory.create(3, PieceColor.BLACK, 'R', new Position(0, 0));
        assertInstanceOf(Rook.class, piece);
        assertEquals(PieceKind.ROOK, piece.getKind());
    }

    @Test
    void testCreatesBishop() {
        Piece piece = PieceFactory.create(4, PieceColor.BLACK, 'B', new Position(0, 2));
        assertInstanceOf(Bishop.class, piece);
        assertEquals(PieceKind.BISHOP, piece.getKind());
    }

    @Test
    void testCreatesKnight() {
        Piece piece = PieceFactory.create(5, PieceColor.WHITE, 'N', new Position(0, 1));
        assertInstanceOf(Knight.class, piece);
        assertEquals(PieceKind.KNIGHT, piece.getKind());
    }

    @Test
    void testCreatesPawn() {
        Piece piece = PieceFactory.create(6, PieceColor.BLACK, 'P', new Position(1, 0));
        assertInstanceOf(Pawn.class, piece);
        assertEquals(PieceKind.PAWN, piece.getKind());
    }

    @Test
    void testCreateWithInvalidKindCharReturnsNull() {
        assertNull(PieceFactory.create(7, PieceColor.WHITE, 'X', new Position(0, 0)));
        assertNull(PieceFactory.create(7, PieceColor.WHITE, ' ', new Position(0, 0)));
    }

    @Test
    void testCreateSetsPieceIdColorAndCell() {
        Position cell = new Position(2, 5);
        Piece piece = PieceFactory.create(42, PieceColor.BLACK, 'N', cell);

        assertEquals(42, piece.getId());
        assertEquals(PieceColor.BLACK, piece.getColor());
        assertEquals(cell, piece.getCell());
    }
}
