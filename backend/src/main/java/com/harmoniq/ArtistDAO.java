package com.harmoniq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Data Access Object (DAO) responsible for performing database operations
 * related to artists.
 *
 * <p>This class provides methods for:
 * <ul>
 *     <li>Finding an artist's database ID by name.</li>
 *     <li>Finding an artist's name by database ID.</li>
 *     <li>Saving new artist records to the database.</li>
 * </ul>
 *
 * @author Harini Baskar

 */
public class ArtistDAO {


    public static Integer findArtistIdByName(String name) throws Exception {

        String sql = "SELECT id FROM artists WHERE LOWER(name) = LOWER(?)";

        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);

        ResultSet rs = ps.executeQuery();
        Integer id = null;

        if (rs.next()) {
            id = rs.getInt("id");
        }

        rs.close();
        ps.close();
        conn.close();

        return id;
    }


    public static String findArtistNameById(int artistId) throws Exception {
        String sql = "SELECT name FROM artists WHERE id = ?";
        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, artistId);

        ResultSet rs = ps.executeQuery();
        String name = null;

        if (rs.next()) {
            name = rs.getString("name");
        }

        rs.close();
        ps.close();
        conn.close();

        return name;
    }


    public static int saveArtist(String name, String mbid) throws Exception {

        String sql = "INSERT INTO artists(name, mbid) VALUES(?, ?)";

        Connection conn = Database.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, name);
        ps.setString(2, mbid);
        ps.executeUpdate();

        ResultSet keys = ps.getGeneratedKeys();
        keys.next();
        int id = keys.getInt(1);

        keys.close();
        ps.close();
        conn.close();

        return id;
    }
}