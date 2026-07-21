package view;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ScaledImagePanel extends JPanel {

    private BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    public Dimension getPreferredSize() {
        if (image != null) {
            return new Dimension(image.getWidth(), image.getHeight());
        }
        return super.getPreferredSize();
    }

    private Rectangle computeDisplayRect() {
        int panelW = getWidth();
        int panelH = getHeight();
        if (image == null || panelW <= 0 || panelH <= 0) {
            return new Rectangle(0, 0, panelW, panelH);
        }

        int imgW = image.getWidth();
        int imgH = image.getHeight();
        double scale = Math.min(panelW / (double) imgW, panelH / (double) imgH);

        int drawW = (int) Math.round(imgW * scale);
        int drawH = (int) Math.round(imgH * scale);
        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;
        return new Rectangle(x, y, drawW, drawH);
    }

    public Point panelToImage(int panelX, int panelY) {
        if (image == null) return null;
        Rectangle rect = computeDisplayRect();
        if (rect.width <= 0 || rect.height <= 0 || !rect.contains(panelX, panelY)) {
            return null;
        }
        int imgX = (int) Math.round((panelX - rect.x) * (image.getWidth() / (double) rect.width));
        int imgY = (int) Math.round((panelY - rect.y) * (image.getHeight() / (double) rect.height));
        return new Point(imgX, imgY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;

        Rectangle rect = computeDisplayRect();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(image, rect.x, rect.y, rect.width, rect.height, null);
        g2.dispose();
    }
}
