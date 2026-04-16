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

import org.axial.prisonsCore.service.PlayerLevelService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand
implements CommandExecutor {
    private final PlayerLevelService playerLevelService;

    public LevelCommand(PlayerLevelService playerLevelService) {
        this.playerLevelService = playerLevelService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        int level = this.playerLevelService.getLevel(player);
        int exp = this.playerLevelService.getExp(player);
        int needed = this.playerLevelService.expNeededForNext(player);
        String msg = "&eLevel: &b" + level + " &7| &eEXP: &b" + exp + "&7/&b" + String.valueOf(needed == 0 ? "Max" : Integer.valueOf(needed));
        player.sendMessage(Text.color(msg));
        return true;
    }
}

