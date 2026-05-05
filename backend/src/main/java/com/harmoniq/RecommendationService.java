package com.harmoniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    Map<String, Song> uniqueSongs = new HashMap<>();

    Map<String, Integer> direct = profile.getArtistWeights();
    Map<String, Integer> related = profile.getRelatedArtistWeights();

    Set<String> allArtists = new HashSet<>();
    allArtists.addAll(direct.keySet());
    allArtists.addAll(related.keySet());

    for (String artist : allArtists) {

        List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

        double weight = direct.getOrDefault(artist, 0) * DIRECT_WEIGHT +
                        related.getOrDefault(artist, 0) * RELATED_WEIGHT;

        for (Song s : songs) {

            songScores.put(
                    s.getId(),
                    songScores.getOrDefault(s.getId(), 0.0) + weight
            );

            uniqueSongs.put(s.getId(), s);
        }
    }

    List<Song> ranked = new ArrayList<>(uniqueSongs.values());

    ranked.sort((a, b) ->
            Double.compare(
                    songScores.getOrDefault(b.getId(), 0.0),
                    songScores.getOrDefault(a.getId(), 0.0)
            )
    );

    return ranked.size() > LIMIT ? ranked.subList(0, LIMIT) : ranked;
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