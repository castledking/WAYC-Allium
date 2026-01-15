package com.wayc.creativetracker.gui;

import com.wayc.creativetracker.CreativeTracker;
import com.wayc.creativetracker.data.ItemLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterGUI {

    public static final String FILTER_TITLE = "§8Filter Options";
    public static final String STATS_TITLE = "§8Statistics";

    public static void openFilterMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(FILTER_TITLE).color(NamedTextColor.DARK_GRAY));

        inv.setItem(10, createMethodFilterItem(ItemLog.AcquisitionMethod.CREATIVE_INVENTORY));
        inv.setItem(11, createMethodFilterItem(ItemLog.AcquisitionMethod.GIVE_COMMAND));
        inv.setItem(12, createMethodFilterItem(ItemLog.AcquisitionMethod.GAMEMODE_CHANGE));

        inv.setItem(14, createShowAllItem());

        inv.setItem(22, createBackItem());

        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, createFillerItem());
            }
        }

        player.openInventory(inv);
    }

    public static void openStatsMenu(Player player) {
        CreativeTracker plugin = CreativeTracker.getInstance();

        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text(STATS_TITLE).color(NamedTextColor.DARK_GRAY));

        inv.setItem(4, createTotalLogsItem(plugin.getLogStorage().getTotalLogs()));

        Map<String, Integer> playerStats = plugin.getLogStorage().getPlayerStats();
        int slot = 19;
        int count = 0;
        for (Map.Entry<String, Integer> entry : playerStats.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(7)
                .toList()) {
            inv.setItem(slot, createPlayerStatItem(entry.getKey(), entry.getValue()));
            slot++;
            count++;
            if (count >= 7)
                break;
        }

        Map<Material, Integer> itemStats = plugin.getLogStorage().getItemStats();
        slot = 37;
        count = 0;
        for (Map.Entry<Material, Integer> entry : itemStats.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(7)
                .toList()) {
            inv.setItem(slot, createItemStatItem(entry.getKey(), entry.getValue()));
            slot++;
            count++;
            if (count >= 7)
                break;
        }

        inv.setItem(10, createLabelItem("§6Top Players", Material.PLAYER_HEAD));
        inv.setItem(28, createLabelItem("§bTop Items", Material.CHEST));

        inv.setItem(49, createBackItem());

        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, createFillerItem());
            }
        }

        player.openInventory(inv);
    }

    private static ItemStack createMethodFilterItem(ItemLog.AcquisitionMethod method) {
        Material material = switch (method) {
            case CREATIVE_INVENTORY -> Material.GRASS_BLOCK;
            case GIVE_COMMAND -> Material.COMMAND_BLOCK;
            case GAMEMODE_CHANGE -> Material.ENDER_PEARL;
            default -> Material.PAPER;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§e" + method.getDisplayName())
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§7Filter to show only"));
            lore.add(Component.text("§7this acquisition type"));
            lore.add(Component.empty());
            lore.add(Component.text("§aClick to filter"));
            lore.add(Component.text("§8method:" + method.name()));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createShowAllItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§aShow All Logs")
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§7Remove all filters"));
            lore.add(Component.empty());
            lore.add(Component.text("§eClick to show all"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§cBack")
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Return to main menu"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createTotalLogsItem(int total) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§6Total Logged Events")
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§f" + total + " §7events recorded"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createPlayerStatItem(String playerName, int count) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§e" + playerName)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Events: §f" + count));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createItemStatItem(Material material, int count) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§b" + material.name())
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Total spawned: §f" + count));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createLabelItem(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }

        return item;
    }
}
