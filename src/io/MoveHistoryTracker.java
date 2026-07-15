package io;

import enums.PieceColor;
import model.Board;
import model.Piece;
import model.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * צופה בלוח מהצד ומזהה מהלכים לפי השוואת מיקומי הכלים בין בדיקה לבדיקה -
 * לא מקבל שום קריאה מקוד ביצוע המהלכים עצמו (RealTimeArbiter/GameEngine).
 */
public class MoveHistoryTracker {

    private final Board board;
    private final List<String> whiteMoves = new ArrayList<>();
    private final List<String> blackMoves = new ArrayList<>();
    private final Map<Integer, Position> lastKnownPositions = new HashMap<>();

    public MoveHistoryTracker(Board board) {
        this.board = board;
    }

    public List<String> getWhiteMoves() { return whiteMoves; }
    public List<String> getBlackMoves() { return blackMoves; }

    public void poll() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece == null) continue;

                Position previous = lastKnownPositions.get(piece.getId());
                if (previous != null && (previous.getRow() != r || previous.getCol() != c)) {
                    List<String> targetList = (piece.getColor() == PieceColor.WHITE) ? whiteMoves : blackMoves;
                    String entry = (targetList.size() + 1) + ". " + piece.getRepresentation()
                            + " " + toSquare(previous) + "-" + toSquare(r, c);
                    targetList.add(entry);
                }

                // עדכון רק לכלים שנצפו כרגע בפועל - כלי שבתנועה/קפיצה כרגע (ולכן זמנית לא נמצא באף משבצת)
                // שומר על המיקום האחרון שבו כן נצפה, כדי שהמהלך יזוהה נכון כשהוא יגיע/ינחת
                lastKnownPositions.put(piece.getId(), new Position(r, c));
            }
        }
    }

    private String toSquare(Position pos) {
        return toSquare(pos.getRow(), pos.getCol());
    }

    private String toSquare(int row, int col) {
        char file = (char) ('a' + col);
        int rank = board.getRows() - row;
        return "" + file + rank;
    }
}
