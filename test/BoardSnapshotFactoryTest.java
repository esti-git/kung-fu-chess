import enums.PieceColor;
import enums.PieceKind;
import input.GameFactory;
import org.junit.jupiter.api.Test;
import view.BoardSnapshot;
import view.BoardSnapshotFactory;
import view.PieceSnapshot;

import static org.junit.jupiter.api.Assertions.*;

class BoardSnapshotFactoryTest {

    private GameFactory buildStandardGame() {
        GameFactory factory = new GameFactory();
        factory.initializeStandardBoard();
        return factory;
    }

    @Test
    void testCaptureMatchesBoardDimensions() {
        GameFactory factory = buildStandardGame();
        BoardSnapshot snapshot = new BoardSnapshotFactory().capture(factory.getEngine());

        assertEquals(8, snapshot.getRows());
        assertEquals(8, snapshot.getCols());
    }

    @Test
    void testCaptureReflectsPiecesOnBoard() {
        GameFactory factory = buildStandardGame();
        BoardSnapshot snapshot = new BoardSnapshotFactory().capture(factory.getEngine());

        PieceSnapshot whiteKing = snapshot.getPieceAt(7, 4);
        assertNotNull(whiteKing);
        assertEquals(PieceColor.WHITE, whiteKing.getColor());
        assertEquals(PieceKind.KING, whiteKing.getKind());

        PieceSnapshot blackKing = snapshot.getPieceAt(0, 4);
        assertNotNull(blackKing);
        assertEquals(PieceColor.BLACK, blackKing.getColor());
        assertEquals(PieceKind.KING, blackKing.getKind());
    }

    @Test
    void testCaptureLeavesEmptySquaresNull() {
        GameFactory factory = buildStandardGame();
        BoardSnapshot snapshot = new BoardSnapshotFactory().capture(factory.getEngine());

        assertNull(snapshot.getPieceAt(3, 3));
        assertNull(snapshot.getPieceAt(4, 4));
    }

    @Test
    void testCaptureIncludesPendingMoveAfterRequestingOne() {
        GameFactory factory = buildStandardGame();
        factory.getEngine().requestMove(new model.Position(6, 4), new model.Position(4, 4));

        BoardSnapshot snapshot = new BoardSnapshotFactory().capture(factory.getEngine());

        assertEquals(1, snapshot.getPendingMoves().size());
        assertEquals(6, snapshot.getPendingMoves().get(0).getFromRow());
        assertEquals(4, snapshot.getPendingMoves().get(0).getToRow());
    }

    @Test
    void testCaptureWithNoActivityHasEmptyPendingLists() {
        GameFactory factory = buildStandardGame();
        BoardSnapshot snapshot = new BoardSnapshotFactory().capture(factory.getEngine());

        assertTrue(snapshot.getPendingMoves().isEmpty());
        assertTrue(snapshot.getPendingJumps().isEmpty());
        assertTrue(snapshot.getPendingRests().isEmpty());
        assertTrue(snapshot.getCaptureLog().isEmpty());
    }
}
