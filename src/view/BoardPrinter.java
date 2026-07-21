package view;

import audio.SoundManager;
import engine.GameEngine;
import enums.PieceColor;
import events.EventBus;
import events.MoveMadeEvent;
import events.PieceCapturedEvent;
import input.CommandRegistry;
import input.Controller;
import input.GameLoop;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class BoardPrinter {

    private static final int HISTORY_PANEL_WIDTH = 190;
    private static final Color FRAME_BACKGROUND = new Color(22, 22, 26);
    private static final Color PANEL_BACKGROUND = new Color(26, 26, 30);
    private static final Color PANEL_ACCENT = new Color(191, 155, 87);
    private static final Color PANEL_TEXT = new Color(225, 222, 215);
    private static final Font MOVES_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final Font SCORE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private final BoardRenderer renderer;
    private final BoardSnapshotFactory snapshotFactory;
    private final MoveHistoryTracker historyTracker;
    private final ScoreTracker scoreTracker;
    private final SoundManager soundManager;
    private final GameAnimationController animationController;
    private GameEngine engine;
    private CommandRegistry registry;
    private Controller controller;
    private Runnable restartAction;

    private JFrame guiWindow;
    private ScaledImagePanel boardPanel;
    private JPanel westPanel;
    private JPanel eastPanel;
    private TitledBorder whiteBorder;
    private TitledBorder blackBorder;
    private JTextArea whiteMovesArea;
    private JTextArea blackMovesArea;
    private JLabel whiteScoreLabel;
    private JLabel blackScoreLabel;
    private int baselineWidth;
    private int baselineHeight;
    private GameLoop gameLoop;

    public BoardPrinter(EventBus eventBus) {
        this.renderer = new BoardRenderer();
        this.snapshotFactory = new BoardSnapshotFactory();
        this.historyTracker = new MoveHistoryTracker(eventBus);
        this.scoreTracker = new ScoreTracker(eventBus);
        this.soundManager = new SoundManager(eventBus);
        this.animationController = new GameAnimationController(eventBus);
        eventBus.subscribe(MoveMadeEvent.TYPE, event -> refreshHistoryPanelsOnEdt());
        eventBus.subscribe(PieceCapturedEvent.TYPE, event -> refreshHistoryPanelsOnEdt());
    }

    private void refreshHistoryPanelsOnEdt() {
        SwingUtilities.invokeLater(this::updateHistoryPanels);
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
        if (gameLoop != null) {
            gameLoop.setRestartAction(restartAction);
        }
    }

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

    public void printConsole() {
        if (engine == null) return;
        BoardSnapshot snapshot = snapshotFactory.capture(engine);
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

    public void printGUI() {
        if (engine == null) return;

        if (guiWindow == null) {
            initGUIWindow();
            startGameLoop();
        } else {
            updateGUIImage();
        }
    }

    private void initGUIWindow() {
        SwingUtilities.invokeLater(() -> {
            guiWindow = new JFrame("Kung Fu Chess");
            guiWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BoardSnapshot snapshot = snapshotFactory.capture(engine);
            Img visualBoard = renderer.render(snapshot, (controller != null) ? controller.selectedPosition() : null);

            boardPanel = new ScaledImagePanel();
            boardPanel.setImage(visualBoard.get());
            boardPanel.setBackground(FRAME_BACKGROUND);

            whiteMovesArea = createMovesArea();
            blackMovesArea = createMovesArea();
            whiteScoreLabel = createScoreLabel();
            blackScoreLabel = createScoreLabel();
            whiteBorder = createTitledBorder("לבן");
            blackBorder = createTitledBorder("שחור");

            westPanel = buildSidePanel(whiteBorder, whiteMovesArea, whiteScoreLabel);
            eastPanel = buildSidePanel(blackBorder, blackMovesArea, blackScoreLabel);

            guiWindow.add(westPanel, BorderLayout.WEST);
            guiWindow.add(boardPanel, BorderLayout.CENTER);
            guiWindow.add(eastPanel, BorderLayout.EAST);

            boardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (registry == null) return;
                    Point imagePoint = boardPanel.panelToImage(e.getX(), e.getY());
                    if (imagePoint == null) return;

                    String commandType = (e.getClickCount() >= 2) ? "jump" : "click";
                    String[] commandParts = {
                        commandType,
                        String.valueOf(imagePoint.x),
                        String.valueOf(imagePoint.y)
                    };
                    registry.dispatch(commandType, commandParts);
                    System.out.println("Mouse " + commandType + " detected at Pixel: (" + imagePoint.x + ", " + imagePoint.y + ")");
                }
            });

            guiWindow.pack();
            guiWindow.setMinimumSize(new Dimension(500, 400));
            guiWindow.setLocationRelativeTo(null);

            baselineWidth = guiWindow.getWidth();
            baselineHeight = guiWindow.getHeight();
            guiWindow.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    rescaleSidePanels();
                }
            });

            guiWindow.setVisible(true);
        });
    }

    private void rescaleSidePanels() {
        if (baselineWidth <= 0 || baselineHeight <= 0 || westPanel == null || eastPanel == null) return;

        double scaleX = guiWindow.getWidth() / (double) baselineWidth;
        double scaleY = guiWindow.getHeight() / (double) baselineHeight;
        double scale = Math.max(0.5, Math.min(2.5, Math.min(scaleX, scaleY)));

        int panelWidth = Math.max(90, (int) Math.round(HISTORY_PANEL_WIDTH * scale));
        westPanel.setPreferredSize(new Dimension(panelWidth, 10));
        eastPanel.setPreferredSize(new Dimension(panelWidth, 10));

        float movesFontSize = (float) Math.max(9.0, MOVES_FONT.getSize() * scale);
        float scoreFontSize = (float) Math.max(11.0, SCORE_FONT.getSize() * scale);
        float titleFontSize = (float) Math.max(10.0, TITLE_FONT.getSize() * scale);

        whiteMovesArea.setFont(MOVES_FONT.deriveFont(movesFontSize));
        blackMovesArea.setFont(MOVES_FONT.deriveFont(movesFontSize));
        whiteScoreLabel.setFont(SCORE_FONT.deriveFont(scoreFontSize));
        blackScoreLabel.setFont(SCORE_FONT.deriveFont(scoreFontSize));
        whiteBorder.setTitleFont(TITLE_FONT.deriveFont(titleFontSize));
        blackBorder.setTitleFont(TITLE_FONT.deriveFont(titleFontSize));

        guiWindow.revalidate();
        guiWindow.repaint();
    }

    private JTextArea createMovesArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(MOVES_FONT);
        area.setBackground(PANEL_BACKGROUND);
        area.setForeground(PANEL_TEXT);
        area.setCaretColor(PANEL_TEXT);
        area.setMargin(new Insets(8, 10, 8, 10));
        return area;
    }

    private JLabel createScoreLabel() {
        JLabel label = new JLabel("ניקוד: 0");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(SCORE_FONT);
        label.setOpaque(true);
        label.setBackground(PANEL_ACCENT);
        label.setForeground(new Color(30, 28, 24));
        label.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        return label;
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_ACCENT, 2), title);
        titledBorder.setTitleFont(TITLE_FONT);
        titledBorder.setTitleColor(PANEL_ACCENT);
        return titledBorder;
    }

    private JPanel buildSidePanel(TitledBorder titledBorder, JTextArea movesArea, JLabel scoreLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(HISTORY_PANEL_WIDTH, 10));
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(titledBorder);

        JScrollPane scrollPane = new JScrollPane(movesArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(scoreLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void updateGUIImage() {
        SwingUtilities.invokeLater(() -> {
            if (engine == null || boardPanel == null) return;
            BoardSnapshot snapshot = snapshotFactory.capture(engine);
            Img visualBoard = renderer.render(snapshot, (controller != null) ? controller.selectedPosition() : null);
            if (animationController.isShowingGameOverOverlay()) {
                drawGameOverOverlay(visualBoard.get());
            }
            boardPanel.setImage(visualBoard.get());
            boardPanel.repaint();
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

        PieceColor winner = animationController.getWinnerColor();
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

    private void startGameLoop() {
        gameLoop = new GameLoop(engine, this::updateGUIImage);
        gameLoop.setRestartAction(restartAction);
        gameLoop.start();
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
