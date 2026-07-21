package protocol;

import enums.PieceColor;

/** Decoded form of an "assign" message: this client's color plus both players' usernames (opponent may be null while waiting). */
public class AssignedIdentity {
    public final PieceColor color;
    public final String whiteName;
    public final String blackName;

    public AssignedIdentity(PieceColor color, String whiteName, String blackName) {
        this.color = color;
        this.whiteName = whiteName;
        this.blackName = blackName;
    }
}
