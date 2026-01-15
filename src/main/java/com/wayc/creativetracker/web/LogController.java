package com.wayc.creativetracker.web;

import com.google.gson.Gson;
import com.wayc.creativetracker.CreativeTracker;
import com.wayc.creativetracker.data.ItemLog;
import org.bukkit.Material;
import spark.Spark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogController {

    public static void register(CreativeTracker plugin, Gson gson) {

        Spark.get("/api/logs", (req, res) -> {
            res.type("application/json");

            int page = 0;
            int pageSize = 50;
            String player = null;
            String item = null;
            String method = null;

            try {
                if (req.queryParams("page") != null) {
                    page = Integer.parseInt(req.queryParams("page"));
                }
                if (req.queryParams("pageSize") != null) {
                    pageSize = Math.min(100, Integer.parseInt(req.queryParams("pageSize")));
                }
                player = req.queryParams("player");
                item = req.queryParams("item");
                method = req.queryParams("method");
            } catch (NumberFormatException ignored) {
            }

            Material itemMaterial = null;
            if (item != null && !item.isEmpty()) {
                itemMaterial = Material.matchMaterial(item);
            }

            ItemLog.AcquisitionMethod acquisitionMethod = null;
            if (method != null && !method.isEmpty()) {
                try {
                    acquisitionMethod = ItemLog.AcquisitionMethod.valueOf(method.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }

            List<ItemLog> logs;
            if (player != null || itemMaterial != null || acquisitionMethod != null) {
                logs = plugin.getLogStorage().searchLogs(player, itemMaterial, acquisitionMethod, page, pageSize);
            } else {
                logs = plugin.getLogStorage().getLogs(page, pageSize);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("logs", logs.stream().map(LogController::logToMap).toList());
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("totalLogs", plugin.getLogStorage().getTotalLogs());
            response.put("totalPages", plugin.getLogStorage().getTotalPages(pageSize));

            return gson.toJson(response);
        });

        Spark.get("/api/logs/:id", (req, res) -> {
            res.type("application/json");

            String id = req.params("id");
            return plugin.getLogStorage().getLogById(id)
                    .map(log -> gson.toJson(logToMap(log)))
                    .orElseGet(() -> {
                        res.status(404);
                        return gson.toJson(Map.of("success", false, "message", "Log not found"));
                    });
        });

        Spark.get("/api/stats", (req, res) -> {
            res.type("application/json");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalLogs", plugin.getLogStorage().getTotalLogs());

            Map<String, Integer> playerStats = plugin.getLogStorage().getPlayerStats();
            List<Map<String, Object>> topPlayers = playerStats.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .map(e -> Map.<String, Object>of("player", e.getKey(), "count", e.getValue()))
                    .toList();
            stats.put("topPlayers", topPlayers);

            Map<Material, Integer> itemStats = plugin.getLogStorage().getItemStats();
            List<Map<String, Object>> topItems = itemStats.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(10)
                    .map(e -> Map.<String, Object>of("item", e.getKey().name(), "count", e.getValue()))
                    .toList();
            stats.put("topItems", topItems);

            Map<String, Long> methodBreakdown = new HashMap<>();
            for (ItemLog log : plugin.getLogStorage().getLogs()) {
                String methodName = log.getMethod().getDisplayName();
                methodBreakdown.merge(methodName, 1L, Long::sum);
            }
            stats.put("methodBreakdown", methodBreakdown);

            return gson.toJson(stats);
        });

        Spark.delete("/api/logs", (req, res) -> {
            res.type("application/json");

            plugin.getLogStorage().clear();
            plugin.getLogger().info("All logs cleared via web interface");

            return gson.toJson(Map.of("success", true, "message", "All logs have been cleared"));
        });
    }

    private static Map<String, Object> logToMap(ItemLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("timestamp", log.getTimestamp());
        map.put("formattedTime", log.getFormattedTime());
        map.put("playerUuid", log.getPlayerUuid().toString());
        map.put("playerName", log.getPlayerName());
        map.put("itemType", log.getItemType().name());
        map.put("amount", log.getAmount());
        map.put("method", log.getMethod().name());
        map.put("methodDisplay", log.getMethod().getDisplayName());
        map.put("world", log.getWorld());
        map.put("x", log.getX());
        map.put("y", log.getY());
        map.put("z", log.getZ());
        map.put("location", log.getLocationString());
        map.put("details", log.getDetails());
        return map;
    }
}
