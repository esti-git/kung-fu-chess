package protocol;

import enums.PieceColor;
import lombok.AllArgsConstructor;

/** Decoded form of an "assign" message: this client's color plus both players' usernames and
 *  ratings (opponent name/rating default while waiting for the second player to join). */
@AllArgsConstructor
public class AssignedIdentity {
    public final PieceColor color;
    public final String whiteName;
    public final String blackName;
    public final int whiteRating;
    public final int blackRating;
}
