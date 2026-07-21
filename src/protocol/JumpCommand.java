package protocol;

import enums.PieceColor;
import enums.PieceKind;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import model.Position;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JumpCommand {

    private static final char PREFIX = 'J';

    public final PieceColor color;
    public final PieceKind kind;
    public final Position position;

    public static boolean isJumpCommand(String raw) {
        return raw != null && raw.length() >= 5 && raw.charAt(0) == PREFIX;
    }

    public static JumpCommand parse(String raw, int boardRows) {
        if (!isJumpCommand(raw)) return null;

        PieceColor color = PieceCodes.colorFromChar(raw.charAt(1));
        PieceKind kind = PieceCodes.kindFromChar(raw.charAt(2));
        if (color == null || kind == null) return null;

        Position position = MoveCommand.squareToPosition(raw.substring(3), boardRows);
        if (position == null) return null;

        return new JumpCommand(color, kind, position);
    }

    public static String build(PieceColor color, PieceKind kind, int row, int col, int boardRows) {
        return "" + PREFIX + PieceCodes.colorChar(color) + PieceCodes.kindChar(kind)
                + MoveCommand.squareName(row, col, boardRows);
    }
}
