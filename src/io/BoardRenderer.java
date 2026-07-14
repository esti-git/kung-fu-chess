package io;

import config.GameConfig;
import enums.PieceState;
import model.Board;
import model.Piece;
import model.Position;
import model.PendingMove;
import model.PendingJump;
import java.awt.*;
import java.util.List;

public class BoardRenderer {

    private final Board board;
    private final String boardBgPath = "assets/board.png"; // נתיב תמונת רקע הלוח שלך

    public BoardRenderer(Board board) {
        this.board = board;
    }

    public Img render(List<PendingMove> pendingMoves, List<PendingJump> pendingJumps, long currentTime) {
        // 1. טעינת רקע הלוח בגודלו המלא
        Img canvas = new Img().read(boardBgPath);

        // גודל משבצת מתוך הקונפיגורציה
        int cellSize = GameConfig.CELL_SIZE; 

        // 2. ציור הכלים הנייחים שנמצאים על הלוח (במצב IDLE)
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece != null && piece.getState() == PieceState.IDLE) {
                    drawPieceAtPixel(canvas, piece, c * cellSize, r * cellSize, cellSize);
                }
            }
        }

        // 3. ציור כלים שנמצאים כרגע בתנועה (PendingMove) במיקומם היחסי בזמן אמת
        for (PendingMove move : pendingMoves) {
            Piece piece = move.getPiece();
            // חישוב התקדמות התנועה (בין 0.0 ל-1.0)
            int distance = Math.max(Math.abs(move.getToRow() - move.getFromRow()), Math.abs(move.getToCol() - move.getFromCol()));
            long duration = (long) distance * GameConfig.MS_PER_CELL;
            long startTime = move.getArrivalTime() - duration;
            
            double progress = 0.0;
            if (duration > 0) {
                progress = (double) (currentTime - startTime) / duration;
                progress = Math.min(1.0, Math.max(0.0, progress));
            }

            // חישוב מיקום נוכחי בפיקסלים (אינטרפולציה ליניארית)
            double startX = move.getFromCol() * cellSize;
            double startY = move.getFromRow() * cellSize;
            double endX = move.getToCol() * cellSize;
            double endY = move.getToRow() * cellSize;

            int currentX = (int) Math.round(startX + (endX - startX) * progress);
            int currentY = (int) Math.round(startY + (endY - startY) * progress);

            drawPieceAtPixel(canvas, piece, currentX, currentY, cellSize);
        }

        // 4. ציור כלים שנמצאים כרגע בקפיצה (PendingJump)
        for (PendingJump jump : pendingJumps) {
            Piece piece = jump.getPiece();
            // הכלים האלו נמצאים באוויר מעל המשבצת שלהם
            int x = jump.getCol() * cellSize;
            int y = jump.getRow() * cellSize;
            drawPieceAtPixel(canvas, piece, x, y, cellSize);
        }

        return canvas;
    }

    private void drawPieceAtPixel(Img canvas, Piece piece, int x, int y, int cellSize) {
        try {
            String path = AssetPathResolver.getPieceImagePath(piece);
            // קריאת תמונת הכלי והתאמת גודלה לגודל משבצת הלוח
            Img pieceImg = new Img().read(path, new Dimension(cellSize, cellSize), true, null);
            // ציור הכלי על גבי הקנבס
            pieceImg.drawOn(canvas, x, y);
        } catch (Exception e) {
            // במקרה שחסרה תמונה, נמנע מקריסה ונרשום אזהרה
            System.err.println("Warning: Could not load image for piece " + piece.getRepresentation() + " - " + e.getMessage());
        }
    }
}