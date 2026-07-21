package protocol;

import enums.PieceColor;
import enums.PieceKind;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import model.Position;

/** Parses raw move strings like "WQe2e5" (color + kind + from-square + to-square) sent by clients. */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MoveCommand {

    public final PieceColor color;
    public final PieceKind kind;
    public final Position from;
    public final Position to;

    public static MoveCommand parse(String raw, int boardRows) {
        if (raw == null || raw.length() != 6) return null;

        PieceColor color = PieceCodes.colorFromChar(raw.charAt(0));
        PieceKind kind = PieceCodes.kindFromChar(raw.charAt(1));
        if (color == null || kind == null) return null;

        Position from = squareToPosition(raw.substring(2, 4), boardRows);
        Position to = squareToPosition(raw.substring(4, 6), boardRows);
        if (from == null || to == null) return null;

        return new MoveCommand(color, kind, from, to);
    }

    public static String squareName(int row, int col, int boardRows) {
        char file = (char) ('a' + col);
        int rank = boardRows - row;
        return "" + file + rank;
    }

    public static Position squareToPosition(String square, int boardRows) {
        char file = Character.toLowerCase(square.charAt(0));
        if (file < 'a' || file > 'z') return null;

        int rank;
        try {
            rank = Integer.parseInt(square.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }

        int col = file - 'a';
        int row = boardRows - rank;
        return new Position(row, col);
    }
}
