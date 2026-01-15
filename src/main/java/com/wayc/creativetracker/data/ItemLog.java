package com.wayc.creativetracker.data;

import org.bukkit.Location;
import org.bukkit.Material;

import java.time.Instant;
import java.util.UUID;

public class ItemLog {
    
    private final String id;
    private final long timestamp;
    private final UUID playerUuid;
    private final String playerName;
    private final Material itemType;
    private final int amount;
    private final AcquisitionMethod method;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final String details;

    public ItemLog(UUID playerUuid, String playerName, Material itemType, int amount, 
                   AcquisitionMethod method, Location location, String details) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now().toEpochMilli();
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.itemType = itemType;
        this.amount = amount;
        this.method = method;
        this.world = location != null ? location.getWorld().getName() : "unknown";
        this.x = location != null ? location.getX() : 0;
        this.y = location != null ? location.getY() : 0;
        this.z = location != null ? location.getZ() : 0;
        this.details = details;
    }

    // Constructor for loading from database
    public ItemLog(String id, long timestamp, UUID playerUuid, String playerName, 
                   Material itemType, int amount, AcquisitionMethod method,
                   String world, double x, double y, double z, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.itemType = itemType;
        this.amount = amount;
        this.method = method;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.details = details;
    }

    // Getters
    public String getId() { return id; }
    public long getTimestamp() { return timestamp; }
    public UUID getPlayerUuid() { return playerUuid; }
    public String getPlayerName() { return playerName; }
    public Material getItemType() { return itemType; }
    public int getAmount() { return amount; }
    public AcquisitionMethod getMethod() { return method; }
    public String getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getDetails() { return details; }

    public String getFormattedTime() {
        return java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(java.time.ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(timestamp));
    }

    public String getLocationString() {
        return String.format("%s (%.0f, %.0f, %.0f)", world, x, y, z);
    }

    public enum AcquisitionMethod {
        CREATIVE_INVENTORY("Creative Inventory"),
        GIVE_COMMAND("Give Command"),
        GAMEMODE_CHANGE("Gamemode Change"),
        UNKNOWN("Unknown");

        private final String displayName;

        AcquisitionMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
