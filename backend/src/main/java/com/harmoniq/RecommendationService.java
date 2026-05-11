package com.harmoniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationService {

    private static final double ARTIST_WEIGHT = 2.0;
    private static final double RELATED_WEIGHT = 1.0;
    private static final double GENRE_WEIGHT = 1.5;
    private static final int LIMIT = 10;

    // ==================================
    // BUILD USER PROFILE
    // ==================================
    public static UserProfile buildUserProfile(List<Playlist> playlists) {

        UserProfile profile = new UserProfile();

        for (Playlist p : playlists) {
            for (Song s : p.getSongs()) {

                // artists
                if (s.getArtists() != null) {
                    for (String artist : s.getArtists()) {
                        profile.addArtist(artist);
                    }
                }

                // related artists
                if (s.getRelatedArtists() != null) {
                    for (String r : s.getRelatedArtists()) {
                        profile.addRelatedArtist(r);
                    }
                }

                // genres ⭐ IMPORTANT
                if (s.getGenres() != null) {
                    for (String g : s.getGenres()) {
                        profile.addGenre(g.toLowerCase());
                    }
                }
            }
        }

        return profile;
    }

    // ==================================
    // MAIN RECOMMENDATION ENGINE
    // ==================================
    public static List<Song> recommend(UserProfile profile) {

        Map<String, Double> songScores = new HashMap<>();
        Map<String, Song> songMap = new HashMap<>();

        // =========================
        // ARTIST-BASED SCORING
        // =========================
        for (String artist : profile.getArtistWeights().keySet()) {

            double weight = profile.getArtistWeights().get(artist) * ARTIST_WEIGHT;

            List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

            for (Song s : songs) {
                songScores.put(s.getId(),
                        songScores.getOrDefault(s.getId(), 0.0) + weight);
                songMap.put(s.getId(), s);
            }
        }

        // =========================
        // RELATED ARTIST SCORING
        // =========================
        for (String artist : profile.getRelatedArtistWeights().keySet()) {

            double weight = profile.getRelatedArtistWeights().get(artist) * RELATED_WEIGHT;

            List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

            for (Song s : songs) {
                songScores.put(s.getId(),
                        songScores.getOrDefault(s.getId(), 0.0) + weight);
                songMap.put(s.getId(), s);
            }
        }

        // =========================
        // ⭐ REAL GENRE MATCHING (FIX)
        // =========================
        for (String genre : profile.getGenreWeights().keySet()) {

            double weight = profile.getGenreWeights().get(genre) * GENRE_WEIGHT;

            // FIX: get songs properly from artists instead of title search fallback
            for (Song s : songMap.values()) {

                if (s.getGenres() == null) continue;

                for (String g : s.getGenres()) {

                    if (g != null && g.toLowerCase().contains(genre)) {

                        songScores.put(s.getId(),
                                songScores.getOrDefault(s.getId(), 0.0) + weight);
                    }
                }
            }
        }

        // =========================
        // RANKING
        // =========================
        List<Song> ranked = new ArrayList<>(songMap.values());

        ranked.sort((a, b) ->
                Double.compare(
                        songScores.getOrDefault(b.getId(), 0.0),
                        songScores.getOrDefault(a.getId(), 0.0)
                )
        );

        return ranked.size() > LIMIT ? ranked.subList(0, LIMIT) : ranked;
    }
}

