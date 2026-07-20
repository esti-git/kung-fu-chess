package protocol;

import enums.PieceColor;
import enums.PieceKind;
import enums.PieceState;
import events.Event;
import events.GameEndedEvent;
import events.GameStartedEvent;
import events.MoveMadeEvent;
import events.PieceCapturedEvent;
import model.Position;
import org.json.JSONArray;
import org.json.JSONObject;
import view.BoardSnapshot;
import view.CaptureSnapshot;
import view.PendingJumpSnapshot;
import view.PendingMoveSnapshot;
import view.PendingRestSnapshot;
import view.PieceSnapshot;

import java.util.ArrayList;
import java.util.List;

/** JSON wire format for broadcasting {@link BoardSnapshot} from the server to thin clients. */
public class StateCodec {

    public static String encodeState(BoardSnapshot snapshot, boolean gameOver, PieceColor winnerColor) {
        JSONObject root = new JSONObject();
        root.put("type", "state");
        root.put("rows", snapshot.getRows());
        root.put("cols", snapshot.getCols());
        root.put("gameClock", snapshot.getGameClock());
        root.put("gameOver", gameOver);
        root.put("winnerColor", winnerColor == null ? JSONObject.NULL : winnerColor.name());

        JSONArray cells = new JSONArray();
        for (int r = 0; r < snapshot.getRows(); r++) {
            JSONArray rowArray = new JSONArray();
            for (int c = 0; c < snapshot.getCols(); c++) {
                PieceSnapshot piece = snapshot.getPieceAt(r, c);
                rowArray.put(piece == null ? JSONObject.NULL : encodePiece(piece));
            }
            cells.put(rowArray);
        }
        root.put("cells", cells);

        JSONArray moves = new JSONArray();
        for (PendingMoveSnapshot move : snapshot.getPendingMoves()) {
            JSONObject m = new JSONObject();
            m.put("fromRow", move.getFromRow());
            m.put("fromCol", move.getFromCol());
            m.put("toRow", move.getToRow());
            m.put("toCol", move.getToCol());
            m.put("arrivalTime", move.getArrivalTime());
            m.put("piece", encodePiece(move.getPiece()));
            moves.put(m);
        }
        root.put("pendingMoves", moves);

        JSONArray jumps = new JSONArray();
        for (PendingJumpSnapshot jump : snapshot.getPendingJumps()) {
            JSONObject j = new JSONObject();
            j.put("row", jump.getRow());
            j.put("col", jump.getCol());
            j.put("startTime", jump.getStartTime());
            j.put("endTime", jump.getEndTime());
            j.put("piece", encodePiece(jump.getPiece()));
            jumps.put(j);
        }
        root.put("pendingJumps", jumps);

        JSONArray rests = new JSONArray();
        for (PendingRestSnapshot rest : snapshot.getPendingRests()) {
            JSONObject r = new JSONObject();
            r.put("endTime", rest.getEndTime());
            r.put("piece", encodePiece(rest.getPiece()));
            rests.put(r);
        }
        root.put("pendingRests", rests);

        JSONArray captures = new JSONArray();
        for (CaptureSnapshot capture : snapshot.getCaptureLog()) {
            JSONObject c = new JSONObject();
            c.put("color", capture.getCapturedColor().name());
            c.put("kind", capture.getCapturedKind().name());
            captures.put(c);
        }
        root.put("captureLog", captures);

        return root.toString();
    }

    public static String encodeEvent(Event event) {
        JSONObject root = new JSONObject();
        root.put("type", "event");
        root.put("eventType", event.getType());

        switch (event.getType()) {
            case MoveMadeEvent.TYPE -> {
                MoveMadeEvent e = (MoveMadeEvent) event;
                root.put("player", e.getPlayer().name());
                root.put("pieceKind", e.getPieceKind().name());
                root.put("pieceRepresentation", e.getPieceRepresentation());
                root.put("fromRow", e.getFrom().getRow());
                root.put("fromCol", e.getFrom().getCol());
                root.put("toRow", e.getTo().getRow());
                root.put("toCol", e.getTo().getCol());
                root.put("boardRows", e.getBoardRows());
            }
            case PieceCapturedEvent.TYPE -> {
                PieceCapturedEvent e = (PieceCapturedEvent) event;
                root.put("capturedColor", e.getCapturedColor().name());
                root.put("capturedKind", e.getCapturedKind().name());
                root.put("capturedBy", e.getCapturedBy().name());
            }
            case GameEndedEvent.TYPE -> {
                GameEndedEvent e = (GameEndedEvent) event;
                root.put("winnerColor", e.getWinnerColor() == null ? JSONObject.NULL : e.getWinnerColor().name());
            }
            case GameStartedEvent.TYPE -> { /* no fields */ }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getType());
        }

