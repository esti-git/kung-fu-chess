package view;

import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.MoveMadeEvent;
import model.Position;

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
                + " " + toSquare(move.getFrom(), move.getBoardRows()) + "-" + toSquare(move.getTo(), move.getBoardRows());
        targetList.add(entry);
    }

    private String toSquare(Position pos, int rows) {
        return toSquare(pos.getRow(), pos.getCol(), rows);
    }

    private String toSquare(int row, int col, int rows) {
        char file = (char) ('a' + col);
        int rank = rows - row;
        return "" + file + rank;
    }
}
