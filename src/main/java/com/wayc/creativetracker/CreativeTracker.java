package com.wayc.creativetracker;

import com.wayc.creativetracker.commands.TrackerCommand;
import com.wayc.creativetracker.data.ItemLog;
import com.wayc.creativetracker.data.LogStorage;
import com.wayc.creativetracker.gui.GUIListener;
import com.wayc.creativetracker.listeners.CommandListener;
import com.wayc.creativetracker.listeners.CreativeInventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import com.wayc.creativetracker.web.WebServer;
import org.bukkit.plugin.java.JavaPlugin;

public class CreativeTracker extends JavaPlugin {

    private static CreativeTracker instance;
    private LogStorage logStorage;
    private WebServer webServer;
    private boolean alliumIntegrationActive;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        logStorage = new LogStorage(getDataFolder(), getLogger());
        logStorage.setMaxLogs(getConfig().getInt("storage.max-logs", 10000));
        logStorage.init();

        getServer().getPluginManager().registerEvents(new CreativeInventoryListener(this), this);
        alliumIntegrationActive = registerAlliumListener();
        getServer().getPluginManager().registerEvents(new CommandListener(this, alliumIntegrationActive), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        TrackerCommand trackerCommand = new TrackerCommand(this);
        getCommand("creativetracker").setExecutor(trackerCommand);
        getCommand("creativetracker").setTabCompleter(trackerCommand);

        if (getConfig().getBoolean("web.enabled", true)) {
            int port = getConfig().getInt("web.port", 6745);
            webServer = new WebServer(this, port);
            webServer.start();
        }

        getLogger().info("CreativeTracker has been enabled!");
        if (webServer != null) {
            getLogger().info("Web interface available at http://localhost:" + getConfig().getInt("web.port", 6745));
        }
    }

    @Override
    public void onDisable() {
        if (webServer != null) {
            webServer.stop();
        }

        if (logStorage != null) {
            logStorage.close();
        }

        getLogger().info("CreativeTracker has been disabled!");
    }

    public void reload() {
        reloadConfig();

        if (webServer != null) {
            webServer.stop();
        }

        if (getConfig().getBoolean("web.enabled", true)) {
            int port = getConfig().getInt("web.port", 6745);
            webServer = new WebServer(this, port);
            webServer.start();
        }
    }

    public static CreativeTracker getInstance() {
        return instance;
    }

    public LogStorage getLogStorage() {
        return logStorage;
    }

    public boolean isAlliumIntegrationActive() {
        return alliumIntegrationActive;
    }

    private boolean registerAlliumListener() {
        if (Bukkit.getPluginManager().getPlugin("Allium") == null) return false;
        try {
            Class<?> eventClass = Class.forName("net.survivalfun.core.events.ItemGiveEvent");
            Listener listener = new Listener() {};
            Bukkit.getPluginManager().registerEvent(eventClass, listener, EventPriority.MONITOR,
                    (l, event) -> onAlliumItemGive(event), this, false);
            getLogger().info("Allium integration enabled - tracking /give and /i via ItemGiveEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void onAlliumItemGive(Event event) {
        try {
            Player target = (Player) event.getClass().getMethod("getTarget").invoke(event);
            ItemStack item = (ItemStack) event.getClass().getMethod("getItem").invoke(event);
            int amount = (Integer) event.getClass().getMethod("getAmountGiven").invoke(event);
            Object executor = event.getClass().getMethod("getExecutor").invoke(event);
            String source = (String) event.getClass().getMethod("getSource").invoke(event);
            if (target == null || target.hasPermission("creativetracker.bypass")) return;
            if (item == null || item.getType() == Material.AIR || amount <= 0) return;
            String executorName = (executor instanceof Player) ? ((Player) executor).getName() : "CONSOLE";
            String details = "give".equalsIgnoreCase(source)
                    ? "Given by " + executorName + " via /give command"
                    : "Self-given via /item command";
            logStorage.addLog(new ItemLog(target.getUniqueId(), target.getName(), item.getType(), amount,
                    ItemLog.AcquisitionMethod.GIVE_COMMAND, target.getLocation(), details));
        } catch (Exception e) {
            getLogger().warning("Failed to handle Allium ItemGiveEvent: " + e.getMessage());
        }
    }

    public WebServer getWebServer() {
        return webServer;
    }
}
