package input;

import engine.GameEngine;
import engine.GameResult;
import model.Piece;
import model.Position;

public class Controller {

    private final GameEngine engine;
    private final BoardMapper boardMapper;
    private Position selectedPosition;

    public Controller(GameEngine engine, BoardMapper boardMapper) {
        this.engine = engine;
        this.boardMapper = boardMapper;
        this.selectedPosition = null;
    }

    public void handleRawClick(String[] parts) {
        if (parts == null || parts.length < 3) return;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            click(x, y);
        } catch (NumberFormatException ignored) {}
    }

    public void handleRawJump(String[] parts) {
        if (parts == null || parts.length < 3) return;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            jump(x, y);
        } catch (NumberFormatException ignored) {}
    }

    public void click(int x, int y) {
        boardMapper.pixelToCell(x, y).ifPresent(position -> {
            if (selectedPosition == null) {
                trySelect(position);
                return;
            }

            if (selectedPosition.equals(position)) {
                clearSelection();
                return;
            }

            Piece selectedPiece = engine.pieceAt(selectedPosition).orElse(null);
            Piece clickedPiece = engine.pieceAt(position).orElse(null);

            if (selectedPiece != null && clickedPiece != null && selectedPiece.getColor() == clickedPiece.getColor()) {
                trySelect(position);
                return;
            }

            GameResult<Void> moveResult = engine.requestMove(selectedPosition, position);
            if (moveResult.isSuccess()) {
                clearSelection();
            }
        });
    }

    public void jump(int x, int y) {
        boardMapper.pixelToCell(x, y).ifPresent(position -> {
            GameResult<Void> jumpResult = engine.requestJump(position);
            if (jumpResult.isSuccess()) {
                clearSelection();
            }
        });
    }

    public void trySelect(Position position) {
        if (position == null) {
            clearSelection();
            return;
        }

        engine.pieceAt(position).ifPresentOrElse(piece -> {
            if (!engine.isPieceBusy(piece.getId())) {
                selectedPosition = position;
            } else {
                clearSelection();
            }
        }, this::clearSelection);
    }

    public Position selectedPosition() {
        return selectedPosition;
    }

    public void clearSelection() {
        selectedPosition = null;
    }
}