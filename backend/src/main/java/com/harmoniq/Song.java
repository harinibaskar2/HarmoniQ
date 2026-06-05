package com.harmoniq;

import java.util.List;

public class Song {
    private String id;
    private String title;
    private List<String> artists;
    private List<String> genres;
    private List<String> relatedArtists;


    /**
 * Represents a song in the HarmoniQ system.
 *
 * A Song contains metadata such as its ID, title, artists,
 * genres, and related artists. This information is used for
 * searching, storing, and generating recommendations.
 *
 * @author Harini Baskar
 */

    public Song() {}

    public Song(String id, String title, List<String> artists, List<String> genres, List<String> relatedArtists) {
        this.id = id;
        this.title = title;
        this.artists = artists;
        this.genres = genres;
        this.relatedArtists = relatedArtists;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getArtists() { return artists; }
    public void setArtists(List<String> artists) { this.artists = artists; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public List<String> getRelatedArtists() { 
        return relatedArtists; 
    }
    public void setRelatedArtists(List<String> relatedArtists) {
        this.relatedArtists = relatedArtists;
    }
}


