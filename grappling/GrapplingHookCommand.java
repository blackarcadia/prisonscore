/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package org.axial.prisonsCore.grappling;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.axial.prisonsCore.grappling.GrapplingHookModule;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GrapplingHookCommand
implements CommandExecutor,
TabCompleter {
    private final GrapplingHookModule module;

    public GrapplingHookCommand(GrapplingHookModule module) {
        this.module = module;
    }

    /*
     * Enabled aggressive block sorting
     */
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player target;
        String sub;
        if (!sender.hasPermission("axialgrapplinghook.give")) {
            sender.sendMessage(Lang.msg("grappling.no-permission", "&cYou do not have permission to use this command."));
            return true;
        }
        String string = sub = args.length > 0 ? args[0].toLowerCase(Locale.ENGLISH) : "";
        if (sub.isEmpty()) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Lang.msg("grappling.console-must-specify-player", "&cConsole must specify a player: /{label} <player>", Map.of("label", label)));
                return true;
            }
            target = (Player)sender;
        } else if (sub.equals("give")) {
            if (args.length >= 2) {
                target = Bukkit.getPlayerExact((String)args[1]);
                if (target == null) {
                    sender.sendMessage(Lang.msg("grappling.player-not-found", "&cPlayer not found: {player}", Map.of("player", args[1])));
                    return true;
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Lang.msg("grappling.console-must-specify-player", "&cConsole must specify a player: /{label} <player>", Map.of("label", label + " give")));
                    return true;
                }
                target = (Player)sender;
            }
        } else {
            target = Bukkit.getPlayerExact((String)args[0]);
            if (target == null) {
                sender.sendMessage(Lang.msg("grappling.player-not-found", "&cPlayer not found: {player}", Map.of("player", args[0])));
                return true;
            }
        }
        ItemStack hook = this.module.getGrapplingHookItem();
        target.getInventory().addItem(new ItemStack[]{hook});
        if (!target.equals((Object)sender)) {
            sender.sendMessage(Lang.msg("grappling.gave-hook", "&aGave a grappling hook to {player}", Map.of("player", target.getName())));
        }
        target.sendMessage(Lang.msg("grappling.received-hook", "&bYou received a grappling hook!"));
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            ArrayList<String> options = new ArrayList<String>();
            options.add("give");
            String prefix = args[0].toLowerCase(Locale.ENGLISH);
            options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(prefix)).collect(Collectors.toList()));
            return options.stream().filter(opt -> opt.toLowerCase(Locale.ENGLISH).startsWith(prefix)).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[1].toLowerCase(Locale.ENGLISH);
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(prefix)).collect(Collectors.toList());
        }
        return new ArrayList<String>();
    }
}
