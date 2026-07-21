package view;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PieceSnapshot {
    private final int id;
    private final PieceColor color;
    private final PieceKind kind;
    private final String representation;
    private final PieceState state;
}
