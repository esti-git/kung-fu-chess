package view;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;

/** תמונת מצב קפואה של כלי בודד, ללא הפניה חיה לאובייקט ה-Piece עצמו */
public class PieceSnapshot {
    private final int id;
    private final PieceColor color;
    private final PieceKind kind;
    private final String representation;
    private final PieceState state;

    public PieceSnapshot(int id, PieceColor color, PieceKind kind, String representation, PieceState state) {
        this.id = id;
        this.color = color;
        this.kind = kind;
        this.representation = representation;
        this.state = state;
    }

    public int getId() { return id; }
    public PieceColor getColor() { return color; }
    public PieceKind getKind() { return kind; }
    public String getRepresentation() { return representation; }
    public PieceState getState() { return state; }
}
