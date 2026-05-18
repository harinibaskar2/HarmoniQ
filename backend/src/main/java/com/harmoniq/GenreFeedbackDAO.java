package com.harmoniq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class GenreFeedbackDAO {

    private static final String DB_URL = "jdbc:sqlite:harmoniq.db";

    // =========================
    // SAVE LIKE
    // =========================
    public static void like(String username, String genre) {
        updateScore(username, genre, 1);
    }

    // =========================
    // SAVE DISLIKE
    // =========================
    public static void dislike(String username, String genre) {
        updateScore(username, genre, -1);
    }

    // =========================
    // CORE UPDATE LOGIC
    // =========================
    private static void updateScore(String username, String genre, int delta) {

        String sql =
            "INSERT INTO genre_feedback (username, genre, score) " +
            "VALUES (?, ?, ?) " +
            "ON CONFLICT(username, genre) " +
            "DO UPDATE SET score = score + ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, genre);
            stmt.setInt(3, delta);
            stmt.setInt(4, delta);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // GET USER GENRE SCORES
    // =========================
    public static Map<String, Integer> getUserScores(String username) {

        Map<String, Integer> scores = new HashMap<>();

        String sql =
            "SELECT genre, SUM(score) as total " +
            "FROM genre_feedback " +
            "WHERE username = ? " +
            "GROUP BY genre";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                scores.put(
                    rs.getString("genre"),
                    rs.getInt("total")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return scores;
    }

    // =========================
    // TOP GENRE (SAFE)
    // =========================
    public static String getTopGenre(String username) {

        Map<String, Integer> scores = getUserScores(username);

        return scores.entrySet()
                .stream()
                .max((a, b) -> Integer.compare(a.getValue(), b.getValue()))
                .map(Map.Entry::getKey)
                .orElse("");
    }
}