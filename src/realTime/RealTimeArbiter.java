package realTime;

import config.GameConfig;
import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import model.Board;
import model.Piece;
import model.PendingJump;
import model.PendingMove;
import model.PendingRest;
import model.Position;
import rules.PawnPromotion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RealTimeArbiter {
    private final Board board;
    private final List<PendingMove> activeMoves = new ArrayList<>();
    private final List<PendingJump> activeJumps = new ArrayList<>();
    private final List<PendingRest> activeRests = new ArrayList<>();
    private long currentTimeMillis;
    private PieceColor winnerColor;

    public RealTimeArbiter(Board board) {
        this.board = board;
    }

    public long getCurrentTimeMillis() {
        return currentTimeMillis;
    }

    public List<PendingMove> getActiveMoves() {
        return activeMoves;
    }

    public List<PendingJump> getActiveJumps() {
        return activeJumps;
    }

    public List<PendingRest> getActiveRests() {
        return activeRests;
    }

    public PieceColor getWinnerColor() {
        return winnerColor;
    }

    /** מנקה תנועות/קפיצות/מנוחות פעילות - לשימוש כשמתחילים משחק חדש על אותו לוח */
    public void reset() {
        activeMoves.clear();
        activeJumps.clear();
        activeRests.clear();
        winnerColor = null;
    }

    private void beginRest(Piece piece, PieceState restState, long durationMs) {
        piece.setState(restState);
        activeRests.add(new PendingRest(piece, currentTimeMillis + durationMs));
    }

    public void startMove(Piece piece, Position source, Position destination) {
        int distance = Math.max(
                Math.abs(destination.getRow() - source.getRow()),
                Math.abs(destination.getCol() - source.getCol())
        );
        long arrivalTime = currentTimeMillis + (long) distance * GameConfig.MS_PER_CELL;
        activeMoves.add(new PendingMove(
                source.getRow(),
                source.getCol(),
                destination.getRow(),
                destination.getCol(),
                piece,
                arrivalTime
        ));
        piece.setState(PieceState.MOVING);

        if (board.getPieceAt(source) == piece) {
            board.clearCellOnly(source);
        }
    }

    public void startJump(Piece piece, Position position) {
        activeJumps.add(new PendingJump(position.getRow(), position.getCol(), piece, currentTimeMillis));
        piece.setState(PieceState.JUMPING);
    }

    public boolean advance(long milliseconds) {
        currentTimeMillis += milliseconds;
        boolean kingCaptured = false;

        while (true) {
            PendingMove nextMove = null;
            long nextMoveTime = Long.MAX_VALUE;
            for (PendingMove m : activeMoves) {
                if (m.getPiece().getState() != PieceState.CAPTURED && m.getArrivalTime() <= currentTimeMillis && m.getArrivalTime() < nextMoveTime) {
                    nextMove = m;
                    nextMoveTime = m.getArrivalTime();
                }
            }

            PendingJump nextJump = null;
            long nextJumpTime = Long.MAX_VALUE;
            for (PendingJump j : activeJumps) {
                if (j.getPiece().getState() != PieceState.CAPTURED && j.getEndTime() <= currentTimeMillis && j.getEndTime() < nextJumpTime) {
                    nextJump = j;
                    nextJumpTime = j.getEndTime();
                }
            }

            if (nextMove == null && nextJump == null) {
                break;
            }

            if (nextMove != null && nextJump != null && nextMoveTime == nextJumpTime) {
                if (processMove(nextMove)) kingCaptured = true;
                activeMoves.remove(nextMove);
            } else if (nextJumpTime <= nextMoveTime) {
                if (processJump(nextJump)) kingCaptured = true;
                activeJumps.remove(nextJump);
            } else {
                if (processMove(nextMove)) kingCaptured = true;
                activeMoves.remove(nextMove);
            }
        }

        activeMoves.removeIf(m -> m.getPiece().getState() == PieceState.CAPTURED);
        activeJumps.removeIf(j -> j.getPiece().getState() == PieceState.CAPTURED);

        Iterator<PendingRest> restIterator = activeRests.iterator();
        while (restIterator.hasNext()) {
            PendingRest rest = restIterator.next();
            if (rest.getPiece().getState() == PieceState.CAPTURED) {
                restIterator.remove();
            } else if (rest.getEndTime() <= currentTimeMillis) {
                rest.getPiece().setState(PieceState.IDLE);
                restIterator.remove();
            }
        }

        return kingCaptured;
    }

    private boolean processMove(PendingMove move) {
        Piece movingPiece = move.getPiece();
        if (movingPiece.getState() == PieceState.CAPTURED) return false;

        Position source = new Position(move.getFromRow(), move.getFromCol());
        Position destination = new Position(move.getToRow(), move.getToCol());

        Piece target = board.getPieceAt(destination);
        if (target != null && target.getColor() == movingPiece.getColor()) {
            board.addPiece(source, movingPiece);
            beginRest(movingPiece, PieceState.LONG_REST, GameConfig.LONG_REST_DURATION_MS);
            return false;
        }

        boolean kingCaptured = false;
        if (target != null) {
            if (target.getState() == PieceState.JUMPING) {
                // הכלי באוויר - לא נתפס עכשיו, רק מתפנה מהמשבצת עד שינחת; הוא זה שיתפוס את מי שינחת עליו
                board.clearCellOnly(destination);
            } else {
                board.removePiece(destination);
                if (target.getKind() == PieceKind.KING) {
                    kingCaptured = true;
                    winnerColor = movingPiece.getColor();
                }
            }
        }

        Piece promotedPiece = PawnPromotion.applyPromotion(movingPiece, destination.getRow(), board.getRows());
        board.addPiece(destination, promotedPiece);
        beginRest(promotedPiece, PieceState.LONG_REST, GameConfig.LONG_REST_DURATION_MS);

        return kingCaptured;
    }

    private boolean processJump(PendingJump jump) {
        Piece piece = jump.getPiece();
        if (piece.getState() == PieceState.CAPTURED) return false;

        Position jumpPosition = new Position(jump.getRow(), jump.getCol());
        Piece existingPiece = board.getPieceAt(jumpPosition);

        boolean kingCaptured = false;

        if (existingPiece == null) {
            board.addPiece(jumpPosition, piece);
        } else if (existingPiece == piece) {
            // הכלי לא הוסר מהלוח בזמן הקפיצה, אז הוא כבר נמצא במקום הנחיתה
        } else if (existingPiece.getColor() != piece.getColor()) {
            board.removePiece(jumpPosition);
            if (existingPiece.getKind() == PieceKind.KING) {
                kingCaptured = true;
                winnerColor = piece.getColor();
            }

            board.addPiece(jumpPosition, piece);
        } else {
            Position originalPos = piece.getCell();
            if (originalPos != null && board.getPieceAt(originalPos) == null) {
                board.addPiece(originalPos, piece);
            } else {
                Position backup = new Position(jump.getRow(), jump.getCol());
                if (board.getPieceAt(backup) == null) {
                    board.addPiece(backup, piece);
                }
            }
        }

        beginRest(piece, PieceState.SHORT_REST, GameConfig.SHORT_REST_DURATION_MS);
        return kingCaptured;
    }

    public boolean isPieceBusy(int pieceId) {
        return activeMoves.stream().anyMatch(move -> move.getPiece() != null && move.getPiece().getId() == pieceId)
                || activeJumps.stream().anyMatch(jump -> jump.getPiece() != null && jump.getPiece().getId() == pieceId);
    }

    public Set<Integer> activePieceIds() {
        Set<Integer> ids = new HashSet<>();
        activeMoves.forEach(move -> ids.add(move.getPiece().getId()));
        activeJumps.forEach(jump -> ids.add(jump.getPiece().getId()));
        return ids;
    }
}