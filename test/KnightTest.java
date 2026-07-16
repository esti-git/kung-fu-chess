import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.Knight;

import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    @Test
    void testLShapeMoveIsLegal() {
        Piece knight = new Knight(1, PieceColor.WHITE);
        assertTrue(knight.isMovementPatternLegal(4, 4, 6, 5, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 6, 3, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 5, 6, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 3, 6, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 2, 5, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 2, 3, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 5, 2, 8));
        assertTrue(knight.isMovementPatternLegal(4, 4, 3, 2, 8));
    }

    @Test
    void testNonLShapeMoveIsIllegal() {
        Piece knight = new Knight(1, PieceColor.WHITE);
        assertFalse(knight.isMovementPatternLegal(4, 4, 4, 6, 8));
        assertFalse(knight.isMovementPatternLegal(4, 4, 6, 6, 8));
        assertFalse(knight.isMovementPatternLegal(4, 4, 5, 5, 8));
        assertFalse(knight.isMovementPatternLegal(4, 4, 4, 4, 8));
    }

    @Test
    void testIsNotSlidingPiece() {
        assertFalse(new Knight(1, PieceColor.WHITE).isSlidingPiece());
    }

    @Test
    void testRepresentationAndIdentity() {
        Piece whiteKnight = new Knight(5, PieceColor.WHITE);
        Piece blackKnight = new Knight(6, PieceColor.BLACK);
        assertEquals("wN", whiteKnight.getRepresentation());
        assertEquals("bN", blackKnight.getRepresentation());
        assertEquals(PieceKind.KNIGHT, whiteKnight.getKind());
        assertEquals(5, whiteKnight.getId());
        assertEquals(PieceColor.BLACK, blackKnight.getColor());
    }
}
