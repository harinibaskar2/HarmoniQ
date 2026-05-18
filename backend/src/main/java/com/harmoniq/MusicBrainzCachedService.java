package com.harmoniq;

import java.util.Collections;
import java.util.List;

public class MusicBrainzCachedService {

    public static List<Song> getSongsByArtist(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return MusicBrainzCache.songCache.get(
                artist.toLowerCase(),
                a -> MusicBrainzService.fetchSongsByArtistName(a)
        );
    }

    public static List<String> getRelatedArtists(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return MusicBrainzCache.relatedArtistCache.get(
                artist.toLowerCase(),
                a -> MusicBrainzService.fetchRelatedArtists(a)
        );
    }

    public static Artist getArtist(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return MusicBrainzCache.artistCache.get(
                name.toLowerCase(),
                n -> MusicBrainzService.fetchArtist(n)
        );
    }
}


