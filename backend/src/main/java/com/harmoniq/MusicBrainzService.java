package com.harmoniq;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MusicBrainzService {

    private static final String USER_AGENT = "HarmoniQ/1.0 (dev)";

    // 🔹 Fetch artist and return name + mbid
    public static Artist fetchArtist(String artistName) {
        try {
            String urlStr = "https://musicbrainz.org/ws/2/artist/?query=artist:" +
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

    // 🔹 Fetch songs by artist MBID
    public static List<Song> fetchSongs(String mbid) {

        List<Song> songs = new ArrayList<Song>();

        try {
            String urlStr = "https://musicbrainz.org/ws/2/recording?artist=" +
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

                    List<String> artists = new ArrayList<String>();
                    List<String> genres = new ArrayList<String>();

                    songs.add(new Song(id, title, artists, genres));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songs;
    }

    private static String sendGet(String urlStr) throws Exception {

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", USER_AGENT);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));

        StringBuilder content = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        return content.toString();
    }
}

