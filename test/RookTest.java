import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.Rook;

import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    @Test
    void testStraightMoveIsLegal() {
        Piece rook = new Rook(1, PieceColor.WHITE);
        assertTrue(rook.isMovementPatternLegal(3, 3, 3, 7, 8));
        assertTrue(rook.isMovementPatternLegal(3, 3, 0, 3, 8));
        assertTrue(rook.isMovementPatternLegal(3, 3, 7, 3, 8));
        assertTrue(rook.isMovementPatternLegal(3, 3, 3, 0, 8));
    }

    @Test
    void testDiagonalMoveIsIllegal() {
        Piece rook = new Rook(1, PieceColor.WHITE);
        assertFalse(rook.isMovementPatternLegal(3, 3, 5, 5, 8));
        assertFalse(rook.isMovementPatternLegal(3, 3, 1, 5, 8));
    }

    @Test
    void testKnightShapedMoveIsIllegal() {
        Piece rook = new Rook(1, PieceColor.WHITE);
        assertFalse(rook.isMovementPatternLegal(3, 3, 5, 4, 8));
    }

    @Test
    void testIsSlidingPiece() {
        assertTrue(new Rook(1, PieceColor.WHITE).isSlidingPiece());
    }

    @Test
    void testRepresentationAndIdentity() {
        Piece whiteRook = new Rook(3, PieceColor.WHITE);
        Piece blackRook = new Rook(4, PieceColor.BLACK);
        assertEquals("wR", whiteRook.getRepresentation());
        assertEquals("bR", blackRook.getRepresentation());
        assertEquals(PieceKind.ROOK, whiteRook.getKind());
        assertEquals(3, whiteRook.getId());
        assertEquals(PieceColor.BLACK, blackRook.getColor());
    }
}
