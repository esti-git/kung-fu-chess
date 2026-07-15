package io;

import config.GameConfig;
import enums.PieceState;
import model.Board;
import model.Piece;
import model.Position;
import model.PendingMove;
import model.PendingJump;
import model.PendingRest;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardRenderer {

    private static final int DEFAULT_FPS = 6;
    private static final Pattern FPS_PATTERN = Pattern.compile("\"frames_per_sec\"\\s*:\\s*([0-9]+(\\.[0-9]+)?)");
    private static final Pattern IS_LOOP_PATTERN = Pattern.compile("\"is_loop\"\\s*:\\s*(true|false)");

    private final Board board;
    private final int cellSize;

    private final Map<String, Img> imageCache = new HashMap<>();
    private final Map<String, Boolean> warnedPaths = new HashMap<>();
    private final Map<String, Integer> frameCountCache = new HashMap<>();
    private final Map<String, Integer> fpsCache = new HashMap<>();
    private final Map<String, Boolean> loopCache = new HashMap<>();

    public BoardRenderer(Board board) {
        this.board = board;
        this.cellSize = GameConfig.CELL_SIZE;
    }

    public Img render(List<PendingMove> pendingMoves, List<PendingJump> pendingJumps, List<PendingRest> pendingRests, Position selectedPosition, long gameClock) {
        // יצירת אובייקט Img חדש
        Img canvas = new Img();

        // הגנה מפני קריסה: אם האובייקט הפנימי לא אותחל בבנאי הריק של Img, נדאג לוודא שיש לו תמונה ריקה בגודל הלוח
        ensureCanvasInitialized(canvas);

        // ציור הרקע (לוח המשבצות)
        drawGrid(canvas);

        Map<Integer, PendingRest> restByPieceId = new HashMap<>();
        for (PendingRest rest : pendingRests) {
            restByPieceId.put(rest.getPiece().getId(), rest);
        }

        // ציור הכלים על הלוח - כלי שנמצא באמצע תנועה/קפיצה מצויר בנפרד (למטה), לפי מיקומו המדויק/אנימציה
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Piece piece = board.getPieceAt(new Position(r, c));
                if (piece == null) continue;

                PendingRest rest = restByPieceId.get(piece.getId());
                if (rest != null) {
                    drawRestOverlay(canvas, c * cellSize, r * cellSize, piece, rest, gameClock);
                }

                String stateFolder = stateFolderFor(piece.getState());
                if (stateFolder != null) {
                    drawPieceAnimated(canvas, piece, c * cellSize, r * cellSize, stateFolder, gameClock);
                }
            }
        }

        // ציור כלים בתנועה
        for (PendingMove move : pendingMoves) {
            drawMovingPiece(canvas, move, gameClock);
        }

        // ציור כלים בקפיצה
        for (PendingJump jump : pendingJumps) {
            drawJumpingPiece(canvas, jump, gameClock);
        }

        // מסגרת סביב המשבצת שנבחרה - מצוירת אחרונה כדי שתהיה תמיד גלויה מעל הכל
        if (selectedPosition != null) {
            drawSelectionHighlight(canvas, selectedPosition);
        }

        return canvas;
    }

    /**
     * פונקציית הגנה שמוודאת של-Img יש BufferedImage תקני ולא null
     */
    private void ensureCanvasInitialized(Img canvas) {
        if (canvas.get() == null) {
            int width = board.getCols() * cellSize;
            int height = board.getRows() * cellSize;
            // צביעת רקע זמני כהה כדי שלא נראה מסך שקוף או לבן במקרה של תקלה
            canvas.blank(width, height, Color.DARK_GRAY);
        }
    }

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);

    private void drawGrid(Img canvas) {
        Graphics2D g = canvas.get().createGraphics();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                g.setColor((r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);
            }
        }
        g.dispose();
    }

    private static final Color REST_TINT = new Color(255, 196, 0);

    /**
     * מציג את הזמן שנותר במנוחה כמלבן מלא-צבע שגובהו קטן בהדרגה (כמו מד שמתרוקן כלפי מטה),
     * במקום לדהות את העוצמה של הצבע עצמו
     */
    private void drawRestOverlay(Img canvas, int x, int y, Piece piece, PendingRest rest, long gameClock) {
        long totalDuration = (piece.getState() == PieceState.LONG_REST)
                ? GameConfig.LONG_REST_DURATION_MS
                : GameConfig.SHORT_REST_DURATION_MS;
        double remainingFraction = totalDuration <= 0 ? 0.0
                : clamp01((rest.getEndTime() - gameClock) / (double) totalDuration);

        int fillHeight = (int) Math.round(remainingFraction * cellSize);
        if (fillHeight <= 0) return;

        Graphics2D g = canvas.get().createGraphics();
        g.setColor(REST_TINT);
        g.fillRect(x, y + (cellSize - fillHeight), cellSize, fillHeight);
        g.dispose();
    }

    private static final Color SELECTION_BORDER = new Color(255, 255, 0);
    private static final int SELECTION_BORDER_WIDTH = 4;

    private void drawSelectionHighlight(Img canvas, Position selectedPosition) {
        int x = selectedPosition.getCol() * cellSize;
        int y = selectedPosition.getRow() * cellSize;
        int inset = SELECTION_BORDER_WIDTH / 2;

        Graphics2D g = canvas.get().createGraphics();
        g.setColor(SELECTION_BORDER);
        g.setStroke(new BasicStroke(SELECTION_BORDER_WIDTH));
        g.drawRect(x + inset, y + inset, cellSize - SELECTION_BORDER_WIDTH, cellSize - SELECTION_BORDER_WIDTH);
        g.dispose();
    }

    /**
     * @param elapsedMs זמן (במילישניות) מאז תחילת מצב האנימציה הנוכחי של הכלי -
     *                  לכלי idle אפשר להעביר את שעון המשחק הכללי (לולאה מתמשכת),
     *                  ולכלי בתנועה/קפיצה יש להעביר את הזמן שחלף מאז תחילת הפעולה.
     */
    private void drawPieceAnimated(Img canvas, Piece piece, int x, int y, String stateFolder, long elapsedMs) {
        // נרמול שם התיקייה לפורמט של שתי אותיות גדולות, למשל: bB -> BB, wP -> PW
        String folderName = getNormalizedFolderName(piece.getRepresentation());

        int totalFrames = getFrameCount(folderName, stateFolder);
        int fps = getFramesPerSec(folderName, stateFolder);
        boolean loop = isLoop(folderName, stateFolder);
        int frameDelayMs = Math.max(1, Math.round(1000f / fps));

        long frameNumber = Math.max(0, elapsedMs) / frameDelayMs;
        int frameIndex;
        if (loop) {
            frameIndex = (int) (frameNumber % totalFrames) + 1;
        } else {
            frameIndex = (int) Math.min(frameNumber, totalFrames - 1) + 1;
        }

        String path = String.format("assets/%s/states/%s/sprites/%d.png",
                folderName, stateFolder, frameIndex);

        try {
            Img pieceImg = getCachedImage(path);
            if (pieceImg != null && pieceImg.get() != null) {
                pieceImg.drawOn(canvas, x, y);
            }
        } catch (Exception e) {
            if (!warnedPaths.containsKey(path)) {
                System.err.println("Could not load animation frame: " + path + " (" + e.getMessage() + ")");
                warnedPaths.put(path, true);
            }
            drawFallbackPiece(canvas, folderName, stateFolder, x, y);
        }
    }

    /**
     * ממפה מצב כלי לתיקיית האנימציה שלו על הלוח הראשי - MOVING/JUMPING מצוירים בנפרד (דרך drawMovingPiece/drawJumpingPiece)
     */
    private String stateFolderFor(PieceState state) {
        switch (state) {
            case IDLE: return "idle";
            case LONG_REST: return "long_rest";
            case SHORT_REST: return "short_rest";
            default: return null;
        }
    }

    /**
     * ממיר את קוד הכלי הלוגי לשם התיקייה הפיזית במערכת הקבצים שלכם (למשל BB לרץ שחור)
     */
    private String getNormalizedFolderName(String representation) {
        if (representation == null || representation.length() < 2) {
            return representation;
        }
        char color = representation.charAt(0); // 'w' או 'b'
        char type = representation.charAt(1);  // 'P', 'R', 'N', 'B', 'Q', 'K'
        
        // התאמה למבנה תיקיות כמו BB, QB, WP
        return (String.valueOf(type) + String.valueOf(color)).toUpperCase();
    }

    private void drawMovingPiece(Img canvas, PendingMove move, long gameClock) {
        int fromRow = move.getFromRow(), fromCol = move.getFromCol();
        int toRow = move.getToRow(), toCol = move.getToCol();

        int distance = Math.max(Math.abs(toRow - fromRow), Math.abs(toCol - fromCol));
        long duration = distance * GameConfig.MS_PER_CELL;
        long startTime = move.getArrivalTime() - duration;

        double t = duration <= 0 ? 1.0 : clamp01((gameClock - startTime) / (double) duration);
        double col = fromCol + (toCol - fromCol) * t;
        double row = fromRow + (toRow - fromRow) * t;

        long elapsedMs = gameClock - startTime;
        drawPieceAnimated(canvas, move.getPiece(),
                (int) Math.round(col * cellSize), (int) Math.round(row * cellSize),
                "move", elapsedMs);
    }

    private void drawJumpingPiece(Img canvas, PendingJump jump, long gameClock) {
        long duration = jump.getEndTime() - jump.getStartTime();
        double t = duration <= 0 ? 1.0 : clamp01((gameClock - jump.getStartTime()) / (double) duration);

        // קשת קלה כלפי מעלה כדי שהקפיצה תיראה כמו קפיצה ולא כמו החלפת פריים במקום
        int jumpHeight = cellSize / 2;
        int yOffset = (int) Math.round(-Math.sin(t * Math.PI) * jumpHeight);

        long elapsedMs = gameClock - jump.getStartTime();
        drawPieceAnimated(canvas, jump.getPiece(),
                jump.getCol() * cellSize, jump.getRow() * cellSize + yOffset,
                "jump", elapsedMs);
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private int getFrameCount(String folderName, String stateFolder) {
        String key = folderName + "/" + stateFolder;
        Integer cached = frameCountCache.get(key);
        if (cached != null) return cached;

        File spritesDir = new File(String.format("assets/%s/states/%s/sprites", folderName, stateFolder));
        File[] files = spritesDir.listFiles((dir, name) -> name.matches("\\d+\\.png"));
        int count = (files != null && files.length > 0) ? files.length : 1;

        frameCountCache.put(key, count);
        return count;
    }

    private int getFramesPerSec(String folderName, String stateFolder) {
        String key = folderName + "/" + stateFolder;
        Integer cached = fpsCache.get(key);
        if (cached != null) return cached;

        int fps = DEFAULT_FPS;
        String config = readStateConfig(folderName, stateFolder);
        if (config != null) {
            Matcher m = FPS_PATTERN.matcher(config);
            if (m.find()) {
                fps = (int) Math.round(Double.parseDouble(m.group(1)));
            }
        }
        if (fps <= 0) fps = DEFAULT_FPS;

        fpsCache.put(key, fps);
        return fps;
    }

    private boolean isLoop(String folderName, String stateFolder) {
        String key = folderName + "/" + stateFolder;
        Boolean cached = loopCache.get(key);
        if (cached != null) return cached;

        boolean loop = true;
        String config = readStateConfig(folderName, stateFolder);
        if (config != null) {
            Matcher m = IS_LOOP_PATTERN.matcher(config);
            if (m.find()) {
                loop = Boolean.parseBoolean(m.group(1));
            }
        }

        loopCache.put(key, loop);
        return loop;
    }

    private String readStateConfig(String folderName, String stateFolder) {
        File configFile = new File(String.format("assets/%s/states/%s/config.json", folderName, stateFolder));
        if (!configFile.exists()) return null;
        try {
            return new String(Files.readAllBytes(configFile.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    private Img getCachedImage(String path) throws Exception {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        Img img = new Img().read(path, new Dimension(cellSize, cellSize), true, null);
        if (img != null) {
            imageCache.put(path, img);
        }
        return img;
    }

    private void drawFallbackPiece(Img canvas, String folderName, String stateFolder, int x, int y) {
        String fallbackPath = String.format("assets/%s/states/%s/sprites/1.png", folderName, stateFolder);
        try {
            Img img = getCachedImage(fallbackPath);
            if (img != null && img.get() != null) {
                img.drawOn(canvas, x, y);
            }
        } catch (Exception ignored) {}
    }
}