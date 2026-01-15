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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class LogViewerGUI {

    public static final String GUI_TITLE_PREFIX = "§8Creative Tracker";
    public static final int PAGE_SIZE = 45; // 5 rows of items

    private final CreativeTracker plugin;

    public LogViewerGUI(CreativeTracker plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player, int page) {
        List<ItemLog> logs = plugin.getLogStorage().getLogs(page, PAGE_SIZE);
        int totalPages = plugin.getLogStorage().getTotalPages(PAGE_SIZE);

        Inventory inv = Bukkit.createInventory(null, 54,
                Component.text(GUI_TITLE_PREFIX + " - Page " + (page + 1) + "/" + Math.max(1, totalPages))
                        .color(NamedTextColor.DARK_GRAY));

        int slot = 0;
        for (ItemLog log : logs) {
            if (slot >= PAGE_SIZE)
                break;
            inv.setItem(slot, createLogItem(log));
            slot++;
        }

        if (page > 0) {
            inv.setItem(45, createNavigationItem(Material.ARROW, "§aPrevious Page", page - 1));
        } else {
            inv.setItem(45, createFillerItem());
        }

        inv.setItem(49, createStatsItem());

        inv.setItem(50, createFilterItem());

        inv.setItem(51, createFillerItem());

        inv.setItem(52, createRefreshItem(page));

        if (page < totalPages - 1) {
            inv.setItem(53, createNavigationItem(Material.ARROW, "§aNext Page", page + 1));
        } else {
            inv.setItem(53, createFillerItem());
        }

        for (int i = 46; i < 49; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, createFillerItem());
            }
        }

        player.openInventory(inv);
    }

    private ItemStack createLogItem(ItemLog log) {
        ItemStack item = new ItemStack(log.getItemType());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(log.getPlayerName())
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§7Item: §f" + log.getItemType().name()));
            lore.add(Component.text("§7Amount: §f" + log.getAmount()));
            lore.add(Component.text("§7Method: §e" + log.getMethod().getDisplayName()));
            lore.add(Component.empty());
            lore.add(Component.text("§7Time: §f" + log.getFormattedTime()));
            lore.add(Component.text("§7Location: §f" + log.getLocationString()));
            lore.add(Component.empty());
            lore.add(Component.text("§8" + log.getDetails()));
            lore.add(Component.empty());
            lore.add(Component.text("§7ID: §8" + log.getId()));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createNavigationItem(Material material, String name, int targetPage) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Click to go to page " + (targetPage + 1)));
            lore.add(Component.text("§8page:" + targetPage));
            meta.lore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createStatsItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§bStatistics")
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§7Total Logs: §f" + plugin.getLogStorage().getTotalLogs()));
            lore.add(Component.empty());
            lore.add(Component.text("§eClick to view detailed stats"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createFilterItem() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§dFilter Logs")
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§7Filter by player, item, or method"));
            lore.add(Component.empty());
            lore.add(Component.text("§eClick to open filter menu"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createRefreshItem(int currentPage) {
        ItemStack item = new ItemStack(Material.SUNFLOWER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§aRefresh")
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("§7Click to refresh the view"));
            lore.add(Component.text("§8refresh:" + currentPage));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }

        return item;
    }
}
