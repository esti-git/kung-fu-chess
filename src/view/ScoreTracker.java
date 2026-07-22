package view;

import enums.PieceColor;
import enums.PieceKind;
import events.Event;
import events.EventBus;
import events.PieceCapturedEvent;

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
        int value = pointValue(captured.getCapturedKind());

        if (captured.getCapturedColor() == PieceColor.WHITE) {
            blackScore += value;
        } else {
            whiteScore += value;
        }
    }

    private int pointValue(PieceKind kind) {
        switch (kind) {
            case PAWN: return 1;
            case KNIGHT:
            case BISHOP: return 3;
            case ROOK: return 5;
            case QUEEN: return 9;
            default: return 0;
        }
    }
}
