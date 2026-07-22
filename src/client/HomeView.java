package client;

import client.logging.ClientLog;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class HomeView {

    private JFrame frame;
    private JButton playButton;
    private JButton roomButton;
    private JLabel statusLabel;
    private Runnable onPlay;
    private Consumer<String> onCreateRoom;
    private Consumer<String> onJoinRoom;

    public void setOnPlay(Runnable onPlay) {
        this.onPlay = onPlay;
    }

    public void setOnCreateRoom(Consumer<String> onCreateRoom) {
        this.onCreateRoom = onCreateRoom;
    }

    public void setOnJoinRoom(Consumer<String> onJoinRoom) {
        this.onJoinRoom = onJoinRoom;
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

            roomButton = new JButton("Room");
            roomButton.setFont(roomButton.getFont().deriveFont(Font.BOLD, 20f));
            roomButton.addActionListener(e -> showRoomDialog());

            JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            buttonsPanel.add(playButton);
            buttonsPanel.add(roomButton);

            statusLabel = new JLabel(" ", SwingConstants.CENTER);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(buttonsPanel, BorderLayout.CENTER);
            panel.add(statusLabel, BorderLayout.SOUTH);

            frame.add(panel);
            frame.setMinimumSize(new Dimension(280, 200));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private void showRoomDialog() {
        JDialog dialog = new JDialog(frame, "Room", true);
        JLabel promptLabel = new JLabel("Room name (Create: blank for a random name; Join: required):");
        JTextField roomIdField = new JTextField(10);

        JButton createBtn = new JButton("Create");
        JButton joinBtn = new JButton("Join");
        JButton cancelBtn = new JButton("Cancel");

        createBtn.addActionListener(e -> {
            String roomName = roomIdField.getText().trim();
            dialog.dispose();
            ClientLog.info("Room: Create clicked, name=" + roomName);
            if (onCreateRoom != null) onCreateRoom.accept(roomName);
        });
        joinBtn.addActionListener(e -> {
            String roomId = roomIdField.getText().trim();
            if (roomId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Enter a room ID to join.");
                return;
            }
            dialog.dispose();
            ClientLog.info("Room: Join clicked, roomId=" + roomId);
            if (onJoinRoom != null) onJoinRoom.accept(roomId);
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
        fieldPanel.add(promptLabel, BorderLayout.NORTH);
        fieldPanel.add(roomIdField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(createBtn);
        buttonsPanel.add(joinBtn);
        buttonsPanel.add(cancelBtn);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(fieldPanel, BorderLayout.CENTER);
        content.add(buttonsPanel, BorderLayout.SOUTH);
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dialog.add(content);
        dialog.setMinimumSize(new Dimension(320, 140));
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    public void showSearching() {
        SwingUtilities.invokeLater(() -> {
            setButtonsEnabled(false);
            if (statusLabel != null) statusLabel.setText("Searching for opponent...");
        });
    }

    public void showWaitingForRoom(String message) {
        SwingUtilities.invokeLater(() -> {
            setButtonsEnabled(false);
            if (statusLabel != null) statusLabel.setText(message);
        });
    }

    public void onSeekTimeout(String message) {
        SwingUtilities.invokeLater(() -> {
            setButtonsEnabled(true);
            if (statusLabel != null) statusLabel.setText(" ");
            JOptionPane.showMessageDialog(frame,
                    message == null || message.isBlank() ? "Couldn't find a match." : message,
                    "No match found", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void onRoomError(String message) {
        SwingUtilities.invokeLater(() -> {
            setButtonsEnabled(true);
            if (statusLabel != null) statusLabel.setText(" ");
            JOptionPane.showMessageDialog(frame,
                    message == null || message.isBlank() ? "Couldn't join that room." : message,
                    "Room error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        if (playButton != null) playButton.setEnabled(enabled);
        if (roomButton != null) roomButton.setEnabled(enabled);
    }

    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) frame.dispose();
        });
    }
}
