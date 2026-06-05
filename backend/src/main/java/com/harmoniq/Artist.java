package com.harmoniq;

/**
 * Represents a music artist in the HarmoniQ application.
 *
 * <p>This class stores basic artist information, including:
 * <ul>
 *     <li>The artist's name.</li>
 *     <li>The artist's MusicBrainz Identifier (MBID).</li>
 * </ul>
 *
 * <p>The MBID is a unique identifier assigned by the MusicBrainz database,
 * allowing artists to be referenced consistently even when names change
 * or multiple artists share the same name.
 *
 * @author Harini Baskar 
 */
public class Artist {


    private String name;


    private String mbid;


    public Artist(String name, String mbid) {
        this.name = name;
        this.mbid = mbid;
    }

   
    public String getName() {
        return name;
    }


    public String getMbid() {
        return mbid;
    }
}