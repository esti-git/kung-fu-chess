package io;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import model.Board;
import model.Piece;
import model.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * צופה בלוח מהצד (כמו MoveHistoryTracker) ומזהה כלים שנתפסו לפי מצב הכלי (CAPTURED) -
 * לא מקבל שום קריאה מקוד ביצוע המהלכים/התפיסות עצמו.
 */
public class ScoreTracker {

    private final Board board;
    private final Map<Integer, Piece> knownPieces = new HashMap<>();
    private final Set<Integer> scoredCaptures = new HashSet<>();
    private int whiteScore;
    private int blackScore;

    public ScoreTracker(Board board) {
        this.board = board;
    }

    public int getWhiteScore() { return whiteScore; }
    public int getBlackScore() { return blackScore; }

    public void poll() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece != null) {
                    knownPieces.putIfAbsent(piece.getId(), piece);
                }
            }
        }

        for (Piece piece : knownPieces.values()) {
            if (piece.getState() == PieceState.CAPTURED && scoredCaptures.add(piece.getId())) {
                int value = pointValue(piece.getKind());
                if (piece.getColor() == PieceColor.WHITE) {
                    blackScore += value;
                } else {
                    whiteScore += value;
                }
            }
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
