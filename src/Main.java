import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // כאן אנחנו מגדירים את הלוח דרך הממשק הכללי, ומאתחלים אותו כ-MatrixBoard
    static Board board = new MatrixBoard();

    static int selectedRow = -1;
    static int selectedCol = -1;

    static long gameClock = 0;

    static List<PendingMove> pendingMoves = new ArrayList<>();
    static List<PendingJump> pendingJumps = new ArrayList<>();

    static boolean isGameOver = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> rawBoardLines = new ArrayList<>();
        boolean readingBoard = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            if (line.equals("Board:")) {
                readingBoard = true;
                continue;
            }

            if (line.equals("Commands:")) {
                readingBoard = false;
                if (!board.validateAndInitialize(rawBoardLines)) return;
                continue;
            }

            if (readingBoard) {
                rawBoardLines.add(line);
            } else {
                processCommand(line);
            }
        }
        scanner.close();
    }

    private static void processCommand(String commandLine) {
        String[] parts = commandLine.split(" +");
        String commandType = parts[0];

        if (isGameOver && (commandType.equals("click") || commandType.equals("wait") || commandType.equals("jump"))) {
            return;
        }

        switch (commandType) {
            case "click":
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                handleClick(x, y);
                break;

            case "jump":
                int jx = Integer.parseInt(parts[1]);
                int jy = Integer.parseInt(parts[2]);
                handleJumpCommand(jx, jy);
                break;

            case "wait":
                int ms = Integer.parseInt(parts[1]);
                gameClock += ms;
                updateBoardPositions();
                break;

            case "print":
                if (parts.length > 1 && parts[1].equals("board")) {
                    board.print();
                }
                break;
        }
    }

    private static void handleClick(int x, int y) {
        int clickedCol = x / 100;
        int clickedRow = y / 100;

        if (clickedRow < 0 || clickedRow >= board.getRows() || clickedCol < 0 || clickedCol >= board.getCols()) {
            return;
        }

        Piece currentSquarePiece = board.getPieceAt(clickedRow, clickedCol);

        if (hasSelection() && selectedRow == clickedRow && selectedCol == clickedCol) {
            if (canPieceJump(clickedRow, clickedCol)) {
                pendingJumps.add(new PendingJump(clickedRow, clickedCol, board.getPieceAt(clickedRow, clickedCol), gameClock));
                board.setPieceAt(clickedRow, clickedCol, null);
            }
            clearSelection();
            return;
        }

        if (currentSquarePiece != null) {
            char currentPieceColor = currentSquarePiece.getColor();

            if (hasSelection() && getSelectedPieceColor() == currentPieceColor) {
                selectedRow = clickedRow;
                selectedCol = clickedCol;
            }
            else if (hasSelection()) {
                if (isMoveLegal(selectedRow, selectedCol, clickedRow, clickedCol)) {
                    executeMove(selectedRow, selectedCol, clickedRow, clickedCol);
                } else {
                    clearSelection();
                }
            }
            else {
                selectedRow = clickedRow;
                selectedCol = clickedCol;
            }
        }
        else {
            if (hasSelection()) {
                if (isMoveLegal(selectedRow, selectedCol, clickedRow, clickedCol)) {
                    executeMove(selectedRow, selectedCol, clickedRow, clickedCol);
                } else {
                    clearSelection();
                }
            }
        }
    }

    private static void handleJumpCommand(int x, int y) {
        int clickedCol = x / 100;
        int clickedRow = y / 100;

        if (clickedRow < 0 || clickedRow >= board.getRows() || clickedCol < 0 || clickedCol >= board.getCols()) {
            return;
        }

        Piece currentPiece = board.getPieceAt(clickedRow, clickedCol);
        if (currentPiece != null) {
            if (canPieceJump(clickedRow, clickedCol)) {
                pendingJumps.add(new PendingJump(clickedRow, clickedCol, currentPiece, gameClock));
                board.setPieceAt(clickedRow, clickedCol, null);
            }
        }
        clearSelection();
    }

    private static boolean canPieceJump(int r, int c) {
        for (PendingMove move : pendingMoves) {
            if (move.fromRow == r && move.fromCol == c) return false;
        }
        for (PendingJump jump : pendingJumps) {
            if (jump.row == r && jump.col == c) return false;
        }
        return true;
    }

    private static void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.getPieceAt(fromRow, fromCol);
        long duration = 1000L;
        long arrivalTime = gameClock + duration;

        pendingMoves.add(new PendingMove(fromRow, fromCol, toRow, toCol, piece, arrivalTime));
        clearSelection();
    }

    private static boolean hasSelection() {
        return selectedRow != -1 && selectedCol != -1;
    }

    private static char getSelectedPieceColor() {
        return board.getPieceAt(selectedRow, selectedCol).getColor();
    }

    private static void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
    }

    private static boolean isMoveLegal(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow && fromCol == toCol) return false;

        for (PendingJump jump : pendingJumps) {
            if (jump.row == fromRow && jump.col == fromCol) return false;
        }

        Piece movingPiece = board.getPieceAt(fromRow, fromCol);
        char movingColor = movingPiece.getColor();

        for (PendingJump jump : pendingJumps) {
            if (jump.row == toRow && jump.col == toCol) {
                if (jump.piece.getColor() == movingColor) return false;
            }
        }

        for (PendingMove move : pendingMoves) {
            if (move.piece.getColor() != movingColor) return false;
        }

        for (PendingMove move : pendingMoves) {
            if (move.fromRow == fromRow && move.fromCol == fromCol) return false;
        }

        for (PendingMove move : pendingMoves) {
            if (move.toRow == toRow && move.toCol == toCol) return false;
        }

        Piece destinationPiece = board.getPieceAt(toRow, toCol);

        if (destinationPiece != null) {
            if (movingColor == destinationPiece.getColor()) return false;
        }

        // קריאה לפולימורפיזם של הכלי
        if (!movingPiece.isMovementPatternLegal(fromRow, fromCol, toRow, toCol, board.getRows())) {
            return false;
        }

        char pieceType = movingPiece.getType();
        if (pieceType == 'R' || pieceType == 'B' || pieceType == 'Q') {
            return isPathClear(fromRow, fromCol, toRow, toCol);
        }

        if (pieceType == 'P') {
            int deltaCol = Math.abs(toCol - fromCol);
            int expectedRowDirection = (movingColor == 'w') ? -1 : 1;
            int actualRowDirection = toRow - fromRow;

            if (deltaCol == 0) {
                if (actualRowDirection == expectedRowDirection) {
                    return board.isEmpty(toRow, toCol);
                }
                if (actualRowDirection == expectedRowDirection * 2) {
                    int middleRow = fromRow + expectedRowDirection;
                    if (!board.isEmpty(middleRow, fromCol) || !board.isEmpty(toRow, toCol)) {
                        return false;
                    }
                    for (PendingMove move : pendingMoves) {
                        if ((move.toRow == middleRow && move.toCol == fromCol) ||
                                (move.toRow == toRow && move.toCol == toCol)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            if (deltaCol == 1) {
                return destinationPiece != null;
            }
        }

        return true;
    }

    private static boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int stepRow = Integer.compare(toRow, fromRow);
        int stepCol = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + stepRow;
        int currentCol = fromCol + stepCol;

        while (currentRow != toRow || currentCol != toCol) {
            if (!board.isEmpty(currentRow, currentCol)) return false;

            for (PendingMove move : pendingMoves) {
                if (move.toRow == currentRow && move.toCol == currentCol) return false;
            }
            currentRow += stepRow;
            currentCol += stepCol;
        }
        return true;
    }

    private static void updateBoardPositions() {
        List<PendingMove> completedMoves = new ArrayList<>();
        for (PendingMove move : pendingMoves) {
            if (gameClock >= move.arrivalTime) {
                completedMoves.add(move);
            }
        }

        for (PendingMove move : completedMoves) {
            board.setPieceAt(move.fromRow, move.fromCol, null);
        }

        for (PendingMove move : completedMoves) {
            boolean capturedByAirborne = false;

            for (PendingJump jump : pendingJumps) {
                if (move.arrivalTime >= jump.startTime && move.arrivalTime <= jump.endTime) {
                    if (jump.row == move.toRow && jump.col == move.toCol) {
                        if (jump.piece.getColor() != move.piece.getColor()) {
                            capturedByAirborne = true;
                            break;
                        }
                    }
                }
            }

            if (capturedByAirborne) {
                if (move.piece.getType() == 'K') isGameOver = true;
                continue;
            }

            Piece target = board.getPieceAt(move.toRow, move.toCol);
            if (target != null && target.getType() == 'K') {
                isGameOver = true;
            }

            Piece finalPiece = move.piece;
            if (finalPiece.getType() == 'P') {
                int promotionRow = (finalPiece.getColor() == 'w') ? 0 : (board.getRows() - 1);
                if (move.toRow == promotionRow) {
                    // הכתרה גנרית ופולימורפית: מייצרים אובייקט מסוג Queen
                    finalPiece = new Queen(finalPiece.getColor());
                }
            }

            board.setPieceAt(move.toRow, move.toCol, finalPiece);
        }

        pendingMoves.removeAll(completedMoves);

        List<PendingJump> completedJumps = new ArrayList<>();
        for (PendingJump jump : pendingJumps) {
            if (gameClock >= jump.endTime) {
                completedJumps.add(jump);
                if (board.isEmpty(jump.row, jump.col)) {
                    board.setPieceAt(jump.row, jump.col, jump.piece);
                }
            }
        }
        pendingJumps.removeAll(completedJumps);
    }
}