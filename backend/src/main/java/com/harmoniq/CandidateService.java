package com.harmoniq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CandidateService {

    private static final int MAX_SONGS_PER_ARTIST = 20;

    public static List<Song> buildCandidates(UserProfile profile) {

        Set<String> seenArtists = new HashSet<>();
        Set<String> seenSongIds = new HashSet<>();
        List<Song> candidates = new ArrayList<>();

        // -----------------------
        // DIRECT ARTISTS
        // -----------------------
        for (String artist : profile.getArtistWeights().keySet()) {

            if (artist == null) continue;

            String normalized = artist.toLowerCase().trim();
            if (!seenArtists.add(normalized)) continue;

            List<Song> songs =
                    MusicBrainzCachedService.getSongsByArtist(normalized);

            addSongs(songs, candidates, seenSongIds);
        }

        // -----------------------
        // RELATED ARTISTS
        // -----------------------
        for (String artist : profile.getRelatedArtistWeights().keySet()) {

            if (artist == null) continue;

            String normalized = artist.toLowerCase().trim();
            if (!seenArtists.add(normalized)) continue;

            List<Song> songs =
                    MusicBrainzCachedService.getSongsByArtist(normalized);

            addSongs(songs, candidates, seenSongIds);
        }

        return candidates;
    }

    // -----------------------
    // SAFE ADDITION LOGIC
    // -----------------------
    private static void addSongs(
            List<Song> songs,
            List<Song> candidates,
            Set<String> seenSongIds
    ) {
        if (songs == null) return;

        int count = 0;

        for (Song s : songs) {

            if (s == null || s.getId() == null) continue;

            if (seenSongIds.contains(s.getId())) continue;

            seenSongIds.add(s.getId());
            candidates.add(s);

            count++;
            if (count >= MAX_SONGS_PER_ARTIST) break;
        }
    }
}

