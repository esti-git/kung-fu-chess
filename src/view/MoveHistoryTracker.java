package view;

import enums.PieceColor;
import model.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * צופה בתמונת המצב (BoardSnapshot) מהצד ומזהה מהלכים לפי השוואת מיקומי הכלים בין בדיקה לבדיקה -
 * לא מקבל שום קריאה מקוד ביצוע המהלכים עצמו (RealTimeArbiter/GameEngine), ולא נוגע בלוח החי כלל.
 */
public class MoveHistoryTracker {

    private final List<String> whiteMoves = new ArrayList<>();
    private final List<String> blackMoves = new ArrayList<>();
    private final Map<Integer, Position> lastKnownPositions = new HashMap<>();

    public List<String> getWhiteMoves() { return whiteMoves; }
    public List<String> getBlackMoves() { return blackMoves; }

    /** מנקה את ההיסטוריה - חובה לקרוא כשמתחילים משחק חדש, אחרת ה-id של כלים חדשים עלול להתנגש עם כלים ישנים */
    public void reset() {
        whiteMoves.clear();
        blackMoves.clear();
        lastKnownPositions.clear();
    }

    public void poll(BoardSnapshot snapshot) {
        int rows = snapshot.getRows();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < snapshot.getCols(); c++) {
                PieceSnapshot piece = snapshot.getPieceAt(r, c);
                if (piece == null) continue;

                Position previous = lastKnownPositions.get(piece.getId());
                if (previous != null && (previous.getRow() != r || previous.getCol() != c)) {
                    List<String> targetList = (piece.getColor() == PieceColor.WHITE) ? whiteMoves : blackMoves;
                    String entry = (targetList.size() + 1) + ". " + piece.getRepresentation()
                            + " " + toSquare(previous, rows) + "-" + toSquare(r, c, rows);
                    targetList.add(entry);
                }

                // עדכון רק לכלים שנצפו כרגע בפועל - כלי שבתנועה/קפיצה כרגע (ולכן זמנית לא נמצא באף משבצת)
                // שומר על המיקום האחרון שבו כן נצפה, כדי שהמהלך יזוהה נכון כשהוא יגיע/ינחת
                lastKnownPositions.put(piece.getId(), new Position(r, c));
            }
        }
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
