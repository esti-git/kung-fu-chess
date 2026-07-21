package input;

import engine.GameEngine;
import common.GameResult;
import view.BoardPrinter;
import model.Piece;
import model.Position;

public class Controller {

    private final GameEngine engine;
    private final BoardMapper boardMapper;
    private final BoardPrinter printer;
    private Position selectedPosition;

    public Controller(GameEngine engine, BoardMapper boardMapper, BoardPrinter printer) {
        this.engine = engine;
        this.boardMapper = boardMapper;
        this.printer = printer;
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
                printer.printGUI();
                return;
            }

            if (selectedPosition.equals(position)) {
                clearSelection();
                printer.printGUI();
                return;
            }

            Piece selectedPiece = engine.pieceAt(selectedPosition).orElse(null);
            Piece clickedPiece = engine.pieceAt(position).orElse(null);

            if (selectedPiece != null && clickedPiece != null && selectedPiece.getColor() == clickedPiece.getColor()) {
                trySelect(position);
                printer.printGUI();
                return;
            }

            engine.requestMove(selectedPosition, position);

            clearSelection();
            printer.printGUI();
        });
    }

    public void jump(int x, int y) {
        boardMapper.pixelToCell(x, y).ifPresent(position -> {
            GameResult<Void> jumpResult = engine.requestJump(position);
            if (jumpResult.isSuccess()) {
                clearSelection();
                printer.printGUI();
            }
        });
    }

    public void trySelect(Position position) {
        if (position == null) {
            clearSelection();
            return;
        }

        engine.pieceAt(position).ifPresentOrElse(piece -> {
            if (piece.getState() == enums.PieceState.IDLE) {
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