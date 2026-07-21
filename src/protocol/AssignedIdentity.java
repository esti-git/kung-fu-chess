package protocol;

import enums.PieceColor;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AssignedIdentity {
    public final PieceColor color;
    public final String whiteName;
    public final String blackName;
    public final int whiteRating;
    public final int blackRating;
}
