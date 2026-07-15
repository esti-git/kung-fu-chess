package view;

import engine.GameEngine;
import enums.PieceColor;
import input.CommandRegistry;
import input.Controller;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class BoardPrinter {

    private static final int HISTORY_PANEL_WIDTH = 190;
    private static final Color PANEL_BACKGROUND = new Color(245, 245, 240);
    private static final Color PANEL_ACCENT = new Color(90, 90, 90);
    private static final Font MOVES_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final Font SCORE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final int HISTORY_POLL_MS = 500; // אין צורך בעדכון מיידי - גם שנייה אחרי זה בסדר
    private static final int RESTART_DELAY_MS = 3000; // כמה זמן להציג GAME OVER לפני שמתחיל משחק חדש

    private final BoardRenderer renderer;
    private final MoveHistoryTracker historyTracker;
    private final ScoreTracker scoreTracker;
    private GameEngine engine;
    private CommandRegistry registry;
    private Controller controller;
    private Runnable restartAction;

    private JFrame guiWindow;
    private JLabel imageLabel;
    private JTextArea whiteMovesArea;
    private JTextArea blackMovesArea;
    private JLabel whiteScoreLabel;
    private JLabel blackScoreLabel;
    private Timer gameLoopTimer; // טיימר לניהול קצב המשחק בזמן אמת
    private Timer historyTimer; // טיימר נפרד ואיטי - "צופה מהצד" על שינויים בלוח, לא מחובר לקוד ביצוע המהלכים/התפיסות
    private long lastSystemTime; // מעקב אחר הזמן האמיתי של המחשב
    private boolean gameOverHandled; // מונע הפעלה כפולה של טיימר ההתחלה מחדש כל עוד המשחק עדיין נגמר

    public BoardPrinter() {
        this.renderer = new BoardRenderer();
        this.historyTracker = new MoveHistoryTracker();
        this.scoreTracker = new ScoreTracker();
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void setRegistry(CommandRegistry registry) {
        this.registry = registry;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setRestartAction(Runnable restartAction) {
        this.restartAction = restartAction;
    }

    /** מאפס את תצוגות ההיסטוריה והניקוד - נקרא לאחר שמשחק חדש מתחיל */
    public void resetTrackers() {
        historyTracker.reset();
        scoreTracker.reset();
        updateHistoryPanels();
    }

    public void handleRawPrint(String[] parts) {
        if (parts == null || parts.length <= 1) {
            printConsole();
            printGUI();
            return;
        }

        if ("board".equals(parts[1])) {
            printConsole();
            printGUI();
        }
    }

    // הדפסה טקסטואלית לקונסול - קוראת רק מתמונת מצב, לא מהלוח החי
    public void printConsole() {
        if (engine == null) return;
        BoardSnapshot snapshot = engine.captureSnapshot();
        for (int i = 0; i < snapshot.getRows(); i++) {
            StringBuilder rowStr = new StringBuilder();
            for (int j = 0; j < snapshot.getCols(); j++) {
                PieceSnapshot piece = snapshot.getPieceAt(i, j);
                rowStr.append(piece == null ? "." : piece.getRepresentation());
                if (j < snapshot.getCols() - 1) rowStr.append(" ");
            }
            System.out.println(rowStr.toString());
        }
    }

    // הדפסה גרפית לחלון והפעלת השעון בזמן אמת
    public void printGUI() {
        if (engine == null) return;

        // אתחול חלון ה-GUI פעם אחת בלבד
        if (guiWindow == null) {
            initGUIWindow();
            startClockLoop(); // הפעלת השעון האוטומטי ברקע
            startHistoryLoop(); // צופה איטי ונפרד שרק בודק מבחוץ מה השתנה בלוח
        } else {
            updateGUIImage();
        }
    }

    private void initGUIWindow() {
        SwingUtilities.invokeLater(() -> {
            guiWindow = new JFrame("Kung Fu Chess");
            guiWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BoardSnapshot snapshot = engine.captureSnapshot();
            Img visualBoard = renderer.render(snapshot, (controller != null) ? controller.selectedPosition() : null);

            imageLabel = new JLabel(new ImageIcon(visualBoard.get()));

            int boardHeight = visualBoard.get().getHeight();
            whiteMovesArea = createMovesArea();
            blackMovesArea = createMovesArea();
            whiteScoreLabel = createScoreLabel();
            blackScoreLabel = createScoreLabel();

            guiWindow.add(buildSidePanel("לבן", whiteMovesArea, whiteScoreLabel, boardHeight), BorderLayout.WEST);
            guiWindow.add(imageLabel, BorderLayout.CENTER);
            guiWindow.add(buildSidePanel("שחור", blackMovesArea, blackScoreLabel, boardHeight), BorderLayout.EAST);

            // מאזין עכבר לשליחת לחיצות
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (registry != null) {
                        // דאבל קליק על משבצת מפעיל קפיצה, קליק בודד מפעיל בחירה/תנועה רגילה
                        String commandType = (e.getClickCount() >= 2) ? "jump" : "click";
                        String[] commandParts = {
                            commandType,
                            String.valueOf(e.getX()),
                            String.valueOf(e.getY())
                        };
                        registry.dispatch(commandType, commandParts);
                        System.out.println("Mouse " + commandType + " detected at Pixel: (" + e.getX() + ", " + e.getY() + ")");
                    }
                }
            });

            guiWindow.pack();
            guiWindow.setLocationRelativeTo(null);
            guiWindow.setVisible(true);
        });
    }

    private JTextArea createMovesArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(MOVES_FONT);
        area.setBackground(PANEL_BACKGROUND);
        area.setForeground(new Color(40, 40, 40));
        area.setMargin(new Insets(8, 10, 8, 10));
        return area;
    }

    private JLabel createScoreLabel() {
        JLabel label = new JLabel("ניקוד: 0");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(SCORE_FONT);
        label.setOpaque(true);
        label.setBackground(PANEL_ACCENT);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        return label;
    }

    private JPanel buildSidePanel(String title, JTextArea movesArea, JLabel scoreLabel, int boardHeight) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(HISTORY_PANEL_WIDTH, boardHeight));
        panel.setBackground(PANEL_BACKGROUND);

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_ACCENT, 2), title);
        titledBorder.setTitleFont(TITLE_FONT);
        titledBorder.setTitleColor(PANEL_ACCENT);
        panel.setBorder(titledBorder);

        JScrollPane scrollPane = new JScrollPane(movesArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(scoreLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void updateGUIImage() {
        SwingUtilities.invokeLater(() -> {
            if (engine == null || imageLabel == null) return;
            BoardSnapshot snapshot = engine.captureSnapshot();
            Img visualBoard = renderer.render(snapshot, (controller != null) ? controller.selectedPosition() : null);
            if (engine.isGameOver()) {
                drawGameOverOverlay(visualBoard.get());
            }
            imageLabel.setIcon(new ImageIcon(visualBoard.get()));
            guiWindow.repaint();
        });
    }

    private void drawGameOverOverlay(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(Color.WHITE);

        String title = "GAME OVER";
        g.setFont(g.getFont().deriveFont(Font.BOLD, 48f));
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (image.getWidth() - titleMetrics.stringWidth(title)) / 2;
        int titleY = image.getHeight() / 2 - 10;
        g.drawString(title, titleX, titleY);

        PieceColor winner = (engine != null) ? engine.getWinnerColor() : null;
        if (winner != null) {
            String winnerText = (winner == PieceColor.WHITE ? "לבן" : "שחור") + " מנצח!";
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            FontMetrics winnerMetrics = g.getFontMetrics();
            int winnerX = (image.getWidth() - winnerMetrics.stringWidth(winnerText)) / 2;
            int winnerY = titleY + titleMetrics.getDescent() + winnerMetrics.getAscent() + 15;
            g.drawString(winnerText, winnerX, winnerY);
        }

        g.dispose();
    }

    /**
     * פונקציה המפעילה לולאת זמן קבועה שמעדכנת את מנוע המשחק
     * ודואגת להזיז את הכלים בצורה חלקה על המסך הגרפי
     */
    private void startClockLoop() {
        lastSystemTime = System.currentTimeMillis();

        // הרצת עדכון בכל 16 מילישניות (כ-60 FPS)
        gameLoopTimer = new Timer(16, e -> {
            if (engine == null) return;

            if (engine.isGameOver()) {
                if (!gameOverHandled) {
                    gameOverHandled = true;
                    scheduleRestart();
                }
            } else {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - lastSystemTime;
                lastSystemTime = currentTime;

                // קידום השעון הפנימי של המנוע בזמן שחלף בפועל!
                engine.advanceClock(elapsed);
            }

            // ריענון הציור על המסך תמיד - כך שגם מסך ה-GAME OVER יישאר מוצג
            updateGUIImage();
        });
        gameLoopTimer.start();
    }

    /**
     * לאחר עיכוב קצר, מפעיל את פעולת ההתחלה-מחדש (אם חוברה) ומשחרר את הדגל כדי שהמחזור יוכל לקרות שוב
     */
    private void scheduleRestart() {
        Timer restartTimer = new Timer(RESTART_DELAY_MS, e -> {
            if (restartAction != null) {
                restartAction.run();
            }
            lastSystemTime = System.currentTimeMillis();
            gameOverHandled = false;
        });
        restartTimer.setRepeats(false);
        restartTimer.start();
    }

    /**
     * טיימר איטי ונפרד לגמרי מלולאת המשחק - רק "מציץ" בלוח מבחוץ ומזהה שינויים במיקומי הכלים.
     * לא מקבל אף קריאה מקוד המהלכים/הקפיצות עצמו, ולכן אין צורך שיתעדכן מיידית.
     */
    private void startHistoryLoop() {
        historyTimer = new Timer(HISTORY_POLL_MS, e -> {
            if (engine == null) return;
            BoardSnapshot snapshot = engine.captureSnapshot();
            historyTracker.poll(snapshot);
            scoreTracker.poll(snapshot);
            updateHistoryPanels();
        });
        historyTimer.start();
    }

    private void updateHistoryPanels() {
        if (whiteMovesArea == null || blackMovesArea == null) return;
        whiteMovesArea.setText(String.join("\n", historyTracker.getWhiteMoves()));
        blackMovesArea.setText(String.join("\n", historyTracker.getBlackMoves()));
        whiteMovesArea.setCaretPosition(whiteMovesArea.getDocument().getLength());
        blackMovesArea.setCaretPosition(blackMovesArea.getDocument().getLength());
        whiteScoreLabel.setText("ניקוד: " + scoreTracker.getWhiteScore());
        blackScoreLabel.setText("ניקוד: " + scoreTracker.getBlackScore());
    }
}
