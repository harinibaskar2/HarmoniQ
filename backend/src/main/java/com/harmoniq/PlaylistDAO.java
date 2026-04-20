package com.harmoniq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaylistDAO {

    private static final String DB_URL =
    "jdbc:sqlite:/Users/harinibaskar/Desktop/College3rdYear/winterquarter/HarmoniQ/backend/users.db";

    public PlaylistDAO() {


        System.out.println("🔥 DB PATH USED = " +
            new java.io.File("users.db").getAbsolutePath());



        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Table for playlists
            String sql = "CREATE TABLE IF NOT EXISTS playlists (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "username TEXT NOT NULL," +
                         "name TEXT NOT NULL," +
                         "UNIQUE(username, name)" +
                         ")";
            stmt.execute(sql);

            // Table for playlist songs (UPDATED with genres column)
            String sqlSongs = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                              "playlist_id INTEGER NOT NULL," +
                              "song_mbid TEXT NOT NULL," +
                              "song_title TEXT NOT NULL," +
                              "artist TEXT," +
                              "genres TEXT," +   // 🔥 ADDED THIS
                              "FOREIGN KEY(playlist_id) REFERENCES playlists(id)" +
                              ")";
            stmt.execute(sqlSongs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch all playlists for a user
    public List<Playlist> getPlaylists(String username) {

        System.out.println("\n=== GET PLAYLISTS DEBUG ===");
        System.out.println("Incoming username = [" + username + "]");
        System.out.println("DB file = " + new java.io.File("users.db").getAbsolutePath());
    
        List<Playlist> playlists = new ArrayList<>();
    
        String sql = "SELECT * FROM playlists WHERE username = ?";
    
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
    
            ps.setString(1, username);
    
            ResultSet rs = ps.executeQuery();
    
            boolean foundAny = false;
    
            while (rs.next()) {
                foundAny = true;
    
                String dbUser = rs.getString("username");
                System.out.println("MATCHED ROW USER = " + dbUser);
    
                Playlist playlist = new Playlist(rs.getString("name"));
                int playlistId = rs.getInt("id");
    
                String sqlSongs = "SELECT * FROM playlist_songs WHERE playlist_id = ?";
    
                try (PreparedStatement psSongs = conn.prepareStatement(sqlSongs)) {
                    psSongs.setInt(1, playlistId);
    
                    ResultSet rsSongs = psSongs.executeQuery();
    
                    while (rsSongs.next()) {
    
                        String genreStr = rsSongs.getString("genres");
    
                        List<String> genres = new ArrayList<>();
                        if (genreStr != null && !genreStr.isEmpty()) {
                            genres = Arrays.asList(genreStr.split(","));
                        }
    
                        Song s = new Song(
                            rsSongs.getString("song_mbid"),
                            rsSongs.getString("song_title"),
                            Collections.singletonList(rsSongs.getString("artist")),
                            genres
                        );
    
                        playlist.addSong(s);
                    }
                }
    
                playlists.add(playlist);
            }
    
            if (!foundAny) {
                System.out.println("❌ NO MATCHING PLAYLISTS FOUND FOR USER: " + username);
            } else {
                System.out.println("✅ Playlists found: " + playlists.size());
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return playlists;
    }

    // Add a song to a user's playlist
    public void addSong(String username, String playlistName, Song song) {

       

        System.out.println("🔥 HIT addSong()");
        System.out.println("🔥 DB PATH USED = " +
            new java.io.File("users.db").getAbsolutePath());

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            // 1️⃣ Get or create playlist
            int playlistId = -1;
            String selectPlaylist = "SELECT id FROM playlists WHERE username = ? AND name = ?";

            try (PreparedStatement ps = conn.prepareStatement(selectPlaylist)) {
                ps.setString(1, username);
                ps.setString(2, playlistName);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    playlistId = rs.getInt("id");
                }
            }

            if (playlistId == -1) {
                String insertPlaylist =
                    "INSERT INTO playlists(username, name) VALUES(?, ?)";

                try (PreparedStatement ps =
                         conn.prepareStatement(insertPlaylist,
                         Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, playlistName);
                    int rows = ps.executeUpdate();
                    System.out.println("ROWS INSERTED = " + rows);

                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) playlistId = keys.getInt(1);
                }
            }

            // 2️⃣ Insert song (NOW INCLUDES GENRES)
            String insertSong =
                "INSERT INTO playlist_songs(" +
                "playlist_id, song_mbid, song_title, artist, genres" +
                ") VALUES(?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSong)) {

                ps.setInt(1, playlistId);
                ps.setString(2, song.getId());
                ps.setString(3, song.getTitle());
                ps.setString(4,
                    song.getArtists().isEmpty()
                        ? "Unknown"
                        : song.getArtists().get(0)
                );

                // 🔥 STORE GENRES
                ps.setString(5, String.join(",", song.getGenres()));

                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}