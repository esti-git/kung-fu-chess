package view;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingRestSnapshot {
    private final PieceSnapshot piece;
    private final long endTime;
}
