package protocol;

import enums.PieceColor;

/** Decoded form of an "assign" message: this client's color plus both players' usernames and
 *  ratings (opponent name/rating default while waiting for the second player to join). */
public class AssignedIdentity {
    public final PieceColor color;
    public final String whiteName;
    public final String blackName;
    public final int whiteRating;
    public final int blackRating;

    public AssignedIdentity(PieceColor color, String whiteName, String blackName, int whiteRating, int blackRating) {
        this.color = color;
        this.whiteName = whiteName;
        this.blackName = blackName;
        this.whiteRating = whiteRating;
        this.blackRating = blackRating;
    }
}
