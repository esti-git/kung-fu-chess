package view;

import config.GameConfig;
import enums.PieceState;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardRenderer {

    private static final int DEFAULT_FPS = 6;
    private static final Pattern FPS_PATTERN = Pattern.compile("\"frames_per_sec\"\\s*:\\s*([0-9]+(\\.[0-9]+)?)");
    private static final Pattern IS_LOOP_PATTERN = Pattern.compile("\"is_loop\"\\s*:\\s*(true|false)");
    private static final int LABEL_MARGIN = GameConfig.BOARD_LABEL_MARGIN; // רצועת שוליים סביב הלוח לאותיות ומספרים - חייב להתאים ל-BoardMapper

    private final int cellSize;

    private final Map<String, Img> imageCache = new HashMap<>();
    private final Map<String, Boolean> warnedPaths = new HashMap<>();
    private final Map<String, Integer> frameCountCache = new HashMap<>();
    private final Map<String, Integer> fpsCache = new HashMap<>();
    private final Map<String, Boolean> loopCache = new HashMap<>();

    public BoardRenderer() {
        this.cellSize = GameConfig.CELL_SIZE;
    }

    public Img render(BoardSnapshot snapshot, model.Position selectedPosition) {
        int rows = snapshot.getRows();
        int cols = snapshot.getCols();
        long gameClock = snapshot.getGameClock();

        // יצירת אובייקט Img חדש
        Img canvas = new Img();

        // הגנה מפני קריסה: אם האובייקט הפנימי לא אותחל בבנאי הריק של Img, נדאג לוודא שיש לו תמונה ריקה בגודל הלוח
        ensureCanvasInitialized(canvas, rows, cols);

        // ציור הרקע (לוח המשבצות + אותיות/מספרים מסביב)
        drawGrid(canvas, rows, cols);
        drawBoardLabels(canvas, rows, cols);

        Map<Integer, PendingRestSnapshot> restByPieceId = new HashMap<>();
        for (PendingRestSnapshot rest : snapshot.getPendingRests()) {
            restByPieceId.put(rest.getPiece().getId(), rest);
        }

        // ציור הכלים על הלוח - כלי שנמצא באמצע תנועה/קפיצה מצויר בנפרד (למטה), לפי מיקומו המדויק/אנימציה
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                PieceSnapshot piece = snapshot.getPieceAt(r, c);
                if (piece == null) continue;

                PendingRestSnapshot rest = restByPieceId.get(piece.getId());
                if (rest != null) {
                    drawRestOverlay(canvas, toPixelX(c), toPixelY(r), piece, rest, gameClock);
                }

                String stateFolder = stateFolderFor(piece.getState());
                if (stateFolder != null) {
                    drawPieceAnimated(canvas, piece, toPixelX(c), toPixelY(r), stateFolder, gameClock);
                }
            }
        }

        // ציור כלים בתנועה
        for (PendingMoveSnapshot move : snapshot.getPendingMoves()) {
            drawMovingPiece(canvas, move, gameClock);
        }

        // ציור כלים בקפיצה
        for (PendingJumpSnapshot jump : snapshot.getPendingJumps()) {
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
    private void ensureCanvasInitialized(Img canvas, int rows, int cols) {
        if (canvas.get() == null) {
            int width = cols * cellSize + LABEL_MARGIN * 2;
            int height = rows * cellSize + LABEL_MARGIN * 2;
            // צביעת רקע זמני כהה כדי שלא נראה מסך שקוף או לבן במקרה של תקלה
            canvas.blank(width, height, Color.DARK_GRAY);
        }
    }

    /** ממיר מיקום עמודה/שורה בלוח לקואורדינטת פיקסלים בקנבס, כולל היסט השוליים של התוויות */
    private int toPixelX(double col) {
        return LABEL_MARGIN + (int) Math.round(col * cellSize);
    }

    private int toPixelY(double row) {
        return LABEL_MARGIN + (int) Math.round(row * cellSize);
    }

    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);

    private void drawGrid(Img canvas, int rows, int cols) {
        Graphics2D g = canvas.get().createGraphics();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                g.setColor((r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                g.fillRect(toPixelX(c), toPixelY(r), cellSize, cellSize);
            }
        }
        g.dispose();
    }

    private static final Color LABEL_COLOR = new Color(50, 50, 50);

    /** מצייר אותיות עמודות (a-h) למעלה ולמטה, ומספרי שורות (1-8) בצדדים - מסביב ללוח */
    private void drawBoardLabels(Img canvas, int rows, int cols) {
        Graphics2D g = canvas.get().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(LABEL_COLOR);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 16f));
        FontMetrics metrics = g.getFontMetrics();

        int boardPixelWidth = cols * cellSize;
        int boardPixelHeight = rows * cellSize;

        for (int c = 0; c < cols; c++) {
            String file = String.valueOf((char) ('a' + c));
            int textX = toPixelX(c) + (cellSize - metrics.stringWidth(file)) / 2;
            g.drawString(file, textX, LABEL_MARGIN - 8);
            g.drawString(file, textX, LABEL_MARGIN + boardPixelHeight + metrics.getAscent() + 4);
        }

        for (int r = 0; r < rows; r++) {
            String rank = String.valueOf(rows - r);
            int textWidth = metrics.stringWidth(rank);
            int textY = toPixelY(r) + (cellSize + metrics.getAscent()) / 2 - 2;
            g.drawString(rank, (LABEL_MARGIN - textWidth) / 2, textY);
            g.drawString(rank, LABEL_MARGIN + boardPixelWidth + (LABEL_MARGIN - textWidth) / 2, textY);
        }

        g.dispose();
    }

    private static final Color REST_TINT = new Color(255, 196, 0);

    /**
     * מציג את הזמן שנותר במנוחה כמלבן מלא-צבע שגובהו קטן בהדרגה (כמו מד שמתרוקן כלפי מטה),
     * במקום לדהות את העוצמה של הצבע עצמו
     */
    private void drawRestOverlay(Img canvas, int x, int y, PieceSnapshot piece, PendingRestSnapshot rest, long gameClock) {
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

    private void drawSelectionHighlight(Img canvas, model.Position selectedPosition) {
        int x = toPixelX(selectedPosition.getCol());
        int y = toPixelY(selectedPosition.getRow());
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
    private void drawPieceAnimated(Img canvas, PieceSnapshot piece, int x, int y, String stateFolder, long elapsedMs) {
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
            Img pieceImg = getCachedImage(path, folderName);
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

    private void drawMovingPiece(Img canvas, PendingMoveSnapshot move, long gameClock) {
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
                toPixelX(col), toPixelY(row),
                "move", elapsedMs);
    }

    private void drawJumpingPiece(Img canvas, PendingJumpSnapshot jump, long gameClock) {
        long duration = jump.getEndTime() - jump.getStartTime();
        double t = duration <= 0 ? 1.0 : clamp01((gameClock - jump.getStartTime()) / (double) duration);

        // קשת קלה כלפי מעלה כדי שהקפיצה תיראה כמו קפיצה ולא כמו החלפת פריים במקום
        int jumpHeight = cellSize / 2;
        int yOffset = (int) Math.round(-Math.sin(t * Math.PI) * jumpHeight);

        long elapsedMs = gameClock - jump.getStartTime();
        drawPieceAnimated(canvas, jump.getPiece(),
                toPixelX(jump.getCol()), toPixelY(jump.getRow()) + yOffset,
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

    private Img getCachedImage(String path, String folderName) throws Exception {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        Img img = new Img().read(path, new Dimension(cellSize, cellSize), true, null);
        if (img != null) {
            recolorPieceImage(img, folderName);
            imageCache.put(path, img);
        }
        return img;
    }

    /**
     * צובע מחדש את דמות הכלי לשחור/לבן (לפי האות האחרונה בשם התיקייה, למשל PW/PB) -
     * שומר על ערוץ השקיפות המקורי (הצללית) אבל מחליף את הצבע עצמו לצבע אחיד
     */
    private static final int OUTLINE_WIDTH = 2;
    private static final int OUTLINE_ALPHA_THRESHOLD = 40; // מתעלם מפיקסלי אנטי-אליאסינג חלשים מדי כשבודקים "צורה אטומה"

    private void recolorPieceImage(Img img, String folderName) {
        if (folderName == null || folderName.isEmpty()) return;
        BufferedImage buffered = img.get();
        if (buffered == null) return;

        boolean isWhite = folderName.charAt(folderName.length() - 1) == 'W';
        Color fillColor = isWhite ? Color.WHITE : Color.BLACK;
        // מסגרת בצבע ניגודי כדי שכלים לבנים לא "ייעלמו" על משבצות בהירות (ולהפך)
        Color outlineColor = isWhite ? Color.BLACK : new Color(230, 230, 230);

        int width = buffered.getWidth();
        int height = buffered.getHeight();

        boolean[][] isOpaque = new boolean[height][width];
        int[][] originalArgb = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = buffered.getRGB(x, y);
                originalArgb[y][x] = argb;
                isOpaque[y][x] = (argb >>> 24) > OUTLINE_ALPHA_THRESHOLD;
            }
        }

        int fillRgb = fillColor.getRGB() & 0x00FFFFFF;
        int outlineArgb = outlineColor.getRGB() | 0xFF000000;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = originalArgb[y][x] >>> 24;
                if (alpha > 0) {
                    buffered.setRGB(x, y, (alpha << 24) | fillRgb);
                } else if (isNearOpaquePixel(isOpaque, x, y, width, height)) {
                    buffered.setRGB(x, y, outlineArgb);
                }
            }
        }
    }

    private boolean isNearOpaquePixel(boolean[][] isOpaque, int cx, int cy, int width, int height) {
        for (int dy = -OUTLINE_WIDTH; dy <= OUTLINE_WIDTH; dy++) {
            int ny = cy + dy;
            if (ny < 0 || ny >= height) continue;
            for (int dx = -OUTLINE_WIDTH; dx <= OUTLINE_WIDTH; dx++) {
                int nx = cx + dx;
                if (nx < 0 || nx >= width) continue;
                if (dx * dx + dy * dy > OUTLINE_WIDTH * OUTLINE_WIDTH) continue;
                if (isOpaque[ny][nx]) return true;
            }
        }
        return false;
    }

    private void drawFallbackPiece(Img canvas, String folderName, String stateFolder, int x, int y) {
        String fallbackPath = String.format("assets/%s/states/%s/sprites/1.png", folderName, stateFolder);
        try {
            Img img = getCachedImage(fallbackPath, folderName);
            if (img != null && img.get() != null) {
                img.drawOn(canvas, x, y);
            }
        } catch (Exception ignored) {}
    }
}
