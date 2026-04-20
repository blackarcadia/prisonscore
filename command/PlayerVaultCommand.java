package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.gui.PlayerVaultMenu;
import org.axial.prisonsCore.service.PlayerVaultService;
import org.axial.prisonsCore.util.Lang;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class PlayerVaultCommand implements CommandExecutor, TabCompleter {
    private final PlayerVaultService vaultService;
    private final PlayerVaultMenu vaultMenu;

    public PlayerVaultCommand(PlayerVaultService vaultService, PlayerVaultMenu vaultMenu) {
        this.vaultService = vaultService;
        this.vaultMenu = vaultMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Text.color("&cUsage: /pv view <player> [vault]"));
                return true;
            }
            this.vaultMenu.openSelector(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("view")) {
            return this.handleAdminView(sender, args);
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Lang.msg("common.players-only", "Only players can use this command."));
            return true;
        }
        Integer vaultNumber = this.parseVaultNumber(args[0]);
        if (vaultNumber == null) {
            player.sendMessage(Text.color("&cUsage: /pv [vault] or /pv view <player> [vault]"));
            return true;
        }
        if (!this.vaultService.canAccess(player, vaultNumber)) {
            player.sendMessage(Text.color("&cYou do not have access to that player vault."));
            return true;
        }
        this.vaultMenu.openVault(player, player, vaultNumber, false);
        return true;
    }

    private boolean handleAdminView(CommandSender sender, String[] args) {
        if (!this.vaultService.isAdmin(sender)) {
            sender.sendMessage(Text.color("&cYou do not have permission to view other players' vaults."));
            return true;
        }
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage(Text.color("&cOnly players can open player vault inventories."));
            return true;
        }
        if (args.length < 2) {
            viewer.sendMessage(Text.color("&cUsage: /pv view <player> [vault]"));
            return true;
        }
        OfflinePlayer target = this.findPlayer(args[1]);
        if (target == null) {
            viewer.sendMessage(Text.color("&cUnknown player."));
            return true;
        }
        if (args.length == 2) {
            this.vaultMenu.openSelector(viewer, target, true);
            return true;
        }
        Integer vaultNumber = this.parseVaultNumber(args[2]);
        if (vaultNumber == null || !this.vaultService.isValidVaultNumber(vaultNumber)) {
            viewer.sendMessage(Text.color("&cVault number must be between 1 and " + this.vaultService.maxVaults() + "."));
            return true;
        }
        this.vaultMenu.openVault(viewer, target, vaultNumber, true);
        return true;
    }

    private OfflinePlayer findPlayer(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName() != null && online.getName().equalsIgnoreCase(input)) {
                return online;
            }
        }
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() == null || !offline.getName().equalsIgnoreCase(input)) {
                continue;
            }
            return offline;
        }
        return null;
    }

    private Integer parseVaultNumber(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<String>();
            if (sender instanceof Player player) {
                for (int i = 1; i <= this.vaultService.accessibleVaults(player); ++i) {
                    completions.add(String.valueOf(i));
                }
            }
            if (this.vaultService.isAdmin(sender)) {
                completions.add("view");
            }
            return this.filter(completions, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("view") && this.vaultService.isAdmin(sender)) {
            return this.filter(this.allPlayerNames(), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("view") && this.vaultService.isAdmin(sender)) {
            List<String> completions = new ArrayList<String>();
            for (int i = 1; i <= this.vaultService.maxVaults(); ++i) {
                completions.add(String.valueOf(i));
            }
            return this.filter(completions, args[2]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String needle = input.toLowerCase(Locale.ROOT);
        return values.stream().distinct().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(needle)).sorted().collect(Collectors.toList());
    }

    private List<String> allPlayerNames() {
        List<String> names = new ArrayList<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName() != null) {
                names.add(player.getName());
            }
        }
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.getName() != null) {
                names.add(player.getName());
            }
        }
        return names;
    }
}
