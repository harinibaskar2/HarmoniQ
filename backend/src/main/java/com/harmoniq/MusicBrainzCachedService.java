package com.harmoniq;

import java.util.Collections;
import java.util.List;



/**
 * Cached service layer for MusicBrainz API requests.
 *
 * Wraps MusicBrainzService calls with an in-memory Caffeine cache
 * to reduce redundant API requests and improve performance.
 *
 * Provides cached access to artists, songs, and related artists.
 *
 * @author Harini Baskar
 */



public class MusicBrainzCachedService {


        /**
     * Retrieves songs for a given artist using cache when possible.
     */

    public static List<Song> getSongsByArtist(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return MusicBrainzCache.songCache.get(
                artist.toLowerCase(),
                a -> MusicBrainzService.fetchSongsByArtistName(a)
        );
    }


     /**
     * Retrieves related artists for a given artist using cache when possible.
     */

    public static List<String> getRelatedArtists(String artist) {
        if (artist == null || artist.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return MusicBrainzCache.relatedArtistCache.get(
                artist.toLowerCase(),
                a -> MusicBrainzService.fetchRelatedArtists(a)
        );
    }


        /**
     * Retrieves artist information using cache when possible.
     */
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


