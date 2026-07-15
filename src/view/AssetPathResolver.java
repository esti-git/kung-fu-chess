package view;

import model.Piece;
import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;

public class AssetPathResolver {

    private static final String ASSETS_DIR = "assets/";

    public static String getPieceImagePath(Piece piece) {
        // 1. קביעת קוד הכלי (למשל BB, KW)
        String colorCode = (piece.getColor() == PieceColor.WHITE) ? "W" : "B";
        String kindCode = getKindCode(piece.getKind());
        String folderName = kindCode + colorCode; // למשל: BB, KW

        // 2. קביעת תיקיית המצב (idle, move, jump)
        String stateFolder = getStateFolder(piece.getState());

        // 3. החזרת נתיב ברירת המחדל (תמונה ראשונה בספרייט / תמונה יחידה)
        // אם יש אנימציה מרובת פריימים נשתמש בפריים 1 כברירת מחדל
        return ASSETS_DIR + folderName + "/states/" + stateFolder + "/sprites/1.png";
    }

    private static String getKindCode(PieceKind kind) {
        switch (kind) {
            case KING: return "K";
            case QUEEN: return "Q";
            case ROOK: return "R";
            case BISHOP: return "B";
            case KNIGHT: return "N";
            case PAWN: return "P";
            default: return "P";
        }
    }

    private static String getStateFolder(PieceState state) {
        switch (state) {
            case MOVING: return "move";
            case JUMPING: return "jump";
            case IDLE:
            default:
                return "idle";
        }
    }
}
