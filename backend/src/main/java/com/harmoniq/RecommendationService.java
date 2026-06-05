package com.harmoniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Service class responsible for generating music recommendations
 * using a content-based filtering approach.
 *
 * This system recommends songs by analyzing similarities between
 * user preferences and song features such as artists, related artists,
 * and genres.
 *
 *
 * @author Harini Baskar 
 */

public class RecommendationService {

    private static final double ARTIST_WEIGHT = 2.0;
    private static final double RELATED_WEIGHT = 1.0;
    private static final double TAG_WEIGHT = 1.5;
    private static final int LIMIT = 10;

    // =========================
    // USER PROFILE
    // =========================
    public static UserProfile buildUserProfile(List<Playlist> playlists) {

        UserProfile profile = new UserProfile();

        for (Playlist p : playlists) {
            for (Song s : p.getSongs()) {

                if (s.getArtists() != null) {
                    for (String a : s.getArtists()) {
                        profile.addArtist(a.toLowerCase());
                    }
                }

                if (s.getRelatedArtists() != null) {
                    for (String r : s.getRelatedArtists()) {
                        profile.addRelatedArtist(r.toLowerCase());
                    }
                }

                if (s.getGenres() != null) {
                    for (String g : s.getGenres()) {
                        profile.addGenre(g.toLowerCase());
                    }
                }
            }
        }

        return profile;
    }

    // =========================
    // PURE RECOMMENDER (NO API CALLS)
    // =========================
    public static List<Song> recommend(UserProfile profile, List<Song> candidates) {

        Map<String, Double> scores = new HashMap<>();
        Map<String, Song> songMap = new HashMap<>();

        // -----------------------
        // INIT CANDIDATES
        // -----------------------
        for (Song s : candidates) {
            if (s == null || s.getId() == null) continue;

            songMap.putIfAbsent(s.getId(), s);
            scores.putIfAbsent(s.getId(), 0.0);
        }

        // -----------------------
        // ARTIST SCORING
        // -----------------------
        for (String artist : profile.getArtistWeights().keySet()) {

            double weight = profile.getArtistWeights().get(artist) * ARTIST_WEIGHT;

            for (Song s : songMap.values()) {

                if (s.getArtists() == null) continue;

                for (String a : s.getArtists()) {
                    if (a != null && a.equalsIgnoreCase(artist)) {
                        scores.put(s.getId(),
                                scores.get(s.getId()) + weight);
                    }
                }
            }
        }

        // -----------------------
        // RELATED ARTIST SCORING
        // -----------------------
        for (String artist : profile.getRelatedArtistWeights().keySet()) {

            double weight = profile.getRelatedArtistWeights().get(artist) * RELATED_WEIGHT;

            for (Song s : songMap.values()) {

                if (s.getArtists() == null) continue;

                for (String a : s.getArtists()) {
                    if (a != null && a.equalsIgnoreCase(artist)) {
                        scores.put(s.getId(),
                                scores.get(s.getId()) + weight);
                    }
                }
            }
        }

        // -----------------------
        // GENRE SCORING
        // -----------------------
        for (Song s : songMap.values()) {

            if (s.getGenres() == null) continue;

            for (String songTag : s.getGenres()) {

                if (songTag == null) continue;

                for (String userTag : profile.getGenreWeights().keySet()) {

                    if (songTag.equalsIgnoreCase(userTag)) {
                        scores.put(
                                s.getId(),
                                scores.get(s.getId()) +
                                        profile.getGenreWeights().get(userTag) * TAG_WEIGHT
                        );
                    }
                }
            }
        }

        // -----------------------
        // SORT
        // -----------------------
        List<Song> ranked = new ArrayList<>(songMap.values());

        ranked.sort((a, b) ->
                Double.compare(
                        scores.getOrDefault(b.getId(), 0.0),
                        scores.getOrDefault(a.getId(), 0.0)
                )
        );

        return ranked.size() > LIMIT ? ranked.subList(0, LIMIT) : ranked;
    }
}