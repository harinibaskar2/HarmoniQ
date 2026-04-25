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

    private static final String DB_URL = DBConfig.DB_URL;

    public PlaylistDAO() {

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // ======================
            // PLAYLISTS TABLE
            // ======================
            String sql =
                "CREATE TABLE IF NOT EXISTS playlists (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "name TEXT NOT NULL," +
                "UNIQUE(username, name)" +
                ")";

            stmt.execute(sql);

            // ======================
            // SONGS TABLE (NO GENRES)
            // ======================
            String sqlSongs =
                "CREATE TABLE IF NOT EXISTS playlist_songs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "playlist_id INTEGER NOT NULL," +
                "song_mbid TEXT NOT NULL," +
                "song_title TEXT NOT NULL," +
                "artist TEXT," +
                "FOREIGN KEY(playlist_id) REFERENCES playlists(id)" +
                ")";

            stmt.execute(sqlSongs);

            System.out.println("✅ Tables initialized");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // GET PLAYLISTS
    // =====================================================
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

                System.out.println("Playlist found: " + playlist.getName() + " (id=" + playlistId + ")");

                // ======================
                // GET SONGS
                // ======================
                String sqlSongs =
                    "SELECT * FROM playlist_songs WHERE playlist_id = ?";

                try (PreparedStatement psSongs = conn.prepareStatement(sqlSongs)) {

                    psSongs.setInt(1, playlistId);

                    ResultSet rsSongs = psSongs.executeQuery();

                    while (rsSongs.next()) {

                        System.out.println("SONG FOUND → " + rsSongs.getString("song_title"));

                        Song song = new Song(
                            rsSongs.getString("song_mbid"),
                            rsSongs.getString("song_title"),
                            Collections.singletonList(rsSongs.getString("artist")),
                            new ArrayList<>()
                        );

                        playlist.addSong(song);
                    }
                }

                System.out.println("TOTAL SONGS IN PLAYLIST " + playlistId +
                        " = " + playlist.getSongs().size());

                playlists.add(playlist);
            }

            System.out.println("✅ TOTAL PLAYLISTS = " + playlists.size());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playlists;
    }

    // =====================================================
    // ADD SONG
    // =====================================================
    public void addSong(String username, String playlistName, Song song) {

        System.out.println("\n🔥 ADD SONG CALLED");

        try (Connection conn = DriverManager.getConnection(DB_URL)) {

            int playlistId = -1;

            // ======================
            // FIND PLAYLIST
            // ======================
            String select =
                "SELECT id FROM playlists WHERE username = ? AND name = ?";

            try (PreparedStatement ps = conn.prepareStatement(select)) {

                ps.setString(1, username);
                ps.setString(2, playlistName);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    playlistId = rs.getInt("id");
                }
            }

            // ======================
            // CREATE IF NOT EXISTS
            // ======================
            if (playlistId == -1) {

                String insertPlaylist =
                    "INSERT INTO playlists(username, name) VALUES(?, ?)";

                try (PreparedStatement ps =
                     conn.prepareStatement(insertPlaylist,
                     Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, username);
                    ps.setString(2, playlistName);

                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();

                    if (keys.next()) {
                        playlistId = keys.getInt(1);
                    }
                }
            }

            System.out.println("FINAL playlistId = " + playlistId);

            // ======================
            // INSERT SONG
            // ======================
            String insertSong =
                "INSERT INTO playlist_songs(" +
                "playlist_id, song_mbid, song_title, artist" +
                ") VALUES(?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSong)) {

                ps.setInt(1, playlistId);
                ps.setString(2, song.getId() == null ? "unknown" : song.getId());
                ps.setString(3, song.getTitle() == null ? "Untitled" : song.getTitle());
                ps.setString(4,
                    (song.getArtists() == null || song.getArtists().isEmpty())
                        ? "Unknown"
                        : song.getArtists().get(0)
                );

                int rows = ps.executeUpdate();

                System.out.println("ROWS INSERTED = " + rows);
            }

            // ======================
            // VERIFY INSERT
            // ======================
            debugSongs();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // DEBUG ALL SONGS
    // =====================================================
    public void debugSongs() {

        String sql = "SELECT * FROM playlist_songs";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== ALL SONGS IN DB ===");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println(
                    rs.getInt("playlist_id") + " | " +
                    rs.getString("song_title") + " | " +
                    rs.getString("artist")
                );
            }

            if (!found) {
                System.out.println("❌ NO SONGS IN DATABASE");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}