package com.harmoniq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationService {

    private static final double ARTIST_WEIGHT = 2.0;
    private static final double RELATED_WEIGHT = 1.0;
    private static final double TAG_WEIGHT = 1.5;
    private static final int LIMIT = 10;

    // =========================
    // BUILD USER PROFILE
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

                // IMPORTANT: treat as unified content tags
                if (s.getGenres() != null) {
                    for (String tag : s.getGenres()) {
                        profile.addGenre(tag.toLowerCase());
                    }
                }
            }
        }

        return profile;
    }

    // =========================
    // RECOMMENDER
    // =========================
    public static List<Song> recommend(UserProfile profile) {

        Map<String, Double> scores = new HashMap<>();
        Map<String, Song> songMap = new HashMap<>();

        // -------------------
        // ARTISTS
        // -------------------
        for (String artist : profile.getArtistWeights().keySet()) {

            double weight = profile.getArtistWeights().get(artist) * ARTIST_WEIGHT;

            List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

            for (Song s : songs) {
                songMap.put(s.getId(), s);
                scores.put(s.getId(),
                        scores.getOrDefault(s.getId(), 0.0) + weight);
            }
        }

        // -------------------
        // RELATED ARTISTS
        // -------------------
        for (String artist : profile.getRelatedArtistWeights().keySet()) {

            double weight = profile.getRelatedArtistWeights().get(artist) * RELATED_WEIGHT;

            List<Song> songs = MusicBrainzService.fetchSongsByArtistName(artist);

            for (Song s : songs) {
                songMap.put(s.getId(), s);
                scores.put(s.getId(),
                        scores.getOrDefault(s.getId(), 0.0) + weight);
            }
        }

        // -------------------
        // TAG / GENRE MATCHING
        // -------------------
        for (Song s : songMap.values()) {

            if (s.getGenres() == null) continue;

            for (String songTag : s.getGenres()) {

                if (songTag == null) continue;

                for (String userTag : profile.getGenreWeights().keySet()) {

                    if (songTag.contains(userTag)) {

                        double weight =
                                profile.getGenreWeights().get(userTag) * TAG_WEIGHT;

                        scores.put(
                                s.getId(),
                                scores.getOrDefault(s.getId(), 0.0) + weight
                        );
                    }
                }
            }
        }

        // -------------------
        // SORT
        // -------------------
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