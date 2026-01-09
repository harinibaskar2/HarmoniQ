package com.harmoniq;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import static spark.Spark.*;

public class Main {

    private static Map<String, String> users = new HashMap<>();
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        port(8080);

        // Serve React frontend
        // Use absolute path to avoid 404
        String frontendPath = "/Users/harinibaskar/Desktop/College3rdYear/winterquarter/HarmoniQ/backend/frontend/build";
        staticFiles.externalLocation(frontendPath);

        // Global exception handler
        exception(Exception.class, (e, req, res) -> {
            e.printStackTrace();
            res.status(500);
            res.body(e.getMessage());
        });

        // Health check
        get("/health", (req, res) -> "HarmoniQ backend running 🚀");

        // Register endpoint
        post("/register", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String username = body.get("username");
            String password = body.get("password");

            if (users.containsKey(username)) {
                res.status(400);
                return gson.toJson(Map.of("status", "error", "message", "Username exists"));
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            users.put(username, hashed);

            return gson.toJson(Map.of("status", "success"));
        });

        // Login endpoint
        post("/login", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String username = body.get("username");
            String password = body.get("password");

            if (!users.containsKey(username) || !BCrypt.checkpw(password, users.get(username))) {
                res.status(401);
                return gson.toJson(Map.of("status", "error", "message", "Invalid credentials"));
            }

            String token = JwtUtil.generateToken(username);
            return gson.toJson(Map.of("status", "success", "token", token));
        });

        // Catch-all route for React SPA (prevents 404 on refresh)
        get("/*", (req, res) -> {
            res.type("text/html");
            return new String(Files.readAllBytes(Paths.get(frontendPath + "/index.html")));
        });
    }
}
