package com.harmoniq;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class Main {

    private static final Map<String, String> users = new HashMap<>();

    private static final Gson gson = new Gson();
    private static final Path USER_FILE = Paths.get("users.json");

    public static void main(String[] args) throws Exception {
        port(8080);

        // Load users from file
        if (Files.exists(USER_FILE)) {
            String json = new String(Files.readAllBytes(USER_FILE), "UTF-8");
            Map<String, String> loaded = gson.fromJson(json, HashMap.class);
            users.putAll(loaded);
        }

        // React frontend build folder
        String frontendPath = "/Users/harinibaskar/Desktop/College3rdYear/winterquarter/HarmoniQ/backend/frontend/build";
        staticFiles.externalLocation(frontendPath);

        // CORS for React dev server
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "http://localhost:3000");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });

        options("/*", (req, res) -> {
            res.status(200);
            return "OK";
        });

        // Global exception handler
        exception(Exception.class, (e, req, res) -> {
            e.printStackTrace();
            res.status(500);
            res.type("application/json");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            res.body(gson.toJson(errorResponse));
        });

        // Health check
        get("/health", (req, res) -> "HarmoniQ backend running 🚀");

        // ---------------- REGISTER ----------------
        post("/register", (req, res) -> {
            res.type("application/json");
            Map<String, String> body = gson.fromJson(req.body(), HashMap.class);
            String username = body.get("username");
            String password = body.get("password");

            Map<String, String> resp = new HashMap<>();
            if (username == null || password == null) {
                res.status(400);
                resp.put("status", "error");
                resp.put("message", "Username and password required");
                return gson.toJson(resp);
            }

            if (users.containsKey(username)) {
                res.status(400);
                resp.put("status", "error");
                resp.put("message", "Username already exists");
                return gson.toJson(resp);
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            users.put(username, hashed);
            Files.write(USER_FILE, gson.toJson(users).getBytes("UTF-8"));

            resp.put("status", "success");
            return gson.toJson(resp);
        });

        // ---------------- LOGIN ----------------
        post("/login", (req, res) -> {
            res.type("application/json");
            Map<String, String> body = gson.fromJson(req.body(), HashMap.class);
            String username = body.get("username");
            String password = body.get("password");

            Map<String, String> resp = new HashMap<>();
            if (username == null || password == null) {
                res.status(400);
                resp.put("status", "error");
                resp.put("message", "Username and password required");
                return gson.toJson(resp);
            }

            if (!users.containsKey(username) || !BCrypt.checkpw(password, users.get(username))) {
                res.status(401);
                resp.put("status", "error");
                resp.put("message", "Invalid credentials");
                return gson.toJson(resp);
            }

            String token = JwtUtil.generateToken(username);

            resp.put("status", "success");
            resp.put("token", token);
            return gson.toJson(resp);
        });



    // ---------------- DISCOVER SONGS ----------------  
// ---------------- DISCOVER SONGS ----------------  
        get("/songs", (req, res) -> {
            res.type("application/json");

            String query = req.queryParams("query");   // song title
            String artist = req.queryParams("artist"); // optional
            String genre = req.queryParams("genre");   // optional

            // Build MusicBrainz query
            StringBuilder mbQuery = new StringBuilder();

            if (query != null && !query.isEmpty()) {
                mbQuery.append("recording:").append(query);
            }
            if (artist != null && !artist.isEmpty()) {
                if (mbQuery.length() > 0) mbQuery.append(" AND ");
                mbQuery.append("artist:").append(artist);
            }
            if (genre != null && !genre.isEmpty()) {
                if (mbQuery.length() > 0) mbQuery.append(" AND ");
                mbQuery.append("tag:").append(genre);
            }

            // If still empty, search all recordings
            if (mbQuery.length() == 0) mbQuery.append("recording:*");

            try {
                String urlStr = "https://musicbrainz.org/ws/2/recording/?query=" +
                                URLEncoder.encode(mbQuery.toString(), "UTF-8") +
                                "&fmt=json&limit=50&inc=artist-credits+tags";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "HarmoniQ/1.0 (dev)");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) content.append(inputLine);
                in.close();
                conn.disconnect();

                // Parse JSON from MusicBrainz
                JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
                List<Song> songs = new ArrayList<>();

                if (json.has("recordings")) {
                    JsonArray recordings = json.getAsJsonArray("recordings");

                    for (JsonElement recElem : recordings) {
                        JsonObject rec = recElem.getAsJsonObject();
                        String id = rec.get("id").getAsString();
                        String title = rec.get("title").getAsString();

                        // Artists
                        List<String> artistsList = new ArrayList<>();
                        if (rec.has("artist-credit")) {
                            JsonArray artistCredit = rec.getAsJsonArray("artist-credit");
                            for (JsonElement a : artistCredit) {
                                JsonObject artistObj = a.getAsJsonObject();
                                if (artistObj.has("name")) artistsList.add(artistObj.get("name").getAsString());
                            }
                        }

                        // Genres / tags
                        List<String> genresList = new ArrayList<>();
                        if (rec.has("tags")) {
                            JsonArray tags = rec.getAsJsonArray("tags");
                            for (JsonElement t : tags) {
                                JsonObject tagObj = t.getAsJsonObject();
                                if (tagObj.has("name")) genresList.add(tagObj.get("name").getAsString());
                            }
                        }

                        songs.add(new Song(id, title, artistsList, genresList));
                    }
                }

                return new Gson().toJson(songs);

            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });
    } 

} 



