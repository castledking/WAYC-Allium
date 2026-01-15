package com.wayc.creativetracker;

import com.wayc.creativetracker.commands.TrackerCommand;
import com.wayc.creativetracker.data.LogStorage;
import com.wayc.creativetracker.gui.GUIListener;
import com.wayc.creativetracker.listeners.CommandListener;
import com.wayc.creativetracker.listeners.CreativeInventoryListener;
import com.wayc.creativetracker.web.WebServer;
import org.bukkit.plugin.java.JavaPlugin;

public class CreativeTracker extends JavaPlugin {

    private static CreativeTracker instance;
    private LogStorage logStorage;
    private WebServer webServer;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        logStorage = new LogStorage(getDataFolder(), getLogger());
        logStorage.setMaxLogs(getConfig().getInt("storage.max-logs", 10000));
        logStorage.init();

        getServer().getPluginManager().registerEvents(new CreativeInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandListener(this), this);
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

    public WebServer getWebServer() {
        return webServer;
    }
}
