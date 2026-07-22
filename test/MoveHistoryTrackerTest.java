import enums.PieceColor;
import enums.PieceKind;
import events.EventBus;
import events.MoveMadeEvent;
import model.Position;
import org.junit.jupiter.api.Test;
import view.MoveHistoryTracker;

import static org.junit.jupiter.api.Assertions.*;

class MoveHistoryTrackerTest {

    @Test
    void testRecordsWhiteMove() {
        EventBus bus = new EventBus();
        MoveHistoryTracker tracker = new MoveHistoryTracker(bus);

        bus.publish(new MoveMadeEvent(PieceColor.WHITE, PieceKind.PAWN, "P",
                new Position(6, 4), new Position(4, 4), 8));

        assertEquals(1, tracker.getWhiteMoves().size());
        assertEquals("1. P e2-e4", tracker.getWhiteMoves().get(0));
        assertTrue(tracker.getBlackMoves().isEmpty());
    }

    @Test
    void testRecordsBlackMoveSeparatelyAndNumbersIndependently() {
        EventBus bus = new EventBus();
        MoveHistoryTracker tracker = new MoveHistoryTracker(bus);

        bus.publish(new MoveMadeEvent(PieceColor.WHITE, PieceKind.PAWN, "P",
                new Position(6, 4), new Position(4, 4), 8));
        bus.publish(new MoveMadeEvent(PieceColor.BLACK, PieceKind.PAWN, "P",
                new Position(1, 4), new Position(3, 4), 8));
        bus.publish(new MoveMadeEvent(PieceColor.WHITE, PieceKind.KNIGHT, "N",
                new Position(7, 6), new Position(5, 5), 8));

        assertEquals(2, tracker.getWhiteMoves().size());
        assertEquals("2. N g1-f3", tracker.getWhiteMoves().get(1));
        assertEquals(1, tracker.getBlackMoves().size());
        assertEquals("1. P e7-e5", tracker.getBlackMoves().get(0));
    }

    @Test
    void testResetClearsBothLists() {
        EventBus bus = new EventBus();
        MoveHistoryTracker tracker = new MoveHistoryTracker(bus);

        bus.publish(new MoveMadeEvent(PieceColor.WHITE, PieceKind.PAWN, "P",
                new Position(6, 4), new Position(4, 4), 8));
        tracker.reset();

        assertTrue(tracker.getWhiteMoves().isEmpty());
        assertTrue(tracker.getBlackMoves().isEmpty());
    }
}
