package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.service.HarbourerBossService;
import org.axial.prisonsCore.service.HarbourerBossService.HarbourerVariant;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class BossCommand implements CommandExecutor, TabCompleter {
    private final HarbourerBossService harbourerBossService;

    public BossCommand(HarbourerBossService harbourerBossService) {
        this.harbourerBossService = harbourerBossService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.boss")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 0) {
            this.sendUsage(sender);
            return true;
        }
        if (!args[0].equalsIgnoreCase("harbourer")) {
            sender.sendMessage(Text.color("&cUnknown boss."));
            this.sendUsage(sender);
            return true;
        }
        if (args.length < 2) {
            this.sendUsage(sender);
            return true;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "spawn" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Text.color("&cOnly players can spawn the Harbourer."));
                    return true;
                }
                HarbourerVariant variant = args.length >= 3 ? HarbourerVariant.fromId(args[2]) : HarbourerVariant.IRON;
                if (variant == null) {
                    sender.sendMessage(Text.color("&cUnknown harbourer type."));
                    return true;
                }
                if (this.harbourerBossService.spawnHarbourer(player.getLocation(), variant) == null) {
                    sender.sendMessage(Text.color("&cFailed to spawn the Harbourer."));
                    return true;
                }
                sender.sendMessage(Text.color("&aSpawned &f" + Text.strip(variant.displayName()) + "&a."));
                return true;
            }
            case "despawn" -> {
                this.harbourerBossService.despawnHarbourer();
                sender.sendMessage(Text.color("&aDespawned &fThe Harbourer&a."));
                return true;
            }
            case "status" -> {
                if (this.harbourerBossService.isActive()) {
                    var anchor = this.harbourerBossService.getAnchorLocation();
                    var variant = this.harbourerBossService.getVariant();
                    String where = anchor == null || anchor.getWorld() == null
                            ? "unknown"
                            : anchor.getWorld().getName() + " " + Math.round(anchor.getX()) + ", " + Math.round(anchor.getY()) + ", " + Math.round(anchor.getZ());
                    sender.sendMessage(Text.color("&a" + (variant == null ? "The Harbourer" : Text.strip(variant.displayName())) + " is active &7at &f" + where + "&7."));
                } else {
                    sender.sendMessage(Text.color("&cThe Harbourer is not active."));
                }
                return true;
            }
            default -> {
                sender.sendMessage(Text.color("&cUnknown action."));
                this.sendUsage(sender);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("prisons.boss")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("harbourer").stream().filter(value -> value.startsWith(args[0].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("harbourer")) {
            return List.of("spawn", "despawn", "status").stream().filter(value -> value.startsWith(args[1].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("harbourer") && args[1].equalsIgnoreCase("spawn")) {
            return List.of("iron", "lapis", "gold", "redstone", "diamond", "emerald").stream().filter(value -> value.startsWith(args[2].toLowerCase(Locale.ROOT))).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Text.color("&eUsage: /boss harbourer <spawn [iron|lapis|gold|redstone|diamond|emerald]|despawn|status>"));
    }
}
