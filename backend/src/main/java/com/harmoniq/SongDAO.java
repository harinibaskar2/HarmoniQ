package com.harmoniq;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {

    public static List<Song> findSongsByArtistId(int artistId, String artistName) throws Exception {

        String sql = "SELECT title, mbid FROM songs WHERE artist_id = ?";

        List<Song> songs = new ArrayList<Song>();

        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, artistId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            List<String> artists = new ArrayList<String>();
            artists.add(artistName);

            songs.add(new Song(
                    rs.getString("mbid"),
                    rs.getString("title"),
                    artists,
                    new ArrayList<String>()
            ));
        }

        rs.close();
        ps.close();
        conn.close();

        return songs;
    }

    public static void saveSong(String title, String mbid, int artistId) throws Exception {

        String sql = "INSERT OR IGNORE INTO songs(title, mbid, artist_id) VALUES(?, ?, ?)";

        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, title);
        ps.setString(2, mbid);
        ps.setInt(3, artistId);

        ps.executeUpdate();

        ps.close();
        conn.close();
    }
}


