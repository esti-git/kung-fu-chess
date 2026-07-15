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
import java.util.Random;
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
    private final Map<Boolean, Img> woodTileCache = new HashMap<>();

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
                    drawPieceShadow(canvas, toPixelX(c), toPixelY(r));
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

    private static final Color FRAME_COLOR = new Color(92, 51, 23);

    /**
     * פונקציית הגנה שמוודאת של-Img יש BufferedImage תקני ולא null, וצובעת מסביב מסגרת עץ עם מעט הבהקה (bevel)
     */
    private void ensureCanvasInitialized(Img canvas, int rows, int cols) {
        if (canvas.get() == null) {
            int width = cols * cellSize + LABEL_MARGIN * 2;
            int height = rows * cellSize + LABEL_MARGIN * 2;
            canvas.blank(width, height, FRAME_COLOR);
            drawFrameBevel(canvas, width, height);
        }
    }

    private void drawFrameBevel(Img canvas, int width, int height) {
        Graphics2D g = canvas.get().createGraphics();
        g.setStroke(new BasicStroke(2f));
        g.setColor(FRAME_COLOR.brighter());
        g.drawLine(1, 1, width - 2, 1);
        g.drawLine(1, 1, 1, height - 2);
        g.setColor(FRAME_COLOR.darker().darker());
        g.drawLine(width - 2, 1, width - 2, height - 2);
        g.drawLine(1, height - 2, width - 2, height - 2);
        g.dispose();
    }

    /** ממיר מיקום עמודה/שורה בלוח לקואורדינטת פיקסלים בקנבס, כולל היסט השוליים של התוויות */
    private int toPixelX(double col) {
        return LABEL_MARGIN + (int) Math.round(col * cellSize);
    }

    private int toPixelY(double row) {
        return LABEL_MARGIN + (int) Math.round(row * cellSize);
    }

    private static final Color LIGHT_WOOD_BASE = new Color(222, 184, 135);
    private static final Color DARK_WOOD_BASE = new Color(139, 94, 60);

    private void drawGrid(Img canvas, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean light = (r + c) % 2 == 0;
                getWoodTile(light).drawOn(canvas, toPixelX(c), toPixelY(r));
            }
        }
    }

    /**
     * מייצר (ומטמין במטמון) משבצת עץ עם גרדיאנט תאורה עדין וגרעיני עץ אקראיים אך קבועים (אותו seed בכל פעם) -
     * במקום צביעת מלבן שטוח
     */
    private Img getWoodTile(boolean light) {
        Img cached = woodTileCache.get(light);
        if (cached != null) return cached;

        Color base = light ? LIGHT_WOOD_BASE : DARK_WOOD_BASE;
        BufferedImage tile = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tile.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // גרדיאנט עדין מכיוון אור עליון-שמאלי, לתחושת עומק
        g.setPaint(new java.awt.GradientPaint(0, 0, base.brighter(), cellSize, cellSize, base.darker()));
        g.fillRect(0, 0, cellSize, cellSize);

        // גרעיני עץ - קווים דקים גליים, seed קבוע כדי שהמרקם לא ירצד בין פריימים
        Random random = new Random(light ? 1001L : 2002L);
        for (int i = 0; i < 14; i++) {
            int y = random.nextInt(cellSize);
            boolean brighten = random.nextBoolean();
            Color grain = brighten ? base.brighter() : base.darker();
            int alpha = 40 + random.nextInt(50);
            g.setColor(new Color(grain.getRed(), grain.getGreen(), grain.getBlue(), alpha));

            int waveHeight = 2 + random.nextInt(4);
            int segments = 6;
            int[] xs = new int[segments + 1];
            int[] ys = new int[segments + 1];
            for (int s = 0; s <= segments; s++) {
                xs[s] = s * cellSize / segments;
                ys[s] = y + (int) (Math.sin(s * 1.3 + i) * waveHeight);
            }
            for (int s = 0; s < segments; s++) {
                g.drawLine(xs[s], ys[s], xs[s + 1], ys[s + 1]);
            }
        }

        g.dispose();

        Img img = new Img().wrap(tile);
        woodTileCache.put(light, img);
        return img;
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

    /**
     * צל רך (מקורב על ידי כמה עיגולים חצי-שקופים בגדלים שונים) מתחת למקום בו הכלי יצויר -
     * נותן תחושת "הכלי עומד" על הלוח במקום לרחף שטוח מעליו
     */
    private void drawPieceShadow(Img canvas, int x, int y) {
        Graphics2D g = canvas.get().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int shadowWidth = (int) (cellSize * 0.6);
        int shadowHeight = (int) (cellSize * 0.18);
        int shadowX = x + (cellSize - shadowWidth) / 2;
        int shadowY = y + cellSize - shadowHeight - (int) (cellSize * 0.06);

        for (int i = 3; i >= 0; i--) {
            int alpha = Math.min(90, 18 * (4 - i));
            g.setColor(new Color(0, 0, 0, alpha));
            int inset = i * 3;
            g.fillOval(shadowX + inset, shadowY + inset / 2, shadowWidth - inset * 2, shadowHeight - inset);
        }

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
        int pixelX = toPixelX(col);
        int pixelY = toPixelY(row);
        drawPieceShadow(canvas, pixelX, pixelY);
        drawPieceAnimated(canvas, move.getPiece(), pixelX, pixelY, "move", elapsedMs);
    }

    private void drawJumpingPiece(Img canvas, PendingJumpSnapshot jump, long gameClock) {
        long duration = jump.getEndTime() - jump.getStartTime();
        double t = duration <= 0 ? 1.0 : clamp01((gameClock - jump.getStartTime()) / (double) duration);

        // קשת קלה כלפי מעלה כדי שהקפיצה תיראה כמו קפיצה ולא כמו החלפת פריים במקום
        int jumpHeight = cellSize / 2;
        int yOffset = (int) Math.round(-Math.sin(t * Math.PI) * jumpHeight);

        int groundX = toPixelX(jump.getCol());
        int groundY = toPixelY(jump.getRow());
        // הצל נשאר על הקרקע בזמן שהכלי קופץ מעליו - נותן תחושת קפיצה אמיתית
        drawPieceShadow(canvas, groundX, groundY);

        long elapsedMs = gameClock - jump.getStartTime();
        drawPieceAnimated(canvas, jump.getPiece(), groundX, groundY + yOffset, "jump", elapsedMs);
    }

    private double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    /** גרדיאנט תלת-נקודתי: הבהקה -> בסיס -> צל, לפי מרחק רדיאלי מנורמל (0=מרכז ההבהקה) */
    private Color shadeColor(Color highlight, Color base, Color shadow, double distance) {
        if (distance < 0.45) {
            return lerp(highlight, base, distance / 0.45);
        }
        return lerp(base, shadow, (distance - 0.45) / 0.85);
    }

    private Color lerp(Color a, Color b, double t) {
        t = clamp01(t);
        int r = (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
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
     * צובע מחדש את דמות הכלי לשחור/לבן (לפי האות האחרונה בשם התיקייה, למשל PW/PB) עם גרדיאנט רדיאלי
     * (הבהקה בפינה עליונה-שמאלית, החשכה כלפי הקצוות) לתחושת חומר מבריק/תלת-ממדי, במקום צבע שטוח אחיד -
     * שומר על ערוץ השקיפות המקורי (הצללית)
     */
    private static final int OUTLINE_WIDTH = 2;
    private static final int OUTLINE_ALPHA_THRESHOLD = 40; // מתעלם מפיקסלי אנטי-אליאסינג חלשים מדי כשבודקים "צורה אטומה"

    private void recolorPieceImage(Img img, String folderName) {
        if (folderName == null || folderName.isEmpty()) return;
        BufferedImage buffered = img.get();
        if (buffered == null) return;

        boolean isWhite = folderName.charAt(folderName.length() - 1) == 'W';
        Color highlight = isWhite ? Color.WHITE : new Color(95, 95, 95);
        Color base = isWhite ? new Color(230, 230, 235) : new Color(35, 35, 38);
        Color shadow = isWhite ? new Color(165, 165, 175) : Color.BLACK;
        // מסגרת בצבע ניגודי כדי שכלים לבנים לא "ייעלמו" על הלוח (ולהפך)
        Color outlineColor = isWhite ? new Color(40, 40, 40) : new Color(225, 225, 230);

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

        double highlightX = width * 0.32;
        double highlightY = height * 0.28;
        double radius = Math.max(width, height) * 0.75;

        int outlineArgb = outlineColor.getRGB() | 0xFF000000;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = originalArgb[y][x] >>> 24;
                if (alpha > 0) {
                    double dx = x - highlightX;
                    double dy = y - highlightY;
                    double dist = Math.sqrt(dx * dx + dy * dy) / radius;
                    int rgb = shadeColor(highlight, base, shadow, dist).getRGB() & 0x00FFFFFF;
                    buffered.setRGB(x, y, (alpha << 24) | rgb);
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
