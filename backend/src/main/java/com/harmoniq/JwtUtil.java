package com.harmoniq;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;



/**
 * Utility class for generating and validating JSON Web Tokens (JWTs).
 *

 *
 * @author Harini Baskar
 */



public class JwtUtil {

    // Secure 256-bit key for HS256 (keep in memory)
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Generate JWT token for a given username
    public static String generateToken(String username) {
        long now = System.currentTimeMillis();
        long expiry = 1000 * 60 * 60; // 1 hour

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiry))
                .signWith(key)  // HS256 secure key
                .compact();
    }

    // Optional: validate a token and return the username
    public static String validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
