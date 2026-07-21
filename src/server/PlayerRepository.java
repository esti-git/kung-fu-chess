package server;

import protocol.LoginResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;

public class PlayerRepository {

    private static final String DB_URL = "jdbc:sqlite:players.db";
    private static final int STARTING_RATING = 1200;

    private final Connection connection;

    public PlayerRepository() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "username TEXT PRIMARY KEY, " +
                        "password TEXT NOT NULL, " +
                        "rating INTEGER NOT NULL DEFAULT " + STARTING_RATING + ")");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open players database", e);
        }
    }

    public LoginResult loginOrRegister(String username, String rawPassword) {
        String hash = sha256(rawPassword);
        try {
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT password, rating FROM players WHERE username = ?")) {
                select.setString(1, username);
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        if (!storedHash.equals(hash)) {
                            return new LoginResult(false, 0, "Incorrect password for '" + username + "'.");
                        }
                        return new LoginResult(true, rs.getInt("rating"), null);
                    }
                }
            }

            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO players(username, password, rating) VALUES(?, ?, ?)")) {
                insert.setString(1, username);
                insert.setString(2, hash);
                insert.setInt(3, STARTING_RATING);
                insert.executeUpdate();
            }
            return new LoginResult(true, STARTING_RATING, null);
        } catch (SQLException e) {
            return new LoginResult(false, 0, "Login failed: " + e.getMessage());
        }
    }

    public void updateRating(String username, int newRating) {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE players SET rating = ? WHERE username = ?")) {
            update.setInt(1, newRating);
            update.setString(2, username);
            update.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update rating for " + username, e);
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
