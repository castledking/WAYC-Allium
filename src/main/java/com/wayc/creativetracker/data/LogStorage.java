package com.wayc.creativetracker.data;

import org.bukkit.Material;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

public class LogStorage {

    private final File dbFile;
    private final Logger logger;
    private Connection connection;
    private int maxLogs = 10000;

    public LogStorage(File dataFolder, Logger logger) {
        this.dbFile = new File(dataFolder, "logs.db");
        this.logger = logger;
    }

    public void setMaxLogs(int maxLogs) {
        this.maxLogs = maxLogs;
    }

    public void init() {
        try {
            dbFile.getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
            logger.info("SQLite database initialized: " + dbFile.getAbsolutePath());
        } catch (SQLException e) {
            logger.severe("Failed to initialize SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS logs (
                id TEXT PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                player_uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                item_type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                method TEXT NOT NULL,
                world TEXT,
                x REAL,
                y REAL,
                z REAL,
                details TEXT
            );
            CREATE INDEX IF NOT EXISTS idx_timestamp ON logs(timestamp DESC);
            CREATE INDEX IF NOT EXISTS idx_player_name ON logs(player_name);
            CREATE INDEX IF NOT EXISTS idx_method ON logs(method);
            """;
        
        try (Statement stmt = connection.createStatement()) {
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement.trim());
                }
            }
        }
    }

    public void addLog(ItemLog log) {
        String sql = """
            INSERT INTO logs (id, timestamp, player_uuid, player_name, item_type, amount, method, world, x, y, z, details)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, log.getId());
            pstmt.setLong(2, log.getTimestamp());
            pstmt.setString(3, log.getPlayerUuid().toString());
            pstmt.setString(4, log.getPlayerName());
            pstmt.setString(5, log.getItemType().name());
            pstmt.setInt(6, log.getAmount());
            pstmt.setString(7, log.getMethod().name());
            pstmt.setString(8, log.getWorld());
            pstmt.setDouble(9, log.getX());
            pstmt.setDouble(10, log.getY());
            pstmt.setDouble(11, log.getZ());
            pstmt.setString(12, log.getDetails());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Failed to add log: " + e.getMessage());
        }

        // Cleanup old logs if exceeding max
        cleanupOldLogs();
    }

    private void cleanupOldLogs() {
        String countSql = "SELECT COUNT(*) FROM logs";
        String deleteSql = """
            DELETE FROM logs WHERE id IN (
                SELECT id FROM logs ORDER BY timestamp ASC LIMIT ?
            )
            """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > maxLogs) {
                    int toDelete = count - maxLogs;
                    try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
                        pstmt.setInt(1, toDelete);
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to cleanup old logs: " + e.getMessage());
        }
    }

    public List<ItemLog> getLogs() {
        return getLogs(0, maxLogs);
    }

    public List<ItemLog> getLogs(int page, int pageSize) {
        List<ItemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM logs ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, page * pageSize);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(resultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get logs: " + e.getMessage());
        }
        
        return logs;
    }

    public List<ItemLog> searchLogs(String playerName, Material itemType, 
                                     ItemLog.AcquisitionMethod method, int page, int pageSize) {
        List<ItemLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM logs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (playerName != null && !playerName.isEmpty()) {
            sql.append(" AND LOWER(player_name) = LOWER(?)");
            params.add(playerName);
        }
        if (itemType != null) {
            sql.append(" AND item_type = ?");
            params.add(itemType.name());
        }
        if (method != null) {
            sql.append(" AND method = ?");
            params.add(method.name());
        }
        
        sql.append(" ORDER BY timestamp DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(page * pageSize);
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                }
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(resultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to search logs: " + e.getMessage());
        }
        
        return logs;
    }

    public int getTotalLogs() {
        String sql = "SELECT COUNT(*) FROM logs";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warning("Failed to get total logs: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalPages(int pageSize) {
        return (int) Math.ceil((double) getTotalLogs() / pageSize);
    }

    public Optional<ItemLog> getLogById(String id) {
        String sql = "SELECT * FROM logs WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(resultSetToLog(rs));
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get log by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Map<String, Integer> getPlayerStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT player_name, COUNT(*) as count FROM logs GROUP BY player_name ORDER BY count DESC LIMIT 20";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("player_name"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            logger.warning("Failed to get player stats: " + e.getMessage());
        }
        
        return stats;
    }

    public Map<Material, Integer> getItemStats() {
        Map<Material, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT item_type, SUM(amount) as total FROM logs GROUP BY item_type ORDER BY total DESC LIMIT 20";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    Material material = Material.valueOf(rs.getString("item_type"));
                    stats.put(material, rs.getInt("total"));
                } catch (IllegalArgumentException ignored) {
                    // Skip invalid materials
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get item stats: " + e.getMessage());
        }
        
        return stats;
    }

    public void clear() {
        String sql = "DELETE FROM logs";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("All logs cleared from database");
        } catch (SQLException e) {
            logger.warning("Failed to clear logs: " + e.getMessage());
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("SQLite database connection closed");
            } catch (SQLException e) {
                logger.warning("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    private ItemLog resultSetToLog(ResultSet rs) throws SQLException {
        return new ItemLog(
                rs.getString("id"),
                rs.getLong("timestamp"),
                UUID.fromString(rs.getString("player_uuid")),
                rs.getString("player_name"),
                Material.valueOf(rs.getString("item_type")),
                rs.getInt("amount"),
                ItemLog.AcquisitionMethod.valueOf(rs.getString("method")),
                rs.getString("world"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                rs.getString("details")
        );
    }
}
