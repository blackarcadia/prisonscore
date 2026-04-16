/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.service.GearManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GearCommand
implements CommandExecutor {
    private final GearManager gearManager;

    public GearCommand(GearManager gearManager) {
        this.gearManager = gearManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("prisons.gear")) {
            player.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.color("&cUsage: /gear <material>"));
            return true;
        }
        Material mat = Material.matchMaterial((String)args[0].toUpperCase());
        if (mat == null || !this.gearManager.isApplicableMaterial(mat)) {
            player.sendMessage(Text.color("&cInvalid material for gear."));
            return true;
        }
        ItemStack base = new ItemStack(mat);
        ItemStack gear = this.gearManager.wrap(base, mat.name().toLowerCase());
        player.getInventory().addItem(new ItemStack[]{gear});
        player.sendMessage(Text.color("&aGiven gear item: &f" + mat.name()));
        return true;
    }
}

