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

import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand
implements CommandExecutor {
    public SetWarpCommand() {
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can set warps."));
            return true;
        }
        if (!sender.hasPermission("prisons.warp.set")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Text.color("&cUsage: /setwarp <name>"));
            return true;
        }
        sender.sendMessage(Text.color("&eWarps are hardcoded and cannot be changed here."));
        return true;
    }
}
