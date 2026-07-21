package view;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** תמונת מצב קפואה של תנועה פעילה - כמו model.PendingMove, אבל עם PieceSnapshot במקום Piece חי */
@Getter
@AllArgsConstructor
public class PendingMoveSnapshot {
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;
    private final PieceSnapshot piece;
    private final long arrivalTime;
}
