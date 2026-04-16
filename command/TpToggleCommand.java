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

import org.axial.prisonsCore.service.TeleportService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpToggleCommand
implements CommandExecutor {
    private final TeleportService teleportService;

    public TpToggleCommand(TeleportService teleportService) {
        this.teleportService = teleportService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Text.color("&cOnly players can use this."));
            return true;
        }
        Player player = (Player)sender;
        this.teleportService.toggleTpa(player.getUniqueId());
        boolean disabled = this.teleportService.isTpaToggled(player.getUniqueId());
        player.sendMessage(Text.color(disabled ? "&cYou will no longer receive TPA requests." : "&aYou will now receive TPA requests."));
        return true;
    }
}

