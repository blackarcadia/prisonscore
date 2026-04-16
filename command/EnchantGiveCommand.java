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

import org.axial.prisonsCore.PrisonsCore;
import org.axial.prisonsCore.service.CustomEnchantService;
import org.axial.prisonsCore.service.EnchantOrbService;
import org.axial.prisonsCore.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantGiveCommand
implements CommandExecutor {
    private final CustomEnchantService customEnchantService;
    private final EnchantOrbService enchantOrbService;
    private final PrisonsCore plugin;

    public EnchantGiveCommand(PrisonsCore plugin, CustomEnchantService customEnchantService, EnchantOrbService enchantOrbService) {
        this.plugin = plugin;
        this.customEnchantService = customEnchantService;
        this.enchantOrbService = enchantOrbService;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        int percent;
        int level;
        if (args.length != 4 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(Text.color(command.getUsage()));
            return true;
        }
        String enchantId = args[1].toLowerCase();
        try {
            level = Integer.parseInt(args[2]);
            percent = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(Text.color("&cInvalid level or percent."));
            return true;
        }
        if (this.customEnchantService.getById(enchantId) == null) {
            sender.sendMessage(Text.color("&cUnknown enchant."));
            return true;
        }
        Player player = target = sender instanceof Player ? (Player)sender : null;
        if (args.length == 4 && !(sender instanceof Player)) {
            target = Bukkit.getPlayerExact((String)sender.getName());
        }
        if (target == null && sender instanceof Player) {
            target = (Player)sender;
        }
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        ItemStack orb = this.enchantOrbService.createOrb(enchantId, level, percent);
        if (orb == null) {
            sender.sendMessage(Text.color("&cCould not create orb."));
            return true;
        }
        target.getInventory().addItem(new ItemStack[]{orb});
        sender.sendMessage(Text.color("&aGave " + target.getName() + " an enchant orb for " + enchantId + " level " + level + " (" + percent + "%)"));
        return true;
    }
}

