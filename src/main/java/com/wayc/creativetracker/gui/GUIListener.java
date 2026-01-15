package com.wayc.creativetracker.gui;

import com.wayc.creativetracker.CreativeTracker;
import com.wayc.creativetracker.data.ItemLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = getInventoryTitle(event);
        if (title == null) {
            return;
        }

        if (title.startsWith(LogViewerGUI.GUI_TITLE_PREFIX)) {
            event.setCancelled(true);
            handleMainMenuClick(player, event);
        } else if (title.equals(FilterGUI.FILTER_TITLE)) {
            event.setCancelled(true);
            handleFilterMenuClick(player, event);
        } else if (title.equals(FilterGUI.STATS_TITLE)) {
            event.setCancelled(true);
            handleStatsMenuClick(player, event);
        }
    }

    private String getInventoryTitle(InventoryClickEvent event) {
        Component titleComponent = event.getView().title();
        if (titleComponent == null) {
            return null;
        }
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(titleComponent);
    }

    private void handleMainMenuClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        List<Component> lore = meta.lore();

        if (lore == null || lore.isEmpty()) {
            return;
        }

        for (Component line : lore) {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                    .plainText().serialize(line);

            if (text.startsWith("page:")) {
                int page = Integer.parseInt(text.substring(5));
                new LogViewerGUI(CreativeTracker.getInstance()).openMainMenu(player, page);
                return;
            }

            if (text.startsWith("refresh:")) {
                int page = Integer.parseInt(text.substring(8));
                new LogViewerGUI(CreativeTracker.getInstance()).openMainMenu(player, page);
                player.sendMessage(Component.text("Logs refreshed!").color(NamedTextColor.GREEN));
                return;
            }
        }

        int slot = event.getSlot();

        if (slot == 49) {
            FilterGUI.openStatsMenu(player);
            return;
        }

        if (slot == 50) {
            FilterGUI.openFilterMenu(player);
        }
    }

    private void handleFilterMenuClick(Player player, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        List<Component> lore = meta.lore();

        int slot = event.getSlot();

        if (slot == 22) {
            new LogViewerGUI(CreativeTracker.getInstance()).openMainMenu(player, 0);
            return;
        }

        if (slot == 14) {
            new LogViewerGUI(CreativeTracker.getInstance()).openMainMenu(player, 0);
            player.sendMessage(Component.text("Showing all logs").color(NamedTextColor.GREEN));
            return;
        }

        if (lore == null || lore.isEmpty()) {
            return;
        }

        for (Component line : lore) {
            String text = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                    .plainText().serialize(line);

            if (text.startsWith("method:")) {
                String methodName = text.substring(7);
                ItemLog.AcquisitionMethod method = ItemLog.AcquisitionMethod.valueOf(methodName);
                player.sendMessage(Component.text("Filtered by: " + method.getDisplayName())
                        .color(NamedTextColor.GREEN));
                // TODO: Implement filtered view
                new LogViewerGUI(CreativeTracker.getInstance()).openMainMenu(player, 0);
                return;
            }
        }
    }

    private void handleStatsMenuClick(Player player, InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 49) {
            new LogViewerGUI(CreativeTracker.getInstance()).openMainMenu(player, 0);
        }
    }
}
