package com.wayc.creativetracker.commands;

import com.wayc.creativetracker.CreativeTracker;
import com.wayc.creativetracker.gui.LogViewerGUI;
import com.wayc.creativetracker.web.WebServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackerCommand implements CommandExecutor, TabCompleter {

    private final CreativeTracker plugin;

    public TrackerCommand(CreativeTracker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("creativetracker.use")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("This command can only be used by players!")
                        .color(NamedTextColor.RED));
                return true;
            }

            new LogViewerGUI(plugin).openMainMenu(player, 0);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                if (!sender.hasPermission("creativetracker.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to reload!")
                            .color(NamedTextColor.RED));
                    return true;
                }

                plugin.reload();
                sender.sendMessage(Component.text("CreativeTracker configuration reloaded!")
                        .color(NamedTextColor.GREEN));
            }

            case "webpassword" -> {
                if (!sender.hasPermission("creativetracker.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to change the password!")
                            .color(NamedTextColor.RED));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /ct webpassword <new-password>")
                            .color(NamedTextColor.YELLOW));
                    return true;
                }

                String newPassword = args[1];
                WebServer webServer = plugin.getWebServer();

                if (webServer != null) {
                    webServer.setPassword(newPassword);
                    sender.sendMessage(Component.text("Web interface password updated!")
                            .color(NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("Web server is not enabled!")
                            .color(NamedTextColor.RED));
                }
            }

            case "stats" -> {
                sender.sendMessage(Component.text("=== CreativeTracker Stats ===")
                        .color(NamedTextColor.GOLD));
                sender.sendMessage(Component.text("Total logged events: " +
                        plugin.getLogStorage().getTotalLogs()).color(NamedTextColor.WHITE));

                if (plugin.getWebServer() != null) {
                    sender.sendMessage(Component.text("Web interface: http://localhost:" +
                            plugin.getConfig().getInt("web.port", 6745))
                            .color(NamedTextColor.AQUA));
                }
            }

            case "clear" -> {
                if (!sender.hasPermission("creativetracker.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to clear logs!")
                            .color(NamedTextColor.RED));
                    return true;
                }

                if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                    sender.sendMessage(Component.text("Are you sure? Use '/ct clear confirm' to confirm.")
                            .color(NamedTextColor.YELLOW));
                    return true;
                }

                plugin.getLogStorage().clear();
                sender.sendMessage(Component.text("All logs have been cleared!")
                        .color(NamedTextColor.GREEN));
            }

            case "help" -> {
                sendHelp(sender);
            }

            default -> {
                sender.sendMessage(Component.text("Unknown subcommand. Use /ct help for help.")
                        .color(NamedTextColor.RED));
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== CreativeTracker Commands ===")
                .color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/ct - Open the log viewer GUI")
                .color(NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/ct stats - View quick statistics")
                .color(NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/ct reload - Reload configuration")
                .color(NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/ct webpassword <password> - Set web password")
                .color(NamedTextColor.WHITE));
        sender.sendMessage(Component.text("/ct clear confirm - Clear all logs")
                .color(NamedTextColor.WHITE));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "stats");

            if (sender.hasPermission("creativetracker.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.addAll(Arrays.asList("reload", "webpassword", "clear"));
            }

            String partial = args[0].toLowerCase();
            for (String sub : subCommands) {
                if (sub.startsWith(partial)) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            if (sender.hasPermission("creativetracker.admin")) {
                completions.add("confirm");
            }
        }

        return completions;
    }
}
