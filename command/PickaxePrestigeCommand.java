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

import org.axial.prisonsCore.gui.PickaxePrestigeMenu;
import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PickaxePrestigeCommand
implements CommandExecutor {
    private final PickaxePrestigeMenu menu;
    private final PickaxeManager pickaxeManager;

    public PickaxePrestigeCommand(PickaxePrestigeMenu menu, PickaxeManager pickaxeManager) {
        this.menu = menu;
        this.pickaxeManager = pickaxeManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player)sender;
        if (!this.pickaxeManager.isPrisonPickaxe(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Text.color("&cHold a prison pickaxe."));
            return true;
        }
        if (!this.pickaxeManager.hasPrestigeToken(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Text.color("&cApply a prestige token first."));
            return true;
        }
        if (!this.pickaxeManager.canPrestige(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Text.color("&cYou do not meet the prestige requirements."));
            return true;
        }
        this.menu.open(player);
        return true;
    }
}

