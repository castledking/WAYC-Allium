package com.wayc.creativetracker.listeners;

import com.wayc.creativetracker.CreativeTracker;
import com.wayc.creativetracker.data.ItemLog;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;

public class CreativeInventoryListener implements Listener {

    private final CreativeTracker plugin;

    public CreativeInventoryListener(CreativeTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!plugin.getConfig().getBoolean("tracking.creative-inventory", true)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (player.hasPermission("creativetracker.bypass")) {
            return;
        }

        ItemStack item = event.getCursor();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemLog log = new ItemLog(
                player.getUniqueId(),
                player.getName(),
                item.getType(),
                item.getAmount(),
                ItemLog.AcquisitionMethod.CREATIVE_INVENTORY,
                player.getLocation(),
                "Set slot " + event.getSlot());

        plugin.getLogStorage().addLog(log);
        plugin.getLogger().fine("Logged creative item: " + player.getName() + " spawned " +
                item.getAmount() + "x " + item.getType().name());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!plugin.getConfig().getBoolean("tracking.creative-inventory", true)) {
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }

        if (player.hasPermission("creativetracker.bypass")) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            item = event.getCursor();
        }

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (event.getClickedInventory() != null &&
                event.getView().getTitle().isEmpty()) { // Creative inventory has no title

            ItemLog log = new ItemLog(
                    player.getUniqueId(),
                    player.getName(),
                    item.getType(),
                    item.getAmount(),
                    ItemLog.AcquisitionMethod.CREATIVE_INVENTORY,
                    player.getLocation(),
                    "Clicked in creative inventory");

            plugin.getLogStorage().addLog(log);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (!plugin.getConfig().getBoolean("tracking.gamemode-command", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (player.hasPermission("creativetracker.bypass")) {
            return;
        }

        if (event.getNewGameMode() == GameMode.CREATIVE) {
            ItemLog log = new ItemLog(
                    player.getUniqueId(),
                    player.getName(),
                    Material.COMMAND_BLOCK, // Symbolic item for gamemode change
                    1,
                    ItemLog.AcquisitionMethod.GAMEMODE_CHANGE,
                    player.getLocation(),
                    "Changed to Creative mode from " + player.getGameMode().name());

            plugin.getLogStorage().addLog(log);
            plugin.getLogger().fine("Logged gamemode change: " + player.getName() + " entered Creative mode");
        }
    }
}
