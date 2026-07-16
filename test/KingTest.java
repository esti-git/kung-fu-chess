import enums.PieceColor;
import enums.PieceKind;
import model.Piece;
import org.junit.jupiter.api.Test;
import rules.pieces.King;

import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    @Test
    void testOneStepMoveIsLegal() {
        Piece king = new King(1, PieceColor.WHITE);
        assertTrue(king.isMovementPatternLegal(4, 4, 5, 5, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 4, 3, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 3, 4, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 5, 4, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 3, 3, 8));
        assertTrue(king.isMovementPatternLegal(4, 4, 3, 5, 8));
    }

    @Test
    void testMoreThanOneStepIsIllegal() {
        Piece king = new King(1, PieceColor.WHITE);
        assertFalse(king.isMovementPatternLegal(4, 4, 4, 6, 8));
        assertFalse(king.isMovementPatternLegal(4, 4, 2, 4, 8));
        assertFalse(king.isMovementPatternLegal(4, 4, 6, 6, 8));
    }

    @Test
    void testKnightShapedMoveIsIllegal() {
        Piece king = new King(1, PieceColor.WHITE);
        assertFalse(king.isMovementPatternLegal(4, 4, 6, 5, 8));
    }

    @Test
    void testIsNotSlidingPiece() {
        assertFalse(new King(1, PieceColor.WHITE).isSlidingPiece());
    }

    @Test
    void testRepresentationAndIdentity() {
        Piece whiteKing = new King(0, PieceColor.WHITE);
        Piece blackKing = new King(16, PieceColor.BLACK);
        assertEquals("wK", whiteKing.getRepresentation());
        assertEquals("bK", blackKing.getRepresentation());
        assertEquals(PieceKind.KING, whiteKing.getKind());
        assertEquals(16, blackKing.getId());
        assertEquals(PieceColor.BLACK, blackKing.getColor());
    }
}
