package com.wayc.creativetracker.listeners;

import com.wayc.creativetracker.CreativeTracker;
import com.wayc.creativetracker.data.ItemLog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener implements Listener {

    private final CreativeTracker plugin;

    private static final Pattern GIVE_PATTERN = Pattern.compile(
            "^/?give\\s+(\\S+)\\s+(\\S+)(?:\\s+(\\d+))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "^/?i(?:tem)?\\s+(\\S+)(?:\\s+(\\d+))?",
            Pattern.CASE_INSENSITIVE);

    public CommandListener(CreativeTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getConfig().getBoolean("tracking.give-command", true)) {
            return;
        }

        Player player = event.getPlayer();
        String command = event.getMessage().substring(1); // Remove leading /

        if (player.hasPermission("creativetracker.bypass")) {
            return;
        }

        processCommand(command, player.getName(), player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (!plugin.getConfig().getBoolean("tracking.give-command", true)) {
            return;
        }

        String command = event.getCommand();
        processCommand(command, "CONSOLE", null);
    }

    private void processCommand(String command, String executorName, Player executor) {
        Matcher giveMatcher = GIVE_PATTERN.matcher(command);
        if (giveMatcher.find()) {
            String targetName = giveMatcher.group(1);
            String itemName = giveMatcher.group(2);
            String amountStr = giveMatcher.group(3);
            int amount = amountStr != null ? Integer.parseInt(amountStr) : 1;

            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) {
                target = Bukkit.getPlayer(targetName);
            }

            if (target != null) {
                if (target.hasPermission("creativetracker.bypass")) {
                    return;
                }

                Material material = parseMaterial(itemName);

                ItemLog log = new ItemLog(
                        target.getUniqueId(),
                        target.getName(),
                        material,
                        amount,
                        ItemLog.AcquisitionMethod.GIVE_COMMAND,
                        target.getLocation(),
                        "Given by " + executorName + " via /give command");

                plugin.getLogStorage().addLog(log);
                plugin.getLogger().fine("Logged give command: " + executorName + " gave " +
                        amount + "x " + material.name() + " to " + target.getName());
            }
            return;
        }

        if (executor != null) {
            Matcher itemMatcher = ITEM_PATTERN.matcher(command);
            if (itemMatcher.find()) {
                String itemName = itemMatcher.group(1);
                String amountStr = itemMatcher.group(2);
                int amount = amountStr != null ? Integer.parseInt(amountStr) : 1;

                Material material = parseMaterial(itemName);

                ItemLog log = new ItemLog(
                        executor.getUniqueId(),
                        executor.getName(),
                        material,
                        amount,
                        ItemLog.AcquisitionMethod.GIVE_COMMAND,
                        executor.getLocation(),
                        "Self-given via /item command");

                plugin.getLogStorage().addLog(log);
                plugin.getLogger().fine("Logged item command: " + executor.getName() + " gave themselves " +
                        amount + "x " + material.name());
            }
        }
    }

    private Material parseMaterial(String name) {
        if (name.contains(":")) {
            name = name.split(":")[1];
        }

        Material material = Material.matchMaterial(name);
        if (material != null) {
            return material;
        }

        material = Material.matchMaterial(name.toUpperCase());
        if (material != null) {
            return material;
        }

        return Material.BARRIER;
    }
}
