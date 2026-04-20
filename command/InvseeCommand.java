package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        if (args.length != 1) {
            viewer.sendMessage(Text.color("&cUsage: /invsee <player>"));
            return true;
        }
        Player target = this.findOnlinePlayer(args[0]);
        if (target == null) {
            OfflinePlayer offline = this.findOfflinePlayer(args[0]);
            if (offline != null && offline.getName() != null) {
                viewer.sendMessage(Text.color("&c" + offline.getName() + " is not online."));
            } else {
                viewer.sendMessage(Text.color("&cThat player was not found."));
            }
            return true;
        }
        viewer.openInventory(target.getInventory());
        viewer.sendMessage(Text.color("&aViewing &f" + target.getName() + "&a's inventory."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return this.completePlayers(args[0]);
    }

    private Player findOnlinePlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName() != null && player.getName().equalsIgnoreCase(input)) {
                return player;
            }
        }
        return null;
    }

    private OfflinePlayer findOfflinePlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.getName() != null && player.getName().equalsIgnoreCase(input)) {
                return player;
            }
        }
        return null;
    }

    private List<String> completePlayers(String input) {
        String needle = input == null ? "" : input.toLowerCase(Locale.ROOT);
        ArrayList<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName() != null) {
                names.add(player.getName());
            }
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            String name = player.getName();
            if (name != null) {
                names.add(name);
            }
        }
        return names.stream()
                .distinct()
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(needle))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}
