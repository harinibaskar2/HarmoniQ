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

            // 🔥 Playlists tied to user_id instead of username
            String playlistsTable =
                    "CREATE TABLE IF NOT EXISTS playlists (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user_id INTEGER NOT NULL," +
                            "name TEXT NOT NULL," +
                            "UNIQUE(user_id, name)," +
                            "FOREIGN KEY(user_id) REFERENCES users(id)" +
                            ")";
            stmt.execute(playlistsTable);

            // 🔥 Duplicate-safe playlist songs
            String playlistSongsTable =
                    "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                            "playlist_id INTEGER NOT NULL," +
                            "song_mbid TEXT NOT NULL," +
                            "song_title TEXT NOT NULL," +
                            "artist TEXT," +
                            "PRIMARY KEY (playlist_id, song_mbid)," +
                            "FOREIGN KEY(playlist_id) REFERENCES playlists(id)" +
                            ")";
            stmt.execute(playlistSongsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Get all playlists for a user
    public List<Playlist> getPlaylists(int userId) {
        List<Playlist> playlists = new ArrayList<>();

        String sql = "SELECT * FROM playlists WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Playlist playlist = new Playlist(rs.getString("name"));
                int playlistId = rs.getInt("id");

                // Fetch songs
                String sqlSongs =
                        "SELECT * FROM playlist_songs WHERE playlist_id = ?";

                try (PreparedStatement psSongs = conn.prepareStatement(sqlSongs)) {
                    psSongs.setInt(1, playlistId);
                    ResultSet rsSongs = psSongs.executeQuery();

                    while (rsSongs.next()) {
                        Song s = new Song(
                                rsSongs.getString("song_mbid"),
                                rsSongs.getString("song_title"),
                                Collections.singletonList(
                                        rsSongs.getString("artist")
                                ),
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

    // ✅ Add song (user_id based)
    public void addSong(int userId, String playlistName, Song song) {

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            conn.setAutoCommit(false);

            int playlistId = -1;

            // 1️⃣ Find playlist
            String findPlaylist =
                    "SELECT id FROM playlists WHERE user_id = ? AND name = ?";

            try (PreparedStatement ps = conn.prepareStatement(findPlaylist)) {
                ps.setInt(1, userId);
                ps.setString(2, playlistName);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    playlistId = rs.getInt("id");
                }
            }

            // 2️⃣ Create playlist if it doesn't exist
            if (playlistId == -1) {
                String insertPlaylist =
                        "INSERT INTO playlists(user_id, name) VALUES(?, ?)";

                try (PreparedStatement ps =
                             conn.prepareStatement(insertPlaylist, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, userId);
                    ps.setString(2, playlistName);
                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        playlistId = keys.getInt(1);
                    }
                }
            }

            // 3️⃣ Insert song (duplicate safe)
            String insertSong =
                    "INSERT OR IGNORE INTO playlist_songs" +
                            "(playlist_id, song_mbid, song_title, artist)" +
                            " VALUES(?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSong)) {

                ps.setInt(1, playlistId);
                ps.setString(2, song.getId());
                ps.setString(3, song.getTitle());
                ps.setString(4,
                        song.getArtists().isEmpty()
                                ? "Unknown"
                                : song.getArtists().get(0)
                );

                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


