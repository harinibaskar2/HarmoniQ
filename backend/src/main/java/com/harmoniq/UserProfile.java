package com.harmoniq;

import java.util.HashMap;
import java.util.Map;



/**
 * Represents a user preference profile used for music recommendations.
 *
 * Stores weighted information about the user's listening habits,
 * including artists, related artists, and genres.
 *
 * These weights are used in the content-based filtering algorithm
 * to generate personalized song recommendations.
 *
 * @author Harini Baskar
 */

public class UserProfile {

    // artist -> weight (how much user likes them)
    private Map<String, Integer> artistWeights = new HashMap<>();

    // related artist signal 
    private Map<String, Integer> relatedArtistWeights = new HashMap<>();

    private Map<String, Integer> genreWeights = new HashMap<>();



    // =========================
    // DIRECT ARTISTS
    // =========================
    public void addArtist(String artist) {
        if (artist == null) return;

        artistWeights.put(
                artist,
                artistWeights.getOrDefault(artist, 0) + 1
        );
    }

    // =========================
    // RELATED ARTISTS
    // =========================
    public void addRelatedArtist(String artist) {
        if (artist == null) return;

        relatedArtistWeights.put(
                artist,
                relatedArtistWeights.getOrDefault(artist, 0) + 1
        );
    }

    public Map<String, Integer> getArtistWeights() {
        return artistWeights;
    }

    public Map<String, Integer> getRelatedArtistWeights() {
        return relatedArtistWeights;
    }

    // =========================
    // TOP ARTIST (DIRECT SIGNAL)
    // =========================
    public String getTopArtist() {

        return artistWeights.entrySet()
                .stream()
                .max((a, b) -> a.getValue() - b.getValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    // =========================
    // TOP RELATED ARTIST 
    // =========================
    public String getTopRelatedArtist() {

        return relatedArtistWeights.entrySet()
                .stream()
                .max((a, b) -> a.getValue() - b.getValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    


    public void addGenre(String genre) {
        if (genre == null) return;
    
        genreWeights.put(
            genre,
            genreWeights.getOrDefault(genre, 0) + 1
        );
    }
    
    public Map<String, Integer> getGenreWeights() {
        return genreWeights;
    }


    public String getTopGenre() {
        return genreWeights.entrySet()
                .stream()
                .max((a, b) -> a.getValue() - b.getValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
} 
 


