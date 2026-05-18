package com.harmoniq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:harmoniq.db";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() throws Exception {

        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS artists (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT UNIQUE, " +
            "mbid TEXT UNIQUE)"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS songs (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "title TEXT, " +
            "mbid TEXT UNIQUE, " +
            "artist_id INTEGER, " +
            "FOREIGN KEY(artist_id) REFERENCES artists(id))"
        );

        // ✅ ADD THIS (THIS IS WHAT YOU'RE MISSING)
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS genre_feedback (" +
            "username TEXT NOT NULL, " +
            "genre TEXT NOT NULL, " +
            "score INTEGER DEFAULT 0, " +
            "PRIMARY KEY(username, genre)" +
            ")"
        );

        stmt.close();
        conn.close();

        System.out.println("✅ Database initialized successfully");
    }
}