package com.harmoniq;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistDAO {

    private static final String DB_URL = "jdbc:sqlite:users.db";

    public PlaylistDAO() {
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

            // Table for playlist songs
            String sqlSongs = "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                              "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                              "playlist_id INTEGER NOT NULL," +
                              "song_mbid TEXT NOT NULL," +
                              "song_title TEXT NOT NULL," +
                              "artist TEXT," +
                              "FOREIGN KEY(playlist_id) REFERENCES playlists(id)" +
                              ")";
            stmt.execute(sqlSongs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch all playlists for a user
    public List<Playlist> getPlaylists(String username) {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlists WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Playlist playlist = new Playlist(rs.getString("name"));
                int playlistId = rs.getInt("id");

                // fetch songs
                String sqlSongs = "SELECT * FROM playlist_songs WHERE playlist_id = ?";
                try (PreparedStatement psSongs = conn.prepareStatement(sqlSongs)) {
                    psSongs.setInt(1, playlistId);
                    ResultSet rsSongs = psSongs.executeQuery();
                    while (rsSongs.next()) {
                        Song s = new Song(
                            rsSongs.getString("song_mbid"),
                            rsSongs.getString("song_title"),
                            Collections.singletonList(rsSongs.getString("artist")),
                            new ArrayList<>()
                        );
                        playlist.addSong(s);
                    }
                }

                playlists.add(playlist);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlists;
    }

    // Add a song to a user's playlist
    public void addSong(String username, String playlistName, Song song) {
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
                String insertPlaylist = "INSERT INTO playlists(username, name) VALUES(?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertPlaylist, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, playlistName);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) playlistId = keys.getInt(1);
                }
            }

            // 2️⃣ Insert song
            String insertSong = "INSERT INTO playlist_songs(playlist_id, song_mbid, song_title, artist) VALUES(?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSong)) {
                ps.setInt(1, playlistId);
                ps.setString(2, song.getId());
                ps.setString(3, song.getTitle());
                ps.setString(4, song.getArtists().isEmpty() ? "Unknown" : song.getArtists().get(0));
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


