package local;

import engine.GameEngine;
import common.GameResult;
import model.Piece;
import model.Position;
import view.SelectionState;

public class LocalController {

    private final GameEngine engine;
    private final LocalBoardMapper boardMapper;
    private final LocalBoardPrinter printer;
    private final SelectionState selection = new SelectionState();

    public LocalController(GameEngine engine, LocalBoardMapper boardMapper, LocalBoardPrinter printer) {
        this.engine = engine;
        this.boardMapper = boardMapper;
        this.printer = printer;
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
            if (!selection.isSelected()) {
                trySelect(position);
                printer.printGUI();
                return;
            }

            if (selection.isSameAsSelected(position)) {
                clearSelection();
                printer.printGUI();
                return;
            }

            Piece selectedPiece = engine.pieceAt(selection.get()).orElse(null);
            Piece clickedPiece = engine.pieceAt(position).orElse(null);

            if (selectedPiece != null && clickedPiece != null && selectedPiece.getColor() == clickedPiece.getColor()) {
                trySelect(position);
                printer.printGUI();
                return;
            }

            engine.requestMove(selection.get(), position);

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
                selection.select(position);
            } else {
                clearSelection();
            }
        }, this::clearSelection);
    }

    public Position selectedPosition() {
        return selection.get();
    }

    public void clearSelection() {
        selection.clear();
    }
}
