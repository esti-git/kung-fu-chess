package rules;

import enums.PieceKind;

public final class PieceValues {

    private PieceValues() {
    }

    public static int pointValue(PieceKind kind) {
        switch (kind) {
            case PAWN: return 1;
            case KNIGHT:
            case BISHOP: return 3;
            case ROOK: return 5;
            case QUEEN: return 9;
            default: return 0;
        }
    }
}
