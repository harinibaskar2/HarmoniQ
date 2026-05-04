package com.harmoniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecommendationService {

    private static final double DIRECT_WEIGHT = 2.0;
    private static final double RELATED_WEIGHT = 1.0;
    private static final int LIMIT = 10;

    // =========================
    // SIMPLE CACHE (REFRESH SUPPORT)
    // =========================
    private static List<Song> cachedRecommendations = new ArrayList<>();
    private static UserProfile cachedProfile = null;

    // ==================================
    // BUILD USER PROFILE
    // ==================================
    public static UserProfile buildUserProfile(List<Playlist> playlists) {

        UserProfile profile = new UserProfile();

        for (Playlist p : playlists) {
            for (Song s : p.getSongs()) {

                if (s.getArtists() != null) {
                    for (String artist : s.getArtists()) {
                        profile.addArtist(artist);
                    }
                }

                if (s.getRelatedArtists() != null) {
                    for (String r : s.getRelatedArtists()) {
                        profile.addRelatedArtist(r);
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

        Map<String, Integer> direct = profile.getArtistWeights();
        Map<String, Integer> related = profile.getRelatedArtistWeights();

        // DIRECT ARTISTS
        scoreSongs(direct, DIRECT_WEIGHT, songScores);

        // RELATED ARTISTS
        scoreSongs(related, RELATED_WEIGHT, songScores);

        // COLLECT UNIQUE SONGS
        Map<String, Song> uniqueSongs = new HashMap<>();

        collectSongs(direct.keySet(), uniqueSongs);
        collectSongs(related.keySet(), uniqueSongs);

        List<Song> ranked = new ArrayList<>(uniqueSongs.values());

        // SORT BY SCORE
        ranked.sort((a, b) ->
                Double.compare(
                        songScores.getOrDefault(b.getId(), 0.0),
                        songScores.getOrDefault(a.getId(), 0.0)
                )
        );

        // LIMIT RESULTS
        if (ranked.size() > LIMIT) {
            ranked = ranked.subList(0, LIMIT);
        }

        // CACHE RESULT
        cachedRecommendations = ranked;
        cachedProfile = profile;

        return ranked;
    }

    // ==================================
    // REFRESH RECOMMENDATIONS (NEW)
    // ==================================
    public static List<Song> refreshRecommendations(List<Playlist> playlists) {

        System.out.println("Refreshing recommendations...");

        UserProfile newProfile = buildUserProfile(playlists);
        return recommend(newProfile);
    }

    // Optional: return cached results instantly
    public static List<Song> getCachedRecommendations() {
        return cachedRecommendations;
    }

    // Optional: force full reset
    public static void clearCache() {
        cachedRecommendations.clear();
        cachedProfile = null;
    }

    // ==================================
    // HELPER: SCORE SONGS
    // ==================================
    private static void scoreSongs(Map<String, Integer> artists,
                                    double weightMultiplier,
                                    Map<String, Double> songScores) {

        for (Map.Entry<String, Integer> entry : artists.entrySet()) {

            String artist = entry.getKey();
            double weight = entry.getValue() * weightMultiplier;

            List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

            for (Song s : songs) {
                songScores.put(
                        s.getId(),
                        songScores.getOrDefault(s.getId(), 0.0) + weight
                );
            }
        }
    }

    // ==================================
    // HELPER: COLLECT SONGS
    // ==================================
    private static void collectSongs(Set<String> artists,
                                     Map<String, Song> uniqueSongs) {

        for (String artist : artists) {
            List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

            for (Song s : songs) {
                uniqueSongs.put(s.getId(), s);
            }
        }
    }
}