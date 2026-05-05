package com.harmoniq;

import java.time.Duration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

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


