package com.harmoniq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;




/**
 * Service class responsible for generating candidate songs
 * for the recommendation system.
 *
 * Uses artists from a user's profile to retrieve songs and
 * build a list of unique recommendation candidates.
 *
 * @author Harini Baskar 
 */


public class CandidateService {

    private static final int MAX_SONGS_PER_ARTIST = 20;
    private static final int MAX_TOTAL_ARTISTS = 30;





    public static List<Song> buildCandidates(UserProfile profile) {

        Set<String> seenArtists = new HashSet<>();
        Set<String> seenSongIds = new HashSet<>();
        List<Song> candidates = new ArrayList<>();

        Queue<String> queue = new LinkedList<>();

        queue.addAll(profile.getArtistWeights().keySet());
        queue.addAll(profile.getRelatedArtistWeights().keySet());

        System.out.println("\n===== BUILDING CANDIDATES =====");
        System.out.println("Seed artists: " + queue);

        while (!queue.isEmpty() && seenArtists.size() < MAX_TOTAL_ARTISTS) {

            String artist = queue.poll();
            if (artist == null) continue;

            String normalized = artist.toLowerCase().trim();

            if (!seenArtists.add(normalized)) continue;

            System.out.println("\n Fetching songs for artist: " + normalized);

            List<Song> songs =
                    MusicBrainzCachedService.getSongsByArtist(normalized);

            if (songs == null || songs.isEmpty()) {
                System.out.println(" No songs returned for: " + normalized);
                continue;
            }

            System.out.println("Songs returned: " + songs.size());

            // DEBUG: show raw songs from API layer
            for (Song s : songs) {
                System.out.println("   - " + s.getTitle()
                        + " | artists=" + s.getArtists());
            }

            addSongs(songs, candidates, seenSongIds);
        }

        System.out.println("\n===== FINAL CANDIDATES =====");
        for (Song s : candidates) {
            System.out.println("✔ " + s.getTitle()
                    + " | " + s.getArtists());
        }

        return candidates;
    }



 

    private static void addSongs(
            List<Song> songs,
            List<Song> candidates,
            Set<String> seenSongIds
    ) {
        if (songs == null) return;

        int count = 0;

        for (Song s : songs) {

            if (s == null || s.getId() == null) continue;

            if (!seenSongIds.add(s.getId())) continue;

            candidates.add(s);

            if (++count >= MAX_SONGS_PER_ARTIST) break;
        }
    }
}