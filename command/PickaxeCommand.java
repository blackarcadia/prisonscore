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
 */
package org.axial.prisonsCore.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.model.PickaxeType;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class PickaxeCommand
implements CommandExecutor,
TabCompleter {
    private final PickaxeManager pickaxeManager;
    private final PrisonsCore plugin;

    public PickaxeCommand(PrisonsCore plugin, PickaxeManager pickaxeManager) {
        this.pickaxeManager = pickaxeManager;
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("give")) {
            String usage = Lang.msg("pickaxe.usage", "&e/" + label + " give <type> <player>");
            sender.sendMessage(usage);
            return true;
        }
        String typeId = args[1].toLowerCase(Locale.ROOT);
        Player target = Bukkit.getPlayerExact((String)args[2]);
        if (target == null) {
            sender.sendMessage(Lang.msg("common.player-not-found", "&cPlayer not found."));
            return true;
        }
        boolean success = this.pickaxeManager.givePickaxe(target, typeId);
        if (!success) {
            sender.sendMessage(Lang.msg("pickaxe.unknown-type", "&cUnknown pickaxe type."));
            return true;
        }
        String msg = Lang.msg("pickaxe.gave", "&aGave {type} pickaxe to {player}").replace("{type}", typeId).replace("{player}", target.getName());
        sender.sendMessage(msg);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            completions.add("give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(this.pickaxeManager.getTypes().stream().map(PickaxeType::getId).collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        return completions;
    }
}

