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
        try {
            String urlStr =
                "https://musicbrainz.org/ws/2/artist/?query=artist:" +
                URLEncoder.encode(artistName, "UTF-8") +
                "&fmt=json&limit=1";

            String response = sendGet(urlStr);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("artists")) {
                JsonArray artists = json.getAsJsonArray("artists");

                if (artists.size() > 0) {
                    JsonObject artist = artists.get(0).getAsJsonObject();

                    String name = artist.has("name") ? artist.get("name").getAsString() : null;
                    String mbid = artist.has("id") ? artist.get("id").getAsString() : null;

                    if (name != null && mbid != null) {
                        return new Artist(name, mbid);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("❌ fetchArtist failed: " + e.getMessage());
        }

        return null;
    }

    // =====================================================
    // FETCH SONGS BY ARTIST MBID
    // =====================================================
    public static List<Song> fetchSongs(String mbid) {

        List<Song> songs = new ArrayList<>();

        try {
            String urlStr =
                "https://musicbrainz.org/ws/2/recording?artist=" +
                mbid +
                "&fmt=json&limit=50";

            String response = sendGet(urlStr);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("recordings")) {

                JsonArray recordings = json.getAsJsonArray("recordings");

                for (JsonElement recElem : recordings) {

                    JsonObject rec = recElem.getAsJsonObject();

                    String id = rec.has("id") ? rec.get("id").getAsString() : null;
                    String title = rec.has("title") ? rec.get("title").getAsString() : null;

                    if (id == null || title == null) continue;

                    // ❌ DO NOT FORCE "Unknown Artist"
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

                    // ✅ If no artist → just skip adding artist info
                    // (frontend will only show song title)

                    songs.add(new Song(
                        id,
                        title,
                        artists,          // may be empty
                        new ArrayList<>()
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ fetchSongs failed: " + e.getMessage());
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

            if (json.has("recordings")) {

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

                    songs.add(new Song(
                        id,
                        songTitle,
                        artists,
                        new ArrayList<>()
                    ));
                }
            }

        } catch (Exception e) {
            System.out.println("❌ fetchSongsByTitle failed: " + e.getMessage());
        }

        return songs;
    }

    // =====================================================
    // FETCH BY ARTIST NAME
    // =====================================================
    public static List<Song> fetchSongsByArtistName(String artistName) {

        Artist artist = fetchArtist(artistName);

        if (artist == null) {
            return new ArrayList<>();
        }

        return fetchSongs(artist.getMbid());
    }

    // =====================================================
    // HTTP REQUEST
    // =====================================================
    private static String sendGet(String urlStr) throws Exception {

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