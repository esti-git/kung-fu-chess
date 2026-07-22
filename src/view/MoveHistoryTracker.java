package view;

import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.MoveMadeEvent;

import java.util.ArrayList;
import java.util.List;

public class MoveHistoryTracker {

    private final List<String> whiteMoves = new ArrayList<>();
    private final List<String> blackMoves = new ArrayList<>();

    public MoveHistoryTracker(EventBus eventBus) {
        eventBus.subscribe(MoveMadeEvent.TYPE, this::onMoveMade);
    }

    public List<String> getWhiteMoves() { return whiteMoves; }
    public List<String> getBlackMoves() { return blackMoves; }

    public void reset() {
        whiteMoves.clear();
        blackMoves.clear();
    }

    public void applyMoveMade(Event event) {
        onMoveMade(event);
    }

    private void onMoveMade(Event event) {
        MoveMadeEvent move = (MoveMadeEvent) event;
        List<String> targetList = (move.getPlayer() == PieceColor.WHITE) ? whiteMoves : blackMoves;
        String entry = (targetList.size() + 1) + ". " + move.getPieceRepresentation()
                + " " + move.getFrom().toAlgebraic(move.getBoardRows()) + "-" + move.getTo().toAlgebraic(move.getBoardRows());
        targetList.add(entry);
    }
}
