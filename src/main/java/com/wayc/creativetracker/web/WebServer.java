package com.wayc.creativetracker.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wayc.creativetracker.CreativeTracker;
import spark.Spark;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {

    private final CreativeTracker plugin;
    private final int port;
    private final Gson gson;
    private final ConcurrentHashMap<String, Long> sessions;
    private static final long SESSION_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours

    private String passwordHash;
    private boolean passwordSet = false;

    public WebServer(CreativeTracker plugin, int port) {
        this.plugin = plugin;
        this.port = port;
        this.gson = new GsonBuilder().create();
        this.sessions = new ConcurrentHashMap<>();
        loadPassword();
    }

    public void start() {
        Spark.port(port);

        Spark.staticFiles.location("/web");

        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });

        Spark.post("/api/setup", (req, res) -> {
            res.type("application/json");

            if (passwordSet) {
                res.status(400);
                return gson.toJson(new ApiResponse(false, "Password already set"));
            }

            SetupRequest setupReq = gson.fromJson(req.body(), SetupRequest.class);
            if (setupReq.password == null || setupReq.password.length() < 4) {
                res.status(400);
                return gson.toJson(new ApiResponse(false, "Password must be at least 4 characters"));
            }

            setPassword(setupReq.password);
            String sessionId = createSession();
            res.cookie("session", sessionId, (int) (SESSION_TIMEOUT / 1000));

            return gson.toJson(new ApiResponse(true, "Password set successfully"));
        });

        Spark.post("/api/login", (req, res) -> {
            res.type("application/json");

            if (!passwordSet) {
                res.status(400);
                return gson.toJson(new LoginResponse(false, "Password not set", true));
            }

            LoginRequest loginReq = gson.fromJson(req.body(), LoginRequest.class);
            if (checkPassword(loginReq.password)) {
                String sessionId = createSession();
                res.cookie("session", sessionId, (int) (SESSION_TIMEOUT / 1000));
                return gson.toJson(new LoginResponse(true, "Login successful", false));
            } else {
                res.status(401);
                return gson.toJson(new LoginResponse(false, "Invalid password", false));
            }
        });

        Spark.get("/api/auth/status", (req, res) -> {
            res.type("application/json");

            boolean authenticated = isAuthenticated(req.cookie("session"));
            return gson.toJson(new AuthStatus(authenticated, !passwordSet));
        });

        Spark.post("/api/logout", (req, res) -> {
            res.type("application/json");

            String session = req.cookie("session");
            if (session != null) {
                sessions.remove(session);
            }
            res.removeCookie("session");

            return gson.toJson(new ApiResponse(true, "Logged out"));
        });

        Spark.before("/api/logs", (req, res) -> {
            if (!isAuthenticated(req.cookie("session"))) {
                Spark.halt(401, gson.toJson(new ApiResponse(false, "Unauthorized")));
            }
        });

        Spark.before("/api/logs/*", (req, res) -> {
            if (!isAuthenticated(req.cookie("session"))) {
                Spark.halt(401, gson.toJson(new ApiResponse(false, "Unauthorized")));
            }
        });

        Spark.before("/api/stats", (req, res) -> {
            if (!isAuthenticated(req.cookie("session"))) {
                Spark.halt(401, gson.toJson(new ApiResponse(false, "Unauthorized")));
            }
        });

        LogController.register(plugin, gson);

        Spark.awaitInitialization();
        plugin.getLogger().info("Web server started on port " + port);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
        plugin.getLogger().info("Web server stopped");
    }

    private String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, System.currentTimeMillis());
        cleanupSessions();
        return sessionId;
    }

    private boolean isAuthenticated(String sessionId) {
        if (sessionId == null)
            return false;

        Long timestamp = sessions.get(sessionId);
        if (timestamp == null)
            return false;

        if (System.currentTimeMillis() - timestamp > SESSION_TIMEOUT) {
            sessions.remove(sessionId);
            return false;
        }

        sessions.put(sessionId, System.currentTimeMillis());
        return true;
    }

    private void cleanupSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> now - entry.getValue() > SESSION_TIMEOUT);
    }

    private void loadPassword() {
        File passwordFile = new File(plugin.getDataFolder(), "password.dat");
        if (passwordFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(passwordFile))) {
                passwordHash = reader.readLine();
                passwordSet = (passwordHash != null && !passwordHash.isEmpty());
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to load password file");
            }
        }
    }

    public void setPassword(String password) {
        passwordHash = hashPassword(password);
        passwordSet = true;
        savePassword();
    }

    private void savePassword() {
        File passwordFile = new File(plugin.getDataFolder(), "password.dat");
        try {
            plugin.getDataFolder().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(passwordFile))) {
                writer.write(passwordHash);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save password file");
        }
    }

    private boolean checkPassword(String password) {
        if (password == null || passwordHash == null)
            return false;
        return passwordHash.equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
        }
    }

    private record SetupRequest(String password) {
    }

    private record LoginRequest(String password) {
    }

    private record LoginResponse(boolean success, String message, boolean needsSetup) {
    }

    private record AuthStatus(boolean authenticated, boolean needsSetup) {
    }

    private record ApiResponse(boolean success, String message) {
    }
}
