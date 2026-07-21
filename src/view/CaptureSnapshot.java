package view;

import enums.PieceColor;
import enums.PieceKind;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** תמונת מצב קפואה של רשומת תפיסה אחת - מי (צבע/סוג) נתפס */
@Getter
@AllArgsConstructor
public class CaptureSnapshot {
    private final PieceColor capturedColor;
    private final PieceKind capturedKind;
}
