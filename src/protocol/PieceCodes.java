package protocol;

import enums.PieceColor;
import enums.PieceKind;

public class PieceCodes {

    public static char colorChar(PieceColor color) {
        return color == PieceColor.WHITE ? 'W' : 'B';
    }

    public static PieceColor colorFromChar(char c) {
        switch (Character.toUpperCase(c)) {
            case 'W': return PieceColor.WHITE;
            case 'B': return PieceColor.BLACK;
            default: return null;
        }
    }

    public static char kindChar(PieceKind kind) {
        switch (kind) {
            case KING: return 'K';
            case QUEEN: return 'Q';
            case ROOK: return 'R';
            case BISHOP: return 'B';
            case KNIGHT: return 'N';
            case PAWN: return 'P';
            default: return '?';
        }
    }

    public static PieceKind kindFromChar(char c) {
        switch (Character.toUpperCase(c)) {
            case 'K': return PieceKind.KING;
            case 'Q': return PieceKind.QUEEN;
            case 'R': return PieceKind.ROOK;
            case 'B': return PieceKind.BISHOP;
            case 'N': return PieceKind.KNIGHT;
            case 'P': return PieceKind.PAWN;
            default: return null;
        }
    }
}
