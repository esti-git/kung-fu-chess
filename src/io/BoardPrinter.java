package io;

import engine.GameEngine;
import model.Board;
import model.Position;
import input.CommandRegistry;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPrinter {

    private static final int HISTORY_PANEL_WIDTH = 160;
    private static final int HISTORY_POLL_MS = 500; // אין צורך בעדכון מיידי - גם שנייה אחרי זה בסדר

    private final Board board;
    private final BoardRenderer renderer;
    private final MoveHistoryTracker historyTracker;
    private GameEngine engine;
    private CommandRegistry registry;

    private JFrame guiWindow;
    private JLabel imageLabel;
    private JTextArea whiteMovesArea;
    private JTextArea blackMovesArea;
    private Timer gameLoopTimer; // טיימר לניהול קצב המשחק בזמן אמת
    private Timer historyTimer; // טיימר נפרד ואיטי - "צופה מהצד" על שינויים בלוח, לא מחובר לקוד ביצוע המהלכים
    private long lastSystemTime; // מעקב אחר הזמן האמיתי של המחשב

    public BoardPrinter(Board board) {
        this.board = board;
        this.renderer = new BoardRenderer(board);
        this.historyTracker = new MoveHistoryTracker(board);
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }

    public void setRegistry(CommandRegistry registry) {
        this.registry = registry;
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

    // הדפסה טקסטואלית לקונסול
    public void printConsole() {
        for (int i = 0; i < board.getRows(); i++) {
            StringBuilder rowStr = new StringBuilder();
            for (int j = 0; j < board.getCols(); j++) {
                model.Piece piece = board.getPieceAt(new Position(i, j));
                rowStr.append(piece == null ? "." : piece.getRepresentation());
                if (j < board.getCols() - 1) rowStr.append(" ");
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

            Img visualBoard = renderer.render(
                engine.getPendingMoves(),
                engine.getPendingJumps(),
                engine.getGameClock()
            );

            imageLabel = new JLabel(new ImageIcon(visualBoard.get()));

            int boardHeight = visualBoard.get().getHeight();
            whiteMovesArea = createMovesArea();
            blackMovesArea = createMovesArea();

            guiWindow.add(wrapHistoryPanel("White", whiteMovesArea, boardHeight), BorderLayout.WEST);
            guiWindow.add(imageLabel, BorderLayout.CENTER);
            guiWindow.add(wrapHistoryPanel("Black", blackMovesArea, boardHeight), BorderLayout.EAST);

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
        return area;
    }

    private JScrollPane wrapHistoryPanel(String title, JTextArea area, int boardHeight) {
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(HISTORY_PANEL_WIDTH, boardHeight));
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    private void updateGUIImage() {
        SwingUtilities.invokeLater(() -> {
            if (engine == null || imageLabel == null) return;
            Img visualBoard = renderer.render(
                engine.getPendingMoves(),
                engine.getPendingJumps(),
                engine.getGameClock()
            );
            imageLabel.setIcon(new ImageIcon(visualBoard.get()));
            guiWindow.repaint();
        });
    }

    /**
     * פונקציה המפעילה לולאת זמן קבועה שמעדכנת את מנוע המשחק
     * ודואגת להזיז את הכלים בצורה חלקה על המסך הגרפי
     */
    private void startClockLoop() {
        lastSystemTime = System.currentTimeMillis();
        
        // הרצת עדכון בכל 16 מילישניות (כ-60 FPS)
        gameLoopTimer = new Timer(16, e -> {
            if (engine != null && !engine.isGameOver()) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - lastSystemTime;
                lastSystemTime = currentTime;

                // קידום השעון הפנימי של המנוע בזמן שחלף בפועל!
                engine.advanceClock(elapsed);

                // ריענון הציור על המסך כדי לראות את הכלים זזים חלקה
                updateGUIImage();
            }
        });
        gameLoopTimer.start();
    }

    /**
     * טיימר איטי ונפרד לגמרי מלולאת המשחק - רק "מציץ" בלוח מבחוץ ומזהה שינויים במיקומי הכלים.
     * לא מקבל אף קריאה מקוד המהלכים/הקפיצות עצמו, ולכן אין צורך שיתעדכן מיידית.
     */
    private void startHistoryLoop() {
        historyTimer = new Timer(HISTORY_POLL_MS, e -> {
            historyTracker.poll();
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
    }
}