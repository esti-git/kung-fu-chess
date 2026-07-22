package view;

import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.PieceCapturedEvent;
import rules.PieceValues;

public class ScoreTracker {

    private int whiteScore;
    private int blackScore;

    public ScoreTracker(EventBus eventBus) {
        eventBus.subscribe(PieceCapturedEvent.TYPE, this::onPieceCaptured);
    }

    public int getWhiteScore() { return whiteScore; }
    public int getBlackScore() { return blackScore; }

    public void reset() {
        whiteScore = 0;
        blackScore = 0;
    }

    public void applyPieceCaptured(Event event) {
        onPieceCaptured(event);
    }

    private void onPieceCaptured(Event event) {
        PieceCapturedEvent captured = (PieceCapturedEvent) event;
        int value = PieceValues.pointValue(captured.getCapturedKind());

        if (captured.getCapturedColor() == PieceColor.WHITE) {
            blackScore += value;
        } else {
            whiteScore += value;
        }
    }
}
