package com.harmoniq;

import java.util.HashMap;
import java.util.Map;


// Simple user profile to track genre preferences
// adds genre to the profile and counts how many times each genre has been added



public class UserProfile {

    private Map<String, Integer> genreCount = new HashMap<>();

    public void addGenre(String genre) {
        genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
    }

    public String getTopGenre() {
        String topGenre = null;
        int max = 0;

        for (Map.Entry<String, Integer> entry : genreCount.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                topGenre = entry.getKey();
            }
        }

        return topGenre;
    }
}




