package view;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** תמונת מצב קפואה של כלי בודד, ללא הפניה חיה לאובייקט ה-Piece עצמו */
@Getter
@AllArgsConstructor
public class PieceSnapshot {
    private final int id;
    private final PieceColor color;
    private final PieceKind kind;
    private final String representation;
    private final PieceState state;
}
