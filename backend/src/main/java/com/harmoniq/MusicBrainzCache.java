package com.harmoniq;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.concurrent.TimeUnit;

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