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

import org.axial.prisonsCore.service.PickaxeManager;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ApplyOreCommand
implements CommandExecutor {
    private final PickaxeManager pickaxeManager;

    public ApplyOreCommand(PickaxeManager pickaxeManager) {
        this.pickaxeManager = pickaxeManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("prisons.applyore")) {
            player.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (!this.pickaxeManager.isPrisonPickaxe(player.getInventory().getItemInMainHand())) {
            player.sendMessage(Text.color("&cHold a prison pickaxe."));
            return true;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        int amount = 1;
        if (args.length >= 1) {
            try {
                amount = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (amount <= 0) {
            player.sendMessage(Text.color("&cAmount must be positive."));
            return true;
        }
        Material type = held.getType();
        if (type == Material.WOODEN_PICKAXE) {
            this.pickaxeManager.addIronBroken(held, amount);
            player.sendMessage(Text.color("&aAdded &f" + amount + " &aore(s) to this pickaxe's iron broken stat."));
        } else if (type == Material.GOLDEN_PICKAXE) {
            this.pickaxeManager.addDiamondBroken(held, amount);
            player.sendMessage(Text.color("&aAdded &f" + amount + " &aore(s) to this pickaxe's diamond broken stat."));
        } else if (type == Material.DIAMOND_PICKAXE) {
            this.pickaxeManager.addEmeraldBroken(held, amount);
            player.sendMessage(Text.color("&aAdded &f" + amount + " &aore(s) to this pickaxe's emerald broken stat."));
        } else {
            this.pickaxeManager.addIronBroken(held, amount);
            player.sendMessage(Text.color("&aAdded &f" + amount + " &aore(s) to this pickaxe's ore broken stat."));
        }
        return true;
    }
}

