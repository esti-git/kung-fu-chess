package view;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class SidePanelFactory {

    public static final int HISTORY_PANEL_WIDTH = 190;
    public static final Color FRAME_BACKGROUND = new Color(22, 22, 26);
    public static final Color PANEL_BACKGROUND = new Color(26, 26, 30);
    public static final Color PANEL_ACCENT = new Color(191, 155, 87);
    public static final Color PANEL_TEXT = new Color(225, 222, 215);
    public static final Font MOVES_FONT = new Font("Consolas", Font.PLAIN, 14);
    public static final Font SCORE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private SidePanelFactory() {
    }

    public static JTextArea createMovesArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(MOVES_FONT);
        area.setBackground(PANEL_BACKGROUND);
        area.setForeground(PANEL_TEXT);
        area.setCaretColor(PANEL_TEXT);
        area.setMargin(new Insets(8, 10, 8, 10));
        return area;
    }

    public static JLabel createScoreLabel(String initialText) {
        JLabel label = new JLabel(initialText);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(SCORE_FONT);
        label.setOpaque(true);
        label.setBackground(PANEL_ACCENT);
        label.setForeground(new Color(30, 28, 24));
        label.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        return label;
    }

    public static TitledBorder createTitledBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PANEL_ACCENT, 2), title);
        titledBorder.setTitleFont(TITLE_FONT);
        titledBorder.setTitleColor(PANEL_ACCENT);
        return titledBorder;
    }

    public static JPanel buildSidePanel(TitledBorder titledBorder, JTextArea movesArea, JLabel scoreLabel) {
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

    public static void rescaleSidePanels(JFrame window, int baselineWidth, int baselineHeight,
                                          JPanel westPanel, JPanel eastPanel,
                                          JTextArea whiteMovesArea, JTextArea blackMovesArea,
                                          JLabel whiteScoreLabel, JLabel blackScoreLabel,
                                          TitledBorder whiteBorder, TitledBorder blackBorder) {
        if (baselineWidth <= 0 || baselineHeight <= 0 || westPanel == null || eastPanel == null) return;

        double scaleX = window.getWidth() / (double) baselineWidth;
        double scaleY = window.getHeight() / (double) baselineHeight;
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

        window.revalidate();
        window.repaint();
    }

    public static void drawGameOverOverlay(BufferedImage image, String title, String winnerText) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(Color.WHITE);

        g.setFont(g.getFont().deriveFont(Font.BOLD, 48f));
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (image.getWidth() - titleMetrics.stringWidth(title)) / 2;
        int titleY = image.getHeight() / 2 - 10;
        g.drawString(title, titleX, titleY);

        if (winnerText != null) {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
            FontMetrics winnerMetrics = g.getFontMetrics();
            int winnerX = (image.getWidth() - winnerMetrics.stringWidth(winnerText)) / 2;
            int winnerY = titleY + titleMetrics.getDescent() + winnerMetrics.getAscent() + 15;
            g.drawString(winnerText, winnerX, winnerY);
        }

        g.dispose();
    }
}
