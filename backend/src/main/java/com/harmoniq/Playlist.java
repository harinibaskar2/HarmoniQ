package com.harmoniq;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a user playlist containing a collection of songs.
 *
 * A playlist has a name and stores multiple Song objects.
 * Songs can be added dynamically to the playlist.
 *
 * @author Harini Baskar
 */

public class Playlist {
    private String name;
    private List<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        songs.add(song);
    }
}