package com.harmoniq;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {

    // artist -> weight (how much user likes them)
    private Map<String, Integer> artistWeights = new HashMap<>();

    public void addArtist(String artist) {
        artistWeights.put(artist,
                artistWeights.getOrDefault(artist, 0) + 1);
    }

    public Map<String, Integer> getArtistWeights() {
        return artistWeights;
    }

    public String getTopArtist() {
        return artistWeights.entrySet()
                .stream()
                .max((a, b) -> a.getValue() - b.getValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}