        return root.toString();
    }

    public static Event decodeEvent(String rawJson) {
        JSONObject root = new JSONObject(rawJson);
        String eventType = root.getString("eventType");

        switch (eventType) {
            case MoveMadeEvent.TYPE:
                return new MoveMadeEvent(
                        PieceColor.valueOf(root.getString("player")),
                        PieceKind.valueOf(root.getString("pieceKind")),
                        root.getString("pieceRepresentation"),
                        new Position(root.getInt("fromRow"), root.getInt("fromCol")),
                        new Position(root.getInt("toRow"), root.getInt("toCol")),
                        root.getInt("boardRows"));
            case PieceCapturedEvent.TYPE:
                return new PieceCapturedEvent(
                        PieceColor.valueOf(root.getString("capturedColor")),
                        PieceKind.valueOf(root.getString("capturedKind")),
                        PieceColor.valueOf(root.getString("capturedBy")));
            case GameStartedEvent.TYPE:
                return new GameStartedEvent();
            case GameEndedEvent.TYPE:
                PieceColor winnerColor = root.isNull("winnerColor") ? null : PieceColor.valueOf(root.getString("winnerColor"));
                return new GameEndedEvent(winnerColor);
            default:
                throw new IllegalArgumentException("Unknown eventType: " + eventType);
        }
    }

    public static String encodeError(String message) {
        JSONObject root = new JSONObject();
        root.put("type", "error");
        root.put("message", message);
        return root.toString();
    }

    public static String peekType(String rawJson) {
        try {
            return new JSONObject(rawJson).optString("type", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decodeErrorMessage(String rawJson) {
        return new JSONObject(rawJson).optString("message", "");
    }

    public static NetworkState decodeState(String rawJson) {
        JSONObject root = new JSONObject(rawJson);
        int rows = root.getInt("rows");
        int cols = root.getInt("cols");
        long gameClock = root.getLong("gameClock");
        boolean gameOver = root.getBoolean("gameOver");
        PieceColor winnerColor = root.isNull("winnerColor") ? null : PieceColor.valueOf(root.getString("winnerColor"));

        PieceSnapshot[][] cells = new PieceSnapshot[rows][cols];
        JSONArray cellsArray = root.getJSONArray("cells");
        for (int r = 0; r < rows; r++) {
            JSONArray rowArray = cellsArray.getJSONArray(r);
            for (int c = 0; c < cols; c++) {
                Object entry = rowArray.get(c);
                if (entry instanceof JSONObject) {
                    cells[r][c] = decodePiece((JSONObject) entry);
                }
            }
        }

        List<PendingMoveSnapshot> moves = new ArrayList<>();
        for (Object o : root.getJSONArray("pendingMoves")) {
            JSONObject m = (JSONObject) o;
            moves.add(new PendingMoveSnapshot(
                    m.getInt("fromRow"), m.getInt("fromCol"), m.getInt("toRow"), m.getInt("toCol"),
                    decodePiece(m.getJSONObject("piece")), m.getLong("arrivalTime")));
        }

        List<PendingJumpSnapshot> jumps = new ArrayList<>();
        for (Object o : root.getJSONArray("pendingJumps")) {
            JSONObject j = (JSONObject) o;
            jumps.add(new PendingJumpSnapshot(
                    j.getInt("row"), j.getInt("col"), decodePiece(j.getJSONObject("piece")),
                    j.getLong("startTime"), j.getLong("endTime")));
        }

        List<PendingRestSnapshot> rests = new ArrayList<>();
        for (Object o : root.getJSONArray("pendingRests")) {
            JSONObject r = (JSONObject) o;
            rests.add(new PendingRestSnapshot(decodePiece(r.getJSONObject("piece")), r.getLong("endTime")));
        }

        List<CaptureSnapshot> captures = new ArrayList<>();
        for (Object o : root.getJSONArray("captureLog")) {
            JSONObject c = (JSONObject) o;
            captures.add(new CaptureSnapshot(PieceColor.valueOf(c.getString("color")), PieceKind.valueOf(c.getString("kind"))));
        }

        BoardSnapshot snapshot = new BoardSnapshot(rows, cols, cells, moves, jumps, rests, captures, gameClock);
        return new NetworkState(snapshot, gameOver, winnerColor);
    }

    private static JSONObject encodePiece(PieceSnapshot piece) {
        JSONObject p = new JSONObject();
        p.put("id", piece.getId());
        p.put("color", piece.getColor().name());
        p.put("kind", piece.getKind().name());
        p.put("representation", piece.getRepresentation());
        p.put("state", piece.getState().name());
        return p;
    }

    private static PieceSnapshot decodePiece(JSONObject p) {
        return new PieceSnapshot(
                p.getInt("id"),
                PieceColor.valueOf(p.getString("color")),
                PieceKind.valueOf(p.getString("kind")),
                p.getString("representation"),
                PieceState.valueOf(p.getString("state")));
    }
}
