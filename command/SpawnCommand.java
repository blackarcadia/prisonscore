/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.TeleportService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand
implements CommandExecutor {
    private final TeleportService teleportService;

    public SpawnCommand(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        Player player = (Player)sender;
        Location spawn = this.teleportService.getSpawn();
        if (spawn == null) {
            sender.sendMessage(Text.color("&cSpawn not set."));
            return true;
        }
        this.teleportService.scheduleTeleport(player, spawn, "spawn", null);
        return true;
    }
}

