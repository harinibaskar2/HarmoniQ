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
    UserDAO userDAO = new UserDAO();


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
        
            final String artist =
                    req.queryParams("artist") == null
                            ? null
                            : req.queryParams("artist").trim();
        
            final String title =
                    req.queryParams("title") == null
                            ? null
                            : req.queryParams("title").trim();
        
            if ((artist == null || artist.isEmpty()) &&
                (title == null || title.isEmpty())) {
                return "[]";
            }
        
        

        
        
            // ---------------------------------------------------
            // 🎤 CASE 1: ARTIST ONLY (DB FIRST)
            // ---------------------------------------------------
            if (artist != null && (title == null || title.isEmpty())) {
        
                Integer artistId = ArtistDAO.findArtistIdByName(artist);
                if (artistId != null) {
                    List<Song> dbSongs = SongDAO.findSongsByArtistId(artistId);
                    if (!dbSongs.isEmpty()) {
                        System.out.println("Returning from DB ✅");
                        return gson.toJson(dbSongs);
                    }
                }
        
                // Fetch from MusicBrainz
                Artist mbArtist = MusicBrainzService.fetchArtist(artist);
                if (mbArtist == null || mbArtist.getMbid() == null) return "[]";
        
                int newArtistId = ArtistDAO.saveArtist(
                        mbArtist.getName(),
                        mbArtist.getMbid()
                );
        
                List<Song> fetchedSongs =
                        MusicBrainzService.fetchSongs(mbArtist.getMbid());
        
                if (fetchedSongs == null) return "[]";
        
                Set<String> seen = new HashSet<>();
                List<Song> songsToReturn = new ArrayList<>();
        
                for (Song s : fetchedSongs) {
                    if (s.getId() != null && !seen.contains(s.getId())) {
        
                        s.setArtists(Collections.singletonList(mbArtist.getName()));
                        SongDAO.saveSong(s.getTitle(), s.getId(), newArtistId);
        
                        songsToReturn.add(s);
                        seen.add(s.getId());
                    }
                }
        
                return gson.toJson(songsToReturn);
            }
        
            // ---------------------------------------------------
            // 🔎 CASE 2: TITLE ONLY
            // ---------------------------------------------------
            if (title != null && (artist == null || artist.isEmpty())) {
        
                List<Song> globalSongs =
                        MusicBrainzService.fetchSongsByTitle(title);
        
                if (globalSongs == null) return "[]";
        
                Set<String> seen = new HashSet<>();
                List<Song> uniqueSongs = new ArrayList<>();
        
                for (Song s : globalSongs) {
        
                    String firstArtist = s.getArtists().isEmpty()
                            ? "Unknown Artist"
                            : s.getArtists().get(0);
        
                    String key = s.getTitle().toLowerCase()
                            + "-" +
                            firstArtist.toLowerCase();
        
                    if (!seen.contains(key)) {
                        uniqueSongs.add(s);
                        seen.add(key);
                    }
                }
        
                return gson.toJson(uniqueSongs);
            }
        
            // ---------------------------------------------------
            // 🎤 + 🔎 CASE 3: ARTIST + TITLE
            // ---------------------------------------------------
            if (artist != null && title != null) {
        
                List<Song> globalSongs =
                        MusicBrainzService.fetchSongsByTitle(title);
        
                if (globalSongs == null) return "[]";
        
                Set<String> seen = new HashSet<>();
                List<Song> filtered = new ArrayList<>();
        
                for (Song s : globalSongs) {
        
                    boolean matchesArtist = s.getArtists().stream()
                            .anyMatch(a ->
                                    a.toLowerCase()
                                     .contains(artist.toLowerCase())
                            );
        
                    if (!matchesArtist) continue;
        
                    String key = s.getTitle().toLowerCase()
                            + "-" +
                            s.getArtists().get(0).toLowerCase();
        
                    if (!seen.contains(key)) {
                        filtered.add(s);
                        seen.add(key);
                    }
                }
        
                return gson.toJson(filtered);
            }
        
            return "[]";
        });

        // ---------------- PLAYLIST ENDPOINTS ----------------

        post("/playlists/add", (req, res) -> {
            res.type("application/json");
        
            Map<String, Object> body = gson.fromJson(req.body(),
                    new TypeToken<Map<String, Object>>() {}.getType());
        
            String username = (String) body.get("username");
            String playlistName = (String) body.get("playlistName");
            Map songMap = (Map) body.get("song");
        
            if (username == null || playlistName == null || songMap == null) {
                res.status(400);
                Map<String, String> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", "Missing fields");
                return gson.toJson(resp);
            }
        
            // ✅ use instance
            Integer userId = userDAO.getUserId(username);
            if (userId == null) {
                res.status(404);
                Map<String, String> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", "User not found");
                return gson.toJson(resp);
            }
        
            // create Song object...
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
        
            playlistDAO.addSong(userId, playlistName, song);
        
            Map<String, String> resp = new HashMap<>();
            resp.put("status", "success");
            return gson.toJson(resp);
        });
        
        // Get all playlists for a user
        get("/playlists", (req, res) -> {
            res.type("application/json");
        
            String username = req.queryParams("username");
            if (username == null) return "[]";
        
            Integer userId = userDAO.getUserId(username);
            if (userId == null) return "[]";
        
            List<Playlist> playlists = playlistDAO.getPlaylists(userId);
            return gson.toJson(playlists);
        });
    }
}