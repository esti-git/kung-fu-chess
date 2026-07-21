package view;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** תמונת מצב קפואה של קפיצה פעילה - כמו model.PendingJump, אבל עם PieceSnapshot במקום Piece חי */
@Getter
@AllArgsConstructor
public class PendingJumpSnapshot {
    private final int row;
    private final int col;
    private final PieceSnapshot piece;
    private final long startTime;
    private final long endTime;
}
