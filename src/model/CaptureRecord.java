package model;

import enums.PieceColor;
import enums.PieceKind;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CaptureRecord {
    private final PieceColor capturedColor;
    private final PieceKind capturedKind;
}
