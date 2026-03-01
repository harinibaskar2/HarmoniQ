package com.harmoniq;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

        // 🔹 Initialize database tables
    Database.initialize();

    // 🔹 Create PlaylistDAO instance
    final PlaylistDAO playlistDAO = new PlaylistDAO();

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
        get("/songs", (req, res) -> {
            res.type("application/json");
            String artist = req.queryParams("artist");
            String title = req.queryParams("title");

            List<Song> uniqueSongs = new ArrayList<>();
            Set<String> seen = new HashSet<>();

            if (title != null && !title.trim().isEmpty()) {
                List<Song> globalSongs = MusicBrainzService.fetchSongsByTitle(title.trim());
                if (globalSongs != null) {
                    for (Song s : globalSongs) {
                        if (artist != null && !artist.trim().isEmpty()) {
                            boolean matchesArtist = s.getArtists().stream()
                                    .anyMatch(a -> a.toLowerCase().contains(artist.trim().toLowerCase()));
                            if (!matchesArtist) continue;
                        }
                        String songArtist = s.getArtists().isEmpty() ? "Unknown Artist" : s.getArtists().get(0);
                        String key = s.getTitle().toLowerCase() + "-" + songArtist.toLowerCase();
                        if (!seen.contains(key)) {
                            uniqueSongs.add(s);
                            seen.add(key);
                        }
                    }
                }
            } else if (artist != null && !artist.trim().isEmpty()) {
                Artist mbArtist = MusicBrainzService.fetchArtist(artist.trim());
                if (mbArtist != null && mbArtist.getMbid() != null && !mbArtist.getMbid().isEmpty()) {
                    int newArtistId = ArtistDAO.saveArtist(mbArtist.getName(), mbArtist.getMbid());
                    List<Song> fetchedSongs = MusicBrainzService.fetchSongs(mbArtist.getMbid());
                    if (fetchedSongs != null) {
                        for (Song s : fetchedSongs) {
                            String key = s.getTitle().toLowerCase() + "-" + mbArtist.getName().toLowerCase();
                            if (!seen.contains(key)) {
                                s.setArtists(Collections.singletonList(mbArtist.getName()));
                                SongDAO.saveSong(s.getTitle(), s.getId(), newArtistId);
                                uniqueSongs.add(s);
                                seen.add(key);
                            }
                        }
                    }
                }
            }

            return gson.toJson(uniqueSongs);
        });

        // ---------------- PLAYLIST ENDPOINTS ----------------

        // Add song to playlist
        post("/playlists/add", (req, res) -> {
            res.type("application/json");
        
            Map<String, Object> body = gson.fromJson(req.body(),
                    new TypeToken<Map<String, Object>>() {}.getType());
        
            String username = (String) body.get("username"); // TODO: replace with JWT logic
            String playlistName = (String) body.get("playlistName");
            Map songMap = (Map) body.get("song");
        
            if (username == null || playlistName == null || songMap == null) {
                res.status(400);
                Map<String, String> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", "Missing fields");
                return gson.toJson(resp);
            }
        
            // safely extract artists and genres
            List<String> artists = new ArrayList<>();
            if (songMap.get("artists") instanceof List) {
                for (Object a : (List) songMap.get("artists")) {
                    artists.add(a.toString());
                }
            }
        
            List<String> genres = new ArrayList<>();
            if (songMap.get("genres") instanceof List) {
                for (Object g : (List) songMap.get("genres")) {
                    genres.add(g.toString());
                }
            }
        
            Song song = new Song(
                    (String) songMap.get("id"),
                    (String) songMap.get("title"),
                    artists,
                    genres
            );
        
            // ✅ Use instance method
            playlistDAO.addSong(username, playlistName, song);
        
            Map<String, String> resp = new HashMap<>();
            resp.put("status", "success");
            return gson.toJson(resp);
        });
        
        // Get all playlists for a user
        get("/playlists", (req, res) -> {
            res.type("application/json");
            String username = req.queryParams("username"); // TODO: replace with JWT logic
            if (username == null) return "[]";
        
            // ✅ Use instance method
            List<Playlist> playlists = playlistDAO.getPlaylists(username);
            return gson.toJson(playlists);
        });
    }
}