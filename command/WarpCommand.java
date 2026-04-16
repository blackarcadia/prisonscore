/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.gui.WarpMenu;
import org.axial.prisonsCore.service.TeleportService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand
implements CommandExecutor {
    private final WarpMenu warpMenu;

    public WarpCommand(TeleportService teleportService, WarpMenu warpMenu) {
        this.warpMenu = warpMenu;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can warp."));
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            this.warpMenu.open(player);
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Text.color("&cUsage: /warp [name]"));
            return true;
        }
        return this.warpMenu.handleWarpCommand(player, args[0]);
    }
}
