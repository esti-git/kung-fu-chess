package model;

import enums.PieceColor;
import enums.PieceKind;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** רשומה קבועה על תפיסת כלי - נשמרת גם אחרי שהכלי עצמו כבר לא בלוח, לשימוש חישוב ניקוד ע"י הצופה */
@Getter
@AllArgsConstructor
public class CaptureRecord {
    private final PieceColor capturedColor;
    private final PieceKind capturedKind;
}
