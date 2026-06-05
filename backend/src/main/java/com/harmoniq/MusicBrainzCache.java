package com.harmoniq;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


/**
 * Cache layer for storing MusicBrainz API responses.
 *
 * Uses Caffeine caching to improve performance by reducing
 * repeated API calls for artists, songs, and related artists.
 *
 * Cached data is automatically expired after a set time to
 * ensure freshness of music information.
 *
 * @author Harini Baskar
 */

public class MusicBrainzCache {

    public static final Cache<String, Artist> artistCache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(24, TimeUnit.HOURS)
                    .build();

    public static final Cache<String, List<Song>> songCache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(12, TimeUnit.HOURS)
                    .build();

    public static final Cache<String, List<String>> relatedArtistCache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(24, TimeUnit.HOURS)
                    .build();
}