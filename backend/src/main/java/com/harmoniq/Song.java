package com.harmoniq;

import java.util.List;

public class Song {
    private String id;
    private String title;
    private List<String> artists;
    private List<String> genres;

    public Song() {}

    public Song(String id, String title, List<String> artists, List<String> genres) {
        this.id = id;
        this.title = title;
        this.artists = artists;
        this.genres = genres;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getArtists() { return artists; }
    public void setArtists(List<String> artists) { this.artists = artists; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }
}