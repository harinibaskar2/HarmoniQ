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

        get("/songs", (req, res) -> {
            res.type("application/json");
        
            String artist =
                    req.queryParams("artist") == null
                            ? ""
                            : req.queryParams("artist").trim();
        
            String title =
                    req.queryParams("title") == null
                            ? ""
                            : req.queryParams("title").trim();
        
            boolean refresh =
                    "true".equalsIgnoreCase(req.queryParams("refresh"));
        
            if (artist.isEmpty() && title.isEmpty()) {
                return "[]";
            }
        
            List<Song> finalResults = new ArrayList<>();
        
            // =====================================================
            // CASE 1: ARTIST SEARCH
            // =====================================================
            if (!artist.isEmpty() && title.isEmpty()) {
        
                Integer artistId = ArtistDAO.findArtistIdByName(artist);
        
                // ⭐ Use DB but allow refresh bypass
                if (!refresh && artistId != null) {
                    List<Song> dbSongs = SongDAO.findSongsByArtistId(artistId);
        
                    if (!dbSongs.isEmpty()) {
                        Collections.shuffle(dbSongs);
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
        
                for (Song s : fetchedSongs) {
                    if (s.getId() == null) continue;
        
                    if (!seen.contains(s.getId())) {
                        s.setArtists(Collections.singletonList(mbArtist.getName()));
        
                        SongDAO.saveSong(
                                s.getTitle(),
                                s.getId(),
                                newArtistId
                        );
        
                        finalResults.add(s);
                        seen.add(s.getId());
                    }
                }
        
                Collections.shuffle(finalResults);
                return gson.toJson(finalResults);
            }
        
            // =====================================================
            // CASE 2: TITLE SEARCH
            // =====================================================
            if (!title.isEmpty() && artist.isEmpty()) {
        
                List<Song> globalSongs =
                        MusicBrainzService.fetchSongsByTitle(title);
        
                if (globalSongs == null) return "[]";
        
                Set<String> seen = new HashSet<>();
        
                for (Song s : globalSongs) {
        
                    String firstArtist = s.getArtists().isEmpty()
                            ? "Unknown Artist"
                            : s.getArtists().get(0);
        
                    String key =
                            s.getTitle().toLowerCase()
                                    + "-"
                                    + firstArtist.toLowerCase();
        
                    if (!seen.contains(key)) {
                        finalResults.add(s);
                        seen.add(key);
                    }
                }
        
                Collections.shuffle(finalResults);
                return gson.toJson(finalResults);
            }
        
            // =====================================================
            // CASE 3: ARTIST + TITLE SEARCH
            // =====================================================
            if (!artist.isEmpty() && !title.isEmpty()) {
        
                List<Song> globalSongs =
                        MusicBrainzService.fetchSongsByTitle(title);
        
                if (globalSongs == null) return "[]";
        
                Set<String> seen = new HashSet<>();
        
                for (Song s : globalSongs) {
        
                    boolean matchesArtist =
                            s.getArtists().stream()
                                    .anyMatch(a ->
                                            a.toLowerCase()
                                                    .contains(artist.toLowerCase())
                                    );
        
                    if (!matchesArtist) continue;
        
                    String key =
                            s.getTitle().toLowerCase()
                                    + "-"
                                    + s.getArtists().get(0).toLowerCase();
        
                    if (!seen.contains(key)) {
                        finalResults.add(s);
                        seen.add(key);
                    }
                }
        
                Collections.shuffle(finalResults);
                return gson.toJson(finalResults);
            }
        
            return gson.toJson(finalResults);
        });
        // ---------------- PLAYLIST ENDPOINTS ----------------

      // ---------------- PLAYLIST ENDPOINTS ----------------

        post("/playlists/add", (req, res) -> {
            res.type("application/json");
        
            Map<String, Object> body = gson.fromJson(
                    req.body(),
                    new TypeToken<Map<String, Object>>() {}.getType()
            );
        
            String username = (String) body.get("username");
            String playlistName = (String) body.get("playlistName");
            Map songMap = (Map) body.get("song");
        
            // =========================
            // VALIDATION
            // =========================
            if (username == null || playlistName == null || songMap == null) {
                res.status(400);
        
                Map<String, String> resp = new HashMap<>();
                resp.put("status", "error");
                resp.put("message", "Missing fields");
        
                return gson.toJson(resp);
            }
        
            // =========================
            // DEBUG INPUT
            // =========================
            System.out.println("=== ADD PLAYLIST REQUEST ===");
            System.out.println("USERNAME: " + username);
            System.out.println("PLAYLIST: " + playlistName);
            System.out.println("SONG MAP: " + songMap);
        
            // =========================
            // EXTRACT DATA
            // =========================
            List<String> artists = new ArrayList<>();
            if (songMap.get("artists") instanceof List) {
                for (Object a : (List<?>) songMap.get("artists")) {
                    artists.add(a.toString());
                }
            }
        
            List<String> genres = new ArrayList<>();
            if (songMap.get("genres") instanceof List) {
                for (Object g : (List<?>) songMap.get("genres")) {
                    genres.add(g.toString());
                }
            }
        
            Song song = new Song(
                    (String) songMap.get("id"),
                    (String) songMap.get("title"),
                    artists,
                    genres,
                    new ArrayList<>()
                
            );
        
            System.out.println("CREATED SONG: " + song.getTitle());
        
            // =========================
            // SAVE TO DB
            // =========================
            try {
                playlistDAO.addSong(username, playlistName, song);
                System.out.println("✅ INSERT SUCCESS");
            } catch (Exception e) {
                System.out.println("❌ INSERT FAILED");
                e.printStackTrace();
            }
        
            // =========================
            // VERIFY DB AFTER INSERT
            // =========================
            List<Playlist> updatedPlaylists = playlistDAO.getPlaylists(username);
        
            System.out.println("AFTER INSERT PLAYLIST COUNT: " + updatedPlaylists.size());
        
            // EXTRA DEBUG (VERY IMPORTANT)
            if (updatedPlaylists.isEmpty()) {
                System.out.println("❌ STILL EMPTY AFTER INSERT → DB ISSUE");
            }
        
            return gson.toJson(updatedPlaylists);
        });


        // Get all playlists for a user
        get("/playlists", (req, res) -> {
            res.type("application/json");

            String username = req.queryParams("username");

            if (username == null) {
                res.status(400);
                return gson.toJson(new ArrayList<>());
            }

            List<Playlist> playlists = playlistDAO.getPlaylists(username);
            return gson.toJson(playlists);
        });


       

        get("/recommendations", (req, res) -> {

            res.type("application/json");
        
            String username = req.queryParams("username");
        
            if (username == null || username.trim().isEmpty()) {
                return "[]";
            }
        
            System.out.println("\n=== RECOMMENDATIONS DEBUG ===");
            System.out.println("USER: " + username);
        
            // 1. Get playlists
            List<Playlist> playlists = playlistDAO.getPlaylists(username);
        
            System.out.println("PLAYLIST COUNT: " + playlists.size());
        
            if (playlists.isEmpty()) {
                System.out.println("❌ No playlists found");
                return "[]";
            }
        
            // 2. Build profile
            UserProfile profile =
                    RecommendationService.buildUserProfile(playlists);
        
            System.out.println("TOP ARTISTS: " + profile.getTopArtist());
        
            // 3. Generate recommendations
            List<Song> recommendations =
                    RecommendationService.recommend(profile);
        
            System.out.println("RECOMMENDATIONS SIZE: " +
                    (recommendations == null ? 0 : recommendations.size()));
        
            if (recommendations == null || recommendations.isEmpty()) {
                System.out.println("❌ No recommendations generated");
                return "[]";
            }
        
            // 4. Print sample recommendations
            for (Song s : recommendations) {
                System.out.println("➡ " + s.getTitle() +
                        " | " + s.getArtists());
            }
        
            return new Gson().toJson(recommendations);
        });

    } 
} 

