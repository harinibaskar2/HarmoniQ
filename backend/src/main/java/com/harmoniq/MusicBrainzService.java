package com.harmoniq;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MusicBrainzService {

    private static final String USER_AGENT = "HarmoniQ/1.0 (dev)";

    // =====================================================
    // FETCH ARTIST
    // =====================================================
    public static Artist fetchArtist(String artistName) {

        return MusicBrainzCache.artistCache.get(artistName, name -> {
    
            try {
                String urlStr =
                        "https://musicbrainz.org/ws/2/artist/?query=artist:" +
                                URLEncoder.encode(name, "UTF-8") +
                                "&fmt=json&limit=1";
    
                String response = sendGet(urlStr);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
    
                if (json.has("artists")) {
    
                    JsonArray artists = json.getAsJsonArray("artists");
    
                    if (artists.size() > 0) {
    
                        JsonObject artist = artists.get(0).getAsJsonObject();
    
                        String n = artist.get("name").getAsString();
                        String mbid = artist.get("id").getAsString();
    
                        return new Artist(n, mbid);
                    }
                }
    
            } catch (Exception e) {
                System.out.println(" fetchArtist failed: " + e.getMessage());
            }
    
            return null;
        });
    }

    // =====================================================
    // FETCH SONGS BY ARTIST MBID (CONTENT BASED CORE)
    // =====================================================
    public static List<Song> fetchSongs(String mbid) {

    List<Song> songs = new ArrayList<>();

    try {
        String urlStr =
                "https://musicbrainz.org/ws/2/recording?artist=" +
                        URLEncoder.encode(mbid, "UTF-8") +
                        "&fmt=json&limit=50&inc=artist-credits";

        String response = sendGet(urlStr);

        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        if (!json.has("recordings")) return songs;

        JsonArray recordings = json.getAsJsonArray("recordings");

        for (JsonElement recElem : recordings) {

            JsonObject rec = recElem.getAsJsonObject();

            String songId = rec.has("id") ? rec.get("id").getAsString() : "";
            String title = rec.has("title") ? rec.get("title").getAsString() : "Unknown Title";

            List<String> artists = new ArrayList<>();

            if (rec.has("artist-credit")) {

                JsonArray artistCredit = rec.getAsJsonArray("artist-credit");

                for (JsonElement acElem : artistCredit) {

                    JsonObject ac = acElem.getAsJsonObject();

                    // Case 1: nested artist object 
                    if (ac.has("artist")) {
                        JsonObject artistObj = ac.getAsJsonObject("artist");

                        if (artistObj != null && artistObj.has("name")) {
                            artists.add(artistObj.get("name").getAsString());
                        }

                    // Case 2: direct name field
                    } else if (ac.has("name")) {
                        artists.add(ac.get("name").getAsString());
                    }
                }
            }

            // FINAL fallback (prevents [Unknown Artist])
            if (artists.isEmpty()) {
                artists.add("Unknown Artist");
            }

            Song song = new Song(
                    songId,
                    title,
                    artists,
                    new ArrayList<>(),
                    new ArrayList<>()
            );

            songs.add(song);
        }

    } catch (Exception e) {
        System.out.println(" fetchSongs failed: " + e.getMessage());
    }

    return songs;
}

    // =====================================================
    // FETCH SONGS BY TITLE
    // =====================================================
    public static List<Song> fetchSongsByTitle(String title) {

        List<Song> songs = new ArrayList<>();

        try {

            String urlStr =
                    "https://musicbrainz.org/ws/2/recording/?query=recording:" +
                            URLEncoder.encode(title, "UTF-8") +
                            "&fmt=json&limit=50";

            String response = sendGet(urlStr);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (!json.has("recordings")) return songs;

            JsonArray recordings = json.getAsJsonArray("recordings");

            for (JsonElement recElem : recordings) {

                JsonObject rec = recElem.getAsJsonObject();

                String id = rec.has("id") ? rec.get("id").getAsString() : null;
                String songTitle = rec.has("title") ? rec.get("title").getAsString() : null;

                if (id == null || songTitle == null) continue;

                List<String> artists = new ArrayList<>();

                if (rec.has("artist-credit")) {

                    JsonArray artistCredit = rec.getAsJsonArray("artist-credit");

                    for (JsonElement acElem : artistCredit) {

                        JsonObject ac = acElem.getAsJsonObject();

                        if (ac.has("name")) {
                            artists.add(ac.get("name").getAsString());
                        }
                    }
                }

                List<String> relatedArtists = new ArrayList<>();

                if (!artists.isEmpty()) {
                    Artist a = fetchArtist(artists.get(0));
                    if (a != null) {
                        relatedArtists = fetchRelatedArtists(a.getMbid());
                    }
                }

                songs.add(new Song(
                        id,
                        songTitle,
                        artists,
                        new ArrayList<>(),
                        relatedArtists
                ));
            }

        } catch (Exception e) {
            System.out.println(" fetchSongsByTitle failed: " + e.getMessage());
        }

        return songs;
    }

    // =====================================================
    // FETCH BY ARTIST NAME (WRAPPER)
    // =====================================================
    public static List<Song> fetchSongsByArtistName(String artistName) {

    Artist artist = fetchArtist(artistName);

    if (artist == null) return new ArrayList<>();

    List<Song> songs = fetchSongs(artist.getMbid());

    List<String> related = fetchRelatedArtists(artist.getMbid());

    // enrich AFTER cache
    for (Song s : songs) {
        s.setRelatedArtists(related);
    }

    return songs;
}

    // =====================================================
    // FETCH RELATED ARTISTS (SIMPLE RELATION SIGNAL)
    // =====================================================
    public static List<String> fetchRelatedArtists(String mbid) {

        return MusicBrainzCache.relatedArtistCache.get(mbid, id -> {

            List<String> related = new ArrayList<>();

            try {
                String urlStr =
                        "https://musicbrainz.org/ws/2/artist/" +
                                id +
                                "?inc=artist-rels&fmt=json";

                String response = sendGet(urlStr);

                //  DEBUG 1: raw response
                System.out.println("\n===== RELATED ARTISTS RAW RESPONSE =====");
                System.out.println(response);

                JsonObject json = JsonParser.parseString(response).getAsJsonObject();

                //  DEBUG 2: full parsed JSON
                System.out.println("\n===== RELATED ARTISTS PARSED JSON =====");
                System.out.println(json);

                if (!json.has("relations")) {
                    System.out.println("No 'relations' field found for MBID: " + id);
                    return related;
                }

                JsonArray relations = json.getAsJsonArray("relations");

                System.out.println(" Number of relations found: " + relations.size());

                for (JsonElement elem : relations) {

                    JsonObject rel = elem.getAsJsonObject();

                    System.out.println(" Relation object: " + rel);

                    if (rel.has("artist")) {
                        JsonObject artistObj = rel.getAsJsonObject("artist");

                        if (artistObj.has("name")) {

                            String name = artistObj.get("name").getAsString();

                            System.out.println(" Related artist found: " + name);

                            related.add(name);
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println(" fetchRelatedArtists failed: " + e.getMessage());
            }

            System.out.println(" FINAL RELATED ARTISTS LIST: " + related);

            return related;
        });
    }



    // FETCH GENRES + TAGS (FIXED VERSION)
    // Uses release-group FIRST
    // =====================================================
    public static List<String> fetchGenresAndTags(String recordingId) {

        List<String> metadata = new ArrayList<>();

        try {

            // STEP 1: Get recording → extract release-group id
            String recUrl =
                    "https://musicbrainz.org/ws/2/recording/" +
                            recordingId +
                            "?inc=releases&fmt=json";

            String recResponse = sendGet(recUrl);
            JsonObject recJson = JsonParser.parseString(recResponse).getAsJsonObject();

            String releaseGroupId = null;

            if (recJson.has("releases")) {
                JsonArray releases = recJson.getAsJsonArray("releases");

                if (releases.size() > 0) {
                    JsonObject firstRelease = releases.get(0).getAsJsonObject();

                    if (firstRelease.has("release-group")) {
                        JsonObject rg = firstRelease.getAsJsonObject("release-group");
                        releaseGroupId = rg.get("id").getAsString();
                    }
                }
            }

            // DEBUG
            System.out.println(" Release Group ID: " + releaseGroupId);

  
            if (releaseGroupId != null) {

                String rgUrl =
                        "https://musicbrainz.org/ws/2/release-group/" +
                                releaseGroupId +
                                "?inc=genres+tags&fmt=json";

                String rgResponse = sendGet(rgUrl);
                JsonObject rgJson = JsonParser.parseString(rgResponse).getAsJsonObject();

                System.out.println(" Release-group response received");

                // =========================
                // GENRES (PRIMARY SOURCE)
                // =========================
                if (rgJson.has("genres")) {
                    JsonArray genres = rgJson.getAsJsonArray("genres");

                    for (JsonElement gElem : genres) {
                        JsonObject g = gElem.getAsJsonObject();

                        if (g.has("name")) {
                            String genre = g.get("name").getAsString().toLowerCase();

                            if (!metadata.contains(genre)) {
                                metadata.add(genre);
                            }
                        }
                    }
                }

                // =========================
                // TAGS
                // =========================
                if (rgJson.has("tags")) {
                    JsonArray tags = rgJson.getAsJsonArray("tags");

                    for (JsonElement tElem : tags) {
                        JsonObject t = tElem.getAsJsonObject();

                        if (t.has("name")) {
                            String tag = t.get("name").getAsString().toLowerCase();

                            if (tag.length() < 2) continue;

                            if (!metadata.contains(tag)) {
                                metadata.add(tag);
                            }
                        }
                    }
                }
            }

            // STEP 3: FALLBACK (recording tags only if release-group empty)
            if (metadata.isEmpty()) {

                System.out.println(" Falling back to recording-level tags");

                String fallbackUrl =
                        "https://musicbrainz.org/ws/2/recording/" +
                                recordingId +
                                "?inc=tags&fmt=json";

                String response = sendGet(fallbackUrl);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();

                if (json.has("tags")) {
                    JsonArray tags = json.getAsJsonArray("tags");

                    for (JsonElement tElem : tags) {
                        JsonObject t = tElem.getAsJsonObject();

                        if (t.has("name")) {
                            String tag = t.get("name").getAsString().toLowerCase();

                            if (tag.length() < 2) continue;

                            if (!metadata.contains(tag)) {
                                metadata.add(tag);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(" fetchGenresAndTags failed: " + e.getMessage());
        }

        System.out.println(" FINAL METADATA: " + metadata);

        return metadata;
    }


    public static List<String> getAllGenres() {
        List<String> genres = new ArrayList<String>();
        genres.add("pop");
        genres.add("country");
        
        genres.add("rock");
        genres.add("r&b");
        genres.add("electronic");
        
        
        
        
        
        
        return genres;
    }

    /**
     * Fetch songs tagged under a specific genre.
     */
    public static List<Song> fetchSongsByGenre(String genre) {
        List<Song> songs = new ArrayList<Song>();
        try {
            String urlStr = "https://musicbrainz.org/ws/2/recording?query=tag:" +
                            URLEncoder.encode(genre, "UTF-8") +
                            "&fmt=json&limit=50";

            String response = sendGet(urlStr);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("recordings")) {
                JsonArray recordings = json.getAsJsonArray("recordings");

                for (JsonElement recElem : recordings) {
                    JsonObject rec = recElem.getAsJsonObject();

                    String id = rec.has("id") ? rec.get("id").getAsString() : null;
                    String title = rec.has("title") ? rec.get("title").getAsString() : null;

                    List<String> artists = new ArrayList<String>();

                    if (rec.has("artist-credit")) {
                        JsonArray artistCredit = rec.getAsJsonArray("artist-credit");
                        for (JsonElement acElem : artistCredit) {
                            JsonObject ac = acElem.getAsJsonObject();
                            if (ac.has("artist")) {
                                JsonObject artistObj = ac.getAsJsonObject("artist");
                                if (artistObj.has("name")) {
                                    artists.add(artistObj.get("name").getAsString());
                                }
                            }
                        }
                    }

                    if (id != null && title != null) {
                        // genres list is empty; handled separately in dropdown
                        songs.add(new Song(id, title, artists, new ArrayList<>(), new ArrayList<>()));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }


    // =====================================================
    // HTTP REQUEST
    // =====================================================
    public static String sendGet(String urlStr) throws Exception {

        
        while (!MusicBrainzRateLimiter.allow()) {
            System.out.println("⏳ Rate limit hit → waiting...");
            Thread.sleep(1000);
        }
    
       
       
       
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("MusicBrainz error: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder content = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            content.append(line);
        }

        in.close();
        conn.disconnect();

        return content.toString();
    }
}