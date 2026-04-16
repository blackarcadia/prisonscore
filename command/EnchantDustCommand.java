/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package org.axial.prisonsCore.command;

import org.axial.prisonsCore.model.EnchantRarity;
import org.axial.prisonsCore.service.EnchantDustService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantDustCommand
implements CommandExecutor {
    private final EnchantDustService dustService;

    public EnchantDustCommand(EnchantDustService dustService) {
        this.dustService = dustService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        int percent;
        EnchantRarity rarity;
        if (args.length < 3) {
            sender.sendMessage(Text.color("&cUsage: /dust give <rarity> <percent> [player]"));
            return true;
        }
        String sub = args[0].toLowerCase();
        if (!sub.equals("give")) {
            sender.sendMessage(Text.color("&cUsage: /dust give <rarity> <percent> [player]"));
            return true;
        }
        try {
            rarity = EnchantRarity.valueOf(args[1].toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            sender.sendMessage(Text.color("&cUnknown rarity. Use BASIC, RARE, ELITE, LEGENDARY."));
            return true;
        }
        try {
            percent = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException ex) {
            sender.sendMessage(Text.color("&cPercent must be a number."));
            return true;
        }
        if (args.length >= 4) {
            target = Bukkit.getPlayer((String)args[3]);
        } else if (sender instanceof Player) {
            Player p;
            target = p = (Player)sender;
        } else {
            sender.sendMessage("Console must specify player.");
            return true;
        }
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        target.getInventory().addItem(new ItemStack[]{this.dustService.createDust(rarity, percent)});
        target.sendMessage(Text.color("&aGiven " + rarity.name() + " dust &f(" + percent + "%)&a."));
        return true;
    }
}

