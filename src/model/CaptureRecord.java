package model;

import enums.PieceColor;
import enums.PieceKind;

/** רשומה קבועה על תפיסת כלי - נשמרת גם אחרי שהכלי עצמו כבר לא בלוח, לשימוש חישוב ניקוד ע"י הצופה */
public class CaptureRecord {
    private final PieceColor capturedColor;
    private final PieceKind capturedKind;

    public CaptureRecord(PieceColor capturedColor, PieceKind capturedKind) {
        this.capturedColor = capturedColor;
        this.capturedKind = capturedKind;
    }

    public PieceColor getCapturedColor() { return capturedColor; }
    public PieceKind getCapturedKind() { return capturedKind; }
}
