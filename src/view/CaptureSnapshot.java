package view;

import enums.PieceColor;
import enums.PieceKind;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CaptureSnapshot {
    private final PieceColor capturedColor;
    private final PieceKind capturedKind;
}
