package client;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/** Minimal post-login lobby: a single Play button that queues the player for matchmaking. Shown
 *  after console login succeeds, replaced by the board window once the server matches a game. */
public class HomeView {

    private JFrame frame;
    private JButton playButton;
    private JLabel statusLabel;
    private Runnable onPlay;

    public void setOnPlay(Runnable onPlay) {
        this.onPlay = onPlay;
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Kung Fu Chess - Home");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            playButton = new JButton("Play");
            playButton.setFont(playButton.getFont().deriveFont(Font.BOLD, 20f));
            playButton.addActionListener(e -> {
                if (onPlay != null) onPlay.run();
            });

            statusLabel = new JLabel(" ", SwingConstants.CENTER);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(playButton, BorderLayout.CENTER);
            panel.add(statusLabel, BorderLayout.SOUTH);

            frame.add(panel);
            frame.setMinimumSize(new Dimension(280, 160));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public void showSearching() {
        SwingUtilities.invokeLater(() -> {
            if (playButton != null) playButton.setEnabled(false);
            if (statusLabel != null) statusLabel.setText("Searching for opponent...");
        });
    }

    /** "seekTimeout" handler: no match was found within the server's seek window. */
    public void onSeekTimeout(String message) {
        SwingUtilities.invokeLater(() -> {
            if (playButton != null) playButton.setEnabled(true);
            if (statusLabel != null) statusLabel.setText(" ");
            JOptionPane.showMessageDialog(frame,
                    message == null || message.isBlank() ? "Couldn't find a match." : message,
                    "No match found", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) frame.dispose();
        });
    }
}
