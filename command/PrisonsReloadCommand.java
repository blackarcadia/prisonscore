/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PrisonsReloadCommand
implements CommandExecutor {
    private final PrisonsCore plugin;

    public PrisonsReloadCommand(PrisonsCore plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prisons.reload")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        this.plugin.reloadAllConfigs();
        sender.sendMessage(Text.color("&aPrisonsCore reloaded."));
        return true;
    }
}

