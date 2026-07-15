package view;

import enums.PieceColor;
import enums.PieceKind;

import java.util.List;

/**
 * צופה בתמונת המצב (BoardSnapshot) מהצד ומזהה תפיסות דרך יומן התפיסות של הארביטר -
 * לא מקבל שום קריאה מקוד ביצוע המהלכים/התפיסות עצמו, ולא מחזיק שום הפניה לכלים חיים.
 */
public class ScoreTracker {

    private int processedCaptureCount;
    private int whiteScore;
    private int blackScore;

    public int getWhiteScore() { return whiteScore; }
    public int getBlackScore() { return blackScore; }

    /** מנקה את הניקוד - חובה לקרוא כשמתחילים משחק חדש */
    public void reset() {
        processedCaptureCount = 0;
        whiteScore = 0;
        blackScore = 0;
    }

    public void poll(BoardSnapshot snapshot) {
        List<CaptureSnapshot> captureLog = snapshot.getCaptureLog();
        for (int i = processedCaptureCount; i < captureLog.size(); i++) {
            CaptureSnapshot capture = captureLog.get(i);
            int value = pointValue(capture.getCapturedKind());
            // מי שנתפס מפסיד את הנקודות, הצד השני מקבל אותן
            if (capture.getCapturedColor() == PieceColor.WHITE) {
                blackScore += value;
            } else {
                whiteScore += value;
            }
        }
        processedCaptureCount = captureLog.size();
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
