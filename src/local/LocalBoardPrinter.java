package local;

import audio.SoundManager;
import engine.GameEngine;
import enums.PieceColor;
import events.EventBus;
import events.MoveMadeEvent;
import events.PieceCapturedEvent;
import view.BoardRenderer;
import view.BoardSnapshot;
import view.BoardSnapshotFactory;
import view.GameAnimationController;
import view.Img;
import view.MoveHistoryTracker;
import view.PieceSnapshot;
import view.ScaledImagePanel;
import view.ScoreTracker;
import view.SidePanelFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LocalBoardPrinter {

    private final BoardRenderer renderer;
    private final BoardSnapshotFactory snapshotFactory;
    private final MoveHistoryTracker historyTracker;
    private final ScoreTracker scoreTracker;
    private final SoundManager soundManager;
    private final GameAnimationController animationController;
    private GameEngine engine;
    private LocalCommandRegistry registry;
    private LocalController controller;
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
    private LocalGameLoop gameLoop;

    public LocalBoardPrinter(EventBus eventBus) {
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

    public void setRegistry(LocalCommandRegistry registry) {
        this.registry = registry;
    }

    public void setController(LocalController controller) {
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
            boardPanel.setBackground(SidePanelFactory.FRAME_BACKGROUND);

            whiteMovesArea = SidePanelFactory.createMovesArea();
            blackMovesArea = SidePanelFactory.createMovesArea();
            whiteScoreLabel = SidePanelFactory.createScoreLabel("ניקוד: 0");
            blackScoreLabel = SidePanelFactory.createScoreLabel("ניקוד: 0");
            whiteBorder = SidePanelFactory.createTitledBorder("לבן");
            blackBorder = SidePanelFactory.createTitledBorder("שחור");

            westPanel = SidePanelFactory.buildSidePanel(whiteBorder, whiteMovesArea, whiteScoreLabel);
            eastPanel = SidePanelFactory.buildSidePanel(blackBorder, blackMovesArea, blackScoreLabel);

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
        SidePanelFactory.rescaleSidePanels(guiWindow, baselineWidth, baselineHeight,
                westPanel, eastPanel, whiteMovesArea, blackMovesArea,
                whiteScoreLabel, blackScoreLabel, whiteBorder, blackBorder);
    }

    private void updateGUIImage() {
        SwingUtilities.invokeLater(() -> {
            if (engine == null || boardPanel == null) return;
            BoardSnapshot snapshot = snapshotFactory.capture(engine);
            Img visualBoard = renderer.render(snapshot, (controller != null) ? controller.selectedPosition() : null);
            if (animationController.isShowingGameOverOverlay()) {
                PieceColor winner = animationController.getWinnerColor();
                String winnerText = winner == null ? null : (winner == PieceColor.WHITE ? "לבן" : "שחור") + " מנצח!";
                SidePanelFactory.drawGameOverOverlay(visualBoard.get(), "GAME OVER", winnerText);
            }
            boardPanel.setImage(visualBoard.get());
            boardPanel.repaint();
        });
    }

    private void startGameLoop() {
        gameLoop = new LocalGameLoop(engine, this::updateGUIImage);
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
