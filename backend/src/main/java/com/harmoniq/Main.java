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
            String artist = req.queryParams("artist");

            if (artist == null || artist.isEmpty()) return "[]";

            // 1️⃣ Check DB first
            Integer artistId = ArtistDAO.findArtistIdByName(artist);
            if (artistId != null) {
                List<Song> dbSongs = SongDAO.findSongsByArtistId(artistId, artist);
                if (!dbSongs.isEmpty()) {
                    System.out.println("Returning from database ✅");
                    return gson.toJson(dbSongs);
                }
            }

            // 2️⃣ Fetch from MusicBrainz if DB empty
            System.out.println("Fetching from MusicBrainz 🌍");
            Artist mbArtist = MusicBrainzService.fetchArtist(artist);
            if (mbArtist == null || mbArtist.getMbid() == null || mbArtist.getMbid().isEmpty()) return "[]";

            // Save artist in DB
            int newArtistId = ArtistDAO.saveArtist(mbArtist.getName(), mbArtist.getMbid());

            // Fetch songs
            List<Song> fetchedSongs = MusicBrainzService.fetchSongs(mbArtist.getMbid());
            if (fetchedSongs == null || fetchedSongs.isEmpty()) return "[]";

            // ✅ Create new list, attach artist name, remove duplicates, save to DB
            Set<String> seenIds = new HashSet<>();
            List<Song> songsToReturn = new ArrayList<>();

            for (Song s : fetchedSongs) {
                if (s.getId() != null && !seenIds.contains(s.getId())) {

                    // Attach artist name
                    s.setArtists(Collections.singletonList(mbArtist.getName()));

                    // Save to DB
                    SongDAO.saveSong(s.getTitle(), s.getId(), newArtistId);

                    songsToReturn.add(s);
                    seenIds.add(s.getId());
                }
            }

            System.out.println("Fetched " + songsToReturn.size() + " songs from MusicBrainz 🌍 for artist: " + mbArtist.getName());

            // ✅ Return the new list with artist names attached
            return gson.toJson(songsToReturn);
        });
    } 

} 



