package client;

import audio.SoundManager;
import client.logging.ClientLog;
import config.GameConfig;
import enums.PieceColor;
import events.Event;
import events.EventBus;
import events.GameEndedEvent;
import events.GameStartedEvent;
import events.MoveMadeEvent;
import events.PieceCapturedEvent;
import model.Position;
import protocol.AssignedIdentity;
import protocol.JumpCommand;
import protocol.LoginResult;
import protocol.MoveCommand;
import protocol.NetworkState;
import protocol.PieceCodes;
import protocol.RoomJoined;
import protocol.SpectateInfo;
import view.BoardRenderer;
import view.BoardSnapshot;
import view.GameAnimationController;
import view.Img;
import view.MoveHistoryTracker;
import view.PieceSnapshot;
import view.ScaledImagePanel;
import view.ScoreTracker;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ClientView {

    private static final int HISTORY_PANEL_WIDTH = 190;
    private static final Color FRAME_BACKGROUND = new Color(22, 22, 26);
    private static final Color PANEL_BACKGROUND = new Color(26, 26, 30);
    private static final Color PANEL_ACCENT = new Color(191, 155, 87);
    private static final Color PANEL_TEXT = new Color(225, 222, 215);
    private static final Font MOVES_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final Font SCORE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private final BoardRenderer renderer = new BoardRenderer();
    private final ScaledImagePanel boardPanel = new ScaledImagePanel();
    private final EventBus eventBus = new EventBus();
    private final MoveHistoryTracker historyTracker = new MoveHistoryTracker(eventBus);
    private final ScoreTracker scoreTracker = new ScoreTracker(eventBus);
    private final SoundManager soundManager = new SoundManager(eventBus);
    private final GameAnimationController animationController = new GameAnimationController(eventBus);

    private GameClient client;
    private volatile BoardSnapshot latestSnapshot;
    private Position selected;

    private PieceColor myColor;
    private String whiteName;
    private String blackName;
    private int whiteRating = 1200;
    private int blackRating = 1200;
    private volatile boolean gameOver;
    private boolean disconnectNotified;

    private boolean spectator;
    private String roomId;
    private JLabel roomLabel;
    private JButton playAgainButton;

    private JDialog countdownDialog;
    private JLabel countdownLabel;
    private Timer countdownTimer;
    private int countdownSecondsLeft;

    private JFrame guiWindow;
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

    public ClientView() {
        eventBus.subscribe(MoveMadeEvent.TYPE, event -> refreshHistoryPanelsOnEdt());
        eventBus.subscribe(PieceCapturedEvent.TYPE, event -> refreshHistoryPanelsOnEdt());
        eventBus.subscribe(GameStartedEvent.TYPE, event -> SwingUtilities.invokeLater(() -> {
            gameOver = false;
            disconnectNotified = false;
            if (playAgainButton != null) playAgainButton.setVisible(false);
            repaintBoard();
        }));
        eventBus.subscribe(GameEndedEvent.TYPE, event -> SwingUtilities.invokeLater(() -> {
            gameOver = true;
            if (playAgainButton != null && !spectator) playAgainButton.setVisible(true);
            repaintBoard();
        }));
    }

    public void setClient(GameClient client) {
        this.client = client;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            guiWindow = new JFrame("Kung Fu Chess - Client");
            guiWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            boardPanel.setBackground(FRAME_BACKGROUND);

            whiteMovesArea = createMovesArea();
            blackMovesArea = createMovesArea();
            whiteScoreLabel = createScoreLabel();
            blackScoreLabel = createScoreLabel();
            whiteBorder = createTitledBorder("White");
            blackBorder = createTitledBorder("Black");

            westPanel = buildSidePanel(whiteBorder, whiteMovesArea, whiteScoreLabel);
            eastPanel = buildSidePanel(blackBorder, blackMovesArea, blackScoreLabel);

            roomLabel = new JLabel(" ", SwingConstants.CENTER);
            roomLabel.setFont(TITLE_FONT);
            roomLabel.setForeground(PANEL_ACCENT);
            roomLabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

            playAgainButton = new JButton("Play Again");
            playAgainButton.setFont(playAgainButton.getFont().deriveFont(Font.BOLD, 16f));
            playAgainButton.setVisible(false);
            playAgainButton.addActionListener(e -> {
                if (client != null) client.sendPlayAgain();
                playAgainButton.setVisible(false);
            });
            JPanel southPanel = new JPanel(new FlowLayout());
            southPanel.add(playAgainButton);

            guiWindow.add(roomLabel, BorderLayout.NORTH);
            guiWindow.add(westPanel, BorderLayout.WEST);
            guiWindow.add(boardPanel, BorderLayout.CENTER);
            guiWindow.add(eastPanel, BorderLayout.EAST);
            guiWindow.add(southPanel, BorderLayout.SOUTH);

            boardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    java.awt.Point imagePoint = boardPanel.panelToImage(e.getX(), e.getY());
                    if (imagePoint == null) return;

                    if (e.getClickCount() >= 2) {
                        handleDoubleClick(imagePoint.x, imagePoint.y);
                    } else {
                        handleClick(imagePoint.x, imagePoint.y);
                    }
                }
            });

            guiWindow.setMinimumSize(new Dimension(500, 400));
            guiWindow.pack();
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
            repaintBoard();
            updateHistoryPanels();
            applyIdentityLabels();
        });
    }

    public void onState(NetworkState state) {
        this.latestSnapshot = state.snapshot;
        SwingUtilities.invokeLater(this::repaintBoard);
    }

    public void onError(String message) {
        System.err.println("Server rejected command: " + message);
        ClientLog.warn("Server rejected command: " + message);
    }

    public void onEvent(Event event) {
        eventBus.publish(event);
    }

    public void onHistory(java.util.List<Event> events) {
        for (Event event : events) {
            if (MoveMadeEvent.TYPE.equals(event.getType())) {
                historyTracker.applyMoveMade(event);
            } else if (PieceCapturedEvent.TYPE.equals(event.getType())) {
                scoreTracker.applyPieceCaptured(event);
            }
        }
        refreshHistoryPanelsOnEdt();
    }

    public void onAssign(AssignedIdentity identity) {
        this.myColor = identity.color;
        this.whiteName = identity.whiteName;
        this.blackName = identity.blackName;
        this.whiteRating = identity.whiteRating;
        this.blackRating = identity.blackRating;
        SwingUtilities.invokeLater(() -> {
            dismissDisconnectCountdown();
            applyIdentityLabels();
        });
    }

    public void onRoomJoined(RoomJoined joined) {
        this.roomId = joined.roomId;
        SwingUtilities.invokeLater(this::applyIdentityLabels);
    }

    public void onSpectate(SpectateInfo info) {
        this.spectator = true;
        this.myColor = null;
        this.whiteName = info.whiteName;
        this.blackName = info.blackName;
        this.whiteRating = info.whiteRating;
        this.blackRating = info.blackRating;
        SwingUtilities.invokeLater(() -> {
            dismissDisconnectCountdown();
            applyIdentityLabels();
        });
    }

    public void onDisconnectCountdown(int seconds) {
        SwingUtilities.invokeLater(() -> {
            countdownSecondsLeft = seconds;
            if (countdownDialog == null) {
                countdownDialog = new JDialog(guiWindow, "Opponent disconnected", false);
                countdownLabel = new JLabel("", SwingConstants.CENTER);
                countdownLabel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
                countdownDialog.add(countdownLabel);
                countdownDialog.setMinimumSize(new Dimension(320, 100));
                countdownDialog.setLocationRelativeTo(guiWindow);
            }
            updateCountdownLabel();
            countdownDialog.setVisible(true);

            if (countdownTimer != null) countdownTimer.stop();
            countdownTimer = new Timer(1000, e -> {
                countdownSecondsLeft--;
                if (countdownSecondsLeft <= 0) {
                    dismissDisconnectCountdown();
                } else {
                    updateCountdownLabel();
                }
            });
            countdownTimer.start();
        });
    }

    private void updateCountdownLabel() {
        if (countdownLabel != null) {
            countdownLabel.setText("Opponent disconnected. Auto-resign in " + countdownSecondsLeft + "s...");
        }
    }

    private void dismissDisconnectCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        if (countdownDialog != null) {
            countdownDialog.setVisible(false);
        }
    }

    public void onRejected(String message) {
        System.err.println("Join rejected: " + message);
        ClientLog.warn("Join rejected: " + message);
        System.exit(0);
    }

    public void onLoginResult(LoginResult result) {
        if (!result.success) {
            onRejected(result.message);
            return;
        }
        System.out.println("Logged in. Rating: " + result.rating);
    }

    public void onOpponentDisconnected(String message) {
        gameOver = true;
        showGameOverOnce(message);
    }

    public void onConnectionClosed(String reason) {
        gameOver = true;
        showGameOverOnce((reason == null || reason.isBlank()) ? "Connection closed. Game over." : reason);
    }

    private void showGameOverOnce(String message) {
        SwingUtilities.invokeLater(() -> {
            dismissDisconnectCountdown();
            if (disconnectNotified) return;
            disconnectNotified = true;
            System.out.println(message);
            if (guiWindow != null) {
                JOptionPane.showMessageDialog(guiWindow, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void applyIdentityLabels() {
        if (guiWindow == null || whiteBorder == null || blackBorder == null) return;

        String white = whiteName == null ? "waiting..." : whiteName + " (" + whiteRating + ")";
        String black = blackName == null ? "waiting..." : blackName + " (" + blackRating + ")";
        whiteBorder.setTitle("White - " + white);
        blackBorder.setTitle("Black - " + black);

        if (spectator) {
            guiWindow.setTitle("Kung Fu Chess - Spectating - " + white + " vs " + black);
        } else if (myColor != null) {
            String you = myColor == PieceColor.WHITE ? "White" : "Black";
            String yourName = myColor == PieceColor.WHITE ? white : black;
            guiWindow.setTitle("Kung Fu Chess - " + you + " - " + yourName);
        }

        if (roomLabel != null) {
            roomLabel.setText(roomId == null ? " " : "Room: " + roomId + (spectator ? "  (spectating)" : ""));
        }

        westPanel.repaint();
        eastPanel.repaint();
    }

    private void refreshHistoryPanelsOnEdt() {
        SwingUtilities.invokeLater(this::updateHistoryPanels);
    }

    private void repaintBoard() {
        BoardSnapshot snapshot = latestSnapshot;
        if (snapshot == null) return;
        Img visualBoard = renderer.render(snapshot, selected);
        if (animationController.isShowingGameOverOverlay()) {
            drawGameOverOverlay(visualBoard.get());
        }
        boardPanel.setImage(visualBoard.get());
        boardPanel.repaint();
    }

    private void handleClick(int pixelX, int pixelY) {
        BoardSnapshot snapshot = latestSnapshot;
        if (snapshot == null || client == null || gameOver || spectator) return;

        Position clicked = pixelToCell(pixelX, pixelY, snapshot.getRows(), snapshot.getCols());
        if (clicked == null) return;

        if (selected == null) {
            if (snapshot.getPieceAt(clicked.getRow(), clicked.getCol()) != null) {
                selected = clicked;
            }
            repaintBoard();
            return;
        }

        if (selected.equals(clicked)) {
            selected = null;
            repaintBoard();
            return;
        }

        PieceSnapshot selectedPiece = snapshot.getPieceAt(selected.getRow(), selected.getCol());
        if (selectedPiece != null) {
            String command = "" + PieceCodes.colorChar(selectedPiece.getColor()) + PieceCodes.kindChar(selectedPiece.getKind())
                    + MoveCommand.squareName(selected.getRow(), selected.getCol(), snapshot.getRows())
                    + MoveCommand.squareName(clicked.getRow(), clicked.getCol(), snapshot.getRows());
            client.sendMove(command);
        }

        selected = null;
        repaintBoard();
    }

    private void handleDoubleClick(int pixelX, int pixelY) {
        BoardSnapshot snapshot = latestSnapshot;
        if (snapshot == null || client == null || gameOver || spectator) return;

        Position clicked = pixelToCell(pixelX, pixelY, snapshot.getRows(), snapshot.getCols());
        if (clicked == null) return;

        PieceSnapshot piece = snapshot.getPieceAt(clicked.getRow(), clicked.getCol());
        if (piece != null) {
            String command = JumpCommand.build(piece.getColor(), piece.getKind(), clicked.getRow(), clicked.getCol(), snapshot.getRows());
            client.sendJump(command);
        }

        selected = null;
        repaintBoard();
    }

    private Position pixelToCell(int x, int y, int rows, int cols) {
        int cellSize = GameConfig.CELL_SIZE;
        int margin = GameConfig.BOARD_LABEL_MARGIN;

        int adjustedX = x - margin;
        int adjustedY = y - margin;
        if (adjustedX < 0 || adjustedY < 0) return null;

        int col = adjustedX / cellSize;
        int row = adjustedY / cellSize;
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return new Position(row, col);
        }
        return null;
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
        JLabel label = new JLabel("Score: 0");
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

    private void updateHistoryPanels() {
        if (whiteMovesArea == null || blackMovesArea == null) return;
        whiteMovesArea.setText(String.join("\n", historyTracker.getWhiteMoves()));
        blackMovesArea.setText(String.join("\n", historyTracker.getBlackMoves()));
        whiteMovesArea.setCaretPosition(whiteMovesArea.getDocument().getLength());
        blackMovesArea.setCaretPosition(blackMovesArea.getDocument().getLength());
        whiteScoreLabel.setText("Score: " + scoreTracker.getWhiteScore());
        blackScoreLabel.setText("Score: " + scoreTracker.getBlackScore());
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
            String winnerText = (winner == PieceColor.WHITE ? "White" : "Black") + " wins!";
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            FontMetrics winnerMetrics = g.getFontMetrics();
            int winnerX = (image.getWidth() - winnerMetrics.stringWidth(winnerText)) / 2;
            int winnerY = titleY + titleMetrics.getDescent() + winnerMetrics.getAscent() + 15;
            g.drawString(winnerText, winnerX, winnerY);
        }

        g.dispose();
    }
}
