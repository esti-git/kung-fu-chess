package view;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** תמונת מצב קפואה של מנוחה פעילה - כמו model.PendingRest, אבל עם PieceSnapshot במקום Piece חי */
@Getter
@AllArgsConstructor
public class PendingRestSnapshot {
    private final PieceSnapshot piece;
    private final long endTime;
}
