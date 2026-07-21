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
    private static final int LABEL_MARGIN = GameConfig.BOARD_LABEL_MARGIN;

    private final int cellSize;

    private final Map<String, Img> imageCache = new HashMap<>();
    private final Map<String, Boolean> warnedPaths = new HashMap<>();
    private final Map<String, Integer> frameCountCache = new HashMap<>();
    private final Map<String, Integer> fpsCache = new HashMap<>();
    private final Map<String, Boolean> loopCache = new HashMap<>();
    private final Map<String, Img> boardImageCache = new HashMap<>();

    public BoardRenderer() {
        this.cellSize = GameConfig.CELL_SIZE;
    }

    public Img render(BoardSnapshot snapshot, model.Position selectedPosition) {
        int rows = snapshot.getRows();
        int cols = snapshot.getCols();
        long gameClock = snapshot.getGameClock();

        Img canvas = new Img();

        ensureCanvasInitialized(canvas, rows, cols);

        drawGrid(canvas, rows, cols);
        drawBoardLabels(canvas, rows, cols);

        Map<Integer, PendingRestSnapshot> restByPieceId = new HashMap<>();
        for (PendingRestSnapshot rest : snapshot.getPendingRests()) {
            restByPieceId.put(rest.getPiece().getId(), rest);
        }

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

        for (PendingMoveSnapshot move : snapshot.getPendingMoves()) {
            drawMovingPiece(canvas, move, gameClock);
        }

        for (PendingJumpSnapshot jump : snapshot.getPendingJumps()) {
            drawJumpingPiece(canvas, jump, gameClock);
        }

        if (selectedPosition != null) {
            drawSelectionHighlight(canvas, selectedPosition);
        }

        return canvas;
    }

    private static final Color FRAME_COLOR = new Color(22, 22, 26);
    private static final Color FRAME_ACCENT = new Color(191, 155, 87);
    private static final Color BRIGHT_GOLD = new Color(255, 200, 60);

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
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setStroke(new BasicStroke(2f));
        g.setColor(FRAME_ACCENT);
        g.drawRect(4, 4, width - 9, height - 9);

        int boardLeft = LABEL_MARGIN - 6;
        int boardTop = LABEL_MARGIN - 6;
        int boardRight = width - LABEL_MARGIN + 6;
        int boardBottom = height - LABEL_MARGIN + 6;
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(new Color(FRAME_ACCENT.getRed(), FRAME_ACCENT.getGreen(), FRAME_ACCENT.getBlue(), 160));
        g.drawRect(boardLeft, boardTop, boardRight - boardLeft, boardBottom - boardTop);

        g.dispose();
    }

    private int toPixelX(double col) {
        return LABEL_MARGIN + (int) Math.round(col * cellSize);
    }

    private int toPixelY(double row) {
        return LABEL_MARGIN + (int) Math.round(row * cellSize);
    }

    private static final String BOARD_IMAGE_PATH = "assets/board.png";

    private void drawGrid(Img canvas, int rows, int cols) {
        int width = cols * cellSize;
        int height = rows * cellSize;
        getBoardBackground(width, height).drawOn(canvas, LABEL_MARGIN, LABEL_MARGIN);
    }

    private Img getBoardBackground(int width, int height) {
        String key = width + "x" + height;
        Img cached = boardImageCache.get(key);
        if (cached != null) return cached;

        Img img;
        try {
            img = new Img().read(BOARD_IMAGE_PATH, new Dimension(width, height), false, null);
        } catch (Exception e) {
            System.err.println("Could not load board image: " + BOARD_IMAGE_PATH + " (" + e.getMessage() + ")");
            img = new Img().blank(width, height, new Color(190, 150, 110));
        }
        boardImageCache.put(key, img);
        return img;
    }

    private static final Color LABEL_COLOR = new Color(214, 186, 130);

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

    private static final Color REST_TINT = BRIGHT_GOLD;

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

    private static final Color SELECTION_BORDER = BRIGHT_GOLD;
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

    private void drawPieceAnimated(Img canvas, PieceSnapshot piece, int x, int y, String stateFolder, long elapsedMs) {

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

    private String stateFolderFor(PieceState state) {
        switch (state) {
            case IDLE: return "idle";
            case LONG_REST: return "long_rest";
            case SHORT_REST: return "short_rest";
            default: return null;
        }
    }

    private String getNormalizedFolderName(String representation) {
        if (representation == null || representation.length() < 2) {
            return representation;
        }
        char color = representation.charAt(0);
        char type = representation.charAt(1);

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

        int jumpHeight = cellSize / 2;
        int yOffset = (int) Math.round(-Math.sin(t * Math.PI) * jumpHeight);

        int groundX = toPixelX(jump.getCol());
        int groundY = toPixelY(jump.getRow());

        drawPieceShadow(canvas, groundX, groundY);

        long elapsedMs = gameClock - jump.getStartTime();
        drawPieceAnimated(canvas, jump.getPiece(), groundX, groundY + yOffset, "jump", elapsedMs);
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
