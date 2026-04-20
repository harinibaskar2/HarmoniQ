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
    // 🔥 NEW: FETCH GENRES FOR A RECORDING
    // =====================================================
    public static List<String> fetchGenres(String recordingId) {
        List<String> genres = new ArrayList<>();

        try {
            String urlStr =
                "https://musicbrainz.org/ws/2/recording/" +
                recordingId +
                "?inc=genres&fmt=json";

            String response = sendGet(urlStr);

            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            if (json.has("genres")) {
                JsonArray arr = json.getAsJsonArray("genres");

                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();

                    if (obj.has("name")) {
                        genres.add(obj.get("name").getAsString());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return genres;
    }

    // =====================================================
    // 🔹 Fetch artist
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

                    String name = artist.get("name").getAsString();
                    String mbid = artist.get("id").getAsString();

                    return new Artist(name, mbid);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // =====================================================
    // 🔹 Fetch songs by artist (FIXED)
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

                    String id = rec.get("id").getAsString();
                    String title = rec.get("title").getAsString();

                    List<String> artists = new ArrayList<>();

                    // 🔥 FIX: fetch real genres
                    List<String> genres = fetchGenres(id);

                    songs.add(new Song(id, title, artists, genres));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }

    // =====================================================
    // 🔹 Fetch songs by title (FIXED)
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

                    if (id != null && songTitle != null) {

                        // 🔥 FIX: fetch genres
                        List<String> genres = fetchGenres(id);

                        songs.add(new Song(id, songTitle, artists, genres));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }

    // =====================================================
    // 🔹 Fetch songs by genre (unchanged but safe)
    // =====================================================
    public static List<Song> fetchSongsByGenre(String genre) {

        List<Song> songs = new ArrayList<>();

        try {
            String urlStr =
                "https://musicbrainz.org/ws/2/recording?query=tag:" +
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

                    List<String> artists = new ArrayList<>();

                    if (id != null && title != null) {

                        // optional enrichment
                        List<String> genres = fetchGenres(id);

                        songs.add(new Song(id, title, artists, genres));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }

    // =====================================================
    // 🔹 HTTP helper
    // =====================================================
    private static String sendGet(String urlStr) throws Exception {

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );

        StringBuilder content = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        return content.toString();
    }

    // =====================================================
    // 🔹 Static genre list (unchanged)
    // =====================================================
    public static List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        genres.add("pop");
        genres.add("country");
        genres.add("rock");
        genres.add("r&b");
        genres.add("electronic");
        return genres;
    }
}