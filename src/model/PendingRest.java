package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingRest {
    private final Piece piece;
    private final long endTime;
}
