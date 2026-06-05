package com.harmoniq;

import java.time.Duration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;



/**
 * Rate limiter for controlling MusicBrainz API requests.
 *
 * Uses Bucket4j to limit requests to prevent exceeding
 * external API rate limits and avoid service blocking.
 *
 * Allows a maximum of 1 request per second.
 *
 * @author Harini Baskar
 */

public class MusicBrainzRateLimiter {

    private static final Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.builder()
                    .capacity(1)
                    .refillGreedy(1, Duration.ofSeconds(1))
                    .build())
            .build();

    public static boolean allow() {
        return bucket.tryConsume(1);
    }
}


