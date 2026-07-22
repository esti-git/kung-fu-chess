import enums.PieceColor;
import enums.PieceKind;
import events.EventBus;
import events.PieceCapturedEvent;
import org.junit.jupiter.api.Test;
import view.ScoreTracker;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTrackerTest {

    @Test
    void testCapturingBlackPieceAwardsWhite() {
        EventBus bus = new EventBus();
        ScoreTracker tracker = new ScoreTracker(bus);

        bus.publish(new PieceCapturedEvent(PieceColor.BLACK, PieceKind.KNIGHT, PieceColor.WHITE));

        assertEquals(3, tracker.getWhiteScore());
        assertEquals(0, tracker.getBlackScore());
    }

    @Test
    void testCapturingWhitePieceAwardsBlack() {
        EventBus bus = new EventBus();
        ScoreTracker tracker = new ScoreTracker(bus);

        bus.publish(new PieceCapturedEvent(PieceColor.WHITE, PieceKind.QUEEN, PieceColor.BLACK));

        assertEquals(9, tracker.getBlackScore());
        assertEquals(0, tracker.getWhiteScore());
    }

    @Test
    void testPointValuesForEachKind() {
        EventBus bus = new EventBus();
        ScoreTracker tracker = new ScoreTracker(bus);

        bus.publish(new PieceCapturedEvent(PieceColor.BLACK, PieceKind.PAWN, PieceColor.WHITE));
        assertEquals(1, tracker.getWhiteScore());

        bus.publish(new PieceCapturedEvent(PieceColor.BLACK, PieceKind.BISHOP, PieceColor.WHITE));
        assertEquals(4, tracker.getWhiteScore());

        bus.publish(new PieceCapturedEvent(PieceColor.BLACK, PieceKind.ROOK, PieceColor.WHITE));
        assertEquals(9, tracker.getWhiteScore());

        bus.publish(new PieceCapturedEvent(PieceColor.BLACK, PieceKind.KING, PieceColor.WHITE));
        assertEquals(9, tracker.getWhiteScore());
    }

    @Test
    void testResetClearsScores() {
        EventBus bus = new EventBus();
        ScoreTracker tracker = new ScoreTracker(bus);

        bus.publish(new PieceCapturedEvent(PieceColor.BLACK, PieceKind.QUEEN, PieceColor.WHITE));
        tracker.reset();

        assertEquals(0, tracker.getWhiteScore());
        assertEquals(0, tracker.getBlackScore());
    }
}
