/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPlayerLevelCommand
implements CommandExecutor {
    private final PlayerLevelService playerLevelService;
    private final PrisonsCore plugin;

    public SetPlayerLevelCommand(PrisonsCore plugin, PlayerLevelService playerLevelService) {
        this.plugin = plugin;
        this.playerLevelService = playerLevelService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int level;
        if (args.length != 2) {
            sender.sendMessage(Text.color(command.getUsage()));
            return true;
        }
        Player target = Bukkit.getPlayerExact((String)args[0]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        try {
            level = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(Text.color("&cInvalid level."));
            return true;
        }
        level = Math.max(1, Math.min(this.playerLevelService.getConfig().getMaxLevel(), level));
        this.playerLevelService.setLevel(target, level);
        sender.sendMessage(Text.color("&aSet " + target.getName() + " level to " + level + "."));
        return true;
    }
}

