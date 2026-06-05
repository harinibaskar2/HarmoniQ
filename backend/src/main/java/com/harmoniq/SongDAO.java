package com.harmoniq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;



/**
 * Data Access Object (DAO) for managing song records in the database.
 *
 * Provides methods to retrieve songs by artist or title, and to
 * store song information in the SQLite database.
 *
 * Acts as the persistence layer between the application and the
 * songs database table.
 *
 * @author Harini Baskar 
 */
public class SongDAO {

    public static List<Song> findSongsByArtistId(int artistId) throws Exception {
        //  Fetch mbid, title
        String sql = "SELECT DISTINCT mbid, title FROM songs WHERE artist_id = ?";
    
        List<Song> songs = new ArrayList<>();
    
        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, artistId);
    
        ResultSet rs = ps.executeQuery();
    
        // Get actual artist name from DB
        String artistName = ArtistDAO.findArtistNameById(artistId);
    
        while (rs.next()) {
            List<String> artists = new ArrayList<>();
            artists.add(artistName);
    
            songs.add(new Song(
                    rs.getString("mbid"),
                    rs.getString("title"),
                    artists,
                    new ArrayList<>(), 
                    new ArrayList<>()
            ));
        }
    
        rs.close();
        ps.close();
        conn.close();
    
        return songs;
    }


    public static List<Song> findSongsByTitle(String title) throws Exception {
        String sql = "SELECT DISTINCT mbid, title, artist_id FROM songs WHERE title LIKE ?";
        List<Song> songs = new ArrayList<>();
    
        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "%" + title + "%");
    
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            // fetch artist name from artist_id
            String artistName = ArtistDAO.findArtistNameById(rs.getInt("artist_id"));
            List<String> artists = new ArrayList<>();
            artists.add(artistName);
    
            songs.add(new Song(
                    rs.getString("mbid"),
                    rs.getString("title"),
                    artists,
                    new ArrayList<>(), 
                    new ArrayList<>()
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

