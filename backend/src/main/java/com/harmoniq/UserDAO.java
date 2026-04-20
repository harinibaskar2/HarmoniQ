package com.harmoniq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {


    private static final String DB_URL =
    "jdbc:sqlite:/Users/harinibaskar/Desktop/College3rdYear/winterquarter/HarmoniQ/backend/users.db";

    public UserDAO() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "passwordHash TEXT NOT NULL" +
                    ")";
            stmt.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addUser(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, passwordHash) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("passwordHash")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ✅ NEW helper
    public Integer getUserId(String username) {
        User user = getUserByUsername(username);
        return (user != null) ? user.getId() : null;
    }
}