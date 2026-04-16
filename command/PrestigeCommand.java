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

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.util.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrestigeCommand
implements CommandExecutor {
    private final PlayerLevelService playerLevelService;
    private final PrisonsCore plugin;

    public PrestigeCommand(PrisonsCore plugin, PlayerLevelService playerLevelService) {
        this.plugin = plugin;
        this.playerLevelService = playerLevelService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can prestige.");
            return true;
        }
        Player player = (Player)sender;
        if (this.playerLevelService.isPrestigeUnlocked(player)) {
            player.sendMessage(Lang.msg("prestige.already", "&cYou have already unlocked prestige levels."));
            return true;
        }
        if (this.playerLevelService.getLevel(player) < this.plugin.getConfig().getInt("player-level.prestige-start", 100)) {
            player.sendMessage(Lang.msg("prestige.locked", "&cReach level 100 before prestiging."));
            return true;
        }
        this.playerLevelService.unlockPrestige(player);
        player.sendMessage(Lang.msg("prestige.unlocked", "&aPrestige unlocked! You can now progress beyond level 100."));
        return true;
    }
}

