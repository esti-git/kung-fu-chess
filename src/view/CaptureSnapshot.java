package view;

import enums.PieceColor;
import enums.PieceKind;

/** תמונת מצב קפואה של רשומת תפיסה אחת - מי (צבע/סוג) נתפס */
public class CaptureSnapshot {
    private final PieceColor capturedColor;
    private final PieceKind capturedKind;

    public CaptureSnapshot(PieceColor capturedColor, PieceKind capturedKind) {
        this.capturedColor = capturedColor;
        this.capturedKind = capturedKind;
    }

    public PieceColor getCapturedColor() { return capturedColor; }
    public PieceKind getCapturedKind() { return capturedKind; }
}
