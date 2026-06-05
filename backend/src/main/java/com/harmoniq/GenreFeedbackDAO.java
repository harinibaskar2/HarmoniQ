package com.harmoniq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




/**
 * DAO class for storing and retrieving user genre feedback.
 *
 * Tracks likes and dislikes for music genres and provides
 * methods to retrieve a user's preferred genres based on
 * accumulated feedback scores.
 *
 * @author Harini Baskar
 */



public class GenreFeedbackDAO {

    private static final String DB_URL = "jdbc:sqlite:harmoniq.db";

    // =========================
    // SAVE LIKE
    // =========================
    public static void like(String username, String genre) {
        updateScore(username, genre, 2); 
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
    // TOP GENRE
    // =========================
    public static List<String> getTopGenres(String username) {

        Map<String, Integer> scores = getUserScores(username);
    
        System.out.println("GENRE SCORES:");
        scores.forEach((k, v) ->
            System.out.println(k + " = " + v)
        );
    
        return scores.entrySet()
                .stream()
                .filter(e -> e.getValue() > 0) // ignore negative/neutral
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}


