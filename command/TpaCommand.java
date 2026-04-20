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

import org.axial.prisonsCore.service.TeleportService;
import org.axial.prisonsCore.service.PlayerToggleService;
import org.axial.prisonsCore.service.PlayerToggleService.Toggle;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpaCommand
implements CommandExecutor {
    private final TeleportService teleportService;
    private final PlayerToggleService toggleService;

    public TpaCommand(TeleportService teleportService, PlayerToggleService toggleService) {
        this.teleportService = teleportService;
        this.toggleService = toggleService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        Player player = (Player)sender;
        if (args.length != 1) {
            sender.sendMessage(Text.color("&cUsage: /tpa <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact((String)args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        if (target.equals((Object)player)) {
            sender.sendMessage(Text.color("&cYou can't send a request to yourself."));
            return true;
        }
        if (this.toggleService != null && !this.toggleService.isEnabled(target, Toggle.TPA_REQUESTS)) {
            sender.sendMessage(Text.color("&c" + target.getName() + " is not recieving tpa requests right now."));
            return true;
        }
        if (!this.teleportService.sendTpa(player, target)) {
            sender.sendMessage(Text.color("&cCould not send request (player may have toggled)."));
        }
        return true;
    }
}
